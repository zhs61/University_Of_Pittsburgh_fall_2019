import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class IndexServer1 {
	public static Map<String, ArrayList<Integer>> registerFile = new HashMap<>();
	public static Map<Integer, InetAddress> registerPeer = new HashMap<>();
	private static int uniquePeerId;
	
	public static void main(String[] args) {
		try {
			ServerSocket server=new ServerSocket(6666);
			System.out.println("listening to 6666...");
			while (true) {
				Socket serverClient=server.accept();
				ServerClientThread sct = new ServerClientThread(serverClient, registerFile, registerPeer, uniquePeerId);
				sct.start();
				sct.join();
				registerFile = sct.registerFile;
				registerPeer = sct.registerPeer;
				uniquePeerId = sct.uniquePeerId;
				
			}
		} catch(Exception e){
		      System.out.println(e);
	    }
	}
}
