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

    private static String loginId = "";
    final public static int DEFAULT_PORT = 5555;
    public static final String ALREADY_LOGGED_IN = "Error, user is already logged in.";
    public static final String USER_COMMANDS = "Command list: \n#quit\n#logoff\n#sethost <host>\n#setport <port>\n#login\n#gethost\n#getport\n#getloginid\n#setloginid";
    public static final String QUIT = "User selected quit - shutting down client.";
    public static final String LOGOFF = "User selected logoff - disconnecting client from server.";
    public static final String LOGIN = "User selected login - connecting client to server.";
    public static final String NO_LOGIN_ID = "No login id provider - shutting down client.";

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
    public ClientConsole(String loginId, String host, int port)
    {
        try
        {
            client= new ChatClient(loginId, host, port, this);
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
            //client.openConnection(); //TEST CASE FOR #5. Phase 2
            while (true)
            {
                message = fromConsole.readLine();
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

    public void specialFunctions(String message) throws IOException {
        if(message.startsWith("#setloginid ")){
            String loginId = message.substring(12).trim();
            client.setLoginId(loginId);
            display("Login id set to: " + client.getLoginId());
        }
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
                client.closeConnection();
                System.exit(1);
            }
            case "#logoff" -> {
                display(LOGOFF);
                client.closeConnection();
            }
            case "#login" -> {
                /* //COMMENTED OUT FOR PHASE 2 - SERVER SIDE # 4. (same function)
                if(client.isConnected()){
                    display(ALREADY_LOGGED_IN);
                    return;
                }*/
                /* // COMMENTED OUT FOR PHASE 2 - SERVER SIDE # 5. (same function)
                if(client.getLoginId().isBlank()){
                    display(NO_LOGIN_ID);
                    System.exit(1);
                }*/
                //this opens a connection even if the server is not listening (server #stop command) causing all messages to keep stacking
                //if (server #start), all stacked messages pass at once
                try{
                    client.openConnection();
                    display(LOGIN);
                    client.handleMessageFromClientUI("#login "+client.getLoginId()); //sends the initial '#login <loginid>' to the server.
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
            case "#getloginid" -> {
                display(String.valueOf(client.getLoginId()));
            }
        }
    }

    //Class methods ***************************************************

    /**
     * This method is responsible for the creation of the Client UI.
     */
    public static void main(String[] args)
    {
        loginId = "";
        String host = "";
        int port = 0;  //The port number

        try
        {
            loginId = args[0];
            host = args[1];
            port = Integer.parseInt(args[2]); //Get port from command line
        }
        catch(ArrayIndexOutOfBoundsException e)
        {
            loginId = "";
            host = "localhost";
            port = DEFAULT_PORT; //Set port to 5555
        }
        ClientConsole chat= new ClientConsole(loginId, host, port);
        chat.accept();  //Wait for console data
    }
}
//End of ConsoleChat class#
