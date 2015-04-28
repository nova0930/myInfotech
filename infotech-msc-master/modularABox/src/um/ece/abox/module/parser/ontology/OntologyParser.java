package um.ece.abox.module.parser.ontology;

import um.ece.abox.module.condition.ConditionChecker;

public interface OntologyParser {
	
	/**
	 * Parse the ontology to collect information that could be used by
	 * condition checkers for ABox module extraction.
	 * @param ont
	 */
	public void parseOntology();
	
	
	/**
	 * Accept a condition checker to allow it access to the information.
	 * @param checker
	 */
	public void accept(ConditionChecker checker);
}
