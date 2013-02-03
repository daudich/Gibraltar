/**
 * Adding Packages
 */
package ncs;

import java.awt.Polygon;
import java.util.Vector;
import java.util.Random;

/**
 * This class contains all the variables that affect the dynamics of the game,
 * such as terrain overview, wind, weather, time, fog, line of sight, etc.
 * 
 * @author Dhiren Audich
 *
 */
public class Environment {
	
	protected Vector<Polygon> Land;
	protected int Rain, Fog, dir[] = {0, 90, 180, 360}, wind_dir, wind_speed;
    
    enum Time {
        DAWN(0), DAY(1), NIGHT(2);
        
        private int time; 
        Time(int time) {
            this.time = time;
        }
        
        public int getTime() {
            return time;
        }
        
        public Time next() {
            return Time.values()[(this.ordinal()+1) % Time.values().length];
        }
    };
    
    public Time time;
    
    public Environment() {
        Land = new Vector<Polygon>();
        changeWeather();
        generateMap();
    }
	
	/**
	 * This method randomly generates and sets the fog, wind, and rain.
	 * 
	 * @return void
	 * @param void
	 * 
	 */
	protected void changeWeather(){
		
		Random random = new Random(System.nanoTime());
		
		this.Rain = random.nextInt(2);
		this.Fog = random.nextInt(2);
		this.wind_dir = pickRand(dir);
		this.wind_speed = random.nextInt(3);
		
	}
	
	/** 
	 * This method randomly update and sets the fog, wind, and rain.
	 * 
	 * @return void
	 * @param void
	 * 
	 */
	protected boolean updateWeather(){
		
		Random random = new Random();
		boolean delta = random.nextDouble() > 0.95;
		
		if(delta){
			this.Rain = random.nextInt(2);
			this.Fog = random.nextInt(2);
			this.wind_dir = pickRand(dir);
			this.wind_speed = random.nextInt(3);
		}
		
		return delta;
		
	}
	
	/** 
	 * Change the time of the day in rotation: dawn->day->twilight->night->(repeat).
	 * 
	 * @return void
	 * @param void
	 * 
	 */
	protected void updateTime(){

		time = time.next();

	}
	
	/**
	 * This method randomly generates shapes and adds them to the Land array list.
	 * According to the specifications, the limits of the map is 4000 x 5000.
	 * 
	 * @param void
	 * @return void
	 */
	protected void generateMap(){
		
		Random random = new Random(System.nanoTime());
		
		//adding a random 4 sided polygon
		int p1 = random.nextInt(1000),
			p2 = random.nextInt(800),
			p3 = random.nextInt(500),
			p4 = random.nextInt(600),
			p5 = random.nextInt(1000),
			p6 = random.nextInt(800),
			p7 = random.nextInt(600),
			p8 = random.nextInt(500),
			x[] = new int[5], y[] = new int[5];
		
		x[0] = p1; y[0] = p2;
		x[1] = p3; y[1] = p4;
		x[2] = p5; y[2] = p6;
		x[3] = p7; y[3] = p8;
		
		Land.add(new Polygon(x, y, 4));
		
		random.setSeed(System.nanoTime());
		
		//adding a random 5 sided polygon
		p1 = random.nextInt(1000) + random.nextInt(1000);
		p2 = random.nextInt(800) + random.nextInt(800);
		p3 = random.nextInt(500) + random.nextInt(500);
		p4 = random.nextInt(600) + random.nextInt(600);
		p5 = random.nextInt(1000) + random.nextInt(1000);
		p6 = random.nextInt(800) + random.nextInt(800);
		p7 = random.nextInt(600) + random.nextInt(600);
		p8 = random.nextInt(500)+ random.nextInt(500);
		int p9 = random.nextInt(1200) + random.nextInt(1200);
		int p10 = random.nextInt(500) + random.nextInt(2000);
		
		x[0] = p1; y[0] = p2;
		x[1] = p3; y[1] = p4;
		x[2] = p5; y[2] = p6;
		x[3] = p7; y[3] = p8;
		x[4] = p9; y[4] = p10;
		
		Land.add(new Polygon(x, y, 5));
		
	}

	private static int pickRand(int[] arr){
		Random random = new Random(System.nanoTime());
        int rnd = random.nextInt(arr.length);
        return arr[rnd];
	}
	
}