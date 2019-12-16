package cmsc420.meeshquest.part2;

import org.w3c.dom.*;
import java.util.ArrayList;
import java.util.Comparator;

/**
 * A straightforward implementation of a standard (unbalanced) binary
 * search tree.
 * 
 * The tree is parameterized by a type P, called a point, which is
 * assumed to implement the interface NamedPoint2D. Such an object
 * supports the functions getName(), getX(), and getY().
 * 
 * The constructor is provided a comparator, for comparing objects of
 * type P.

 */

public class BinarySearchTree<P extends NamedPoint2D> {
	
	private final boolean DEBUG = false; // produce extra debugging output

	// -----------------------------------------------------------------
	// Node processing - Used internally only
	// -----------------------------------------------------------------

	/**
	 * A node of the tree. Each stores a point and the links to the left
	 * and right children.
	 */

	private class Node {
		P point;
		Node left;
		Node right;

		/**
		 * Node constructor.
		 *
		 * @param point The node's data object
		 * @param left The left subtree
		 * @param right The right subtree
		 */

		Node(P point, Node left, Node right) {
			this.point = point;
			this.left = left;
			this.right = right;
		}
		
		/**
		 * Copy the data from one node to this one.
		 *
		 * @param other The source node
		 */

		void copyFrom(Node other) {
			point = other.point;
		}

		public String toString() {
			return "(" + point.toString() + ")";
		}
	}

	// -----------------------------------------------------------------
	// Node processing utilities
	// -----------------------------------------------------------------
	
	/**
	 * Find a point in the subtree rooted at a given node. Note
	 * that the point being deleted does not need to match fully. It
	 * suffices that it has enough information to satisfy the
	 * comparator.
	 *
	 * @param point The point being sought
	 * @param p The root of the subtree to search
	 */

	P find(P point, Node p) {
		if (p == null)
			return null; // unsuccessful search
		else if (comparator.compare(point, p.point) < 0) // point is smaller?
			return find(point, p.left); // ... search left
		else if (comparator.compare(point, p.point) > 0) // point is larger?
			return find(point, p.right); // ... search right
		else
			return p.point; // successful search
	}
	
	/**
	 * Insert a point into the subtree rooted at a given node.
	 *
	 * @param point The point being inserted
	 * @param p The root of the subtree in which to insert
	 */

	Node insert(P point, Node p) throws Exception {
		if (p == null) // fell out of the tree?
			p = new Node(point, null, null); // ... create a new leaf node here
		else if (comparator.compare(point, p.point) < 0) // point is smaller?
			p.left = insert(point, p.left); // ...insert left
		else if (comparator.compare(point, p.point) > 0) // point is larger?
			p.right = insert(point, p.right); // ...insert right
		else
			throw new Exception("duplicateCityName"); // point is equal ...duplicate key!
		return p; // return ref to current node
	}
	
	/**
	 * Find the replacement node. (Utility used by deletion.) This is
	 * the preorder successor p. It is assumed that p has a non-null 
	 * right child.
	 *
	 * @param p The root of the subtree in which to insert
	 */

	Node findReplacement(Node p) {
		Node r = p.right; // start in p's right subtree
		while (r.left != null)
			r = r.left; // go to the leftmost node
		return r;
	}

	/**
	 * Delete a point from the subtree rooted at a given node. Note
	 * that the point being deleted does not need to match fully. It
	 * suffices that it has enough information to satisfy the
	 * comparator.
	 *
	 * @param point The point being deleted
	 * @param p The root of the subtree in which to delete
	 */

	Node delete(P point, Node p) throws Exception {
		if (p == null) // fell out of tree?
			throw new Exception("cityDoesNotExist"); // ...error - no such key
		else {
			if (comparator.compare(point, p.point) < 0) // look in left subtree
				p.left = delete(point, p.left);
			else if (comparator.compare(point, p.point) > 0) // look in right subtree
				p.right = delete(point, p.right);
			// found it!
			else if (p.left == null || p.right == null) { // either child empty?
				if (p.left == null)
					return p.right; // return replacement node
				else
					return p.left;
			} else { // both children present
				Node r = findReplacement(p); // find replacement node
				if (DEBUG) {
					System.out.println("Binary search tree: Replacement node for deletion is " + r.point);
				}
				p.copyFrom(r); // copy its contents to p
				p.right = delete(r.point, p.right); // delete the replacement
			}
		}
		return p;
	}
	
