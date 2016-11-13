import java.net.*;
import java.io.*;
import java.nio.charset.Charset;

public class Main
{  
   public static void main(String[] args) {
       
        try {
            File file = new File("beans.txt");       
            FileReader filereader = new FileReader(file);
            BufferedReader br = new BufferedReader(filereader);
            String line;
            while((line = br.readLine()) != null) {
                String[] words = line.split(" ");
                System.out.println(line);
            }
        }catch(Exception e){
            System.out.println("Exception: " + e);
        }
    }  
}