import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Scanner;

public class Directory_server implements Runnable{

	public final ArrayList<String> registerNode= new ArrayList<>();
	public Hashtable<String, ArrayList<String>> registerFile = new Hashtable<>();
	public int port = 6666;
	public String address = "";
	public int ds_port = 6666;
	public int bs_port = 6665;
	public String ds_address = "127.0.0.1";
	public String bs_address = "127.0.0.1";
	public Thread runningThread = null;
	public ServerSocket serverSocket = null;
	public boolean backup = false;

	public Directory_server(int port, boolean backup, Properties p) {
		this.backup = backup;
		this.bs_port = Integer.parseInt(p.getProperty("secondary_directory_sever_port"));
		this.ds_port = Integer.parseInt(p.getProperty("directory_sever_port"));
		this.bs_address = p.getProperty("secondary_directory_sever");
		this.ds_address = p.getProperty("primary_directory_sever");
		if (backup) {
			this.port = this.bs_port;
		} else {
			this.port = this.ds_port;
		}
		if (backup) {
			this.address = this.bs_address;
		} else {
			this.address = this.ds_address;
		}
	}

	@Override
	public void run() {
		synchronized(this){
			this.runningThread = Thread.currentThread();
		}
		openServerSocket();


		final Thread ping= new Thread(){
			public void run(){
				while(true) {
					try {
						Thread.sleep(5*1000);
						pingNodes();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};
		ping.start();
		if (backup) {
			final Thread ping_ds= new Thread(){
				public void run(){
					while(true) {
						try {
							Thread.sleep(5*1000);
							pingDs();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			};
			ping_ds.start();
		} else {
			Thread backup_sever_request = new Thread() {
			    public void run() {
			        try {
			            ServerSocket listentoPingSocket = new ServerSocket(6664);
			            while (true) {
			                Socket s = listentoPingSocket.accept();
			                pingFromBs worker = new pingFromBs(s);
			                worker.start();
			            }
			        } catch (IOException e) {
			            e.printStackTrace();
			        }
			    }
			};
			backup_sever_request.start();

		}
		System.out.println("Server is listenning on port: " + port);
		while (true) {
			Socket clientSocket = null;
			try {
				clientSocket = this.serverSocket.accept();
			} catch (IOException e) {
				throw new RuntimeException(
						"Error accepting client connection", e);
			}
			new Thread(
					new ServerClientThread(
							clientSocket, 0, registerNode, registerFile, backup)
					).start();

		}

	}

	/**
	 * Ping the storage nodes to detect failures
	 */
	public void pingDs() {
		System.out.println("Start to ping directory server:");
		try {
			Socket socket = new Socket(ds_address, 6664);
			//Send the message to the server
			OutputStream os = socket.getOutputStream();
			OutputStreamWriter osw = new OutputStreamWriter(os);
			BufferedWriter bw = new BufferedWriter(osw);
			String sendMessage = "ping\n";
			bw.write(sendMessage);
			bw.flush();
			System.out.println("Message sent to the directory server: "+ ds_address);
			socket.setSoTimeout(10 * 1000);
			InputStream is = socket.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			if (br.readLine()==null) {
				System.out.println("DS Server failed detected: " + ds_address);
				backup = false;
			} else {
				System.out.println("DS OK: " + ds_address);
			}
		} catch (IOException e) {
			System.out.println("DS Server failed: " + ds_address);
			backup = false;
		}



	}

	/**
	 * Ping the storage nodes to detect failures
	 */
	public void pingNodes() {
		if (registerNode.size()==0) {
			System.out.println("No nodes registered!");
			return;
		} else {
			System.out.println("Start to ping nodes:");
			for (int i=0;i<registerNode.size();i++) {
				String add=registerNode.get(i);
				String[] addSplit = add.split(":");
				String address = addSplit[0];
				int port = Integer.parseInt(addSplit[1]);
				try {
					Socket socket = new Socket(address, port);
					//Send the message to the server
					OutputStream os = socket.getOutputStream();
					OutputStreamWriter osw = new OutputStreamWriter(os);
					BufferedWriter bw = new BufferedWriter(osw);
					String sendMessage = "ping\n";
					bw.write(sendMessage);
					bw.flush();
					System.out.println("Message sent to the storage node : "+add);
					socket.setSoTimeout(10 * 1000);
					InputStream is = socket.getInputStream();
					InputStreamReader isr = new InputStreamReader(is);
					BufferedReader br = new BufferedReader(isr);
					if (br.readLine()==null) {
						System.out.println("Node failed detected: " + add);
						synchronized (registerNode) {
							registerNode.remove(add);
						}
					} else {
						System.out.println("Node OK: " + add);
					}
				} catch (IOException e) {
					System.out.println("Storage node failed: " + add);
					synchronized (registerNode) {
						registerNode.remove(add);
					}
				}

			}
		}
	}

	private void openServerSocket() {
		try {
			this.serverSocket = new ServerSocket(this.port);
		} catch (IOException e) {
			throw new RuntimeException("Cannot open port 8080", e);
		}
	}

	public static void main(String[] args) {
		Properties prop = new Properties();
		try {
			InputStream in =
					new FileInputStream(new File("resources/DFS.properties"));
			prop.load(in);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Scanner scan = new Scanner(System.in);
		System.out.print("Please enter the port of main Directory Server: ");
		int port = scan.nextInt();
		System.out.print("Is this a back up server?");
		boolean back_up = scan.nextBoolean();
		Directory_server ds = new Directory_server(port, back_up, prop);
		new Thread(ds).start();
	}
	
	public class pingFromBs extends Thread {
		public Socket s;
		
		public pingFromBs( Socket s) {
			this.s = s;
		}
		@Override
		public void run() {
			try {
				DataInputStream inStream = new DataInputStream(s.getInputStream());
				DataOutputStream outStream = new DataOutputStream(s.getOutputStream());
				Scanner scan = new Scanner(inStream);
				String operator = scan.nextLine();
				String serverMessage="";
				if (operator.equals("ping")) {
					serverMessage = "true\n";
				}
				outStream.write(serverMessage.getBytes());
				outStream.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
	}
}

