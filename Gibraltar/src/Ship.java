package ncs;

import java.util.Random;
import java.awt.Point;

/**
 * This class contains all the information pertaining to a ship in the game.
 * Since each client can only have one ship, clientId is synonymous to shipId. 
 * 
 * @author Dhiren Audich
 *
 */
public class Ship {
	
	int clientId;
    String name;
    
    boolean ready;
	double speed, direction, health;
    
    Point pos;
	
	enum Type {
        MOW(0, 0.8333), FRIGATE(1, 0.6667), SLOOP(2, 0.3333);
        
        private int type;
        private double accuracy;
        
        Type(int type, double accuracy) {
            this.type = type;
            this.accuracy = accuracy;
        }
        
        public int getType() {
            return this.type;
        }
        
        public int getWidth() {
            return 20;
        }
        
        public int getLength() {
            int length = 80;
            for (int i = type; --i >= 0; length -= 20 * i);
            return length;
        }
        
        public double getHealth() {
            return 5.0 - type;
        }
        
        public int getVFactor() {
            return 50 + 20 * type;
        }
        
        public double getAccuracy() {
            return accuracy;
        }
    };
    
    Type shipType;
    
	/**
	 * This constructor randomly assigns a ship and sets the required parameters.
	 * 
	 * @param int id, String nom
	 * 
	 */
	public Ship(int id, String nom, int ship_type){
		
		this.clientId = id;
		this.name = nom;
        this.ready = false;
		
		Random random = new Random(System.nanoTime());
		
		this.pos = new Point(400 + random.nextInt(800), 400 + random.nextInt(800));		
		
		configureShip(Type.values()[ship_type]);
		
	}
    
    /**
     * This method sets the ship's parameters such as: length, width, health, initial conditions, etc.
     * 
     * @return void
     * @param type
     */
	public void configureShip(Type type) {
        this.shipType = type;
        
        // Variable ship attributes
        this.direction = 90.0;
        this.health = shipType.getHealth();
        this.speed = shipType.getVFactor() * 0.1; //initial speed
        
    }

	/**
	 * Assign new heading angle to the ship and return a bool if the process was successful or not.
	 * 
	 * @param double degrees
	 * @return boolean
	 */
	protected boolean changeHeading(int degrees){
		
		double dx = Math.sin(Math.toRadians(degrees)) * (shipType.getLength() / 2), dy = Math.cos(Math.toRadians(degrees)) * (shipType.getLength() / 2);
		
		//checking if the resultant point is in boundaries of the map
			
        if(this.direction + degrees > 180){
            this.direction = -360 + this.direction + degrees; 
        }
        //consider the opposite case and follow the similar logic
        //-175: 25 to the left. -180 - -175 = -5 and 180-20 = 160
        else if(this.direction + degrees < -180){
            this.direction = 360 + this.direction - degrees;
        }
        else { //all other cases which do not fall on the borderline
            this.direction += degrees;
        }
        return true;

	}
    
	/**
	 * Assign new heading angle to the ship and return a bool if the process was successful or not.
	 * 
	 * @param double degrees
	 * @return boolean

	protected boolean changeHeading(double degrees){
		
		double dx = Math.sin(degrees) * (this.length / 2), dy = Math.cos(degrees) * (this.length / 2);
		
		//checking if the resultant point is in boundaries of the map
		if((this.pos.x + dx > 0) && (this.pos.x + dx < 5000) 
				&& (this.pos.y + dy > 0) && (this.pos.y + dy < 4000)){
		
			this.pos.translate((int)Math.round(dx),(int)Math.round(dy));
			
			if(this.direction + degrees > 180){
				double temp = this.direction - 180;//will be negative number
				degrees += temp;//adjusting the rotation
				this.direction = -180 + degrees;//changing the direction
			}
			//consider the opposite case and follow the similar logic
			//-175: 25 to the left. -180 - -175 = -5 and 180-20 = 160
			else if(this.direction + degrees < -180){
				double temp = -180 - this.direction;//will be negative number
				degrees += temp;//adjusting the rotation
				this.direction = 180 - degrees;//changing the direction
			}
			else//all other cases which do not fall on the borderline
				this.direction += degrees;
			
			return true;
			
		}
		else
			return false;
		
		
	}
     */
	
	/**
	 * This method update the ships position with respect to its present speed and direction in 0.33 seconds.
	 * 
	 * @param void
	 * @return boolean
	 */
	protected boolean move(int wind_speed, int wind_dir){
		
		double distance = this.speed * .333, dx, dy;
		
		dx = Math.sin(Math.toRadians(this.direction)) * distance;
		dy = Math.cos(Math.toRadians(this.direction)) * distance;
		
        dx += Math.sin(Math.toRadians(wind_dir)) * wind_speed;
        dy += Math.cos(Math.toRadians(wind_dir)) * wind_speed;
        
		//checking if the resultant point is in boundaries of the map
		if((this.pos.x + dx > 0) && (this.pos.x + dx < 5000) 
				&& (this.pos.y + dy > 0) && (this.pos.y + dy < 4000)){
			
			this.pos.translate((int)Math.round(dx),(int)Math.round(dy));
			return true;
			
		}
		
		return false;
		
	}
	
	/**
	 * changeSpeed	Boolean	Assign new speed to the ship and return a bool if the process was successful or not.
	 * 
	 * @return boolean
	 * @param double factor
	 */
	protected boolean changeSpeed(double factor, int wind_speed, int wind_dir){
		
		if(factor > 1.0)
			return false;
	/*	
		int ship_dir;
		
		if(this.direction <= 0)
			ship_dir = (int) Math.floor(this.direction + 360);
		else
			ship_dir = (int) Math.floor(this.direction);
		
		if(ship_dir == wind_dir){//if ship is moving with the wind
			this.speed = shipType.getVFactor() * (factor + 0.1) ;
		}
		else{
			
			int test = ship_dir + 180;
			
			if(test >= 360)
				test -= 360;
			if(test == wind_dir){//if wind is against the ship
			
				if(factor-0.1 >= 0)
					this.speed = shipType.getVFactor() * (factor - 0.1) ;
				else
					this.speed = 0;
			
			}
			
		}
	*/	
        this.speed = shipType.getVFactor() * factor;
		return true;
		
	}
	
	/**
	 * Calculates damage for the ship according to the provided parameters and returns a bool if the process was successful or not.
	 * 
	 * @param void
	 * @return boolean
	 */
	protected boolean calculateDamage(){		
		if(Math.random() <= shipType.getAccuracy()) {
			this.health -= 0.5;
            return (this.health > 0);
		} else {
			return false;
        }
	}
	
	/**
	 * This method will return ship's state for broadcasting.
	 * 
	 * @param void
	 * @return String
	 */
	protected String getShipState(){
		
		return this.clientId + " " + this.pos.x + " " + this.pos.y + " " + this.speed + " " + this.direction + " " + health;
		
	}

}