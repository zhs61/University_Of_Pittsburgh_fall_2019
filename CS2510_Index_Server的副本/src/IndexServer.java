import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class IndexServer {

	// ArrayList<Integer> pid = new ArrayList<Integer>();
	public static Map<String, ArrayList<Integer>> registerFile = new HashMap<>();
	public static Map<Integer, InetAddress> registerPeer = new HashMap<>();
	private static int uniquePeerId = 1;
	private static String indexing_sever_port;
	
	public static void main(String[] args) throws IOException {
		String operator = "";
		String content = "";
		config();
		final ServerSocket server = new ServerSocket(Integer.parseInt(indexing_sever_port));
		System.out.println("Listening for connection on port " + indexing_sever_port + " ....");
		while (true) {
			try(Socket clientSocket = server.accept()) {
				InputStreamReader isr = new InputStreamReader(clientSocket.getInputStream());
				OutputStreamWriter osw = new OutputStreamWriter(clientSocket.getOutputStream());
				
				Thread t = new ClientHandler(clientSocket, isr, osw, registerFile, registerPeer, uniquePeerId);
				t.start();
//				InputStreamReader isr = new InputStreamReader(clientSocket.getInputStream());
//				BufferedReader reader = new BufferedReader(isr);
//				operator = reader.readLine();
//				if (operator.equals("start")) {
//					int peerId = start(clientSocket.getInetAddress());
//					if (peerId >= 1) {
//						PrintWriter out = new PrintWriter(clientSocket.getOutputStream());
//			            out.println("true");
//			            out.println(peerId +"");
//			            out.flush();
//					} else {
//						PrintWriter out = new PrintWriter(clientSocket.getOutputStream());
//			            out.println("false");
//			            out.println("-1");
//			            out.flush();
//					}
//				} else if (operator.equals("register")) {
//					content = reader.readLine();
//					String peerId = reader.readLine();
//					if (register(content, peerId) == 1) {
//						PrintWriter out = new PrintWriter(clientSocket.getOutputStream());
//			            out.println("true");
//			            out.flush();
//					} else {
//						PrintWriter out = new PrintWriter(clientSocket.getOutputStream());
//			            out.println("false");
//			            out.flush();
//					}
//				} else if (operator.equals("search")) {
//					content = reader.readLine();
//					String result = search(content);
//					PrintWriter out = new PrintWriter(clientSocket.getOutputStream());
//		            out.println(result);
//		            out.flush();
//				}
			}
			
		}
	}
	
	private static String search(String filename) {
		String result = "";
		if (registerFile.containsKey(filename)) {
			ArrayList<Integer> peerId = registerFile.get(filename);
			for (int pid : peerId) {
				if (registerPeer.containsKey(pid)) {
					String address = registerPeer.get(pid).getHostAddress();
					result += address;
					result += "\n";
				}
			}
			return result;
		} else { 
			return "file not exist!";
		}
	}
	/**
	 * Initialize the peer in the index server, assign a uniqie id back to the client
	 * @param address
	 * @return peerId
	 */
	private static int start(InetAddress address) {
		int peerId = uniquePeerId;
		uniquePeerId++;
		registerPeer.put(peerId, address);
		return peerId;
	}
	
	/**
	 * register the file with a unique pid of each file
	 * @param filename
	 * @return 1 if register success
	 * 		   0 if register fail
	 */
	private static int register(String filename, String peerId) {
		ArrayList<Integer> pid = new ArrayList<Integer>();
		if (registerFile.containsKey(filename)) {
			pid = registerFile.get(filename);
		}
		pid.add(Integer.parseInt(peerId));
		registerFile.put(filename, pid);
		return 1;
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
