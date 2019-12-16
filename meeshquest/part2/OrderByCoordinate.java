package cmsc420.meeshquest.part2;

import java.util.Comparator;

/**
 * Compares two objects that implement the NamedPoint2D
 * interface by coordinates, lexicographically on x then y.
 */
public class OrderByCoordinate<P extends NamedPoint2D> implements Comparator<P> {
	public int compare(NamedPoint2D p1, NamedPoint2D p2) {
		double result = p1.getX() - p2.getX();
		if (result != 0) {
			return (int) result;
		} else {
			return (int) (p1.getY() - p2.getY());
		}
	}
}
