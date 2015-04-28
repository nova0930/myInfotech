package um.ece.abox.module.condition;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;

import um.ece.abox.module.helper.OntologyABoxHelper;
import um.ece.abox.module.helper.OntologyRBoxHelper;
import um.ece.abox.module.parser.concept.ConceptParser;
import um.ece.abox.module.parser.concept.ExactCardinalityRestrictionParser;
import um.ece.abox.module.parser.concept.ExistentialRestrictionParser;
import um.ece.abox.module.parser.concept.MaxCardinalityRestrictionParser;
import um.ece.abox.module.parser.concept.MinCardinalityRestrictionParser;
import um.ece.abox.module.parser.concept.QueryConceptParser;
import um.ece.abox.module.parser.concept.UniversalRestrictionParser;

public class QueryConceptChecker extends AbstractConditionChecker {
	
	Set<OWLObjectPropertyExpression> involvedRoles;
	ConditionChecker container;
	
	public QueryConceptChecker(OntologyABoxHelper ah, OntologyRBoxHelper rh, 
									ConditionChecker c) {
		super(ah, rh);
		this.container = c;
		this.involvedRoles = new HashSet<OWLObjectPropertyExpression>();
	}
	
	
	 
	public boolean checkCondition(OWLObjectPropertyExpression role,
								OWLNamedIndividual sub, OWLNamedIndividual obj) {
		
		SyntacticConditionChecker container = (SyntacticConditionChecker) this.container;
		// get all parents roles of the role that are used for concept definition
		return container.getInvolvedParentRoles(role, this.involvedRoles).size() > 0;
	}

	 
	public void visit(ConceptParser parser) {}

	 
	public void visit(ExistentialRestrictionParser parser) {}

	 
	public void visit(UniversalRestrictionParser parser) {}

	 
	public void visit(MaxCardinalityRestrictionParser parser) {}

	 
	public void visit(MinCardinalityRestrictionParser parser) {}

	 
	public void visit(ExactCardinalityRestrictionParser parser) {}

	 
	public void visit(QueryConceptParser parser) {
		this.involvedRoles = parser.getFillers().keySet();
	}

	

}
