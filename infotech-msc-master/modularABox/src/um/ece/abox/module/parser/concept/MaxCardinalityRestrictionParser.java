package um.ece.abox.module.parser.concept;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.*;


import um.ece.abox.module.condition.ConditionChecker;

public class MaxCardinalityRestrictionParser extends ConceptParser {

	public MaxCardinalityRestrictionParser() {
		this.fillers = new HashMap<OWLObjectPropertyExpression, Set<OWLClassExpression>>();
		this.lhsConcepts = new HashMap<OWLObjectPropertyExpression, Set<OWLClassExpression>>();
		this.minCardinalities = new HashMap<OWLObjectPropertyExpression, Integer>();
	}
	
	 
	public void parseAsLHSConcept(OWLClassExpression concept, OWLSubClassOfAxiom gci) {
		// Do nothing
		return;
	}

	 
	public void parseAsRHSConcept(OWLClassExpression concept, OWLSubClassOfAxiom gci) {
		
		OWLObjectMaxCardinality c = (OWLObjectMaxCardinality) concept;
		OWLObjectPropertyExpression role = c.getProperty();
		OWLClassExpression filler = c.getFiller();
		OWLClassExpression subclass = gci.getSubClass();
		int card = c.getCardinality();
		
		if (filler.isOWLThing() || subclass.isOWLThing() 
				|| filler.isOWLNothing() || subclass.isOWLNothing())
			return;
		
		Set<OWLClassExpression> fillers = this.fillers.get(role);
		Set<OWLClassExpression> lhs = this.lhsConcepts.get(role);
		Integer minCard = this.minCardinalities.get(role);
		
		if (fillers == null)
			fillers = new HashSet<OWLClassExpression>();
		fillers.add(filler);

		if (lhs == null)
			lhs = new HashSet<OWLClassExpression>();
		lhs.add(subclass);
		
		minCard = (minCard == null) ? card : Math.min(card, minCard);
		

		this.fillers.put(role, fillers);
		this.lhsConcepts.put(role, lhs);
		this.minCardinalities.put(role, minCard);		
	}

	 
	public void accept(ConditionChecker checker) {
		checker.visit(this);
		
	}

}
