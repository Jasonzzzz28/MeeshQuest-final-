package cmsc420.meeshquest.part2;

import java.util.Comparator;

/**
 * Compares two objects that implement the NamedPoint2D
 * interface by name
 */
public class OrderByName<P extends NamedPoint2D> implements Comparator<P> {
	public int compare(NamedPoint2D p1, NamedPoint2D p2) {
		return p1.getName().compareTo(p2.getName());
	}
}
