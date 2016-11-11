import java.net.*;
import java.io.*;

public class SocServer 
{
    public static void main(String[] args) throws Exception
    {
        System.out.println("Server is started");
        ServerSocket ss = new ServerSocket(9999);

        System.out.println("S: Server is waiting for client request");
        Socket s = ss.accept();

        System.out.println("S: Client Accepted");

        BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
        String str = br.readLine();

        System.out.println("S: Client Data : " + str);

        String nickName = str.substring(0, 3);

        OutputStreamWriter os = new OutputStreamWriter(s.getOutputStream());
        PrintWriter out = new PrintWriter(os);
        out.println(nickName);
        out.flush();
        System.out.println("S: Data sent from Server to Client");
    }    
}