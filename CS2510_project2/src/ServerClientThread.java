import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Random;
import java.util.Scanner;

class ServerClientThread extends Thread {
	Socket serverClient;
	int clientNo;
	String address;
	int port;
	ArrayList<String> registerNode;
	Hashtable<String, ArrayList<String>> registerFile;

	ServerClientThread(Socket inSocket,int counter, ArrayList<String> registerNode, Hashtable<String, ArrayList<String>> registerFile){
		serverClient = inSocket;
		clientNo=counter;
		this.registerNode = registerNode;
		this.registerFile = registerFile;
	}

	public void run(){
		try{
			DataInputStream inStream = new DataInputStream(serverClient.getInputStream());
			DataOutputStream outStream = new DataOutputStream(serverClient.getOutputStream());
			String clientMessage="", serverMessage="";
			Scanner scan = new Scanner(inStream);
			String operator = scan.nextLine();
			address = serverClient.getInetAddress().getHostAddress();
			port = serverClient.getPort();
			String addPort = address + ":" + port;

			if (operator.equals("newFile")) {
				System.out.println("Start of New File method ============================");
				String filename = scan.nextLine();
				String Lport = scan.nextLine();
				String addLPort = address + ":" + Lport;
				if (newFile(filename, addLPort)){
					serverMessage = "true";
				} else {
					serverMessage = "false";
				}
				System.out.println("End of New File method ============================");
			} else if (operator.equals("getFile")) {
				serverMessage = getFileList();
				System.out.println("Directory server obtain the file list to client: " + addPort);
			} else if (operator.equals("connect")) {
				serverMessage = connect();
				System.out.println("Directory server ontain the address to client: " + addPort);
			} else if (operator.equals("init")) {
				if (initial(scan, address)) {
					System.out.println("Successfully initial storage node: " + addPort);
				} else {
					System.out.println("Unsuccessfully initial storage node: " + addPort);
				}
				serverMessage = "true";
			}

			outStream.write(serverMessage.getBytes());
			outStream.flush();
			inStream.close();
			outStream.close();
			serverClient.close();
		}catch(Exception ex){
			System.out.println(ex);
		}finally{
			System.out.println("Client-" + clientNo + " exit!! ");
		}
	}

	/**
	 * invoke by client to get the ip address of a random storage node.
	 * @return
	 */
	private synchronized String connect() {
		Random r = new Random();
		int index = r.nextInt(registerNode.size());
		return registerNode.get(index);
	}

	/**
	 * Get all the file store in the file system.
	 * @return
	 */
	private synchronized String getFileList() {
		System.out.println("Client wants to get the file list.");
		String result = "";
		for (String str : registerFile.keySet()) {
			result += str + "\n";
		}
		return result;
	}

	/**
	 * Invoke by storage node to get the location of the other storage node.
	 * @param filename
	 * @param addPort
	 * @return
	 */
	private synchronized boolean newFile(String filename, String addPort) {
		System.out.println("Storage node wants to add new file: " + filename);
		String nodeNeedUpdate = "";
		for (String add : registerNode) {
			if (!add.contentEquals(addPort)) {
				nodeNeedUpdate = nodeNeedUpdate + add + "\n";
			}
		}
		ArrayList<String> result = sendReplicaRequest(filename, nodeNeedUpdate);
		if (result == null) {
			System.out.println("Not succeffuly set up the socket.");
			return false;
		} else if (result.size()==0) {
			System.out.println("Succeffully consistent all nodes and file.");
		} else if (result.size()>=1) {
			System.out.println("Some node are not successfully get file: ");
			for (String add : result) {
				System.out.println(add);
			}
			return false;
		}
		return true;
	}

	/**
	 * Establish a socket to call call the replica method in storage node to send file to other storage nodes.
	 * @param filename
	 * @param nodeNeedUpdate
	 * @return 
	 */
	private synchronized ArrayList<String> sendReplicaRequest(String filename, String nodeNeedUpdate) {
		try {
			Socket socket=new Socket(address,port);
			DataInputStream inStream = new DataInputStream(socket.getInputStream());
			DataOutputStream outStream = new DataOutputStream(socket.getOutputStream());
			String clientMessage="", serverMessage="";
			serverMessage = "replicate\n" + filename + "\n" + nodeNeedUpdate;
			outStream.write(serverMessage.getBytes());
			System.out.println("Sending request to storage node: " + address + ":" + port);
			outStream.flush();
			Scanner scan = new Scanner(inStream);
			ArrayList<String> failedNode = new ArrayList<>();
			while (scan.hasNextLine()) {
				String result = scan.nextLine();
				String address = scan.nextLine();
				if (result.contentEquals("false")) {
					failedNode.add(address);
				}
			}
			outStream.close();
			inStream.close();
			socket.close();
			return failedNode;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * When the storage nodes are online, they will call this method to initial themselves in the system.
	 * it will first record the address of the node and store the filenames in the storage node.
	 * @param scan
	 */
	private synchronized boolean initial(Scanner scan, String address) {
		String port = scan.nextLine();
		String addLPort = address + ":" + port;
		registerNode.add(addLPort);
		System.out.println("Successfully registered address: " + addLPort);
		int numOfFile = Integer.parseInt(scan.nextLine());
		for (int i = 0; i < numOfFile; i++) {
			String filename = scan.nextLine();
			if (registerFile.contains(filename)) {
				ArrayList<String> temp = registerFile.get(filename);
				temp.add(addLPort);
				registerFile.put(filename, temp);
			} else {
				ArrayList<String> temp = new ArrayList<>();
				temp.add(addLPort);
				registerFile.put(filename, temp);
			}
			System.out.println("Registered file: " + filename);
		}
		return true;
	}
}