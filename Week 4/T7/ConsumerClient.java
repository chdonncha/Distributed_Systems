import java.io.*;
import java.net.*;
import java.util.*;

public class ConsumerClient
{
	private static InetAddress host;
	private static final int PORT = 1234;

	public static void main(String[] args)
	{
		try
		{
			host = InetAddress.getLocalHost();
		}
		catch(UnknownHostException uhEx)
		{
			System.out.println("\nHost ID not found!\n");
			System.exit(1);
		}
		sendMessages();
	}

	private static void sendMessages()
	{
		Socket socket = null;

		try
		{
			socket = new Socket(host,PORT);

			Scanner networkInput =
						new Scanner(socket.getInputStream());
			PrintWriter networkOutput =
					new PrintWriter(
							socket.getOutputStream(),true);

			//Set up stream for keyboard entry...
			Scanner userEntry = new Scanner(System.in);

			String response;
			char message;
			do
			{
				System.out.print(
							"Enter message ('QUIT' to exit): ");
				message =  userEntry.nextLine().charAt(0);

				if(message != '0' && message != '1')
				{
					System.out.println(message);
					System.out.println("\nPlease choose either 0 or 1");
					break;
				}
				else if(message != '0')
				{
					networkOutput.println(message);
					response = networkInput.nextLine();
					System.out.println("\nSERVER> " + response);
				}

			}while (message != ('0'));
		}
		catch(IOException ioEx)
		{
			ioEx.printStackTrace();
		}

		finally
		{
			try
			{
				System.out.println("\nClosing connection...");
				socket.close();
			}
			catch(IOException ioEx)
			{
				System.out.println("Unable to disconnect!");
				System.exit(1);
			}
		}
	}
}
