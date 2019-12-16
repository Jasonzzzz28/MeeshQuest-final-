package cmsc420.meeshquest.part2;

/**
 * A 2-dimensional point with a name. This abstracts the essential
 * aspects of the City object that our program uses for the purpose
 * of defining data structures.
 */
public interface NamedPoint2D {
	public float getX(); // get x-coordinate
	public float getY(); // get y-coordinate
	public float get(int i); // get i-th coordinate (0=x, 1=y)
	public Point2D getPoint2D(); // return the point
	public String getName(); // get name
	public float dist(Point2D p);
}
