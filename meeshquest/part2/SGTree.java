package cmsc420.meeshquest.part2;

import org.w3c.dom.*;

import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

public class SGTree<P extends NamedPoint2D> {
//	private final boolean DEBUG = true; // produce extra debugging output
	private final boolean DEBUG = false; // produce extra debugging output

	private final int BALANCE_NUM = 2; // numerator in balance ratio
	private final int BALANCE_DENOM = 3; // denominator in balance ratio

	// -----------------------------------------------------------------
	// Node processing - Used internally only
	//
	// There are two types of nodes, external and internal. Values are
	// stored in external nodes, and internal nodes store keys only.
	// -----------------------------------------------------------------

	/**
	 * A node of the tree. This is an abstract object, which is extended for
	 * internal and external nodes.
	 */
	private abstract class Node { // generic node type
		final boolean isExternal; // is node external?

		Node(boolean isExternal) { // constructor
			this.isExternal = isExternal;
		}

		abstract P find(P pt); // find point in subtree

		abstract Node insert(P pt) throws Exception; // insert point into subtree

		abstract Node delete(P pt) throws Exception; // delete point from subtree

		abstract Node rebalance(P pt); // find scapegoat and rebalance tree

		abstract void entryList(List<P> list); // return list of entries in subtree

		abstract void print(Element result); // print subtree to result

		abstract P nearestNeighborSearch(P queryPoint, P candidate) throws Exception;
	}

	// -----------------------------------------------------------------
	// Internal node
	// -----------------------------------------------------------------

	/**
	 * An internal node of the tree. The associated point is just used for splitting
	 * left and right subtrees by the associated comparator.
	 */
	private class InternalNode extends Node {
		private final Comparator<P> comparator; // comparator for ordering the tree
		final P splitter; // point object used for splitting
		int size; // node size (number of external descendants)
		int height; // node height (max number of edges to external)
		
		Node left; // children
		Node right;
		int SDim;

		/**
		 * Constructor.
		 * 
		 * @param splitter The point used to split left (<=) from right (>)
		 * @param left     The left subtree
		 * @param right    The right subtree
		 */
		InternalNode(P splitter, int Dim, Node left, Node right) {
			super(false);
			this.splitter = splitter;
			this.SDim = Dim;
			if (Dim == 0) {
				comparator = new OrderByCoordinate<P>();
			}
			else {
				comparator = new YXcompare<P>();
			}
			this.left = left;
			this.right = right;
			updateSizeAndHeight();
		}

		P nearestNeighborSearch(P qp, P best) throws Exception {
			best = left.nearestNeighborSearch(qp, best); 				
			best = right.nearestNeighborSearch(qp, best); 
			return best;
		}

		/**
		 * Find point in internal node.
		 *
		 * @param pt The point being sought.
		 */
		P find(P pt) {
			if (comparator.compare(pt, splitter) <= 0) {
				return left.find(pt);
			} else {
				return right.find(pt);
			}
		}

		/**
		 * Insert into this subtree. This just passes the request to the appropriate
		 * child.
		 *
		 * @param pt The point to insert.
		 * @return The updated subtree root after insertion.
		 */
		Node insert(P pt) throws Exception {
			if (comparator.compare(pt, splitter) <= 0) { // pt is less or equal
				left = left.insert(pt);
				updateSizeAndHeight(); // update this node's information
			} else { // pt is larger
				right = right.insert(pt);
				updateSizeAndHeight(); // update this node's information
			}
			return this;
		}

		/**
		 * Find the scapegoat node and rebalance the tree. Using the point to retrace
		 * the search path, we find the first (closest to root) node that fails the
		 * balance condition. We then rebuild this tree by invoking rebuild. Otherwise,
		 * we recurse on the appropriate child. Since rebuilding causes subtree heights
		 * to change, we update our size and height values just prior to returning.
		 *
		 * @param pt The point defining the search path.
		 * @return The updated subtree root after rebalancing.
		 */
		Node rebalance(P pt) {
			if (comparator.compare(pt, splitter) <= 0) { // pt is less or equal based on the cutting dimension
				if (2 * getSize(this) < 3 * getSize(left)) { // too unbalanced?
					return rebuild(this); // this is the scapegoat
				} else { // balance is okay
					left = left.rebalance(pt); // continue the search
					updateSizeAndHeight(); // update this node's information
					return this;
				}
			} else { // pt is larger
				if (2 * getSize(this) < 3 * getSize(right)) { // too unbalanced?
					return rebuild(this); // this is the scapegoat
				} else { // balance is okay
					right = right.rebalance(pt); // continue the search
					updateSizeAndHeight(); // update this node's information
					return this;
				}
			}
		}

