package um.ece.abox.module.parser.concept;

import java.util.*;

import org.semanticweb.owlapi.model.*;

import um.ece.abox.module.condition.ConditionChecker;

public class ExistentialRestrictionParser extends ConceptParser {

	public ExistentialRestrictionParser() {
		this.fillers = new HashMap<OWLObjectPropertyExpression, Set<OWLClassExpression>>();
		this.rhsConcepts = new HashMap<OWLObjectPropertyExpression, Set<OWLClassExpression>>();
	}
	
	 
	public void parseAsLHSConcept(OWLClassExpression concept, OWLSubClassOfAxiom gci) {
		
		OWLObjectSomeValuesFrom c = (OWLObjectSomeValuesFrom)concept;
		OWLObjectPropertyExpression role = c.getProperty();
		OWLClassExpression filler = c.getFiller();
		OWLClassExpression superclass = gci.getSuperClass();	
		
		// if r.h.s. concept is the top entity, do nothing
		if (superclass.isOWLThing() || filler.isOWLThing()
				|| superclass.isOWLNothing() || filler.isOWLNothing())
			return; // superclass.isAnonymous() ||
		
		Set<OWLClassExpression> fillers = this.fillers.get(role);
		Set<OWLClassExpression> rhs = this.rhsConcepts.get(role);
		
		if (fillers == null)
			fillers = new HashSet<OWLClassExpression>();
		fillers.add(filler);

		if (rhs == null)
			rhs = new HashSet<OWLClassExpression>();
		rhs.add(superclass);
		
		this.fillers.put(role, fillers);
		this.rhsConcepts.put(role, rhs);
	}

	 
	public void parseAsRHSConcept(OWLClassExpression concept, OWLSubClassOfAxiom gci) {
		// do nothing if \eixists R.C in r.h.s.
		return;
	}

	 
	public void accept(ConditionChecker checker) {
		checker.visit(this);
	}

}
