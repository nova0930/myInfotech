package um.ece.abox.module.condition;

import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;

import um.ece.abox.module.parser.concept.*;


/**
 * A condition checker to verify whether a given role assertion is
 * essential to the inference of some individual classification.
 * 
 * @author Jia
 *
 */
public interface ConditionChecker {
	
	public static final int EXIST_RESTRICTION = 1, 
							FORALL_RESTRICTION = 2, 
							MIN_CARDINALITY_RESTRICTION = 3,
							MAX_CARDINALITY_RESTRICTION = 4,
							INDIVIDUAL_IDENTIFICATION = 5,  
							QUERY_SPECIFIC = 6,
							OTHER = 7;//, ALL = 4, NONE = 0;
	
	
	/**
	 * Check conditions to decide if it is possible for the given role assertion to affect
	 * classification of the given individual (i.e. target individual).
	 * 
	 * @param assertion
	 * @param target
	 */
	public boolean checkCondition(OWLObjectPropertyAssertionAxiom assertion, OWLNamedIndividual target);
	
	
	/**
	 * Visit Concept Parsers to access information that has been collected by them.
	 * @param conParser
	 */
	public void visit(ConceptParser parser);
	
	public void visit(ExistentialRestrictionParser parser);
	
	public void visit(UniversalRestrictionParser parser);
	
	public void visit(MaxCardinalityRestrictionParser parser);
	
	public void visit(MinCardinalityRestrictionParser parser);
	
	public void visit(ExactCardinalityRestrictionParser parser);
	
	public void visit(QueryConceptParser parser);

}
