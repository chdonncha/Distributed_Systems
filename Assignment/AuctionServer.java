import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

public class AuctionServer implements Runnable {

  // Array of clients
  private AuctionServerThread clients[] = new AuctionServerThread[50];
  private ServerSocket server = null;
  private Thread thread = null;
  private int clientCount = 0;

  public String item = "chair";

  public AuctionServer(int port) {
    try {

      System.out.println("Binding to port " + port + ", please wait  ...");
      server = new ServerSocket(port);
      System.out.println("Server started: " + server.getInetAddress());
      start();
    } catch (IOException ioe) {
      System.out.println("Can not bind to port " + port + ": " +
                         ioe.getMessage());
    }
  }

  public void run() {
    while (thread != null) {
      try {

        System.out.println("Waiting for a client ...");
        addThread(server.accept());
        displayItem();
        int pause = (int)(Math.random() * 3000);
        Thread.sleep(pause);

      } catch (IOException ioe) {
        System.out.println("Server accept error: " + ioe);
        stop();
      } catch (InterruptedException e) {
        System.out.println(e);
      }
    }
  }

  public ArrayList<String> readFromFile() {

    ArrayList<String> list = new ArrayList<String>();

    try {
      File file = new File("items.txt");
      FileReader filereader = new FileReader(file);
      BufferedReader br = new BufferedReader(filereader);
      String line;
      while ((line = br.readLine()) != null) {
        String[] words = line.split(" ");
        list.addAll(Arrays.asList(line));
      }
    } catch (Exception e) {
      System.out.println("Exception: " + e);
    }
    return list;
  }

  public void displayItem() {
    readFromFile();
    ArrayList<String> list = new ArrayList<String>();
    list = readFromFile();

    try {
      while (true) {
        // loop for every element in ArrayList
        for (String s : list) {
          // System.out.println(s);
          // wait for 60 seconds
          Thread.sleep(60 * 1000);
          // broad current item for auction to all users
          broadcast("item for auction: " + s);
        }
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  public void start() {
    if (thread == null) {
      thread = new Thread(this);
      thread.start();
    }
  }

  public void stop() { thread = null; }

  private int findClient(int ID) {
    for (int i = 0; i < clientCount; i++)
      if (clients[i].getID() == ID)
        return i;
    return -1;
  }

  // take the user input and send to all users
  public synchronized void broadcast(String input) {

    // printout input sent from client
    // System.out.println("C: " + input);

    //  if (input.equals(".bye")){
    //   clients[findClient(ID)].send(".bye");
    //       remove(ID);
    //    }
    //    else

    // known error will only execute once, for only one user
    String[] cmds = {"-- ShowItems", "-- ShowCurrentItem"};
    String[] cmdDesc = {"Shows all items on auction",
                        "Shows current item for auction"};
    String bid = "/bid";

    if (input.equals("-- Help")) {
      for (int i = 0; i < clientCount; i++) {
        for (int j = 0; j < cmds.length; j++) {
          clients[i].send(cmds[j] + "\t\t" + cmdDesc[j]);
        }
      }
    }

    if (input.equals("-- ShowItems")) {
      ArrayList<String> list = new ArrayList<String>();
      list = readFromFile();
      for (int i = 0; i < clientCount; i++) {
        clients[i].send("\n All the items up for auction today: \n");
        for (String s : list) {
          clients[i].send(s);
        }
      }
    }

    String subBid = input.substring(0, 4);

    if (subBid.equals(bid)) {
      for (int i = 0; i < clientCount; i++) {
        // String amount = bid.substring(5, input.length());
        // System.out.println(amount);
        clients[i].send("user has bid 1");
      }
    }

    notifyAll();
  }
  public synchronized void remove(int ID) {
    int pos = findClient(ID);
    if (pos >= 0) {
      AuctionServerThread toTerminate = clients[pos];
      System.out.println("Removing client thread " + ID + " at " + pos);

      if (pos < clientCount - 1)
        for (int i = pos + 1; i < clientCount; i++)
          clients[i - 1] = clients[i];
      clientCount--;

      try {
        toTerminate.close();
      } catch (IOException ioe) {
        System.out.println("Error closing thread: " + ioe);
      }
      toTerminate = null;
      System.out.println("Client " + pos + " removed");
      notifyAll();
    }
  }

  private void addThread(Socket socket) {
    if (clientCount < clients.length) {

      System.out.println("Client accepted: " + socket);
      // clients[clientCount] = new AuctionServerThread(this, socket);
      AuctionServerThread client = new AuctionServerThread(this, socket);
      try {
        client.open();
        broadcast("item for auction: chair");
        client.start();
        clients[clientCount] = client;
        clientCount++;
      } catch (IOException ioe) {
        System.out.println("Error opening thread: " + ioe);
      }
    } else
      System.out.println("Client refused: maximum " + clients.length +
                         " reached.");
  }

  public static void main(String args[]) {
    AuctionServer server = null;
    if (args.length != 1)
      System.out.println("Usage: java AuctionServer port");
    else
      server = new AuctionServer(Integer.parseInt(args[0]));
  }
}