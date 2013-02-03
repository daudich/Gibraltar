package ncs;

import java.net.ServerSocket;
import java.util.Queue;
import java.io.*;

public class Server extends Thread{

	private ServerSocket server_socket;
	private static final int PORT = 5283;
	private boolean alive;
		
    /**
     * Puts the server into a listening loop, where it just waits for
     * new connections from clients, gets a socket to communicate with
     * them, and then creates a ServerThread object in its own thread
     * to listen for messages for that client (so we, the Server,
     * don't have to block on that, and can instead keep waiting for
     * new connections!)
     * 
     * @param void
     * @return void
     * 
     */
    public void run() {
    	setName("Server Thread");
        
        
    	this.alive = true;
    	Queue <Client> clientQueue = Controller.getInstance().getClientQueue();
    	
        try {
            server_socket = new ServerSocket(PORT);
        } catch (Exception e) {
            e.printStackTrace();
        }
	
		while (this.alive) {
		    
			System.out.println ("Waiting for incoming connection.");
		    
			try {
                Client client = new Client(server_socket.accept());
				clientQueue.add(client);
		    } catch (IOException e) {
                System.out.println ("Failed to add Client");
		    	e.printStackTrace();
		    }
		    
		}
		
    }
    
    /**
     *  Tell the server to stop listening for incoming 
     *  clients and start shutting down
     *
     *  @param void
     *  @return void
     */
    public void shutdown() {
    	
        this.alive = false;
        
    }

}
