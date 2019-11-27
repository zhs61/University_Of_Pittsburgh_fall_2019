import java.io.*;
import java.util.Properties;

public class DFS_client {
    private static String primary_directory_sever="";
    private static String secondary_directory_sever="";
    private static int directory_port=0;
    public static void main(String[] args) throws IOException {
        //support get file and add new file
        Properties prop = new Properties();
        try {
            InputStream in =
                    new FileInputStream(new File("resources/DFS.properties"));
            prop.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        primary_directory_sever=prop.getProperty("primary_directory_sever");
        secondary_directory_sever=prop.getProperty("secondary_directory_sever");
        directory_port=Integer.parseInt(prop.getProperty("directory_sever_port"));
        String op=args[0];
        if(op.equals("getFileList")){
            String res=safe_send("getFile");
            if(res.equals("connection fails")){
                System.out.println("connection fails");
                return;
            }
            String[] files=res.split("\n");
            System.out.println("List files:");
            for (String file:files){
                System.out.println(file);
            }
        }else if(op.equals("addFile")){
            String filename=args[1];
            String filelocation=args[2];
            SocketClient s=get_connection();
            if (s==null)return;
            s.set_port(Integer.parseInt(prop.getProperty("client_data_communication_port")));
            File f=new File(filelocation);
            InputStream in=new FileInputStream(f);
            boolean suceess=s.sendFile(filename,f.length(),in);
            if(suceess){
                System.out.println("Successfully add file");
            }else{
                System.out.println("Add file failed");
            }
        }else{
            //"getFile"
            String filename=args[1];
            String location=args[2];
            String filelocation=location+filename;
            SocketClient s=get_connection();
            if (s==null)return;
            String file=s.sendMessage_reply("to_client\n"+filename);
            int fail=0;
            while (file.equals("connection failed")&&fail<=3){
                fail++;
                s=get_connection();
                file=s.sendMessage_reply("to_client\n"+filename);
            }
            if (fail==3){
                System.out.println("Cant get file");
                return;
            }
            file=file.split("\n",2)[1];
            File f=new File(filelocation);
            BufferedWriter out=new BufferedWriter(new FileWriter(f));
            out.write(file);
            out.close();
            System.out.println("Successfully get file");
        }
    }

    private static SocketClient get_connection() {
        String res=safe_send("connect");
        if(res.equals("connection fails")){
            System.out.println("connection fails");
            return null;
        }
        String address=res.split(":")[0];
        int port=Integer.parseInt(res.split(":")[1]);
        return new SocketClient(address,port);
    }

    static String safe_send(String message){
        SocketClient s = new SocketClient(primary_directory_sever,directory_port);
        String res1=s.sendMessage_reply(message);
        s=new SocketClient(secondary_directory_sever,directory_port);
        String res2=s.sendMessage_reply(message);
        if (res1.equals("connection fails")){
            return res2;
        }else {
            return res1;
        }
    }
}