		/**
		 * Delete from this subtree. Since this is an extended tree, if we delete a
		 * child and find that it is null, then this child must be the external node
		 * containing the deleted value. We unlink ourselves from the tree by returning
		 * a pointer to our other child.
		 *
		 * @param pt The point to delete
		 * @return The updated root of the subtree
		 * @throws Exception If the point does not exist in the subtree
		 */
		Node delete(P pt) throws Exception {
			if (comparator.compare(pt, splitter) <= 0) { // delete from left
				left = left.delete(pt);
				if (left == null) {
					return right; // subtree gone, return sibling
				} else {
					updateSizeAndHeight(); // update this node's information
					return this;
				}
			} else { // delete from right
				right = right.delete(pt); // update this node's information
				if (right == null) {
					return left; // subtree gone, return sibling
				} else {
					updateSizeAndHeight();
					return this;
				}
			}
		}

		/**
		 * Updates the size and height of an internal node.
		 *
		 * @param p The node whose information is to be updated
		 */
		void updateSizeAndHeight() {
			size = getSize(left) + getSize(right);
			height = 1 + Math.max(getHeight(left), getHeight(right));
		}

		/**
		 * Add entries of this subtree to the list.
		 * 
		 * @param list list in which to store elements
		 */
		void entryList(List<P> list) {
			left.entryList(list);
			right.entryList(list);
		}

		/**
		 * Print node information to result.
		 * 
		 * @param element The document element in which to store result
		 *
		 */
		void print(Element element) {
			// print this item
			Element out = resultsDoc.createElement("internal");
			out.setAttribute("splitDim", Integer.toString((int) SDim));
			out.setAttribute("x", Integer.toString((int) splitter.getX()));
			out.setAttribute("y", Integer.toString((int) splitter.getY()));
			element.appendChild(out);

			left.print(out); // recurse on children
			right.print(out);
		}

	}

	// -----------------------------------------------------------------
	// External node
	// -----------------------------------------------------------------

	/**
	 * An external node of the tree. This stores an object of type P.
	 */
	private class ExternalNode extends Node {
		private final Comparator<P> comparator= new OrderByCoordinate<P>(); // comparator for ordering the tree
		final P point; // the associated point object

		/**
		 * Constructor from a point.
		 * 
		 * @param point The point object stored in this node
		 */
		ExternalNode(P point) {
			super(true);
			this.point = point;
		}

		/**
		 * Find point in external node.
		 * 
		 * @param pt The point to seek
		 * 
		 * @return A reference to the point if found or else null
		 */
		P find(P pt) {
			if (comparator.compare(pt, point) == 0)
				return point;
			else
				return null;
		}

		/**
		 * Insertion at an external node. The point stored in the external node and this
		 * point are placed into a list, and we then invoke buildTree to construct the
		 * associated tree.
		 * 
		 * @param pt The point to insert.
		 * @return The root of the new subtree.
		 */
		Node insert(P pt) throws Exception {
			ArrayList<P> list = new ArrayList<P>(); // array list for points
			list.add(pt); // add points to list
			list.add(point);
//			Collections.sort(list, comparator); // sort the list

			return buildTree(list); // build a tree and return
		}

		/**
		 * Find the scapegoat and rebalance tree.
		 *
		 * @param pt The point defining the search path
		 */
		Node rebalance(P pt) {
			assert (false); // should never get here
			return null;
		}

		/**
		 * Delete from this node.
		 * 
		 * @param pt The point to delete
		 * @return The updated root of the subtree
		 */
		Node delete(P pt) throws Exception {
			if (comparator.compare(pt, point) == 0) { // found it
				return null;
			} else {
				throw new Exception("cityDoesNotExist");
			}
		}

		/**
		 * Add entry to the list.
		 * 
		 * @param list The list into which items are added
		 */
		void entryList(List<P> list) {
			list.add(point);
		}

		/**
		 * Print node information to result.
		 * 
		 * @param element The element of the document
		 */
		void print(Element element) {
			Element out = resultsDoc.createElement("external");
			out.setAttribute("name", point.getName());
			out.setAttribute("x", Integer.toString((int) point.getX()));
			out.setAttribute("y", Integer.toString((int) point.getY()));
			element.appendChild(out);
		}

		P nearestNeighborSearch(P qp, P best) throws Exception {
			if (best == null) { 
				return point;
			} 
			else { 
				float dist = qp.dist(point.getPoint2D());
				float bdist = qp.dist(best.getPoint2D());
				if (dist < bdist) {	
					return point;
				} else {
					return best;
				}
			}
		}
	}

	// -----------------------------------------------------------------
	// Tree utilities
	// -----------------------------------------------------------------

	/**
	 * Returns the size of a node's subtree.
	 *
	 * @param p The root of the subtree.
	 *
	 * @return The number of external nodes in the subtree.
	 */
	@SuppressWarnings("unchecked")
	int getSize(Node p) {
		if (p.isExternal)
			return 1;
		else
			return ((InternalNode) p).size;
	}