	/**
	 * Generates a list of all the items in the tree according
	 * to an inorder traversal.
	 *
	 * @param list The list of items
	 * @param p The root of the subtree to enumerate (non-null)
	 */

	void entryList(ArrayList<P> list, Node p) {
		if (p.left != null) entryList(list, p.left);
		list.add(p.point);
		if (p.right != null) entryList(list, p.right);
	}
	
	/**
	 * Generates an XML element summarizing this subtree structure.
	 * The XML node structure mimics the tree's structure.
	 *
	 * @param element The element of the XML document
	 * @param p The node whose subtree to printed
	 */

	void print(Element element, Node p) {
		if (p == null) return; // empty subtree
		Element out = resultsDoc.createElement("node");
		out.setAttribute("name", p.point.getName());
		out.setAttribute("x", Integer.toString((int) p.point.getX()));
		out.setAttribute("y", Integer.toString((int) p.point.getY()));
		element.appendChild(out);

        print(out, p.left); // recurse on children
        print(out, p.right);
    }

	/**
	 * Debug print subtree.
	 * 
	 * @param prefix String indentation to make hierarchy clearer
	 * @param p The node whose subtree to printed (non-null)
	 */
	String debugPrint(String prefix, Node p) {
		return (p.left == null ? "" : debugPrint(prefix + "| ", p.left) + System.lineSeparator())
				+ prefix + p.point.toString()
				+ (p.right == null ? "" : System.lineSeparator() + debugPrint(prefix + "| ", p.right));
	}


	// -----------------------------------------------------------------
	// Private member data
	// -----------------------------------------------------------------

	private Node root; // root of tree
	private final Comparator<P> comparator; // comparator for ordering the tree
	private int size; // number of elements in tree
	private final Document resultsDoc; // results document (for printing)

	// -----------------------------------------------------------------
	// Public members
	// -----------------------------------------------------------------

	/**
	 * Creates an empty tree with the given comparator. The comparator
	 * can be anything, but for Meeshquest, it compares items by name.
	 *
	 * @param comparator The comparator used for ordering the tree.
	 */
	public BinarySearchTree(Comparator<P> comparator, Document resultsDoc) {
		root = null;
		this.comparator = comparator;
		this.resultsDoc = resultsDoc;
		size = 0;
	}

	/**
	 * Size of the tree.
	 *
	 * @return The number of items in the tree
	 */
	public int size() {
		return size;
	}

	/**
	 * Find an element in the tree
	 *
	 * @param point The item to look for (only the relevant members are needed)
	 * @return A reference to the element or null if not found
	 */
	public P find(P point) {
		return find(point, root);
	}

	/**
	 * Insert a point
	 *
	 * @param point The point to be inserted
	 */
	public void insert(P point) throws Exception {
		root = insert(point, root);
		size++;
		if (DEBUG) {
			System.out.println("Binary search tree: After inserting " + point + System.lineSeparator() + debugPrint("  ", root));
		}
	}

	/**
	 * Delete a point
	 *
	 * @param point The point to be deleted
	 */
	public void delete(P point) throws Exception {
		root = delete(point, root);
		size--;
		if (DEBUG) {
			System.out.println("Binary search tree: After deleting " + point + System.lineSeparator() + debugPrint("  ", root));
		}
	}

	/**
	 * Remove all items, resulting in an empty tree
	 */
	public void clear() {
		root = null;
		size = 0;
	}

	/**
	 * Return a list of entries
	 *
	 * @return The list of entries for all elements in the tree
	 */
	public ArrayList<P> entryList() {
		ArrayList<P> list = new ArrayList<P>();
		if (root != null) {
			entryList(list, root);
		}
		return list;
	}

	/**
	 * Print the tree. This is used for the command
	 * printBinarySearchTree.
	 *
	 * @param output The output XML document
	 */
    public void print(Element element) {
        Element out = resultsDoc.createElement("binarysearchtree");
        element.appendChild(out);
        if (root != null) {
        	print(out, root);
        }
    }

	/**
	 * Print the tree for debugging.
	 *
	 * @param prefix The string prefix to appear before each line
	 */
	
	String debugPrint(String prefix) {
		if (root != null) return debugPrint(prefix, root);
		else return new String();
	}

}
