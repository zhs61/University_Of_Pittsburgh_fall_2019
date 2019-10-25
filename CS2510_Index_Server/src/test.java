
public class test {
	
	public static void main(String[] args) {
		SocketClient sc = new SocketClient("localhost", 6666);
		String line = sc.sendMessage("start");
//		String line = sc.sendMessage("register\ngg\n1");
// 		String line = sc.sendMessage("search\ngg");
//		String line = sc.sendMessage("search\nggg");
		//String line = sc.sendMessage("register\nggg\n2");
		System.out.println(line);
		
	}
}

//InputStreamReader isr = new InputStreamReader(clientSocket.getInputStream());
//BufferedReader reader = new BufferedReader(isr);
//operator = reader.readLine();
//if (operator.equals("start")) {
//	int peerId = start(clientSocket.getInetAddress());
//	if (peerId >= 1) {
//		PrintWriter out = new PrintWriter(clientSocket.getOutputStream());
//        out.println("true");
//        out.println(peerId +"");
//        out.flush();
//	} else {
//		PrintWriter out = new PrintWriter(clientSocket.getOutputStream());
//        out.println("false");
//        out.println("-1");
//        out.flush();
//	}
//} else if (operator.equals("register")) {
//	content = reader.readLine();
//	String peerId = reader.readLine();
//	if (register(content, peerId) == 1) {
//		PrintWriter out = new PrintWriter(clientSocket.getOutputStream());
//        out.println("true");
//        out.flush();
//	} else {
//		PrintWriter out = new PrintWriter(clientSocket.getOutputStream());
//        out.println("false");
//        out.flush();
//	}
//} else if (operator.equals("search")) {
//	content = reader.readLine();
//	String result = search(content);
//	PrintWriter out = new PrintWriter(clientSocket.getOutputStream());
//    out.println(result);
//    out.flush();
//}