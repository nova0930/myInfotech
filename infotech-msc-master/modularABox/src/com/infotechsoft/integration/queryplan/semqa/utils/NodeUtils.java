package com.infotechsoft.integration.queryplan.semqa.utils;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_Blank;
import com.hp.hpl.jena.graph.Node_Literal;
import com.hp.hpl.jena.graph.Node_URI;
import com.hp.hpl.jena.sparql.core.Var;
import com.infotechsoft.integration.queryplan.semqa.exception.SemQAException;

/**
 * Represents a singleton.
 */

public class NodeUtils {

	/**
	 * Holds singleton instance
	 */
	private static NodeUtils instance;

	/**
	 * prevents instantiation
	 */
	private NodeUtils() {
		// prevent creation
	}

	/**
	 * Returns the singleton instance.
	 @return	the singleton instance
	 */
	static public NodeUtils getInstance() {
		if (instance == null) {
			instance = new NodeUtils();
		}
		return instance;
	}
	public static Node copyNode(Node in) throws SemQAException {
		// a procedure to clone Node objects
		if (in instanceof Node_URI) {
			return Node.createURI(in.getURI());
		}
		else if (in instanceof Node_Literal) {
			return Node.createLiteral(in.getLiteralLexicalForm());
		}
		else if (in instanceof Var) {
			return Var.alloc(((Var) in).getVarName());
		}
		else if (in instanceof Node_Blank) {
			return Node.createAnon(); // label does not matter in blank nodes - they are just that
		}
		else { // unknown node type
			throw new SemQAException(": unknown node type");
		}
	}
}