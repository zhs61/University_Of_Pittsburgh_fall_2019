import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Random;
import java.util.Scanner;

class ServerClientThread extends Thread {
	Socket serverClient;
	int clientNo;
	int squre;
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
			System.out.println("Thread-" +clientNo+ ": input message is: "+clientMessage);

			if (operator.equals("report")) {
				boolean isRegisted = registerNode(scan, addPort);
				if (isRegisted) {
					System.out.println("Successfuly registeed Thread: " + clientNo);
					serverMessage = "true";
				} else {
					System.out.println("Unuccessfuly registeed Thread: " + clientNo);
					serverMessage = "false";
				}
			} else if (operator.equals("registerFile")) {
				String filename = scan.nextLine();
				String uId = scan.nextLine();
				registerFile(filename, uId);
				System.out.println("Successfuly registeed File: " + filename + " Client: " + uId);
			} else if (operator.equals("newFile")) {
				String filename = scan.nextLine();
				serverMessage = newFile(filename, addPort);
			} else if (operator.equals("getFile")) {
				serverMessage = getFileList();
			} else if (operator.equals("connect")) {
				serverMessage = connect();
			} else if (operator.equals("init")) {
				initial(scan, addPort);
			}

			outStream.writeUTF(serverMessage);
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
		String result = "";
		for (String str : registerFile.keySet()) {
			result += str + "\n";
		}
		return result;
	}

	/**
	 * Register a single file at a time, add the storage address to the directory server.
	 * @param filename
	 * @param addPort
	 */
	private synchronized void registerFile(String filename, String addPort) {
		ArrayList<String> temp;
		if (registerFile.contains(filename)) {
			temp = registerFile.get(filename);	
		} else {
			temp = new ArrayList<String>();
		}
		temp.add(addPort);
		registerFile.put(filename, temp);
	}

	/**
	 * Invoke by storae node to get the location fo the other storage node.
	 * @param filename
	 * @param addPort
	 * @return
	 */
	private synchronized String newFile(String filename, String addPort) {
		String result = "";
		if (registerFile.contains(filename)) {
			ArrayList<String> temp = registerFile.get(filename);
			for (String add : temp) {
				if (!add.equals(addPort)) {
					result += add +"\n";
				}
			}
		} else {
			System.out.println("File not exist!");
		}
		return result;
	}

	/**
	 * Invoke by storage node to report the file store on it. 
	 * @param scan
	 * @param addPort
	 * @return
	 */
	private synchronized boolean registerNode(Scanner scan, String addPort) {
		String filenames = "";
		while(scan.hasNextLine()) {
			String line = scan.nextLine();
			filenames += line + " ";
		}
		String[] split = filenames.split(" ");
		for (String filename : split) {
			if (registerFile.contains(filename)) {
				ArrayList<String> temp = registerFile.get(filename);
				temp.add(addPort);
				registerFile.put(filename, temp);
			} else {
				ArrayList<String> temp = new ArrayList<>();
				temp.add(addPort);
				registerFile.put(filename, temp);
			}
			
		}
		return true;
	}
	
	private synchronized boolean checkConsistency() {
		for (String filename : registerFile.keySet()) {
			ArrayList<String> nodes = registerFile.get(filename);
			if (nodes.size()!=registerNode.size()) {
				String nodeNeedUpdate = "";
				for (String node : registerNode) {
					if (!nodes.contains(node)) {
						nodeNeedUpdate += node + "\n";
					}
				}
				sendReplicaRequest(filename, nodeNeedUpdate);
			}else {
				continue;
			}
		}
		return true;
	}

	private synchronized void sendReplicaRequest(String filename, String nodeNeedUpdate) {
		try {
			Socket socket=new Socket(address,port);
			DataInputStream inStream = new DataInputStream(serverClient.getInputStream());
			DataOutputStream outStream = new DataOutputStream(serverClient.getOutputStream());
			String clientMessage="", serverMessage="";
			serverMessage = "replicate\n" + filename + "\n" + nodeNeedUpdate;
			outStream.writeUTF(serverMessage);
			outStream.flush();
			
			outStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private synchronized void initial(Scanner scan, String addPort) {
		registerNode.add(addPort);
		while (scan.hasNext()) {
			String filename = scan.nextLine();
			if (registerFile.contains(filename)) {
				ArrayList<String> temp = registerFile.get(filename);
				temp.add(addPort);
				registerFile.put(filename, temp);
			} else {
				ArrayList<String> temp = new ArrayList<>();
				temp.add(addPort);
				registerFile.put(filename, temp);
			}
		}
	}
}