package um.ece.abox.module.parser.concept;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLQuantifiedObjectRestriction;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import um.ece.abox.module.condition.ConditionChecker;

public class QueryConceptParser extends ConceptParser {

	public QueryConceptParser() {
		this.fillers = new HashMap<OWLObjectPropertyExpression, Set<OWLClassExpression>>();
								   
	}
	
	public void parseQuery(OWLClassExpression query) {
		
		if (query == null)
			return; 
		
		this.fillers.clear();
		
		Set<OWLClassExpression> juncts = ConceptParser.getConceptJuncts(query);
		for (OWLClassExpression cls : juncts) {
			ClassExpressionType type = cls.getClassExpressionType();
			if (type.equals(ClassExpressionType.OBJECT_ALL_VALUES_FROM)
					|| type.equals(ClassExpressionType.OBJECT_SOME_VALUES_FROM)
					|| type.equals(ClassExpressionType.OBJECT_MAX_CARDINALITY)
					|| type.equals(ClassExpressionType.OBJECT_MIN_CARDINALITY)) {
				
				OWLObjectPropertyExpression r =	((OWLQuantifiedObjectRestriction) cls).getProperty();
				this.fillers.put(r, new HashSet<OWLClassExpression>());
			}
		}
	}
	
	 
	public void parseAsLHSConcept(OWLClassExpression concept, OWLSubClassOfAxiom gci) {}

	 
	public void parseAsRHSConcept(OWLClassExpression concept, OWLSubClassOfAxiom gci) {}

	 
	public void accept(ConditionChecker checker) {
		checker.visit(this);
	}

}
