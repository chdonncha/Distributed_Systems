import java.net.*;
import java.io.*;

public class AuctionServer implements Runnable
{

   // Array of clients
   private AuctionServerThread clients[] = new AuctionServerThread[50];
   private ServerSocket server = null;
   private Thread       thread = null;
   private int clientCount = 0;

   public String item = "chair";

   public AuctionServer(int port)
   {
	  try {

		 System.out.println("Binding to port " + port + ", please wait  ...");
       server = new ServerSocket(port);
         System.out.println("Server started: " + server.getInetAddress());
         start();
      }
      catch(IOException ioe)
      {
		  System.out.println("Can not bind to port " + port + ": " + ioe.getMessage());
      }
   }

   public void run()
   {
	  while (thread != null)
      {
		 try{

			System.out.println("Waiting for a client ...");
            addThread(server.accept());
            displayItem();
			int pause = (int)(Math.random()*3000);
			Thread.sleep(pause);

         }
         catch(IOException ioe){
			System.out.println("Server accept error: " + ioe);
			stop();
         }
         catch (InterruptedException e){
		 	System.out.println(e);
		 }
    }
   }

  public void readFromFile() {
        try {
            File file = new File("items.txt");       
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

  public void displayItem() {
    readFromFile();

    try {
      while(true) {
        broadcast("item for auction: chair");
        Thread.sleep(5 * 1000);
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  public void start()
    {
		if (thread == null) {
		  thread = new Thread(this);
          thread.start();
       }
    }

   public void stop(){
	   thread = null;

   }

   private int findClient(int ID)
   {
	   for (int i = 0; i < clientCount; i++)
         if (clients[i].getID() == ID)
            return i;
      return -1;
   }

   // take the user input and send to all users
   public synchronized void broadcast(String input)
   {

     // printout input sent from client
     //System.out.println("C: " + input);

	  //  if (input.equals(".bye")){
		//   clients[findClient(ID)].send(".bye");
    //       remove(ID);
    //    }
    //    else
         for (int i = 0; i < clientCount; i++){
      // broadcast to everyone but the user it was sent from
			//if(clients[i].getID() != ID)
            	clients[i].send(input); // sends messages to clients
		}
       notifyAll();
   }
   public synchronized void remove(int ID)
   {
	  int pos = findClient(ID);
      if (pos >= 0){
		 AuctionServerThread toTerminate = clients[pos];
         System.out.println("Removing client thread " + ID + " at " + pos);

         if (pos < clientCount-1)
            for (int i = pos+1; i < clientCount; i++)
               clients[i-1] = clients[i];
         clientCount--;

         try{
			 toTerminate.close();
	     }
         catch(IOException ioe)
         {
			 System.out.println("Error closing thread: " + ioe);
		 }
		 toTerminate = null;
		 System.out.println("Client " + pos + " removed");
		 notifyAll();
      }
   }

   private void addThread(Socket socket)
   {
	  if (clientCount < clients.length){

		 System.out.println("Client accepted: " + socket);
         //clients[clientCount] = new AuctionServerThread(this, socket);
         AuctionServerThread client = new AuctionServerThread(this, socket);
         try{
			client.open();
      broadcast("item for auction: chair");
            client.start();
            clients[clientCount] = client;
            clientCount++;
         }
         catch(IOException ioe){
			 System.out.println("Error opening thread: " + ioe);
		  }
	  }
      else
         System.out.println("Client refused: maximum " + clients.length + " reached.");
   }


   public static void main(String args[]) {
	   AuctionServer server = null;
      if (args.length != 1)
         System.out.println("Usage: java AuctionServer port");
      else
         server = new AuctionServer(Integer.parseInt(args[0]));
   }

}