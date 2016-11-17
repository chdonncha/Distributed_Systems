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
        String[] userCommand = commandInput.split(" ");
            switch(userCommand[0]){
                case "/displayCurrent":
                    requestCurrentItem();
                    break;
                case "/displayAll":
                    requestAllItems();
                    break;
                case "/help":
                    displayHelp();
                    break;
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
    
    private void requestCurrentItem(){
        try{
            streamOut.writeInt(ServerCommands.REQUEST_CURRENT_AUCTION);
        }catch(IOException ex){
            System.out.println("Could not request current item");
        }
    }
    
    private void requestAllItems(){
        try{
            streamOut.writeInt(ServerCommands.REQUEST_ALL_AUCTIONS);
        }catch(IOException ex){
            System.out.println("Could not request all items");
        }
    }
    
    private void userBidOnItem(String amount){
        try{
            int amountInt = Integer.parseInt(amount);
            userBidOnItem(amountInt);
        } catch (Exception e) {
            System.out.println("invalid command - please enter bid amount");
        }
    }
    
    private void userBidOnItem(int amount){
        try {
            streamOut.writeInt(ServerCommands.USER_BID);
            streamOut.writeUTF(chatName);
            streamOut.writeInt(amount);
        } catch (IOException ex) {
            System.out.println("Could not send user bid command");
        }
    }
    
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
                            "Shows current item for auction"};
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
