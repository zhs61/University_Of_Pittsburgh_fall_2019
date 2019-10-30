import java.net.*;
import java.io.*;
import java.util.Scanner;

public class SocketClient {
    private String hostName;
    private int portNum;
    private int delaySecond;
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
            return null;
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
            if (socket == null) {
                return "can't create connection";
            }

            PrintWriter out = new PrintWriter(socket.getOutputStream());
            out.println(strMessage);
            out.flush();

            final DataInputStream inStream = new DataInputStream(socket.getInputStream());
            serverString=inStream.readUTF();
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
            return str;
        }
    }

}
