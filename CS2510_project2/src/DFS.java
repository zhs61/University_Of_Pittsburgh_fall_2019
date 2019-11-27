import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DFS {
    public static void main(String[] args){
        Properties prop = new Properties();
        try {
            InputStream in =
                    new FileInputStream(new File("resources/DFS.properties"));
            prop.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String op=args[0];
        if(op.equals("start")){
            StorageNode s=new StorageNode(prop);
            s.start();
        }
    }
}
