/*
    References:
    * The AuctionServer was started using the basis of the Chatserver lab code
*/

import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

public class AuctionServer implements Runnable {

    // Array of clients
    private AuctionServerThread clients[] = new AuctionServerThread[50];
    private ServerSocket server = null;
    private Thread thread = null;
    private int clientCount = 0;
    private AuctionItem _currentAuctionItem;

    private Queue<AuctionItem> _auctionItems;

    public AuctionServer(int port) {
        _auctionItems = new LinkedList<AuctionItem>();
        addAuctionItemsFromFile();
        startServer(port);
        startRotatingItems();
    }

    public AuctionItem getCurrentAuctionItem() {
        return _currentAuctionItem;
    }

    private void setCurrentAuctionItem(AuctionItem item) {
        _currentAuctionItem = item;
        broadcast("Item " + item.getItemName() + " is now up for auction");
    }

    private void startServer(int port) {
        try {
            System.out.println("Binding to port " + port + ", please wait  ...");
            server = new ServerSocket(port);
            System.out.println("Server started: " + server.getInetAddress());
            start();
        } catch (IOException ioe) {
            System.out.println("Can not bind to port " + port + ": "
                    + ioe.getMessage());
        }
    }

    private void startRotatingItems() {
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    while (true) {
                        // loop for every element in ArrayList
                        if (_auctionItems.peek() != null) {
                            setCurrentAuctionItem(_auctionItems.remove());
                            Thread.sleep(30 * 1000);
                            broadcast("There is now 30 seconds left to bid");
                            Thread.sleep(30 * 1000);
                            // Auction failed to bid re-add it to the list.
                            if (getCurrentAuctionItem().getHighBidderName() == null) {
                                _auctionItems.add(getCurrentAuctionItem());
                                broadcast("Item " + getCurrentAuctionItem().getItemName() + " did not sell and will go up for sale later");
                            } else {
                                notifyUser("Congratulations you won the item " + getCurrentAuctionItem().getItemName(), getCurrentAuctionItem().getHighBidderID());
                                broadcast("Item " + getCurrentAuctionItem().getItemName() + " has sold for " + getCurrentAuctionItem().getCurrentBid() + " to " + getCurrentAuctionItem().getHighBidderName());
                            }

                        } else {
                            // Let the thread sleep for a bit before checking if a new item has been added
                            Thread.sleep(10);
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        t.start();
    }

    public void run() {
        while (thread != null) {
            try {
                System.out.println("Waiting for a client ...");
                addThread(server.accept());
                int pause = (int) (Math.random() * 3000);
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

    private void addAuctionItemsFromFile() {
        ArrayList<String> list = readFromFile();
        for (String item : list) {
            AuctionItem auctionItem = new AuctionItem(item);
            _auctionItems.add(auctionItem);
        }
    }

    public void start() {
        if (thread == null) {
            thread = new Thread(this);
            thread.start();
        }
    }

    public void stop() {
        thread = null;
    }

    private int findClient(int ID) {
        for (int i = 0; i < clientCount; i++) {
            if (clients[i].getID() == ID) {
                return i;
            }
        }
        return -1;
    }

    // We received input from a user. Break down the stream and determin how we should handle
    // the input.
    public synchronized void processUserInput(int command, DataInputStream stream, int clientID) throws IOException {
        switch (command) {
            case ServerCommands.USER_CONNECTED:
                String userConnectedName = stream.readUTF();
                userConnected(userConnectedName);
                break;
            case ServerCommands.USER_BID:
                String userBidName = stream.readUTF();
                int amount = stream.readInt();
                userBidOnItem(userBidName, amount, clientID);
                break;
            case ServerCommands.REQUEST_CURRENT_AUCTION:
                userRequestCurrentAuctionItem(clientID);
                break;
            case ServerCommands.REQUEST_ALL_AUCTIONS:
                userRequestAllAuctions(clientID);
                break;
        }

        notifyAll();
    }

    private void userRequestAllAuctions(int clientID) {
        notifyUser("The items currently up for auction are", clientID);
        if (getCurrentAuctionItem() != null) {
            notifyUser(getCurrentAuctionItem().getItemName(), clientID);
        }
        for (AuctionItem item : _auctionItems) {
            notifyUser(item.getItemName(), clientID);
        }
    }

    private void userRequestCurrentAuctionItem(int clientID) {
        String message = "";
        if (getCurrentAuctionItem() == null) {
            message = "There are currently no auction items";
        } else {
            message = "The item currently up for auction is " + getCurrentAuctionItem().getItemName() + " and has a current bid of " + getCurrentAuctionItem().getCurrentBid();
        }
        notifyUser(message, clientID);
    }

    private void userBidOnItem(String username, int amount, int clientID) {
        AuctionItem currentItem = getCurrentAuctionItem();

        if (currentItem == null) {
            notifyUser("No item available to bid on", clientID);
            return;
        }

        if (currentItem.bidOnItem(username, clientID, amount)) {
            notifyUser("you bid on item", clientID);
            broadcast(username + " is the new high bidder for amount " + amount);
        } else {
            notifyUser("your bid was not accepted.", clientID);
        }
    }

    // User connected event. Inform all connected users that a new user has connected.
    private void userConnected(String username) {
        broadcast("User connected to server: " + username);
    }

    // Sends a message to a specific user rather than broadcasting.
    private void notifyUser(String message, int userID) {
        for (int i = 0; i < clientCount; i++) {
            if (clients[i].getID() == userID) {
                clients[i].send(message);
                break;
            }
        }
    }

    private void broadcast(String message) {
        for (int i = 0; i < clientCount; i++) {
            clients[i].send(message);
        }
    }

    public synchronized void remove(int ID) {
        int pos = findClient(ID);
        if (pos >= 0) {
            AuctionServerThread toTerminate = clients[pos];
            System.out.println("Removing client thread " + ID + " at " + pos);

            if (pos < clientCount - 1) {
                for (int i = pos + 1; i < clientCount; i++) {
                    clients[i - 1] = clients[i];
                }
            }
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
        } else {
            System.out.println("Client refused: maximum " + clients.length
                    + " reached.");
        }
    }

    public static void main(String args[]) {
        AuctionServer server = null;
        if (args.length != 1) {
            System.out.println("Usage: java AuctionServer port");
        } else {
            server = new AuctionServer(Integer.parseInt(args[0]));
        }
    }
}
