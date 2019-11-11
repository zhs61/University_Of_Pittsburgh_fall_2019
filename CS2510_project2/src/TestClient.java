import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class TestClient {
	public static void main(String[] args) throws Exception {
		try{
			Socket socket=new Socket("127.0.0.1",6666);
			DataInputStream inStream=new DataInputStream(socket.getInputStream());
			DataOutputStream outStream=new DataOutputStream(socket.getOutputStream());
			Scanner scan = new Scanner(System.in);
			String clientMessage="",serverMessage="";
			PrintWriter out = new PrintWriter(socket.getOutputStream());
            out.println("register\ngggg");
			out.flush();
			serverMessage=inStream.readUTF();
			System.out.println(serverMessage);

			outStream.close();
			outStream.close();
			socket.close();
		}catch(Exception e){
			System.out.println(e);	
		}
	}

}
