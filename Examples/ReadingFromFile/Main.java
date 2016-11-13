import java.net.*;
import java.io.*;
import java.nio.charset.Charset;

public class Main
{  
   public static void main(String[] args) {
         String line;

        try (
            InputStream fis = new FileInputStream("beans.txt");
            InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
            BufferedReader br = new BufferedReader(isr);
            //System.out.println(line);
        ).catch (IOException e) {
            System.err.println("error: " + e.getMessage());
        }
            while ((line = br.readLine()) !=null) {
                String[] words = line.split(" ");        
        }
    }  
}