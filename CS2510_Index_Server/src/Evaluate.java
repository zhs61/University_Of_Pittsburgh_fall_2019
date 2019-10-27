	import java.io.IOException;

public class Evaluate {
    public static void main(String[] args) throws InterruptedException, IOException {
        int filenumber=Integer.parseInt(args[0]);
        int request_num=Integer.parseInt(args[1]);
        int frequency=Integer.parseInt(args[2]);
        int[] ports={6667,6669,6671,6673,6675,6677};
        String[] filenames={"100M.txt","100k.txt","10M.txt","1M.txt","1k.txt","200M.txt","20k.txt","500k.txt", "50m.txt", "5M.txt" ,"5k.txt"};
        boolean[] splt={true,false};
        Peer_server[] peers=new Peer_server[6];
        //for(boolean d:splt) {
            for (int i = 0; i < ports.length; i++) {
                Peer_server server = new Peer_server("localhost", ports[i], true,"file/peer"+(i+1));
                peers[i]=server;
                new Thread(server).start();
                System.out.println("Sever"+(i+1)+" is online");
            }
            Peer_server worker=peers[3];
            worker.get_file("1k.txt");
            double[] res=worker.get_stat();

        //}



    }
}
