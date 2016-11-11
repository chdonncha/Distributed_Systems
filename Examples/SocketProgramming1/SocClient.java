import java.net.*;
import java.io.*;

public class SocClient 
{

    public static void main(String[] args) throws Exception
    {
        String ip = "localhost";
        int port = 9999; // 0-1023 to 65535
        Socket s = new Socket(ip,port);

        String str = "Donncha Cassidy";

        OutputStreamWriter os = new OutputStreamWriter(s.getOutputStream());
        PrintWriter out = new PrintWriter(os);
        out.println(str);
        os.flush();

        BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
        String nickName = br.readLine();

        System.out.println("C: Data from the Server: " + nickName);

    }
}