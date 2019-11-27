import java.net.*;
import java.io.*;
import java.util.Scanner;

public class SocketClient {
    private String hostName;
    private int portNum;
    private int delaySecond;
    private Socket s=null;
    public SocketClient(String address,int port) {
        this.hostName = address;
        this.portNum = port;
        this.delaySecond = 5000;
    }
    public SocketClient(Socket s) {
        this.s=s;
    }
    private Socket getSocket() {
        if(s==null) {
            Socket socket = null;
            try {
                socket = new Socket(hostName, portNum);
            } catch (IOException e) {
                return null;
            }
            return socket;
        }else {
            return s;
        }
    }
    public String getHostName(){
        return s.getInetAddress().getHostAddress()+" "+s.getPort();
    }

    public boolean sendFile(String filename,long filesize,InputStream file){
        Socket socket=null;
        try {
            socket = getSocket();
            assert socket != null;
            final BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream());
            BufferedInputStream inStream=new BufferedInputStream(file);
            out.write((filename+"\n").getBytes());
            out.write((filesize+"\n").getBytes());
            long actual_size=0;
            byte[] buffer=new byte[8*1024];
            int len;
            while((len=inStream.read(buffer))>0){
                out.write(buffer,0,len);
                actual_size+=len;
            }
            assert actual_size==filesize;
            out.flush();
            StringBuilder str= new StringBuilder();
            inStream=new BufferedInputStream(socket.getInputStream());
            while ((len=inStream.read(buffer))>0){
                str.append(new String(buffer, 0, len));
            }
            out.close();
            return str.toString().equals("success");
        }catch (SocketTimeoutException e){
            return false;
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void sendMessage(String strMessage) {
        String str = "";
        String serverString = "";
        strMessage+="\n";
        Socket socket;
        try {
            socket = getSocket();
            if (socket == null) {
                throw new SocketException();
            }
            BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream());
            out.write(strMessage.getBytes());
            out.flush();
            out.close();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
        public void set_port(int port){
            this.portNum=port;
        }
    public String sendMessage_reply(String strMessage) {
        String str = "";
        String serverString = "";
        Socket socket;
        strMessage+="\n";
        try {
            socket = getSocket();
            if (socket == null) {
                return  "connection fails";
            }
            BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream());
            out.write(strMessage.getBytes());
            out.flush();
            String temp="";
            final BufferedInputStream inStream = new BufferedInputStream(socket.getInputStream());
            byte[] buffer=new byte[8*1024];
            int len;
            while((len=inStream.read(buffer))>0){
                temp=temp+new String(buffer,0,len);
            }
            inStream.close();
            return temp;

        } catch (SocketTimeoutException e) {
            str = "connection failed";
        } catch (Exception e) {
            str="2177";
        }
        return  "connection fails";
    }

}
