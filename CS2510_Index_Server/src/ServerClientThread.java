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
	Map<Integer, String> registerPeer;
	Map<String, String> peerLoad;
	int uniquePeerId;
	
	ServerClientThread(Socket inSocket, Map<String, ArrayList<Integer>> registerFile, Map<Integer, String> registerPeer, int uniquePeerId, Map<String, String> peerLoad){
	    serverClient = inSocket;
	    this.registerFile = registerFile;
	    this.registerPeer = registerPeer;
	    this.uniquePeerId = uniquePeerId;
	    this.peerLoad = peerLoad;
	}
	
	public void run() {
		try{
		    DataInputStream inStream = new DataInputStream(serverClient.getInputStream());
		    DataOutputStream outStream = new DataOutputStream(serverClient.getOutputStream());
		    Scanner s = new Scanner(inStream);	
		    String operator = s.nextLine();
		    if (operator.equals("start")) {
		    	int peerId = start(serverClient.getInetAddress(), serverClient.getLocalPort());
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
		    	String filename = s.nextLine();
		    	String split = s.nextLine();
		    	String result = "";
		    	if (split.startsWith("tru")) {
		    		result = search1(filename);
		    	} else {
		    		result = search2(filename);
		    	}
		    	outStream.writeUTF(result);
	    		outStream.flush();
		    } else if (operator.contentEquals("load")) {
		    	String peerid = s.nextLine();
		    	String load = s.nextLine();
		    	int result = load(peerid, load);
		    	if (result == 1) {
		            outStream.writeUTF("load updated.");
		    		outStream.flush();
		    	} else {
		    		outStream.writeUTF("load not successfully updated!");
		    		outStream.flush();
		    	}
		    }
		    inStream.close();
		    outStream.close();
		    serverClient.close();
		}catch(Exception ex){
		    System.out.println(ex);
		}finally{
		}
		
	}
	
	/**
	 * update the load if a peer registered
	 * @param peerid
	 * @param load
	 * @return 1 successfully updated
	 * 		   0 unsuccessfully updated
	 */
	private int load(String peerid, String load) {
		if (registerPeer.containsKey(Integer.parseInt(peerid))) {
			peerLoad.put(peerid, load);
			return 1;
		} else {
			return 0;
		}
	}

	/**
	 * register the peer, store the address and port
	 * @param address
	 * @param port
	 * @return 1
	 */
	private int start(InetAddress address, int port) {
		int peerId = uniquePeerId;
		uniquePeerId+=1;
		String addAndPort = address.getHostAddress() + ":" + port;
		registerPeer.put(peerId, addAndPort);
		return peerId;
	}
	
	/**
	 * register files 
	 * @param filename
	 * @param peerId
	 * @return
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
	
	/**
	 * first search, return max 10 peer who has file
	 * @param filename
	 * @return address+port
	 */
	private String search1(String filename) {
		String result = "";
		if (registerFile.containsKey(filename)) {
			ArrayList<Integer> peerId = registerFile.get(filename);
			if (peerId.size()>10) {
				for (int i = 0; i < 10; i++ ) {
					int pid = peerId.get(i);
					if (registerPeer.containsKey(pid)) {
						String address = registerPeer.get(pid);
						result += address;
						result += "\n";
					}
				}
			} else {
				for (int pid : peerId) {
					if (registerPeer.containsKey(pid)) {
						String address = registerPeer.get(pid);
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
	
	/**
	 * second search method will return the peer with lowest load
	 * @param filename
	 * @return
	 */
	private String search2(String filename) {
		String result = "";
		if (registerFile.containsKey(filename)) {
			ArrayList<Integer> peerId = registerFile.get(filename);
			int min = -1;
			int minPid = -1;
			for (int pid : peerId) {
				if (peerLoad.containsKey(pid+"")) {
					int localMin = Integer.parseInt(peerLoad.get(pid+""));
					if (localMin<min) {
						min = localMin;
						minPid = pid;
					}
				} else {
					result += registerPeer.get(pid);
					result += "\n";
					return result;
				}
			}
			if (minPid != -1) {
				result += registerPeer.get(minPid);
				result += "\n";
			} else {
				result = "file not exist!";
			}
			return result;
		} else { 
			return "file not exist!";
		}
	}
}
	