	/**
	 * Returns the height of a node's subtree.
	 *
	 * @param p The node whose height we seek
	 *
	 * @return The height of the subtree (max no. of edges to external)
	 */
	@SuppressWarnings("unchecked")
	int getHeight(Node p) {
		if (p.isExternal)
			return 0;
		else
			return ((InternalNode) p).height;
	}

	Node rebuild(Node p) {
		if (p.isExternal) {
			return p; // external - nothing to do
		}
		ArrayList<P> list = new ArrayList<P>(); // place to store points
		p.entryList(list); // get all the cities under this node, and stored in the list

		Node t = buildTree(list); // build new subtree from the list
		
		return t;
	}

	Node buildTree(List<P> list) {
		int s = list.size();
		if (s == 0) { 
			return null;
		} else if (s == 1) { 
			return new ExternalNode(list.get(0));
		} else {
			ArrayList<P> xds = new ArrayList<P>(list); 
			Collections.sort(xds, new OrderByCoordinate<P>()); 
			ArrayList<P> yds = new ArrayList<P>(list); 
			Collections.sort(yds, new YXcompare<P>()); 

			int Dim = 0; 
			List<P> use = xds;
			if (xds.get(xds.size() - 1).getX() - xds.get(0).getX() 
					< yds.get(yds.size() - 1).getY() - yds.get(0).getY()) { 
				use = yds;
				Dim = 1;
			}

			int m = s - s/2; 
			P splitter = use.get(m - 1); 
			Node l = buildTree(use.subList(0, m));
			Node r = buildTree(use.subList(m, s));
			InternalNode res = new InternalNode(splitter, Dim, l, r);
			res.updateSizeAndHeight(); 
			return res;
		}
	}

	// -----------------------------------------------------------------
	// Private member data
	// -----------------------------------------------------------------

	private Node root = null; // root of the tree

	private final Document resultsDoc; // results document (for printing)
	private int nItems; // number of items (equals getSize(root))
	private int maxItems; // upper bound on the number of items

	// -----------------------------------------------------------------
	// Public members
	// -----------------------------------------------------------------

	/**
	 * Creates an empty tree with the given comparator.
	 *
	 * @param comparator The comparator used for ordering the tree
	 */
	public SGTree(Document resultsDoc) {
		root = null;
		this.resultsDoc = resultsDoc;
		maxItems = nItems = 0;
	}

	/**
	 * Size of the tree.
	 *
	 * @return The number of items in the tree
	 */
	public int size() {
		return nItems;
	}

	/**
	 * Find an point in the tree. Note that the point being deleted does not need to
	 * match fully. It suffices that it has enough information to satisfy the
	 * comparator.
	 *
	 * @param pt The item being sought (only the relevant members are needed)
	 * @return A reference to the element where found or null if not found
	 */
	public P find(P pt) {
		if (root == null) {
			return null;
		} else {
			return root.find(pt);
		}
	}

	/**
	 * Insert a point
	 *
	 * @param point The point to be inserted
	 */
	public void insert(P pt) throws Exception {

		if (root == null) {
			root = new ExternalNode(pt);
		} else {
			root = root.insert(pt);
		}
		// keep tracking the size and height of tree
		nItems++;
		maxItems++;
		assert (nItems == getSize(root));
		int maxAllowedHeight = (int) (Math.log(maxItems) / Math.log((double) BALANCE_DENOM / (double) BALANCE_NUM));
		if (getHeight(root) > maxAllowedHeight) { // out of balance
			root = root.rebalance(pt);
		}
	}

	/**
	 * Delete a point. Note that the point being deleted does not need to match
	 * fully. It suffices that it has enough information to satisfy the comparator.
	 *
	 * @param point The point to be deleted
	 */
	public void delete(P pt) throws Exception {
		
		if (root == null) {
			throw new Exception("cityDoesNotExist");
		} else {
			root = root.delete(pt);
		}
		nItems--;
		if (2 * nItems < maxItems) {
			root = rebuild(root);
			if (DEBUG) {
				System.out.println("kd-tree: Triggered rebuild after deletion. n = " + nItems + " m = " + maxItems);
				System.out.println();
			}
			maxItems = nItems;
		}
	}

	/**
	 * Remove all items, resulting in an empty tree
	 */
	public void clear() {
		root = null;
		maxItems = nItems = 0;
	}

	public boolean isEmpty () {
		if (root == null) return true;
		return false;
	}
	/**
	 * Return a list of entries
	 *
	 * @return the list of entries for all elements in the tree
	 */
	public List<P> entryList() {
		ArrayList<P> list = new ArrayList<P>();
		if (root != null) {
			root.entryList(list);
		}
		return list;
	}

	/**
	 * Print the tree
	 *
	 * @param output the output XML document
	 */
	public void print(Element element) {
		Element out = resultsDoc.createElement("KdTree");
		element.appendChild(out);
		if (root != null)
			root.print(out);
	}

	public P nearNS(P queryPoint) throws Exception {
		P res = root.nearestNeighborSearch(queryPoint, null); 
		return res;
	}

}
