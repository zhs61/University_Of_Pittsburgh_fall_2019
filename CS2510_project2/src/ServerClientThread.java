import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Scanner;

class ServerClientThread extends Thread {
	Socket serverClient;
	int clientNo;
	int squre;
	Hashtable<Integer, String> registerNode;
	Hashtable<String, ArrayList<Integer>> registerFile;
	
	ServerClientThread(Socket inSocket,int counter, Hashtable<Integer, String> registerNode, Hashtable<String, ArrayList<Integer>> registerFile){
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

			System.out.println("From Client-" +clientNo+ ": Number is :"+clientMessage);

			if (operator.equals("registerNode")) {
				boolean isRegisted = registerNode(scan.nextLine());
				if (isRegisted) {
					System.out.println("Successfuly registeed Client: " + clientNo);
					serverMessage = "clientNo";
				} else {
					System.out.println("Unuccessfuly registeed Client: " + clientNo);
					serverMessage = "-1";
				}
			} else if (operator.equals("registerFile")) {
				String filename = scan.nextLine();
				String uId = scan.nextLine();
				registerFile(filename, uId);
				System.out.println("Successfuly registeed File: " + filename + " Client: " + uId);
			} else if (operator.equals("newFile")) {
				String filename = scan.nextLine();
				newFile(filename, scan);
			}

			outStream.writeUTF(serverMessage);
			inStream.close();
			outStream.close();
			serverClient.close();
		}catch(Exception ex){
			System.out.println(ex);
		}finally{
			System.out.println("Client-" + clientNo + " exit!! ");
		}
	}

	private synchronized void registerFile(String filename, String id) {
		int uId = Integer.parseInt(id);
		ArrayList<Integer> temp;
		if (registerFile.contains(filename)) {
			temp = registerFile.get(filename);	
		} else {
			temp = new ArrayList<Integer>();
		}
		temp.add(uId);
		registerFile.put(filename, temp);
	}

	private synchronized void newFile(String filename, Scanner scan) {
		
		
	}

	private synchronized boolean registerNode(String address) {
		registerNode.put(clientNo, address);
		return true;
	}
}