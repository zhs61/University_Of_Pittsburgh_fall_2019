import java.io.*;
import java.util.Date;
import java.util.Properties;

public class Peer_client {
    public static void main(String[] args) throws IOException {
        InputStream inputStream = null;
        Properties prop = new Properties();
        try {
            String propFileName = "./resources/config.properties";

            inputStream = new FileInputStream(propFileName);

            if (inputStream != null) {
                prop.load(inputStream);
            } else {
                throw new IOException("property file '" + propFileName + "' not found in the classpath");
            }
            // get the property value and print it out

        } catch (Exception e) {
            System.out.println("Exception: " + e);
            System.exit(1);
        }
        inputStream.close();

        if(args[0].equals("start")) {
            //start peer service
            Peer_server server=new Peer_server(prop);
            server.run();
        }else if(args[0].equals("get")){
            //get file from other peer
            SocketClient sender=new SocketClient(prop.getProperty("localhost"),Integer.parseInt(prop.getProperty("peer_client_port")));
            sender.sendMessage("get\n"+args[1]);
        }else if(args[0].equals("register")){
            //register a file
            SocketClient sender=new SocketClient(prop.getProperty("localhost"),Integer.parseInt(prop.getProperty("peer_client_port")));
            sender.sendMessage("register\n"+args[1]);
        }else {
            //stop service
            SocketClient sender=new SocketClient(prop.getProperty("localhost"),Integer.parseInt(prop.getProperty("peer_client_port")));
            sender.sendMessage("stop");
        }

    }
}
