package um.ece.abox.module.condition;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLClassExpression;
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

public class UniversalRestrictionConditionChecker extends AbstractConditionChecker {

	protected Map<OWLObjectPropertyExpression, Set<OWLClassExpression>> fillers;
	protected Map<OWLObjectPropertyExpression, Set<OWLClassExpression>> lhsConcepts;
	ConditionChecker container;
	
	public UniversalRestrictionConditionChecker(OntologyABoxHelper ah,
												OntologyRBoxHelper rh,
												ConditionChecker c) {
		super(ah, rh);
		this.container = c;
		this.fillers = new HashMap<OWLObjectPropertyExpression, Set<OWLClassExpression>>();
	}

	 
	public boolean checkCondition(OWLObjectPropertyExpression role,
									OWLNamedIndividual sub, OWLNamedIndividual obj) {
		
		SyntacticConditionChecker container = (SyntacticConditionChecker) this.container;
		// get all roles that are used for concept definition
		Set<OWLObjectPropertyExpression> roles = this.fillers.keySet();
		
		// get all parents roles of the role that are used for concept definition
		Set<OWLObjectPropertyExpression> involvedRoles = 
								container.getInvolvedParentRoles(role, roles);
		
		if (involvedRoles.size() < 1)
			return false;
		
		// get all lhs concepts
		Set<OWLClassExpression> involvedLHSClasses = 
								container.getInvolvedClasses(involvedRoles, this.lhsConcepts);
		// get all filler classes
		Set<OWLClassExpression>  fillerClass = 
								container.getInvolvedClasses(involvedRoles, this.fillers);
		
		// get all named classes of the individuals 
		Set<OWLClassExpression> subClasses = this.aboxHelper.getClasses(sub);
		Set<OWLClassExpression> objClasses = this.aboxHelper.getClasses(obj);
		
		// Condition for returning False (one of them satisfied):
		// 1. sub classes are disjoint from every LHS concept.
		// 2. obj classes are subsumed by every Filler concept.
		// 3. obj classes are disjoint from every Filler Concept.
		return  !container.isDisjointFrom(subClasses, involvedLHSClasses)
				&& !container.isSubclassOf(objClasses, fillerClass)
				&& !container.isDisjointFrom(objClasses, fillerClass)
				;
		
	}
	
	 
	public void visit(ExistentialRestrictionParser parser) {}

	 
	public void visit(UniversalRestrictionParser parser) {
		this.fillers = parser.getFillers();
		this.lhsConcepts = parser.getLHSConcepts();
	}

	 
	public void visit(MaxCardinalityRestrictionParser parser) {
		// TODO Auto-generated method stub
		
	}

	 
	public void visit(MinCardinalityRestrictionParser parser) {}

	 
	public void visit(ExactCardinalityRestrictionParser parser) {}

	 
	public void visit(ConceptParser parser) {}

	 
	public void visit(QueryConceptParser parser) {
		// TODO Auto-generated method stub
		
	}

	

}
