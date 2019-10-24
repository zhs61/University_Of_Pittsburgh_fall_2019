
public class test {
	
	public static void main(String[] args) {
		SocketClient sc = new SocketClient("localhost", 6666);
		String line = sc.sendMessage("start");
//		String line = sc.sendMessage("register\nggg\n1");
// 		String line = sc.sendMessage("search\nggg");
//		String line = sc.sendMessage("search\nggg");
		//String line = sc.sendMessage("register\nggg\n2");
		System.out.println(line);
		
	}
}
