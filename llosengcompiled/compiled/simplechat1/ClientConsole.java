// This file contains material supporting section 3.7 of the textbook:
// "Object Oriented Software Engineering" and is issued under the open-source
// license found at www.lloseng.com 

import java.io.*;
import client.*;
import common.*;

/**
 * This class constructs the UI for a chat client.  It implements the
 * chat interface in order to activate the display() method.
 * Warning: Some of the code here is cloned in ServerConsole 
 *
 * @author Fran&ccedil;ois B&eacute;langer
 * @author Dr Timothy C. Lethbridge  
 * @author Dr Robert Lagani&egrave;re
 * @version July 2000
 */
public class ClientConsole implements ChatIF
{
  //Class variables *************************************************
  /**
   * The default port to connect on & comments.
   */

  final public static int DEFAULT_PORT = 5555;
    public static final String ALREADY_LOGGED_IN = "Error, user is already logged in.";
    public static final String USER_COMMANDS = "Command list: \n#quit\n#logoff\n#sethost <host>\n#setport <port>\n#login\n#gethost\n#getport";
    public static final String QUIT = "User selected quit - shutting down client.";
    public static final String LOGOFF = "User selected logoff - disconnecting client from server.";
    public static final String LOGIN = "User selected login - connecting client to server.";


    //Instance variables **********************************************

  /**
   * The instance of the client that created this ConsoleChat.
   */
  ChatClient client;


  //Constructors ****************************************************

  /**
   * Constructs an instance of the ClientConsole UI.
   *
   * @param host The host to connect to.
   * @param port The port to connect on.
   */
  public ClientConsole(String host, int port)
  {
    try
    {
      client= new ChatClient(host, port, this);
    }
    catch(IOException exception)
    {
      System.out.println("Error: Can't setup connection!" + " Terminating client.");
      System.exit(1);
    }
  }

  //Instance methods ************************************************

  /**
   * This method waits for input from the console.  Once it is
   * received, it sends it to the client's message handler.
   */
  public void accept()
  {
    try
    {
      BufferedReader fromConsole =
        new BufferedReader(new InputStreamReader(System.in));
      String message;

      while (true)
      {
        message = fromConsole.readLine();
          /**
           *3. Currently, the client simply sends to the server everything the end-user types. When the server receives these messages it simply
           * echoes them to all clients. Add a mechanism so that the user of the client can type commands that perform special functions.
           */
          String command = "";
          if (message.startsWith("#")) {
              command = message;
              specialFunctions(command);
          }
          else {
              client.handleMessageFromClientUI(message);
          }
      }
    }
    catch (Exception ex)
    {
      System.out.println
        ("Unexpected error while reading from console!"); //
    }
  }

  /**
   * This method overrides the method in the ChatIF interface.  It
   * displays a message onto the screen.
   *
   * @param message The string to be displayed.
   */
  public void display(String message)
  {
    System.out.println("> " + message); //this is what the client console returns from ChatClient.java (ChatIF implementation)
  }

    /**
     * Each command should start with the ’#’ symbol - in fact, anything that starts with that symbol should be considered a command.
     * You should implement the following commands:
     * (a) #quit cause the client to terminate gracefully. Make sure the connection to the server is terminated before exiting the program.
     * (b) #logoff causes the client to disconnect from the server, but not quit.
     * (c) #sethost <host> calls the setHost method in the client. Only allowed if the client is logged off; displays an error message otherwise.
     * (d) #setport <prot> calls the setPort method in the client, with the same constraints as #sethost.
     * (e) #login causes the client to connect to the server. Only allowed if the client is not already connected; display an error message otherwise.
     * (f) #gethost displays the current host name.
     * (g) #getport displays the current port number.
     */

    public void specialFunctions(String message) throws IOException {
        if(message.startsWith("#sethost ")){
            if (client.isConnected()) {
                display("ERROR. Host can only be set when logged off.");
                return;
            }
            String newHost = message.substring(9).trim();
            client.setHost(newHost);
            display("Host set to: " + client.getHost());
        }
        if(message.startsWith("#setport ")){
            if (client.isConnected()) {
                display("ERROR. Port can only be set when logged off.");
                return;
            }
            String newPort = message.substring(9).trim();
            try {
                Integer.parseInt(newPort);
                client.setPort(Integer.parseInt(newPort));
                display("Port set to: " + client.getPort());
            } catch (NumberFormatException e) {
                display("ERROR. Port must be a number.");
            }
        }
        switch (message) {
            case "#" -> {
                display(USER_COMMANDS);
            }
            case "#quit" -> {
                display(QUIT);
                client.quit();
            }
            case "#logoff" -> {
                display(LOGOFF);
                client.closeConnection();
            }
            case "#login" -> {
                if(client.isConnected()){
                    display(ALREADY_LOGGED_IN);
                    return;
                }
                //this opens a connection even if the server is not listening (server #stop command) causing a "bug" where the message keeps stacking
                //if (server #start), all stacked messages pass at once
                try{
                    client.openConnection();
                    display(LOGIN);
                }
                catch (Exception ex)
                {
                    System.out.println("ERROR - Server down. Cannot log in.");
                }
            }
            case "#gethost" -> {
                display(client.getHost());
            }
            case "#getport" -> {
                display(String.valueOf(client.getPort()));
            }
        }
    }

  //Class methods ***************************************************

  /**
   * This method is responsible for the creation of the Client UI.
   *
   * @param args[0] The host to connect to.
   */
  public static void main(String[] args)
  {
    String host = "";
    int port = 0;  //The port number

    try
    {
        /**
         * 2. The client currently always uses a default port. Modify the client so that
         * it obtains the port number from the command line. (look at the way it
         * obtains the host name from the command line).
         * METHOD:
         * Step 1: Change the EchoServer DEFAULT_PORT to something other than 5555, for example: 888
         * Step 2: Click vertical ellipse.
         * Step 3: Select "Run with Parameters..."
         * Step 4: In the "Program arguments" field, write "localhost 888"; to change the ClientConsole port from default
         */
        host = args[0];
        port = Integer.parseInt(args[1]); //Get port from command line
    }
    catch(ArrayIndexOutOfBoundsException e)
    {
        host = "localhost";
        port = DEFAULT_PORT; //Set port to 5555
    }
    ClientConsole chat= new ClientConsole(host, port);
    chat.accept();  //Wait for console data
  }
}
//End of ConsoleChat class
