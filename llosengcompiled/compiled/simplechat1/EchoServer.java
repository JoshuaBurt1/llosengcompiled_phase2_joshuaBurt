// This file contains material supporting section 3.7 of the textbook:
// "Object Oriented Software Engineering" and is issued under the open-source
// license found at www.lloseng.com

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import common.ChatIF;
import ocsf.server.*;

/**
 * This class overrides some of the methods in the abstract
 * superclass in order to give more functionality to the server.
 *
 * @author Dr Timothy C. Lethbridge
 * @author Dr Robert Lagani&egrave;re
 * @author Fran&ccedil;ois B&eacute;langer
 * @author Paul Holden
 * @version July 2000
 */
public class EchoServer extends AbstractServer
{
    /**
     * Needed to determine #logoff clients
     */
    private String[] joinedConnections = new String[0];

    //Class variables *************************************************

  /**
   * The default port to listen on.
   */
  final public static int DEFAULT_PORT = 5555;


  //Constructors ****************************************************

  /**
   * Constructs an instance of the echo server.
   *
   * @param port The port number to connect on.
   */
  public EchoServer(int port)
  {
    super(port);
  }


  //Instance methods ************************************************

    /**
     * 1. Currently, the server ignores situations where clients connect or disconnect. Modify the server so that it prints out a nice message
     * whenever a client connects or disconnects. (write code in EchoServer that overrides certain methods found in AbstractServer).
     *
     * overrides protected void clientConnected(ConnectionToClient client) {}
     * overrides synchronized protected void clientDisconnected(ConnectionToClient client) {}
     */

    synchronized protected void clientConnected(ConnectionToClient client)
    {
        System.out.println("Welcome / Bienvenue / 欢迎: " + client);
        // updates joinedConnections array
        joinedConnections = new String[]{Arrays.toString(getClientConnections())};
        //System.out.println("Current connections: " + Arrays.toString(joinedConnections));
    }

    public void disconnectedString() {
        // compares currentConnections array to joinedConnections array, difference is the disconnected client
        String[] currentConnections = new String[]{Arrays.toString(getClientConnections())};
        //System.out.println("Previous connections: " + Arrays.toString(joinedConnections));
        //System.out.println("Current connections: " + Arrays.toString(currentConnections));
        Set<String> oldSet = new HashSet<>(Arrays.asList(joinedConnections));
        Set<String> newSet = new HashSet<>(Arrays.asList(currentConnections));
        oldSet.removeAll(newSet);
        for (String disconnected : oldSet) {
            System.out.println("Goodbye / Au revoir / 再见: " + disconnected);
        }
    }

    synchronized protected void clientDisconnected(ConnectionToClient client) {
        disconnectedString();
    }

    synchronized protected void clientException(ConnectionToClient client, Throwable exception) {
        disconnectedString();
    }

  /**
   * This method handles any messages received from the client.
   *
   * @param msg The message received from the client.
   * @param client The connection from which the message originated.
   */
  public void handleMessageFromClient(Object msg, ConnectionToClient client)
  {

      String check = msg.toString();
      if(check.startsWith("SERVER msg> #")) { // to prevent client spoofing as server
          System.out.println("Illegal phrase from client: " + client);
          return;
      }
      System.out.println("Message received: " + msg + " from " + client);
      this.sendToAllClients(msg); // this sends the message back to the client (echo from the server); AbstractServer.java
  }

  /**
   * This method overrides the one in the superclass.  Called
   * when the server starts listening for connections.
   */
  protected void serverStarted()
  {
    System.out.println("Server listening for connections on port " + getPort());
  }

  /**
   * This method overrides the one in the superclass.  Called
   * when the server stops listening for connections.
   */
  protected void serverStopped()
  {
      System.out.println("Server has stopped listening for connections.");
  }

