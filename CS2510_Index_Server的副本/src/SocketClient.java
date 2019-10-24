
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class SocketClient {
    private String hostName;
    private int portNum;
    private int delaySecond;    // 发文接收返回报文延时
    public SocketClient(String address,int port) {
        this.hostName = address;
        this.portNum = port;
        this.delaySecond = 5000;
    }
    private Socket getSocket() {
        Socket socket = null;
        try {
            socket = new Socket(hostName, portNum);
        }  catch (IOException e) {
            System.out.println("-hostName=" + hostName + "   portNum="
                    + portNum + "---->IO Error" + e.getMessage());
        }
        return socket;
    }
    public String sendMessage(String strMessage) {
        String str = "";
        String serverString = "";
        Socket socket;
        char tagChar[]= new char[1024];
        try {
            socket = getSocket();
            // socket.setKeepAlive(true);
            if (socket == null) { // 未能得到指定的Socket对象,Socket通讯为空
                return "0001";
            }

            PrintWriter out = new PrintWriter(socket.getOutputStream());
            out.println(strMessage);
            out.flush();

            final BufferedInputStream inStream = new BufferedInputStream(socket.getInputStream());
            byte[] buffer=new byte[1024*8];
            int len;
            String temp=null;
            while ((len=inStream.read(buffer))>0){
                temp=new String(buffer,0,len);
                serverString=serverString.concat(temp);
                buffer=new byte[1024*8];
            }
            inStream.close();
            //out.close();
            str=serverString;
            socket.close();

        } catch (IOException e) {

            e.printStackTrace();
            str = "2191";
        } catch (Exception e) {
            str="2177";
        } finally {
            socket = null;
            str.trim();
            //log.info("--->返回的socket通讯字符串="+str);
            return str;
        }
    }

}