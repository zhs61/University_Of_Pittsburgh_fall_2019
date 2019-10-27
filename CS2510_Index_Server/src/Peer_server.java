import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Scanner;

public class Peer_server implements Runnable{
    private int id=-1;
    private String indexing_sever_address;
    private int indexing_sever_port;
    private String file_folder;
    private ArrayList<String> filenames;
    ServerSocket serverSocket;
    private int peer_server_port;
    private int peer_client_port;
    private boolean split;
    private int load=0;
    private int bytes=0;
    private int message=0;
    private int response=0;
    private double aver_reponse;
    private boolean initalized=false;
    public Peer_server(String addess,int port,boolean split,String folder){
        indexing_sever_address = addess;
        indexing_sever_port = 6666;
        peer_server_port=port;
        peer_client_port=port+1;
        file_folder=folder;
        filenames=new ArrayList<String>();
        this.split=split;
    }

    //run service
    public void run() {
        //contact indexing services
        long srt=System.currentTimeMillis();
        SocketClient sender=new SocketClient(indexing_sever_address,indexing_sever_port);
        String resout=sender.sendMessage("start\n"+split);
        long end=System.currentTimeMillis();
        message++;bytes+=("start"+resout).length()*2;
        response+=end-srt;aver_reponse=(double)response/message;
        id=Integer.parseInt(resout.split("\n")[1]);
        //scan local files
        File[] files = new File(file_folder).listFiles();
        for(File file :files){
            boolean res=register(file.getName());
            if(res){
                filenames.add(file.getName());
            }
        }
        initalized=true;
        //wait for file request from other peers
        final Thread worker=new Thread(new Runnable(){
            @Override
            public void run() {
                Thread sender_thread=null;
                try {
                    serverSocket=new ServerSocket(peer_server_port);
                    while (true) {
                        Socket socket = serverSocket.accept();
                        DataInputStream dis = new DataInputStream(socket.getInputStream());
                        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                        sender_thread=new download_handler(socket,dis,dos);
                        sender_thread.start();
                        load++;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        worker.run();
        load++;


        //send load to indexing server
        if(!split) {
            Thread send_load = new Thread(new Runnable() {
                public void run() {
                    Thread client = null;
                    while (true) {
                        SocketClient sock = new SocketClient(indexing_sever_address, indexing_sever_port);
                        sock.sendMessage("load\n" + id+"\n"+load);
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            send_load.run();
        }
    }

    //resigter a file to indexing server
    public boolean register(String filename){
        long srt=System.currentTimeMillis();
        SocketClient sender=new SocketClient(indexing_sever_address,indexing_sever_port);
        String resout=sender.sendMessage("register\n"+filename+"\n"+id);
        long end=System.currentTimeMillis();
        message++;bytes+=("register\n"+id+"\n"+filename+resout).length()*2;
        response+=end-srt;aver_reponse=(double)response/message;
        if(!resout.equals("true")){
            System.out.println("register failed");
            return false;
        }
        return true;
    }

    public boolean unregister(String filename){
        SocketClient sender=new SocketClient(indexing_sever_address,indexing_sever_port);
        String resout=sender.sendMessage("unregister\n"+filename);
        if(!resout.equals("true")){
            System.out.println("unregister failed");
            return false;
        }
        return true;
    }

    //get file from other peer
    public boolean get_file(String filename) throws IOException, InterruptedException {
        while (!initalized){
            Thread.sleep(500);
        }
        //get peer location from indexing server;
        long srt=System.currentTimeMillis();
        if(filenames.contains(filename)){
            return false;
        }
        SocketClient get_peer_address=new SocketClient(indexing_sever_address,indexing_sever_port);
        String peers=get_peer_address.sendMessage("search\n"+filename+"\n"+split);
        long end=System.currentTimeMillis();
        message++;bytes+=("search\n"+filename+peers).length()*2;
        response+=end-srt;aver_reponse=(double)response/message;
        if(peers.equals("file not exist!"))return false;
        String[] peer_address=peers.split("\n");
        if(!split){
            for(String peer:peer_address){
                SocketClient sock = null;
                try {
                    String[] location=peer.split(":");
                    srt=System.currentTimeMillis();
                    sock = new SocketClient(location[0], Integer.parseInt(location[1]));
                    String file=sock.sendMessage(split+"\n"+filename);
                    end=System.currentTimeMillis();
                    message++;bytes+=(split+"\n"+filename+file).length()*2;
                    response+=end-srt;aver_reponse=(double)response/message;
                    BufferedWriter bos = new BufferedWriter(new FileWriter(file_folder+"/"+filename));
                    bos.write(file);
                    bos.close();
                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return false;
        }else{
            int order=0;
            String[] file_parts=new String[peer_address.length];
            for(String peer:peer_address){
                SocketClient sock = null;
                    obtain_split o=new obtain_split(peer,peer_server_port,filename,peer_address.length,order,file_folder);
                    o.start();
                    order++;
            }
            //merge files to one
            BufferedWriter res = new BufferedWriter(new FileWriter(file_folder+"/"+filename));
            char[] buffer=new char[1024];
            int count=0;
            for(int i=0;i<peer_address.length;i++){
                File part=new File(file_folder+"/"+filename+"_"+i);
                BufferedReader in=new BufferedReader(new FileReader(part));
                while((count=in.read(buffer))>0){
                    res.write(buffer);
                }
                in.close();
                part.delete();
            }
            res.close();
            return true;
        }

    }

    //get mertics
    public double[] get_stat(){
        double[] stat=new double[3];
        stat[0]=message;stat[1]=bytes;stat[2]=aver_reponse;
        return stat;
    }


    //obtain file splted
    class obtain_split extends Thread {
        final String address;
        final int port;
        final String res;
        final String filename;
        final int order;
        final int total;
        final String folder;
        private SocketClient sock;
        public obtain_split (String a, int p ,String f,int total,int order,String folder)
        {
            address=a;
            this.port=p;
            res="";
            filename=f;
            this.total=total;
            this.order=order;
            this.folder=folder;
        }

        @Override
        public void run() {
            long srt=System.currentTimeMillis();
            sock=new SocketClient(address,port);
            String part=sock.sendMessage(true+"\n"+filename+"\n"+total+"\n"+order);
            long end=System.currentTimeMillis();
            message++;bytes+=(true+"\n"+filename+"\n"+total+"\n"+order+part).length()*2;
            response+=end-srt;aver_reponse=(double)response/message;
            try {
                BufferedWriter bos = new BufferedWriter(new FileWriter(folder+"/"+filename+"_"+order));
                bos.write(part);
                bos.close();
            }  catch (IOException e) {
                e.printStackTrace();
            }
        }
    }




    //thread to server multiple request at same time
    class download_handler extends Thread
    {
        final DataInputStream dis;
        final DataOutputStream dos;
        final Socket s;
        final String res;
        final boolean done;

        // Constructor
        public download_handler(Socket s, DataInputStream dis, DataOutputStream dos)
        {
            this.s = s;
            this.dis = dis;
            this.dos = dos;
            done=false;
            res="";
        }

        @Override
        public void run()
        {
            String received;
            String toreturn;
            try {
                String[] input = dis.readUTF().split("\n");
                boolean split=input[0].equals("true");
                String filename_to_send=input[1];
                int order=Integer.parseInt(input[3]);
                int parts=Integer.parseInt(input[2]);
                if(filename_to_send.equals("stop")){
                    return;
                }
                if(!filenames.contains(filename_to_send))
                {
                    dos.writeUTF("File does not exit");
                }
                //send file to peer
                byte[] mybytearray=null;
                int read_size;
                try{
                    File file_to_send = new File(file_folder + "/" + filename_to_send);
                    if(!split) {
                        mybytearray = new byte[(int) file_to_send.length()];
                        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file_to_send));
                        read_size=bis.read(mybytearray, 0, mybytearray.length);
                        dos.write(mybytearray, 0, mybytearray.length);
                        dos.flush();
                        s.close();
                    }else {
                        int buffer_size=(int) ((file_to_send.length()+parts)/parts);
                        mybytearray = new byte[buffer_size];
                        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file_to_send));
                        read_size=bis.read(mybytearray,buffer_size*order,buffer_size);
                        dos.write(mybytearray, buffer_size*order, read_size);
                        dos.flush();
                        s.close();
                    }
                    load--;

                }catch (IOException e){
                    unregister(filename_to_send);
                    dos.writeUTF("File does not exit");
                    filenames.remove(filename_to_send);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            try
            {
                // closing resources
                this.dis.close();
                this.dos.close();

            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }

}
