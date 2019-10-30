import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class Evaluate {
	public static void main(String[] args) throws InterruptedException, IOException {
		int[] ports={6667,6669,6671,6673,6675,6677};
		int filenumber=Integer.parseInt(args[0]);
		int request_num=Integer.parseInt(args[1]);
		int frequency=Integer.parseInt(args[2]);
		String[] filenames={"100M.txt","100k.txt","10M.txt","1M.txt","1k.txt","200M.txt","20k.txt","500k.txt", "50m.txt", "5M.txt" ,"5k.txt"};
		boolean[] splt={true,false};
		Peer_server[] peers=new Peer_server[6];
		for (int i = 0; i < ports.length; i++) {
			Peer_server server = new Peer_server("localhost", ports[i], true,"file/peer"+(i+1));
			peers[i]=server;
			new Thread(server).start();
			System.out.println("Sever"+(i+1)+" is online");
		}
		ArrayList<Peer_server> peersList = new ArrayList<>();
		ArrayList<double[]> result = new ArrayList<>();
		for (int i = 2; i <= peers.length; i++ ) {
			for (int j = 0; j < i; j++) {
				Peer_server peer1 = peers[j];
				peersList.add(peer1);
			}
			result = testMultiThread(peersList, filenames, request_num, frequency);
			System.out.println(peersList.size() + " Clients: ");
			for (double[] s : result) {
				for (double ele : s) {
					System.out.print(ele + " ");
				}
				System.out.println();
			}
			System.out.println();
			peersList = new ArrayList<>();
		}
		System.exit(0);
	}

	/**
	 * use the method to test the concurrent send request to indexserver
	 * @param peersList
	 * @throws InterruptedException 
	 * @throws IOException 
	 */
	private static ArrayList<double[]> testMultiThread(ArrayList<Peer_server> peersList, String[] filenames, int request_num, int frequency) throws IOException, InterruptedException {
		
		Random r=new Random();
		ArrayList<double[]> result = new ArrayList<>();
		int randomNumber=r.nextInt(filenames.length);
		String filename = filenames[randomNumber];
		for (int i = 0; i < peersList.size(); i++) {
			Peer_server peer = peersList.get(i);
			peer.message = 0;	
			for (int j = 0; j < request_num; j++) {
				boolean existFile = peer.get_file(filenames[5]);
				File file = new File("file/peer" + (i+1) + "/" + filenames[5]);
				if (existFile) {
					file.delete();
				}
			}
			// File file = new File("file/peer" + (i+1) + "/" + filenames[0]);
			double[] res = peer.get_stat();
			result.add(res);
		}
		return result;
	}
}
