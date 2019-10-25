import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Scanner;

public class Peer_server implements Runnable{
    private int id;
    private String indexing_sever_address;
    private int indexing_sever_port;
    private String file_folder;
    private ArrayList<String> filenames;
    ServerSocket serverSocket;
    private int peer_server_port;
    private int peer_client_port;
    private Properties prop;
    private boolean split;
    public Peer_server(Properties p){
        prop=p;
        indexing_sever_address = prop.getProperty("indexing_sever_address");
        indexing_sever_port = Integer.parseInt(prop.getProperty("indexing_sever_port"));
        peer_server_port=Integer.parseInt(prop.getProperty("peer_server_port"));
        peer_client_port=Integer.parseInt(prop.getProperty("peer_client_port"));
        file_folder=prop.getProperty("file_folder");
        filenames=new ArrayList<String>();
        split=prop.getProperty("split").equals("true");
    }

    //run service
    public void run() {
        //contact indexing services
        SocketClient sender=new SocketClient(indexing_sever_address,indexing_sever_port);
        String resout=sender.sendMessage("start");
        id=Integer.parseInt(resout);
        //scan local files
        File[] files = new File(file_folder).listFiles();
        for(File file :files){
            boolean res=register(file.getName());
            if(res){
                filenames.add(file.getName());
            }
        }
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
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        worker.run();
        //serve client request
        Thread client=new Thread(new Runnable(){
            public void run(){
                Thread client=null;
                try {
                    serverSocket=new ServerSocket(peer_client_port);
                    while (true) {
                        Socket socket = serverSocket.accept();
                        DataInputStream dis = new DataInputStream(socket.getInputStream());
                        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                        Scanner s=new Scanner(dis);
                        String operation=s.nextLine();
                        String arg=null;
                        if(operation.equals("get")){
                            arg=s.nextLine();
                            get_file(arg);
                        }else if(operation.equals("register")){
                            arg=s.nextLine();
                            register(arg);
                        }else {
                            worker.stop();
                            return;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        client.start();
    }

    public boolean register(String filename){
        SocketClient sender=new SocketClient(indexing_sever_address,indexing_sever_port);
        String resout=sender.sendMessage("register\n"+id+"\n"+filename);
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


    public boolean get_file(String filename) throws IOException {
        //get peer location from indexing server;
        if(filenames.contains(filename)){
            return false;
        }
        SocketClient get_peer_address=new SocketClient(indexing_sever_address,indexing_sever_port);
        String peers=get_peer_address.sendMessage("search\n"+filename);
        String[] peer_address=peers.split("\n");
        if(!split){
            for(String peer:peer_address){
                SocketClient sock = null;
                try {
                    sock = new SocketClient(peer, peer_server_port);
                    String file=sock.sendMessage(split+"\n"+filename);
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
            BufferedWriter bos = new BufferedWriter(new FileWriter(file_folder+"/"+filename));
            for(String peer:peer_address){
                SocketClient sock = null;
                try {
                    sock = new SocketClient(peer, peer_server_port);
                    String filename_part=filename+"_"+order;
                    String file=sock.sendMessage(split+"\n"+filename+"\n"+peer_address.length+"\n"+order);
                    order++;
                    bos.write(file);
                    bos.close();
                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return true;
        }

    }



    //obtain file splted
    class obtain_split implements Runnable {
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
            sock=new SocketClient(address,port);
            String part=sock.sendMessage(true+"\n"+filename+"\n"+total+"\n"+order);
            try {
                BufferedWriter bos = new BufferedWriter(new FileWriter(folder+"/"+filename+"_"+order));
                bos.write(part);
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
