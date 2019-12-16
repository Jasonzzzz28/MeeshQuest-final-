package cmsc420.meeshquest.part2;

import org.w3c.dom.*;

/**
 * Command processor for MeeshQuest, Part 1, Fall 2019. Provides methods for
 * processing XML elements and parameters for the various commands.
 *
 */
public class CommandHandler {

	// Command names
	private final static String CREATE_CITY = "createCity";
	private final static String DELETE_CITY = "deleteCity";
	private final static String LIST_CITIES = "listCities";
	private final static String CLEAR_ALL = "clearAll";
	private final static String PRINT_KDTREE = "printKdTree";
	private final static String PRINT_BSTREE = "printBinarySearchTree";
	private final static String NEAR = "nearestNeighbor";

	// Parameter lists for various commands
	private final static String[] CREATE_CITY_PARAMS = { "name", "x", "y", "radius", "color" };
	private final static String[] DELETE_CITY_PARAMS = { "name" };
	private final static String[] LIST_CITY_PARAMS = { "sortBy" };
	private final static String[] CLEAR_ALL_PARAMS = {};
	private final static String[] PRINT_KDTREE_PARAMS = {};
	private final static String[] PRINT_BSTREE_PARAMS = {};
	private final static String[] NEAR_PARAMS = { "x", "y" };

	// Parameter values for "listCities" command
	private final static String SORT_BY_NAME = "name";
	private final static String SORT_BY_COORDINATE = "coordinate";

	private Document resultsDoc; // document into which results will be written
	private Element resultsRoot; // root element of resultsDoc
	private BinarySearchTree<City> bsTreeByName; // binary-search tree (sorted by name)
	private SGTree<City> sgTreeByCoordinate; // SG tree (sorted by coordinate)
	private float mapWidth;
	private float mapHeight;

	/**
	 * Main constructor from the results document and size of the map.
	 * 
	 * @param resultsDoc XML document containing the results
	 * @param mapWidth   Width of the map (x-extent)
	 * @param mapHeight  Height of the map (y-extent)
	 */
	public CommandHandler(Document resultsDoc, float mapWidth, float mapHeight) {
		this.mapWidth = mapWidth;
		this.mapHeight = mapHeight;
		this.resultsDoc = resultsDoc;
		this.resultsRoot = resultsDoc.createElement("results");
		resultsDoc.appendChild(resultsRoot); // root element
		// create the associated tree structures
		this.bsTreeByName = new BinarySearchTree<City>(new OrderByName<City>(), resultsDoc);
		this.sgTreeByCoordinate = new SGTree<City>( resultsDoc);
		
	}

	/**
	 * Takes a map containing set of parameter name-value pairs and appends a list
	 * of parameter elements. This is used in the success and error handlers to
	 * generate a summary of the command parameters.
	 * 
	 * @param paramNames Names of the parameters to be output
	 * @param inParams   A map of parameter name-value pairs
	 * @param outParams  The output element for the attribute values
	 */
	private void applyInParamsToOutParams(String[] paramNames, NamedNodeMap inParams, Element outParams) {
		for (int i = 0; i < paramNames.length; ++i) {
			Attr inParam = (Attr) inParams.getNamedItem(paramNames[i]);
			Element outParam = resultsDoc.createElement(inParam.getName());
			outParam.setAttribute("value", inParam.getValue());
			outParams.appendChild(outParam);
		}
	}

	/**
	 * Obtain the parameter list for a given command. Throws exception if the
	 * command name is not recognized.
	 * 
	 * @param cmd The command
	 * @return An array containing the list of associated parameters
	 */
	private String[] paramsListFromCmd(Element cmd) throws UnsupportedOperationException {
		switch (cmd.getNodeName()) {
		case CREATE_CITY:
			return CREATE_CITY_PARAMS;
		case DELETE_CITY:
			return DELETE_CITY_PARAMS;
		case LIST_CITIES:
			return LIST_CITY_PARAMS;
		case CLEAR_ALL:
			return CLEAR_ALL_PARAMS;
		case PRINT_KDTREE:
			return PRINT_KDTREE_PARAMS;
		case PRINT_BSTREE:
			return PRINT_BSTREE_PARAMS;
		case NEAR:
			return NEAR_PARAMS;
		default:
			throw new UnsupportedOperationException("unknown command: `" + cmd.getNodeName() + "`");
		}
	}

