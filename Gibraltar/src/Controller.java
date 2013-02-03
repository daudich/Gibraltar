package ncs;

import java.awt.Polygon;
import java.util.*;
import java.util.concurrent.SynchronousQueue;

public class Controller extends Thread {
    
	private static Controller instance = null;
    
	protected SynchronousQueue<String> processQueue = new SynchronousQueue<String>(true);
	protected SynchronousQueue<Client> clientQueue = new SynchronousQueue<Client>(true);
	
	//protected Queue <String> processQueue = new ConcurrentLinkedQueue<String>();	
    //protected Queue <Client> clientQueue = new ConcurrentLinkedQueue<Client>();
    
	private Vector<Ship> shipArray;
    
    private Vector<Client> clientArray;
    
	private Environment environment;
    
    private boolean started;
	
	protected Controller(){
		this.started = false;
        this.shipArray = new Vector<Ship>();
        this.clientArray = new Vector<Client>();
        this.environment = new Environment();
	}
	
    /**
     *  Returns a reference to this Singleton Class
     *
     *  @param void
     *  @return Controller
     */
	public static Controller getInstance() {
		
		if(instance == null){
			instance = new Controller();
			instance.start();
		}
		
		return instance;
	}

    /**
     *  Returns the queue for sending commands to this Controller
     *
     *  @param void
     *  @return Queue<String>
     */
	public SynchronousQueue<String> getProcessQueue() {
		
		return processQueue;
		
	}
    
    /**
     *  Returns the queue for relaying clients to this Controller
     *
     *  @param void
     *  @return Queue<Client>
     */
    public SynchronousQueue<Client> getClientQueue() {
		
		return clientQueue;
		
	}
    
