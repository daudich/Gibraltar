package ncs;

import java.io.*;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.SynchronousQueue;

public class Client extends Thread{
	
	protected SynchronousQueue <String> messageQueue = new SynchronousQueue<String>(true);
    
    private Socket socket;
    
    private String tag;
    
	private boolean alive;
    
    
	/**
	 * This constructor merely accepts new client socket and forms an instance.
	 * 
	 * @param Socket client
	 * @return void
	 */
	public Client(Socket client){
		this.socket = client;
		this.tag = "";
	}

	/**
	 * This class relays messages to and from the client and the server. Every new message
	 * received is pushed onto the processQueue and whenever a message has to be sent it
	 * is popped from the messageQueue.
	 * 
	 * @see java.lang.Thread#run()
	 * @param void
	 * @return void
	 */
	@Override
	public void run() {
	
		this.alive = true;
		
		SynchronousQueue <String> processQueue = Controller.getInstance().getProcessQueue();
		
        BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} catch (IOException e) {
			System.err.println("Couldn't get I/O for the connection.");
			e.printStackTrace();
			System.exit(1);
		}
		
		new Thread(new Runnable() {
            public void run(){
				PrintWriter out = null;
				try {
					out = new PrintWriter(socket.getOutputStream(), true);
				} catch (IOException e) {
					System.err.println("Couldn't get I/O for the connection.");
					e.printStackTrace();
					System.exit(1);
				}
				while (alive){
                    try {
                        out.println(messageQueue.take());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
				}
			}
		}).start();
				
		
		while(this.alive){
			
			try {
				String cmd = in.readLine();
				if(cmd != null && !tag.equals(""))
					processQueue.put(tag.concat(":").concat(cmd));
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		 
	}
    
    /**
     *  This method is used to customise the tag that the
     *  client prepends to all server bound messages
     *
     *  @param tag
     *  @return void
     */
    public void setTag(String tag) {
        this.tag = tag;
        setName(String.format("Client " + tag));
    }
    

	/**
	 * This method returns the message queue. This method is usually called by the Controller class
	 * to add new messages to be passed on to the respective client.
	 * 
	 * @param void
     * @return messageQueue
	 */
	protected SynchronousQueue<String> getMessageQueue() {
		
		return this.messageQueue;
		
	}

    /**
     *  Tell the client to stop listening for incoming messages and start shutting down.
     *  
     *  @param void
     *  @return void
     */
    public void shutdown() {
    	
        this.alive = false;
        
    }
    
}
