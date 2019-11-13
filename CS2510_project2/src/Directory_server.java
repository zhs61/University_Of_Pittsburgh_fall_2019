import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Hashtable;

public class Directory_server {
	
	public static ArrayList<String> registerNode= new ArrayList<>();
	public static Hashtable<String, ArrayList<String>> registerFile = new Hashtable<>();

	public static void main(String[] args) {
		try{
			ServerSocket server=new ServerSocket(6666);
			int counter=0;
			System.out.println("Server Started ....");
			while(true){
				counter++;
				Socket serverClient=server.accept();  //server accept the client connection request
				System.out.println(" >> " + "Thread No:" + counter + " started!");
				ServerClientThread sct = new ServerClientThread(serverClient,counter, registerNode, registerFile); //send  the request to a separate thread
				sct.start();
			}
		}catch(Exception e){
			System.out.println(e);
		}

	}

}