	/**
	 * Generates an error output element. It creates a number of elements that
	 * summarize the command. In contrast to handleSuccess, which is given the
	 * output element as a parameter, this function generates its own XML element.
	 * 
	 * @param errType The type of error (stored as the tag of the error element)
	 * @param inCmd   The input command generating this error
	 */
	private void handleError(String errType, Element inCmd) {
		Element outErr = resultsDoc.createElement("error");
		resultsRoot.appendChild(outErr);
		outErr.setAttribute("type", errType);

		Element outCmd = resultsDoc.createElement("command");
		outErr.appendChild(outCmd);
		outCmd.setAttribute("name", inCmd.getNodeName());

		Element outParams = resultsDoc.createElement("parameters");
		outErr.appendChild(outParams);

		NamedNodeMap inParams = inCmd.getAttributes();
		applyInParamsToOutParams(paramsListFromCmd(inCmd), inParams, outParams);
	}

	/**
	 * Generates a success output element. It creates a number of elements that
	 * summarize the command.
	 * 
	 * @param inCmd  The input command generating this error
	 * @param output The output
	 */
	// generates a success node combining the elements "success", "parameters", and
	// "output"
	private void handleSuccess(Element inCmd, Element output) {
		Element outSucc = resultsDoc.createElement("success");
		resultsRoot.appendChild(outSucc);

		Element outCmd = resultsDoc.createElement("command");
		outSucc.appendChild(outCmd);
		outCmd.setAttribute("name", inCmd.getNodeName());

		Element outParams = resultsDoc.createElement("parameters");
		outSucc.appendChild(outParams);

		NamedNodeMap inParams = inCmd.getAttributes();
		applyInParamsToOutParams(paramsListFromCmd(inCmd), inParams, outParams);
		outSucc.appendChild(output);
	}

	/**
	 * Process the createCity command. This checks whether a city of the name or
	 * coordinates exists, and if so it generates the appropriate error element.
	 * Otherwise, it inserts the city into the binary search tree and the SG tree.
	 * 
	 * @param cmd The command element
	 */
	private void createCity(Element cmd) {
		// Parse data
		String name = cmd.getAttribute("name");
		float x = Float.parseFloat(cmd.getAttribute("x"));
		float y = Float.parseFloat(cmd.getAttribute("y"));
		float radius = Float.parseFloat(cmd.getAttribute("radius"));
		String color = cmd.getAttribute("color");

		// Create city
		City city = new City(x, y, name, color, radius);

		if (x > mapWidth || y > mapHeight) {
			handleError("cityOutOfBounds", cmd);
			return;
		}

		// Test whether the city exists (by name or coordinates)
		City altCity = sgTreeByCoordinate.find(city);
		if (altCity != null) {
			handleError("duplicateCityCoordinates", cmd);
			return;
		}
		altCity = bsTreeByName.find(city);
		if (altCity != null) {
			handleError("duplicateCityName", cmd);
			return;
		}

		// Insert the city
		try {
			bsTreeByName.insert(city); // insert into binary search tree
			sgTreeByCoordinate.insert(city); // insert into SG tree
		} catch (Exception e) {
			assert (false); // Huh? Above test should have caught errors
		}

		handleSuccess(cmd, resultsDoc.createElement("output")); // output is trivial
	}

	/**
	 * Process the deleteCity command. This creates a partial city based just on the
	 * name and checks for it in the binary search tree. If it does not exist, and
	 * error is generated. Otherwise, we obtain full city element from the binary
	 * search tree and delete it from both structures.
	 * 
	 * @param cmd The command element
	 */
	private void deleteCity(Element cmd) {
		// Parse data
		String name = cmd.getAttribute("name");
		City nameOnly = new City(name); // Create a bogus city for look-up purposes only
		City city = bsTreeByName.find(nameOnly);
		if (city == null) {
			handleError("cityDoesNotExist", cmd);
			return;
		}

		// Delete the city
		try {
			bsTreeByName.delete(city); // delete city
			sgTreeByCoordinate.delete(city);
		} catch (Exception e) {
			assert (false); // Huh? Above test should have caught error
		}

		Element output = resultsDoc.createElement("output");
		Element deletedCity = resultsDoc.createElement("cityDeleted");
		output.appendChild(deletedCity);
		deletedCity.setAttribute("name", city.getName());
		deletedCity.setAttribute("x", Integer.toString((int) city.getX()));
		deletedCity.setAttribute("y", Integer.toString((int) city.getY()));
		deletedCity.setAttribute("color", city.getColor());
		deletedCity.setAttribute("radius", Integer.toString((int) city.getRadius()));

		handleSuccess(cmd, output);
	}

	/**
	 * Creates the appropriate XML elements to represent a list of cities.
	 * 
	 * @param city     The city to add
	 * @param cityList The list in which to add it
	 */
	private void addCityToListElement(City city, Element cityList) {
		Element cityNode = resultsDoc.createElement("city");
		cityList.appendChild(cityNode);
		cityNode.setAttribute("color", city.getColor());
		cityNode.setAttribute("name", city.getName());
		cityNode.setAttribute("radius", Integer.toString((int) city.getRadius()));
		cityNode.setAttribute("x", Integer.toString((int) city.getX()));
		cityNode.setAttribute("y", Integer.toString((int) city.getY()));
	}

