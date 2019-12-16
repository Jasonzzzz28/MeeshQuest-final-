package cmsc420.meeshquest.part2;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import cmsc420.xml.XmlUtility;

/**
 * Canonical implementation of MeeshQuest, Part 1, CMSC 420-0201, Fall 2019.
 */
public class MeeshQuest {

// --------------------------------------------------------------------------------------------
//  Uncomment these to read from standard input and output (USE THESE FOR YOUR FINAL SUBMISSION)
//	private static final boolean USE_STD_IO = true; 
//	private static String inputFileName = "";
//	private static String outputFileName = "";
// --------------------------------------------------------------------------------------------
//  Uncomment these to read from a file (USE THESE FOR YOUR TESTING ONLY)
	private static final boolean USE_STD_IO = false;
	private static String inputFileName = "test/mytest-input-5.xml";
	private static String outputFileName = "test/mytest-output-5.xml";
// --------------------------------------------------------------------------------------------

	public static void main(String[] args) throws Exception {

		// configure to read from file rather than standard input/output
		if (!USE_STD_IO) {
			try {
				System.setIn(new FileInputStream(inputFileName));
				System.setOut(new PrintStream(outputFileName));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}

		// results will be stored here
		Document resultsDoc = null;
		try {
			// generate XML document for results
			resultsDoc = XmlUtility.getDocumentBuilder().newDocument();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			return;
		}

		try {
			// validate and parse XML input
			Document input = XmlUtility.validateNoNamespace(System.in);
			// get input document root node
			Element rootNode = input.getDocumentElement();
			final float mapWidth = Float.parseFloat(rootNode.getAttribute("spatialWidth"));
			final float mapHeight = Float.parseFloat(rootNode.getAttribute("spatialHeight"));

			// set up command handler
			final CommandHandler commandHandler = new CommandHandler(resultsDoc, mapWidth, mapHeight);

			// get list of all nodes in document
			final NodeList nl = rootNode.getChildNodes();
			for (int i = 0; i < nl.getLength(); i++) {
				// process only commands (ignore comments)
				if (nl.item(i).getNodeType() == Document.ELEMENT_NODE) {
					// get next command to process
					Element commandNode = (Element) nl.item(i); // (ignore warning - just a skeleton)
					commandHandler.handleCommand(commandNode);
				}
			}
		} catch (SAXException | IOException | ParserConfigurationException e) {
			Element fatalError = resultsDoc.createElement("fatalError");
			resultsDoc.appendChild(fatalError);
		} finally {
			try {
				// print the contents of the your results document
				XmlUtility.print(resultsDoc);
			} catch (TransformerException e) {
				e.printStackTrace();
			}
		}
	}
}