    public void run() {
        
        /**
         *  Command Processing Thread
         *
         *  relays incoming commands from clients and
         */
        Thread cpt = new Thread(new Runnable() {
            public void run() {
            	
                while (true) {
                    String str = null;
					try {
						str = processQueue.take();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}//poll();
                    if (str != null) {
                        StringTokenizer st = new StringTokenizer(str, ":;");
                        
                        try {
                            int id = Integer.parseInt(st.nextToken());
                            String name = st.nextToken();                       
                            String[] args = new String[st.countTokens()];
                            for (int i = 0; i < args.length; args[i++] = st.nextToken());
                            
                            if (name.equals("register")) {
                                if (args.length == 2)
                                    client_register(id, Integer.parseInt(args[0]), args[1]);
                                else
                                    client_register(id, Integer.parseInt(args[0]));
                            } else if (name.equals("ready")) {
                                client_ready(id);
                            } else if (name.equals("fire")) {
                                client_fire(id, Integer.parseInt(args[0]));
                            } else if (name.equals("speed")) {
                                client_speed(id, Double.parseDouble(args[0]));
                            } else if (name.equals("setHeading")) {
                                client_setHeading(id, Integer.parseInt(args[0]));
                            } else if (name.equals("disconnect")) {
                                client_disconnect(id);
                            } else {
                                System.err.println("WARNING: Bad Message from Client");    
                            }
                            
                        } catch (NoSuchElementException ex) {
                            ex.printStackTrace();
                            System.err.println("WARNING: Bad Message from Client");
                        }
                    }
                }
            }
        });
        cpt.setName("Command Processing Thread");
        cpt.start();
        
        /**
         *  Client Pre-processing Thread
         *
         *  Adds incoming clients to collection and set their tag for
         *  further recognition
         */
        cpt = new Thread(new Runnable() {
            public void run() {
                while(true) {
                    Client c = null;
					try {
						c = clientQueue.take();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}//poll();
                    if (c != null) {
                        clientArray.add(c);
                        c.setTag(Integer.toString(clientArray.indexOf(c)));
                        c.start();
                    }
                }
            }
        });
        cpt.setName("Client Pre-processing Thread");
        cpt.start();
        
        setName("Main Game Thread");
        // Main Game Thread
        while(!started){
			try {
			    sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	//yield();
        }
        
        System.out.println ("Game Started");
        
        while (true) {
            if (environment.updateWeather()) {
                server_rain();
                server_fog();
                server_wind();
            }
            for (Ship ship : shipArray) {
                if (ship.move(environment.wind_speed, environment.wind_dir)) {
                    server_shipState(ship.clientId, ship.speed, ship.direction, ship.health);   
                }
            }
            try {
                sleep(100);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
    
    /**
     *  Client-to-Server Setup Messages
     */    
    
    
    /**
     *  Creates and registers a ship to requesting client
     *
     *  @param id           Identification number for the client 
     *  @param ship_type    Type of ship to be registered
     *  @return void
     */
    protected void client_register(int id, int ship_type) {
        client_register(id, ship_type, "");
    }
    
    /**
     *  Returns the queue for relaying clients to this Controller
     *
     *  @param id           Identification number for the client 
     *  @param ship_type    Type of ship to be registered
     *  @param name         Name of the ship to register
     *  @return void
     */
    protected void client_register(int id, int ship_type, String name) {
        Ship ship = new Ship(id, name, ship_type);
        shipArray.add(id, ship);
        
        server_registered(id);
        server_shore();
    }
    
    /**
     *  Marks client as ready to play
     *
     *  @param id           Identification number for the client 
     *  @return void
     */
    protected void client_ready(int id) {
        Ship ship = shipArray.get(id);
        
        if (ship.ready)
            return;
        ship.ready = true;
        
        if (started) {
            server_start(id);
            server_ship(id);
            server_ship(id, ship.shipType.getType());
        } else {        
            boolean ready = true;
            for (Ship s : shipArray) {
                if (!(ready &= s.ready)) {
                    break;
                }
            }
            
            if ((started = ready)) {
                server_start();
                for (Ship s : shipArray)
                    server_ship(s.clientId);
            }
        }
    }
    
    /**
     *  Client-to-Server Gameplay Messages
     */    

    /**
     *  Fire a shot from requesting clients ship to another
     *
     *  @param  id           Identification number for the client 
     *  @param  tid           Identification number for the target 
     *  @return void
     */
    protected void client_fire(int id, int tid) {
        Ship ship = shipArray.get(id);
        
        server_firing(id, tid);
        if (ship.calculateDamage())
            server_shipState(ship.clientId, ship.speed, ship.direction, ship.health);
    }

    /**
     *  Sets the speed of the requesting clients ship
     *
     *  @param  id              Identification number for the client
     *  @param  s               Percentage of total speed
     *  @return void
     */
    protected void client_speed(int id, double s) {
        Ship ship = shipArray.get(id);
        if (ship.changeSpeed(s, environment.wind_speed, environment.wind_dir));
 //           server_shipState(id, ship.speed, ship.direction, ship.health);
    }

    /**
     *  Sets the speed of the requesting clients ship
     *
     *  @param  id              Identification number for the client
     *  @param  h               Bearing from [-180,180]
     *  @return void
     */
    protected void client_setHeading(int id, int h) {
        Ship ship = shipArray.get(id);
        if (ship.changeHeading(h));
            //server_shipState(id, ship.speed, ship.direction, ship.health);
    }
    
    /**
     *  Client-to-Server End game Messages
     */
    
    /**
     *  Disconnects the requesting client
     *
     *  @param  id              Identification number for the client
     *  @return void
     */
    protected void client_disconnect(int id) {
    }
    
    /**
     *  Server-to-Client Setup Messages
     */    
    
    /**
     *  Sends start message to all clients
     *
     *  @return void
     */
    protected void server_start() {
        String str = String.format("start;");
        for (Client c : clientArray) {
            try {
				c.getMessageQueue().put(str);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
    }
    
    /**
     *  Sends start message to one clients
     *
     *  @return void
     */
    protected void server_start(int id) {
        try {
			clientArray.get(id).getMessageQueue().put("start;");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    }
    
    /**
     *  Send registration confirmation to newly registered client
     *
     *  @return void
     */
    protected void server_registered(int id) {
        String str = String.format("registered:%d;", id);
        try {
			clientArray.get(id).getMessageQueue().put(str);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}//add(str);
    }

    /**
     *  Send shore positions to newly registered client
     *
     *  @return void
     */
    protected void server_shore() {
        String str = "";
        // Shape or Polygon ?
        for (Polygon p : environment.Land) {
            str.concat("shore");
            for (int i = 0; i < p.npoints; i++) {
                str.concat(String.format(":%d:%d", p.xpoints[i], p.ypoints[i]));
            }
            str.concat(";\n");
        }
        str.concat("shore:x;\n");
        System.out.println(str);
        
        for (Client c : clientArray) {
            try {
				c.getMessageQueue().put(str);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
    }
    
    /**
     *  Client-to-Server Environment Messages
     */
    
    /**
     *  Send fog info to all clients
     *
     *  @return void
     */
    protected void server_fog() {
        String str = String.format("fog:%d;", environment.Fog);
        for (Client c : clientArray) {
            try {
				c.getMessageQueue().put(str);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
    }

    /**
     *  Send rain info to all clients
     *
     *  @return void
     */
    protected void server_rain() {
        String str = String.format("rain:%d;", environment.Rain);
        for (Client c : clientArray) {
            try {
				c.getMessageQueue().put(str);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
    }
    
    /**
     *  Send the time to all clients
     *
     *  @return void
     */
    protected void server_time() {
        String str = String.format("time:%d;", environment.time.getTime());
        for (Client c : clientArray) {
            try {
				c.getMessageQueue().put(str);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
    }
    
    /**
     *  Send wind info to all clients
     *
     *  @return void
     */
    protected void server_wind() {
        String str = String.format("wind:%d:%d;", environment.wind_speed, environment.wind_dir);
        for (Client c : clientArray) {
            try {
				c.getMessageQueue().put(str);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
    }

    /**
     *  Client-to-Server Update Messages
     */    
    
    /**
     *  Tell all clients about a shot fired
     *
     *  @param  id           Identification number for the shooter 
     *  @param  tid           Identification number for the target 
     *  @return void
     */
    protected void server_firing(int id, int tid) {
        String str = String.format("firing:%d:%d;", id, tid); 
        for (Client c : clientArray) {
            try {
				c.getMessageQueue().put(str);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
    }
    
    /**
     *  Sends this client, info about all ships in the water
     *
     *  @param  id           Identification number for new client
     *  @return void
     */
    protected void server_ship(int id) {
        Client c = clientArray.get(id);
        
        System.out.println("Send all ship info to client");
        
        for (Ship s : shipArray) {
            if (s.ready) {
                try {
                    c.getMessageQueue().put(String.format("ship:%d:%d;", s.clientId, s.shipType.getType()));
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     *  Sends all ready clients info about the new ship
     *
     *  @param  id           Identification number for new client
     *  @param  ship_type    type of Ship under clients control
     *  @return void
     */
    protected void server_ship(int id, int ship_type) {
        String str = String.format("ship:%d:%d;", id, ship_type);
        
        System.out.println("Send ship info to all clients");
        
        Client c = clientArray.get(id);
        
        for (Ship s : shipArray) {
            if (s.ready && s.clientId != id) {
                try {
					clientArray.get(s.clientId).getMessageQueue().put(str);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
        }
    }
    
    /**
     *  Tells all clients about
     *  the state change of a ship
     *
     *  @param  id          Identification number for new client
     *  @param  speed       Percentage of total speed
     *  @param  direction   
     *  @return void
     */    
    protected void server_shipState(int id, double speed, double direction, double damage) {
        
        Ship ship = shipArray.get(id);
    	String str = String.format("shipState:%d:%d:%d:%f:%f:%f;", id, ship.pos.x, ship.pos.y, speed, direction, damage);
        
        for (Ship s : shipArray) {
            try {
                if (s.ready) 
                    clientArray.get(s.clientId).getMessageQueue().put(str);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        
    }
    
    /**
     *  Client-to-Server Endgame Messages
     */ 
    
    protected void server_gameover() {
        String str = String.format("gameover;");
        for (Client c : clientArray) {
            try {
				c.getMessageQueue().put(str);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
    }

    static Server server;
    
	public static void main (String[] args){
	
		//start the server
		server = new Server();
        
        server.start();
        
        System.out.println ("Started");
        
        // query server via stdin
		
	}
	
}
