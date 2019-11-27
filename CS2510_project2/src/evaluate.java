import java.io.*;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Random;

public class evaluate {
    public static void main(String args){

    }

    class client_thread extends Thread{
        Properties p;
        int uplod_ratio;
        int request_num;
        int frequency;
        int max=1677721600;
        String main_directory;
        String back_up_directory;
        int directory_port;
        double averge_response=0;
        long total_time=0;
        int total_message=0;
        long total_bytes=0;
        double averge_messgae=0;
        double averge_byte=0;
        public client_thread(int request_num,int uplod_ratio,int frequency,Properties p){
            this.frequency=frequency;
            this.uplod_ratio=uplod_ratio;
            this.request_num=request_num;
            this.p=p;
            main_directory=p.getProperty("primary_directory_sever");
            back_up_directory=p.getProperty("secondary_directory_sever");
            directory_port=Integer.parseInt(p.getProperty("directory_sever_port"));
        }
        @Override
        public void run() {
            for (int i=0;i<request_num;i++){
                long startTime = System.currentTimeMillis();
                int coin=new Random().nextInt(100);
                if(coin>=uplod_ratio){
                    //upload
                    File f=new File("test/testfile/random"+i);
                    int actual_size=new Random().nextInt(max);
                    byte[] file=new byte[actual_size];
                    new Random().nextBytes(file);
                    BufferedWriter in= null;
                    try {
                        in = new BufferedWriter(new FileWriter(f));
                        in.write(new String(file));
                        in.close();
                        InputStream input=new FileInputStream(f);
                        SocketClient s=get_connection();
                        if(s!=null)
                            s.sendFile("random"+i,f.length(),input);
                            total_message+=1;
                            total_bytes+=f.length();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    f.delete();

                }else {
                    //get file
                    String res=safe_send("getFile");
                    if(res.equals("connection fails")){
                        System.out.println("connection fails");
                        return;
                    }
                    String[] files=res.split("\n");
                    int pick=new Random().nextInt(files.length);
                    SocketClient s=get_connection();
                    String rep=s.sendMessage_reply("to_client\n"+files[pick]);
                    total_message+=1;
                    total_bytes+=res.getBytes().length;
                }
                long endTime = System.currentTimeMillis();
                total_time+=endTime-startTime;
            }
            averge_response=total_time/(double)request_num;
        }

        private  SocketClient get_connection() {
            String res=safe_send("connect");
            if(res.equals("connection fails")){
                System.out.println("connection fails");
                return null;
            }
            String address=res.split(":")[0];
            int port=Integer.parseInt(res.split(":")[0]);
            return new SocketClient(address,port);
        }

         String safe_send(String message){
            SocketClient s = new SocketClient(main_directory,directory_port);
            String res=s.sendMessage_reply(message);
            total_message+=1;
            total_bytes+=message.getBytes().length+res.getBytes().length;
            if (res.equals("connection fails")){
                s=new SocketClient(back_up_directory,directory_port);
                res=s.sendMessage_reply(message);
                total_message+=1;
                total_bytes+=message.getBytes().length+res.getBytes().length;
            }
            return res;
        }

        ArrayList<Double> get_stat(){
            ArrayList<Double> res=new ArrayList<>();
            res.add((double)total_message);
            res.add((double)total_bytes);
            res.add(averge_response);
            return res;
        }
    }


}
