package um.ece.abox.module.parser.concept;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.*;

import um.ece.abox.module.condition.ConditionChecker;

public class UniversalRestrictionParser extends ConceptParser {

	public UniversalRestrictionParser() {
		this.fillers = new HashMap<OWLObjectPropertyExpression, Set<OWLClassExpression>>();
		this.lhsConcepts = new HashMap<OWLObjectPropertyExpression, Set<OWLClassExpression>>();
	}
	
	 
	public void parseAsLHSConcept(OWLClassExpression concept, OWLSubClassOfAxiom gci) {
		return;
	}

	 
	public void parseAsRHSConcept(OWLClassExpression concept, OWLSubClassOfAxiom gci) {
		
		OWLObjectAllValuesFrom c = (OWLObjectAllValuesFrom) concept;
		OWLObjectPropertyExpression role = c.getProperty();
		OWLClassExpression filler = c.getFiller();
		OWLClassExpression subclass = gci.getSubClass();
		
		if (filler.isOWLThing() || subclass.isOWLThing()
				|| filler.isOWLNothing() || subclass.isOWLNothing())
			return;
		
		Set<OWLClassExpression> fillers = this.fillers.get(role);
		Set<OWLClassExpression> lhs = this.lhsConcepts.get(role);
		
		if (fillers == null)
			fillers = new HashSet<OWLClassExpression>();
		fillers.add(filler);

		if (lhs == null)
			lhs = new HashSet<OWLClassExpression>();
		lhs.add(subclass);
		
		this.fillers.put(role, fillers);
		this.lhsConcepts.put(role, lhs);
	}

	 
	public void accept(ConditionChecker checker) {
		checker.visit(this);
	}

}
