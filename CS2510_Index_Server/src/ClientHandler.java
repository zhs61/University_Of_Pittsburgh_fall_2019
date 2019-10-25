import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ClientHandler extends Thread{
	
	final InputStreamReader isr;
	final OutputStreamWriter osw;
	final Socket clientSocket;
	private Map<String, ArrayList<Integer>> registerFile;
	private Map<Integer, InetAddress> registerPeer;
	private int uniquePeerId;
	
	public ClientHandler(Socket s, InputStreamReader isr, OutputStreamWriter osw, Map<String, ArrayList<Integer>> registerFile, Map<Integer, InetAddress> registerPeer, int uniquePeerId)  
    { 
        this.clientSocket = s; 
        this.isr = isr; 
        this.osw = osw; 
        this.registerFile = registerFile;
        this.registerPeer = registerPeer;
        this.uniquePeerId = uniquePeerId;
    } 
	
	public void run(){
		BufferedReader reader = new BufferedReader(isr);
		operator = reader.readLine();
		if (operator.equals("start")) {
			int peerId = start(clientSocket.getInetAddress());
			if (peerId >= 1) {
				PrintWriter out = new PrintWriter(clientSocket.getOutputStream());
	            out.println("true");
	            out.println(peerId +"");
	            out.flush();
			} else {
				PrintWriter out = new PrintWriter(clientSocket.getOutputStream());
	            out.println("false");
	            out.println("-1");
	            out.flush();
			}
		} else if (operator.equals("register")) {
			content = reader.readLine();
			String peerId = reader.readLine();
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
			content = reader.readLine();
			String result = search(content);
			PrintWriter out = new PrintWriter(clientSocket.getOutputStream());
            out.println(result);
            out.flush();
		}
	}
	
	private String search(String filename) {
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
	private int start(InetAddress address) {
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
	private int register(String filename, String peerId) {
		ArrayList<Integer> pid = new ArrayList<Integer>();
		if (registerFile.containsKey(filename)) {
			pid = registerFile.get(filename);
		}
		pid.add(Integer.parseInt(peerId));
		registerFile.put(filename, pid);
		return 1;
	}
	
}
