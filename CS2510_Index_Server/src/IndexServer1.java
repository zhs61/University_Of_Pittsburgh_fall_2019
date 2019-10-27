import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class IndexServer1 {
	public static Map<String, ArrayList<Integer>> registerFile = new HashMap<>();
	public static Map<Integer, String> registerPeer = new HashMap<>();
	public static Map<String, String> peerLoad = new HashMap<>();
	private static int uniquePeerId;
	private static String indexing_sever_port;
	
	public static void main(String[] args) {
		
		try {
			config();
			ServerSocket server=new ServerSocket(Integer.parseInt(indexing_sever_port));
			System.out.println("listening to 6666...");
			while (true) {
				Socket serverClient=server.accept();
				ServerClientThread sct = new ServerClientThread(serverClient, registerFile, registerPeer, uniquePeerId, peerLoad);
				sct.start();
				sct.join();
				registerFile = sct.registerFile;
				registerPeer = sct.registerPeer;
				uniquePeerId = sct.uniquePeerId;
				peerLoad = sct.peerLoad;
			}
		} catch(Exception e){
		      System.out.println(e);
	    }
	}
	
	private static void config() throws IOException {
		InputStream inputStream = null;
		Properties prop = new Properties();
		try {
		    String propFileName = "./resources/config.properties";

		    inputStream = new FileInputStream(propFileName);

		    if (inputStream != null) {
		        prop.load(inputStream);
		    } else {
		        throw new IOException("property file '" + propFileName + "' not found in the classpath");
		    }
		    // get the property value and print it out
		    indexing_sever_port = prop.getProperty("indexing_sever_port");
		    
		} catch (Exception e) {
		    System.out.println("Exception: " + e);
		    System.exit(1);
		}
		inputStream.close();
	}
}