    /**
     * 2. Currently, the server does not allow any user input. Study the way user input in obtained from the client, using the ClientConsole
     * class, which implements the ChatIF interface. Create an analogous mechanism on the server side. (add a new class ServerConsole
     * that also implements the ChatIF interface. Anything typed on the server’s console should be echoed to the server’s console and to all
     * the clients. The message is prefixed by the string SERVER msg>).
     */

    public static class ServerConsole implements ChatIF {
        private EchoServer server;

        public ServerConsole(EchoServer server) {
            this.server = server;
        }

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
                    String serverMessage = "SERVER msg> " + message;
                    System.out.println(serverMessage);
                    if (serverMessage.startsWith("SERVER msg> #")) {
                        specialFunctions(serverMessage);
                    }
                    else {
                        server.sendToAllClients(serverMessage);
                    }
                }
            }
            catch (Exception ex)
            {
                System.out.println
                        ("Unexpected error while reading from console!");
            }
        }

        public void display(String message) {
            System.out.println("SERVER msg> " + message); //this is what the server console returns from ChatClient.java (ChatIF implementation)
        }

        /**
         *In a similar manner to the way you implemented commands on the client side, add a mechanism so that the user of the server can type
         * commands that perform special functions.
         * 1. #quit cause the server to terminate gracefully.
         * 2. #stop causes the server to stop listening for new clients.
         * 3. #close causes the server not only to stop listening for new clients, but also to disconnect all existing clients.
         * 4. #setport <port> calls the setPort method in the server. Only allowed if the server is closed.
         * 5. #start causes the server starts to listening for new clients. Only valid if the server is stopped.
         * 6. #getport displays the current port number.
         */

        public void specialFunctions(String message) throws IOException {
            if(message.startsWith("SERVER msg> #setport ")){
                //cannot use "if (server.isListening()) {" control because client will continue to communicate on a different port if changed
                /*if (server.isListening()) {
                    System.out.println("ERROR. Port can only be set if the server is closed.");
                    return;
                }*/
                server.close();
                String newPort = message.substring(21).trim();
                try {
                    Integer.parseInt(newPort);
                    server.setPort(Integer.parseInt(newPort));
                    display("Port set to: " + server.getPort());
                } catch (NumberFormatException e) {
                    display("ERROR. Port must be a number.");
                }
            }
            switch (message) {
                case "SERVER msg> #" -> {
                    System.out.println("Command list: \n#quit\n#stop\n#close\n#setport <port>\n#start\n#getport");
                }
                case "SERVER msg> #quit" -> {
                    System.out.println("Server is terminating");
                    System.exit(1);
                }
                case "SERVER msg> #stop" -> {
                    server.sendToAllClients("Server has stopped listening for connections.");
                    server.stopListening(); //New clients cannot log in. If already connected, client can still chat.
                }
                case "SERVER msg> #close" -> {
                    server.close(); //Server has stopped listening for connections. Clients are disconnected. New clients cannot log in.
                }
                case "SERVER msg> #start" -> {
                    server.listen(); //the server starts to listening for new clients
                }
                case "SERVER msg> #getport" -> {
                    System.out.println(server.getPort());
                }
            }
        }
    }


    //Class methods ***************************************************
  /**
   * This method is responsible for the creation of
   * the server instance (there is no UI in this phase).
   *
   * @param args[0] The port number to listen on.  Defaults to 5555
   *          if no argument is entered.
   */
  public static void main(String[] args)
  {
    int port = 0; //Port to listen on

    try
    {
      port = Integer.parseInt(args[0]); //Get port from command line
    }
    catch(Throwable t)
    {
      port = DEFAULT_PORT; //Set port to 5555
    }

    EchoServer sv = new EchoServer(port);

    try
    {
      sv.listen(); //Start listening for connections
    }
    catch (Exception ex)
    {
      System.out.println("ERROR - Could not listen for clients!");
    }
      ServerConsole consoleChat = new ServerConsole(sv);
      consoleChat.accept();  //Wait for console data
  }
}
//End of EchoServer class
