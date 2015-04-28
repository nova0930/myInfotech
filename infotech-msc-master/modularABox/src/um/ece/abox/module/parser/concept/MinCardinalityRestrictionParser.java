package um.ece.abox.module.parser.concept;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.*;

import um.ece.abox.module.condition.ConditionChecker;

public class MinCardinalityRestrictionParser extends ConceptParser {
	
	public MinCardinalityRestrictionParser() {
		this.fillers = new HashMap<OWLObjectPropertyExpression, Set<OWLClassExpression>>();
		this.rhsConcepts = new HashMap<OWLObjectPropertyExpression, Set<OWLClassExpression>>();
		this.minCardinalities = new HashMap<OWLObjectPropertyExpression, Integer>();
	}
	
	 
	public void parseAsLHSConcept(OWLClassExpression concept, OWLSubClassOfAxiom gci) {
		
		OWLObjectMinCardinality c = (OWLObjectMinCardinality) concept;
		OWLObjectPropertyExpression role = c.getProperty();
		OWLClassExpression filler = c.getFiller();
		OWLClassExpression superclass = gci.getSuperClass();
		int card = c.getCardinality();
		
		// if r.h.s. concept is the top entity, do nothing
		if (superclass.isOWLThing() || filler.isOWLThing()
				|| superclass.isOWLNothing() || filler.isOWLNothing())
			return; // superclass.isAnonymous() ||
		
		Set<OWLClassExpression> fillers = this.fillers.get(role);
		Set<OWLClassExpression> rhs = this.rhsConcepts.get(role);
		Integer minCard = this.minCardinalities.get(role);
		
		if (fillers == null)
			fillers = new HashSet<OWLClassExpression>();
		fillers.add(filler);

		if (rhs == null)
			rhs = new HashSet<OWLClassExpression>();
		rhs.add(superclass);
		
		minCard = (minCard == null) ? card : Math.min(card, minCard);
		
		this.fillers.put(role, fillers);
		this.rhsConcepts.put(role, rhs);
		this.minCardinalities.put(role, minCard);	
	}

	 
	public void parseAsRHSConcept(OWLClassExpression concept, OWLSubClassOfAxiom gci) {
		// if \geq nR.C in r.h.s., do nothing
		return;
	}

	 
	public void accept(ConditionChecker checker) {
		checker.visit(this);
		
	}

}
