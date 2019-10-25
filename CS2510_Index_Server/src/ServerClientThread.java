import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;

public class ServerClientThread extends Thread{
	Socket serverClient;
	Map<String, ArrayList<Integer>> registerFile;
	Map<Integer, InetAddress> registerPeer;
	int uniquePeerId;
	
	ServerClientThread(Socket inSocket, Map<String, ArrayList<Integer>> registerFile, Map<Integer, InetAddress> registerPeer, int uniquePeerId){
	    serverClient = inSocket;
	    this.registerFile = registerFile;
	    this.registerPeer = registerPeer;
	    this.uniquePeerId = uniquePeerId;
	}
	
	public void run() {
		try{
		    DataInputStream inStream = new DataInputStream(serverClient.getInputStream());
		    DataOutputStream outStream = new DataOutputStream(serverClient.getOutputStream());
		    Scanner s = new Scanner(inStream);	
		    String operator = s.nextLine();
		    if (operator.equals("start")) {
		    	int peerId = start(serverClient.getInetAddress());
		    	if (peerId >= 0) {
		    		outStream.writeUTF("true\n" + peerId);
		    		outStream.flush();
		    	} else {
		    		outStream.writeUTF("false\n-1");
		    		outStream.flush();
		    	}
		    } else if (operator.equals("register")) {
		    	String content = s.nextLine();
		    	String peerId = s.nextLine();
		    	if (register(content, peerId) == 1) {
		            outStream.writeUTF("true");
		    		outStream.flush();
		    	} else {
		    		outStream.writeUTF("false");
		    		outStream.flush();
		    	}
		    } else if (operator.equals("search")) {
		    	String content = s.nextLine();
		    	String result = search(content);
		    	outStream.writeUTF(result);
	    		outStream.flush();
		    }
		    inStream.close();
		    outStream.close();
		    serverClient.close();
		}catch(Exception ex){
		    System.out.println(ex);
		}finally{
		}
		
	}
	private int start(InetAddress address) {
		int peerId = uniquePeerId;
		uniquePeerId+=1;
		registerPeer.put(peerId, address);
		return peerId;
	}
	private int register(String filename, String peerId) {
		ArrayList<Integer> pid = new ArrayList<Integer>();
		if (registerFile.containsKey(filename)) {
			pid = registerFile.get(filename);
		}
		pid.add(Integer.parseInt(peerId));
		registerFile.put(filename, pid);
		return 1;
	}
	private String search(String filename) {
		String result = "";
		if (registerFile.containsKey(filename)) {
			ArrayList<Integer> peerId = registerFile.get(filename);
			if (peerId.size()>10) {
				for (int i = 0; i < 10; i++ ) {
					int pid = peerId.get(i);
					if (registerPeer.containsKey(pid)) {
						String address = registerPeer.get(pid).getHostAddress();
						result += address;
						result += "\n";
					}
				}
			} else {
				for (int pid : peerId) {
					if (registerPeer.containsKey(pid)) {
						String address = registerPeer.get(pid).getHostAddress();
						result += address;
						result += "\n";
					}
				}
			}
			return result;
		} else { 
			return "file not exist!";
		}
	}
}
	