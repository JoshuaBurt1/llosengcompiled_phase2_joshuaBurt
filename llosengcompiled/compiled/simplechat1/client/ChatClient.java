// This file contains material supporting section 3.7 of the textbook:
// "Object Oriented Software Engineering" and is issued under the open-source
// license found at www.lloseng.com 

package client;

import ocsf.client.*;
import common.*;
import java.io.*;

/**
 * This class overrides some of the methods defined in the abstract
 * superclass in order to give more functionality to the client.
 *
 * @author Dr Timothy C. Lethbridge
 * @author Dr Robert Lagani&egrave;
 * @author Fran&ccedil;ois B&eacute;langer
 * @version July 2000
 */
public class ChatClient extends AbstractClient
{
    //Class variables *************************************************
    /**
     * Comments for closed connections.
     */
    public static final String CONNECTION_CLOSED = "The server has shut down - quitting.";
    public static final String CLOSE = "Server closed unexpectedly - client disconnected from server.";

    //Instance variables **********************************************

    /**
     * The interface type variable.  It allows the implementation of
     * the display method in the client.
     */
    ChatIF clientUI;


    //Constructors ****************************************************

    /**
     * Constructs an instance of the chat client.
     *
     * @param host The server to connect to.
     * @param port The port number to connect on.
     * @param clientUI The interface type variable.
     */

    public ChatClient(String host, int port, ChatIF clientUI)
            throws IOException
    {
        super(host, port); //Call the superclass constructor
        this.clientUI = clientUI;
        //openConnection(); //if commented, client needs to log in. If not client logs in automatically
    }


    //Instance methods ************************************************

    /**
     * This method handles all data that comes in from the server.
     *
     * @param msg The message from the server.
     */
    public void handleMessageFromServer(Object msg)
    {
        clientUI.display(msg.toString());
    }

    /**
     * This method handles all data coming from the UI
     *
     * @param message The message from the UI.
     */
    public void handleMessageFromClientUI(String message)
    {
        try
        {
            sendToServer(message);
        }
        catch(IOException e)
        {
            clientUI.display("Could not send message to server. Enter # to see all commands.");
            //quit(); //if not commented and user types into console while no server -> error
        }
    }

    /**
     * 1. In Simple Chat, if the server shuts down while a client is connected, the client does not respond, and continues to wait for messages.
     * Modify the client so that it responds to the shutdown of server by printing a message saying the server has shut down, and quitting.
     * (look at the methods called connectionClosed and connectionException).
     * METHOD:
     * This function displays the "server shutdown" String value in the UI
     * Note: the client MUST be logged in for a server shutdown to cause client to quit
     */

    protected void connectionException(Exception exception) {
        //System.out.println(exception.toString());
        // #close = java.io.EOFException -> closeConnection();
        // #quit = java.net.SocketException: Connection reset -> quit();
        if (exception instanceof java.io.EOFException) {
            clientUI.display(CLOSE);
            try {
                closeConnection();
            } catch(IOException e){
                System.out.println("Error closing connection.");
                quit();
            }
        }
        else {
            clientUI.display(CONNECTION_CLOSED);
            quit();
        }
    }

    /**
     * This method terminates the client.
     */
    public void quit()
    {
        try
        {
            closeConnection();
        }
        catch(IOException e) {}
        //System.exit(0);
    }
}
//End of ChatClient class
