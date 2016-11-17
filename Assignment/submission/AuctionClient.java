/*
    References:
    * The AuctionClient was started using the basis of the Chatserver lab code
*/

import java.net.*;
import java.io.*;

public class AuctionClient implements Runnable {

    private Socket socket = null;
    private Thread thread = null;
    private BufferedReader console = null;
    private DataOutputStream streamOut = null;
    private AuctionClientThread client = null;
    private DataInputStream streamIn = null;
    private String chatName;

    public AuctionClient(String serverName, int serverPort, String name) {

        System.out.println("Establishing connection. Please wait ...");

        this.chatName = name;
        try {
            socket = new Socket(serverName, serverPort);
            System.out.println("Connected: " + socket);
            start();
            userConnectedToServer();

        } catch (UnknownHostException uhe) {
            System.out.println("Host unknown: " + uhe.getMessage());
        } catch (IOException ioe) {
            System.out.println("Unexpected exception: " + ioe.getMessage());
        }
    }

    public void run() {
        while (thread != null) {
            try {
                String message = console.readLine();
                processUserInput(message);

            } catch (IOException ioe) {
                System.out.println("Sending error: " + ioe.getMessage());
                stop();
            }
        }
    }

    private void processUserInput(String commandInput) {    
        // take users input and compare against available commmands
        // added as part of the required extra functionality
        String[] userCommand = commandInput.split(" ");
            switch(userCommand[0]){
                // displays current item on auction
                case "/displayCurrent":
                    requestCurrentItem();
                    break;
                // displays list of all auction items
                case "/displayAll":
                    requestAllItems();
                    break;
                // displays a list of all the available commands
                case "/help":
                    displayHelp();
                    break;
                // bid <amount> allows user to place their bid
                case "/bid":
                    if(userCommand.length != 2){
                        System.out.println("Bid takes 2 arguments");
                    }
                    String bidAmount = userCommand[1];
                    userBidOnItem(bidAmount);
                    break;
                // if the input does not match any of the above commands let the user know it's invalid
                default:
                    invalidCommand(userCommand[0]);
                    break;
            }
    }
    
    private void invalidCommand(String command){
        System.out.println("Invalid Command: " + command);
    }
    
    // displays the current item on auction
    private void requestCurrentItem(){
        try{
            streamOut.writeInt(ServerCommands.REQUEST_CURRENT_AUCTION);
            streamOut.flush();
        }catch(IOException ex){
            System.out.println("Could not request current item");
        }
    }
    
    // displays a list of all the items that will be on auction
    private void requestAllItems(){
        try{
            streamOut.writeInt(ServerCommands.REQUEST_ALL_AUCTIONS);
            streamOut.flush();
        }catch(IOException ex){
            System.out.println("Could not request all items");
        }
    }
    
    // parses the amount bid on an item from a string to an int
    private void userBidOnItem(String amount){
        try{
            int amountInt = Integer.parseInt(amount);
            userBidOnItem(amountInt);
        // if the amount isn't an integer than the exception will be caught
        } catch (Exception ex) {
            System.out.println("invalid command - please enter bid amount");
        }
    }
    
    // handles a user bidding on an item
    private void userBidOnItem(int amount){
        try {
            streamOut.writeInt(ServerCommands.USER_BID);
            streamOut.writeUTF(chatName);
            streamOut.writeInt(amount);
                        streamOut.flush();
        } catch (IOException ex) {
            System.out.println("Could not send user bid command");
        }
    }

    // handle a user connection to server
    private void userConnectedToServer(){
        try {
            streamOut.writeInt(ServerCommands.USER_CONNECTED);
            streamOut.writeUTF(chatName);
            streamOut.flush();
        } catch (IOException ex) {
            System.out.println("Could not send userconnected command");
        }
    }

    private void displayHelp() {
        String[] cmds = {"/bid <amount>", "/displayAll", "/displayCurrent"};
        String[] cmdDesc = {"Place a bid on the current item for auction", 
                            "Shows all items on auction",
                            "Shows current item for auction" + "\n"};
        for(int i = 0; i < cmds.length; i++){
            System.out.println(cmds[i] + "\t\t" + cmdDesc[i]);
        }
    }

    public void handle(String msg) {
        if (msg.equals(".bye")) {
            System.out.println("Good bye. Press RETURN to exit ...");
            stop();
        } else {
            System.out.println(msg);
        }
    }

    public void start() throws IOException {
        console = new BufferedReader(new InputStreamReader(System.in));

        // must be kept on or the client is dropped after one message
        streamOut = new DataOutputStream(socket.getOutputStream());
        if (thread == null) {
            client = new AuctionClientThread(this, socket);
            thread = new Thread(this);
            thread.start();
        }
    }
    
    public void stop() {
        try {
            if (console != null) {
                console.close();
            }
            if (streamOut != null) {
                streamOut.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException ioe) {
            System.out.println("Error closing ...");

        }
        client.close();
        thread = null;
    }

    public static void main(String args[]) {
        AuctionClient client = null;
        if (args.length != 3) {
            System.out.println("Usage: java AuctionClient host port name");
        } else {
            client = new AuctionClient(args[0], Integer.parseInt(args[1]), args[2]);
        }
    }
}
