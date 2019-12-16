package cmsc420.meeshquest.part2;

/**
 * City object for MeeshQuest, Part 1, Fall 2019. For the sake of the
 * dictionary data structures, we assume that it supports getX(), getY(),
 * and getName() functions.
 *
 */
public class City implements NamedPoint2D {

	// Private data (The x,y coordinates are provided by the Point2D class)
	
	private final Point2D point; // city location
    private final String name; // city name
    private final String color; // city color
    private final float radius; // city radius

    /**
     * Basic city constructor.
     * @param x City's x-coordinate
     * @param y City's y-coordinate
     * @param name City's name (must start with a letter)
     * @param color City's color
     * @param radius City's radius
     */
    public City(float x, float y, String name, String color, float radius) {
        this.point = new Point2D(x, y);
        this.name = name;
        this.color = color;
        this.radius = radius;
    }
    
    /**
     * City constructor from name alone.
     * 
     * @param name City's name (must start with a letter)
     */
    public City(String name) {
    	this.point = new Point2D();
        this.name = name;
        this.color = "black";
        this.radius = 0;
    }
    
    public float dist (Point2D x) {
    	return point.dist(x);
    }

    // Standard functions - Getters and toString
	public float getX() {
		return point.getX();
	}
	
	public float getY() {
		return point.getY();
	}
	
	public float get(int i) {
		return point.get(i);
	}
	
	public Point2D getPoint2D() {
		return point;
	}

    public String getName() {
        return name;
    }

    public String getColor() {
        return color;
    }

    public float getRadius() {
        return radius;
    }
    
    public String toString() {
    	return name + "(" + (int) getX() + "," + (int) getY() + ")";
    }
}
