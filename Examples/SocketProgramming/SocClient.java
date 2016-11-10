import java.new.Socket;

public class SocClient {

    public static void main(String[] args) {
        String ip = "localhost";
        init port = 9999; // 0-1023 to 65535
        Socket s = new Socket(ip,port);

        String str = "Donncha Cassidy";

        OutputStreamWriter os = new OutputStreamWriter(s.getOutputStream());
        PrintWriter out = new PrintWriter(os);
         

    }
}