package um.ece.abox.module.parser.concept;

import java.util.*;

import org.semanticweb.owlapi.model.*;

import um.ece.abox.module.condition.ConditionChecker;

/**
 * A concept parse class that interprets a given complex concept and collects 
 * related syntactic information in the TBox.
 * 
 * @author Jia
 *
 */
public abstract class ConceptParser {
	
	// information to collect when parsing concepts in given GCIs.
	Map<OWLObjectPropertyExpression, Set<OWLClassExpression>> fillers;
	Map<OWLObjectPropertyExpression, Set<OWLClassExpression>> lhsConcepts;
	Map<OWLObjectPropertyExpression, Set<OWLClassExpression>> rhsConcepts;
	Map<OWLObjectPropertyExpression, Integer> minCardinalities;
	
	
	/**
	 * Parse the concept as a one that occurs in the left hand side (lhs) 
	 * of a general concept inclusion axiom (GCI).
	 * @param lhs
	 * @param rhs
	 */
	public abstract void parseAsLHSConcept(OWLClassExpression concept, OWLSubClassOfAxiom gci);
	
	
	/**
	 * Parse the concept as a one that occurs in the right hand side (rhs) 
	 * of a general concept inclusion axiom (GCI).
	 * @param lhs
	 * @param rhs
	 */
	public abstract void parseAsRHSConcept(OWLClassExpression concept, OWLSubClassOfAxiom gci);
	
	
	/**
	 * Implements visitor pattern and accepts a condition-checker, allowing the
	 * checker to collect information used for condition check.
	 * @param checker
	 */
	public abstract void accept(ConditionChecker checker);
	
	
	
	public Map<OWLObjectPropertyExpression, Set<OWLClassExpression>>  getFillers() {
		return this.fillers;
	}
	
	
	public Map<OWLObjectPropertyExpression, Set<OWLClassExpression>>  getLHSConcepts() {
		return this.lhsConcepts;
	}
	
	
	public Map<OWLObjectPropertyExpression, Set<OWLClassExpression>>  getRHSConcepts() {
		return this.rhsConcepts;
	}
	
	public Map<OWLObjectPropertyExpression, Integer> getMinCardinality() {
		return this.minCardinalities;
	}
	
	/**
	 * Interpret Conjunction and Disjunction to get all juncts.
	 * @param concept
	 * @return
	 */
	public static Set<OWLClassExpression> getConceptJuncts(OWLClassExpression concept) {
		
		ClassExpressionType type = concept.getClassExpressionType();
		
		if (type.equals(ClassExpressionType.OBJECT_EXACT_CARDINALITY)) {
			concept = concept.getNNF();
			type = concept.getClassExpressionType();
		}
		
		Set<OWLClassExpression> juncts = new HashSet<OWLClassExpression>();
		
		if (type.equals(ClassExpressionType.OBJECT_INTERSECTION_OF))
			for (OWLClassExpression c : concept.asConjunctSet())
				juncts.addAll(getConceptJuncts(c));
		else if (type.equals(ClassExpressionType.OBJECT_UNION_OF))
			for (OWLClassExpression c : concept.asDisjunctSet())
				juncts.addAll(getConceptJuncts(c));
		else
			juncts.add(concept);
			
		return juncts;
	}
}
