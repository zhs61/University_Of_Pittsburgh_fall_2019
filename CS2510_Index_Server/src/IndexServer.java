import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;

public class IndexServer {

	// ArrayList<Integer> pid = new ArrayList<Integer>();
	public static Map<String, ArrayList<Integer>> registerFile = new HashMap<>();
	public static Map<Integer, InetAddress> registerPeer = new HashMap<>();
	private static int uniquePeerId = 1;
	private static String indexing_sever_port;
	static ServerSocket server;
	
	public static void main(String[] args) throws IOException {
		String operator = "";
		String content = "";
		config();
		System.out.println("Listening for connection on port " + indexing_sever_port + " ....");
		Thread worker=new Thread(new Runnable(){
			@Override
            public void run() {
				try {
					server = new ServerSocket(Integer.parseInt(indexing_sever_port));
					while (true) {
						Socket clientSocket = server.accept();
						DataInputStream isr = new DataInputStream(clientSocket.getInputStream());
						DataOutputStream osw = new DataOutputStream(clientSocket.getOutputStream());
						Scanner reader=new Scanner(isr);
	                    String operator=reader.nextLine();
					if (operator.equals("start")) {
	                	int peerId = start(clientSocket.getInetAddress());
	                	if (peerId >= 1) {
//	                		PrintWriter out = new PrintWriter(clientSocket.getOutputStream());
//	                        out.println("true");
//	                        out.println(peerId +"");
//	                        out.flush();
	                		osw.writeUTF("true");
	                		osw.flush();
	                	} else {
	                		PrintWriter out = new PrintWriter(clientSocket.getOutputStream());
	                        out.println("false");
	                        out.println("-1");
	                        out.flush();
	                	}
	                } else if (operator.equals("register")) {
	                	String content = reader.nextLine();
	                	String peerId = reader.nextLine();
	                	if (register(content, peerId) == 1) {
	                		PrintWriter out = new PrintWriter(clientSocket.getOutputStream());
	                        out.println("true");
	                        out.flush();
	                	} else {
	                		PrintWriter out = new PrintWriter(clientSocket.getOutputStream());
	                        out.println("false");
	                        out.flush();
	                	}
	                } else if (operator.equals("search")) {
	                	String content = reader.nextLine();
	                	String result = search(content);
	                	PrintWriter out = new PrintWriter(clientSocket.getOutputStream());
	                    out.println(result);
	                    out.flush();
	                }
					}
				} catch (IOException e) {
	                e.printStackTrace();
	            }
			}
		});
		worker.run();
			

	}	
	
	static class ClientHandler extends Thread {
		final DataInputStream isr;
		final DataOutputStream osw;
		final Socket clientSocket;
		
		public ClientHandler(Socket s, DataInputStream isr, DataOutputStream osw) { 
	        this.clientSocket = s; 
	        this.isr = isr; 
	        this.osw = osw; 
	    } 
		
		public void run() {
			try {
				String[] input = isr.readUTF().split("\n");
				String operator = input[0];
				if (operator.equals("start")) {
					int peerId = IndexServer.start(clientSocket.getInetAddress());
					if (peerId >= 1) {
						// PrintWriter out = new PrintWriter(clientSocket.getOutputStream());
//			            out.println("true");
//			            out.println(peerId +"");
//			            out.flush();
						osw.writeChars("true");
						osw.writeChars(peerId + "");
						osw.flush();
					} else {
//						PrintWriter out = new PrintWriter(clientSocket.getOutputStream());
//			            out.println("false");
//			            out.println("-1");
//			            out.flush();
			            osw.writeChars("false");
						osw.writeChars("-1");
						osw.flush();
					}
				} else if (operator.equals("register")) {
					String content = input[1];
					String peerId = input[2];
					if (register(content, peerId) == 1) {
//						PrintWriter out = new PrintWriter(clientSocket.getOutputStream());
//			            out.println("true");
//			            out.flush();
			            osw.writeChars("true");
						osw.flush();
					} else {
//						PrintWriter out = new PrintWriter(clientSocket.getOutputStream());
//			            out.println("false");
//			            out.flush();
			            osw.writeChars("false");
						osw.flush();
					}
				} else if (operator.equals("search")) {
					String content = input[1];
					String result = search(content);
//					PrintWriter out = new PrintWriter(clientSocket.getOutputStream());
//		            out.println(result);
//		            out.flush();
					osw.writeChars(result);
					osw.flush();
				}
			} catch (IOException e) {
                e.printStackTrace();
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
