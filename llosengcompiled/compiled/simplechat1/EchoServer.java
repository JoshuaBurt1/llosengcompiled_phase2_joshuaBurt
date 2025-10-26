// This file contains material supporting section 3.7 of the textbook:
// "Object Oriented Software Engineering" and is issued under the open-source
// license found at www.lloseng.com

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
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
    String loginId = "loginId";
    String joinedLoginId = "";
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
        // 1. The #login commend should be recognized by the server.
        if(check.startsWith("#login ")) {
            joinedLoginId = check.substring(7).trim();
            if(joinedLoginId.isBlank()){
                try {
                    client.sendToClient("SERVER msg> Invalid login id.");
                    client.close();
                }
                catch (Exception ex){
                    System.out.println(ex);
                }
            }
            // 4. The #login command should only be allowed as the first command received after a client connect.
            // If #login is received at any other time, the server should send an error message back to the client.
            try{
                client.getInfo(loginId); // if the loginId exists -> already logged in
                if(client.getInfo(loginId)==null){
                    //2. The login id should be saved, so that the server can always identify the client.
                    client.setInfo(loginId,joinedLoginId);
                    return;
                }
                else{
                    client.sendToClient("SERVER msg> Already logged in.");
                }
            }
            catch (Exception ex){
                System.out.println(ex);
            }
            return;
        }
        // 5. If the #login command is not received as the first command, then the server should send an error message back to the client
        // and terminate the client’s connection. (use the method close in ConnectionToClient).
        if(client.getInfo(loginId)==null) { // the only way the client loginId is not null is if #login command is used.
            try {
                client.sendToClient("SERVER msg> Error user, must log in first.");
                client.close();
            }
            catch (Exception ex){
                return;
            }
        }
        if(client.getInfo(loginId)!=null) {
            if (check.startsWith("SERVER msg> #")) { // to prevent client spoofing as server
                System.out.println("Illegal phrase from client: " + client);
                return;
            }
            //3. Each message echoed by the server should be prefixed by the login id of the client that sent the message.
            System.out.println(client.getInfo(loginId) + ": " + msg);
            this.sendToAllClients(msg); // this sends the message back to the client (echo from the server); AbstractServer.java
        }
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