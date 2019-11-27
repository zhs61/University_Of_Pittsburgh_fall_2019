
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Properties;
import java.net.InetAddress;
import java.util.Scanner;

public class StorageNode extends Thread{
    private String primary_directory_sever;
    private String secondary_directory_sever;
    private int directory_sever_port;
    private int secondary_directory_sever_port;
    private int directory_sever_request_port;
    private int storage_data_communication_port;
    private int client_data_communication_port;

    private String file_folder;
    private final ArrayList<String> files;
    StorageNode(Properties p){
        primary_directory_sever=p.getProperty("primary_directory_sever");
        secondary_directory_sever=p.getProperty("secondary_directory_sever");
        directory_sever_port=Integer.parseInt(p.getProperty("directory_sever_port"));
        secondary_directory_sever_port=Integer.parseInt(p.getProperty("secondary_directory_sever_port"));
        directory_sever_request_port=Integer.parseInt(p.getProperty("directory_sever_request_port"));
        storage_data_communication_port=Integer.parseInt(p.getProperty("storage_data_communication_port"));
        client_data_communication_port=Integer.parseInt(p.getProperty("client_data_communication_port"));

        files=new ArrayList<String>();
        file_folder=p.getProperty("file_folder");
    }
    @Override
    public void run() {
        //start storagenode
        try {
            System.out.println("Strage node start on"+InetAddress.getLocalHost());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        // scan local file
        File dir=new File(file_folder);
        if (dir.listFiles()!=null) {
            for (File f : dir.listFiles()) {
                files.add(f.getName());
                System.out.println("Find file "+f.getName());
            }
        }

        //start service
        /*
         1. receieve file from other node
         2. get file from client
         3. get request from directory server
        */
        Thread directory_sever_request = new Thread() {
            public void run() {
                try {
                    ServerSocket lis = new ServerSocket(directory_sever_request_port);
                    while (true) {
                        Socket s = lis.accept();
                        directory_sever_request_handler worker = new directory_sever_request_handler(s);
                        worker.start();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        directory_sever_request.start();
        //accept data from other storage node
        Thread storage_data_pipeline = new Thread() {
            public void run() {
                try {
                    ServerSocket lis = new ServerSocket(storage_data_communication_port);
                    while (true) {
                        Socket s = lis.accept();
                        data_communication_handler worker = new data_communication_handler(s);
                        worker.start();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        storage_data_pipeline.start();


        Thread client_data_pipeline = new Thread() {
            public void run() {
                try {
                    ServerSocket lis = new ServerSocket(client_data_communication_port);
                    while (true) {
                        Socket s = lis.accept();
                        data_communication_handler worker = new data_communication_handler(s);
                        worker.start();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        client_data_pipeline.start();

        //send init
        String files_string=get_files();
        SocketClient main=new SocketClient(primary_directory_sever,directory_sever_port);
        System.out.println("Send initial files to primary directory sever");
        String res1=main.sendMessage_reply(files_string);

        SocketClient back=new SocketClient(secondary_directory_sever,secondary_directory_sever_port);
        System.out.println("Send initial files to secondary directory sever");
        String res2=back.sendMessage_reply(files_string);
        if(res1.equals("connection fails")){
            System.out.println("cant connect to primary directory sever");
        }
        if(res2.equals("connection fails")){
            System.out.println("cant connect to backup directory sever");
        }
        if(res1.equals("connection fails")&&res2.equals("connection fails")){
            System.out.println("Please start directory sever first");
            System.exit(1);
        }
        System.out.println("Storage node started");

    }
    private class directory_sever_request_handler extends Thread
    {
        Socket s;
        directory_sever_request_handler(Socket s){
            this.s=s;
        }
        @Override
        public  void run()
        {
            try {
                InputStream in=s.getInputStream();
                OutputStream out=new DataOutputStream(s.getOutputStream());
                Scanner s=new Scanner(in);
                String op=s.nextLine();
                //replicate to other storage node
                switch (op) {
                    case "replicate": {
                        String file = s.nextLine();
                        int node_num=Integer.parseInt(s.nextLine());
                        for (int i = 0; i < node_num; i++) {
                            String address = s.nextLine();
                            SocketClient replicate = new SocketClient(address, storage_data_communication_port);
                            File f=new File(file_folder + "/" + file);
                            InputStream file_in = new FileInputStream(f);
                            boolean res = replicate.sendFile(file,f.length(), file_in);
                            if (!res) {
                                out.write(("true\n" + replicate.getHostName()).getBytes());
                            } else {
                                out.write(("false\n" + replicate.getHostName()).getBytes());
                            }
                        }
                        break;
                    }
                    case "get_files":
                        StringBuilder str = new StringBuilder();
                        str.append("files:\n");
                        for (String file : files) {
                            str.append("\n").append(file);
                        }
                        out.write(str.toString().getBytes());
                        break;
                    //client want to get file
                    case "to_client": {
                        String file = s.nextLine();
                        File f = new File(file_folder + "/" + file);
                        InputStream filein = new FileInputStream(f);
                        out = new BufferedOutputStream(this.s.getOutputStream());
                        BufferedInputStream inStream=new BufferedInputStream(filein);
                        out.write((file+"\n").getBytes());
                        byte[] buffer=new byte[8*1024];
                        int len;
                        while((len=inStream.read(buffer))>0){
                            out.write(buffer,0,len);
                        }
                        out.flush();
                        out.close();
                    }
                    //res
                    case "ping":
                        out.write("alive".getBytes());
                        out.close();
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private class data_communication_handler extends Thread
    {
        Socket s;
        data_communication_handler(Socket s){
            this.s=s;
        }
        @Override
        public  void run()
        {
            try {
                BufferedReader in=new BufferedReader(new InputStreamReader(s.getInputStream()));
                String filename=in.readLine();
                if (files.contains(filename)){
                    OutputStream out=new DataOutputStream(s.getOutputStream());
                    out.write("success".getBytes());
                    out.close();
                    return;
                }
                long file_size=Long.parseLong(in.readLine());
                File f=new File(file_folder+"/"+filename);
                BufferedWriter b=new BufferedWriter(new FileWriter(f));
                char[] buffer=new char[1024];
                int len=0;
                while (file_size>0&&(len=in.read(buffer))>0){
                    b.write(buffer,0,len);
                    file_size-=len;
                }
                synchronized (files) {
                    files.add(filename);
                }
                OutputStream out=new DataOutputStream(s.getOutputStream());
                out.write("success".getBytes());
                out.close();
                b.close();
                System.out.println("receive file:"+filename+" from "+s.getInetAddress());
                //notify directory node
                if(s.getLocalPort()==client_data_communication_port) {
                    SocketClient newfile = new SocketClient(primary_directory_sever, directory_sever_port);
                    newfile.sendMessage("newFile\n" + filename + "\n" + directory_sever_request_port);
                    newfile = new SocketClient(secondary_directory_sever, secondary_directory_sever_port);
                    newfile.sendMessage("newFile\n" + filename + "\n" + directory_sever_request_port);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

     private String get_files(){
        StringBuilder str=new StringBuilder();
        str.append("init\n");
        str.append(directory_sever_request_port).append("\n");
        str.append(files.size()).append("\n");
        for(String file:files)
            str.append(file).append("\n");
        return str.toString();
    }
}