	/**
	 * Produce a list of cities in the results document. If the list is empty, a
	 * "noCitiesToList" error is generated. If the "sortBy" parameter is not
	 * recognized, an UnsupportedOperation exception is thrown.
	 * 
	 * @param cmd The XML element for this command
	 */
	private void listCities(Element cmd) throws UnsupportedOperationException {
		// Handle empty-tree error (in theory, both trees have the same size)
		if (sgTreeByCoordinate.size() == 0 || bsTreeByName.size() == 0) {
			handleError("noCitiesToList", cmd);
			return;
		}

		String sortBy = cmd.getAttribute("sortBy"); // how to sort the list

		// Construct output
		Element output = resultsDoc.createElement("output"); // create output document
		Element cityList = resultsDoc.createElement("cityList"); // create element containing cities
		output.appendChild(cityList);
		if (sortBy.equals(SORT_BY_NAME)) { // enumerate cities in order by name
			for (City city : bsTreeByName.entryList()) { // get entries from the binary search tree
				addCityToListElement(city, cityList);
			}
		}
		else {
			throw new UnsupportedOperationException("unknown sort method: `" + sortBy + "`");
		}

		handleSuccess(cmd, output); // generate an appropriate success node
	}

	/**
	 * Removes all cities from the dictionaries. This command always succeeds.
	 * 
	 * @param cmd The XML element for this command
	 */
	private void clearAll(Element cmd) {
		sgTreeByCoordinate.clear();
		bsTreeByName.clear();
		handleSuccess(cmd, resultsDoc.createElement("output")); // output is empty
	}

	/**
	 * Prints the binary search tree to the results document. If the tree is empty,
	 * a "mapIsEmpty" error results.
	 * 
	 * @param cmd The XML element for this command
	 */
	private void printBSTree(Element cmd) {
		// Handle empty-tree error
		if (bsTreeByName.size() == 0) {
			handleError("mapIsEmpty", cmd);
			return;
		}
		// Construct output
		Element output = resultsDoc.createElement("output");
		bsTreeByName.print(output);

		handleSuccess(cmd, output);
	}

	private void printKDTree(Element cmd) {
		// Handle empty-tree error
		if (sgTreeByCoordinate.size() == 0) {
			handleError("mapIsEmpty", cmd);
			return;
		}
		// Construct output
		Element output = resultsDoc.createElement("output");
		sgTreeByCoordinate.print(output);

		handleSuccess(cmd, output);
	}

	private void nearestN(Element cmd) throws Exception {
		int x = Integer.parseInt(cmd.getAttribute("x")); 
		int y = Integer.parseInt(cmd.getAttribute("y")); 

		if (x > mapWidth || y > mapHeight) {
			handleError("queryOutOfBounds", cmd);
		}

		else if (sgTreeByCoordinate.size() == 0) {
			handleError("mapIsEmpty", cmd);
		}
		else { 
			City c = new City(x, y, "", "", 0);
			City res = (City) sgTreeByCoordinate.nearNS(c);
	
			Element output = resultsDoc.createElement("output"); 
	
			Element resN = resultsDoc.createElement("nearestNeighbor");
			
			resN.setAttribute("x", Integer.toString((int) res.getX()));
			resN.setAttribute("y", Integer.toString((int) res.getY()));
			resN.setAttribute("color", res.getColor());
			resN.setAttribute("name", res.getName());
			resN.setAttribute("radius", Integer.toString((int) res.getRadius()));
			output.appendChild(resN);
			handleSuccess(cmd, output);
		}
	}

	/**
	 * Process one command. This invokes the appropriate function for processing a
	 * command. Throws an exception if the command in not valid (which should never
	 * happen).
	 * 
	 * @param cmd The command to process
	 * @throws Exception
	 */
	public void handleCommand(Element cmd) throws Exception {
		switch (cmd.getNodeName()) {
		case CREATE_CITY:
			createCity(cmd);
			break;
		case DELETE_CITY:
			deleteCity(cmd);
			break;
		case LIST_CITIES:
			listCities(cmd);
			break;
		case CLEAR_ALL:
			clearAll(cmd);
			break;
		case PRINT_KDTREE:
			printKDTree(cmd);
			break;
		case PRINT_BSTREE:
			printBSTree(cmd);
			break;
		case NEAR:
			nearestN(cmd);
			break;
		default:
			throw new UnsupportedOperationException("unknown command: `" + cmd.getNodeName() + "`");
		}
	}
}
