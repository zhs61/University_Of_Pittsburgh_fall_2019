import java.io.File;
import java.io.IOException;
import java.util.Random;

public class Single_clint_evalution {
	public static void main(String[] args) throws InterruptedException, IOException {
		int filenumber=Integer.parseInt(args[0]);
		int request_num=Integer.parseInt(args[1]);
		int frequency=Integer.parseInt(args[2]);
		String[] filenames={"100M.txt","100k.txt","10M.txt","1M.txt","1k.txt","200M.txt","20k.txt","500k.txt", "50m.txt", "5M.txt" ,"5k.txt"};
		boolean[] splt={true,false};
		Peer_server peer1=null;
		Peer_server peer2=null;
		//for(boolean d:splt) {
		peer1 = new Peer_server("localhost",6667, false,"file/peer1");
		peer2 = new Peer_server("localhost", 6669, false,"file/peer6");
		new Thread(peer1).start();
		new Thread(peer2).start();
		int calls=0;
		while (calls<request_num) {
			for (int i = 0; i < filenames.length; i++) {
				peer2.get_file(filenames[i]);
			}
			calls++;
		}
		System.out.println(peer2.get_stat());
//		new Thread(peer2).start();
//		int calls=0;
//		while (calls<request_num){
//			int file_choose= new Random().nextInt(filenumber);
//			peer2.get_file(filenames[file_choose]);
//			calls++;
//			File temp=new File("file/peer6/"+filenames[file_choose]);
//			Thread.sleep((int)1.0/frequency);
//		}
//		for(double x:peer2.get_stat()){
//			System.out.print(x+" ");
//		}

		//}
	}
}
