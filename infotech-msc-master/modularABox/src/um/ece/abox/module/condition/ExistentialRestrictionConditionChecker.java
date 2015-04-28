package um.ece.abox.module.condition;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.*;

import um.ece.abox.module.helper.OntologyABoxHelper;
import um.ece.abox.module.helper.OntologyRBoxHelper;
import um.ece.abox.module.parser.concept.*;

public class ExistentialRestrictionConditionChecker extends AbstractConditionChecker {
	
	protected Map<OWLObjectPropertyExpression, Set<OWLClassExpression>> fillers;
	protected Map<OWLObjectPropertyExpression, Set<OWLClassExpression>> rhsConcepts;
	
	ConditionChecker container;
	public ExistentialRestrictionConditionChecker(OntologyABoxHelper ah,
									OntologyRBoxHelper rh, ConditionChecker c) {
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
		
		// get all rhs concepts
		Set<OWLClassExpression> involvedRHSClasses = 
								container.getInvolvedClasses(involvedRoles, this.rhsConcepts);
		// get all filler classes
		Set<OWLClassExpression>  fillerClass = 
								container.getInvolvedClasses(involvedRoles, this.fillers);
		
		// get all named classes of the individuals 
		Set<OWLClassExpression> subClasses = this.aboxHelper.getClasses(sub);
		Set<OWLClassExpression> objClasses = this.aboxHelper.getClasses(obj);
		
		// Condition for returning False (one of them satisfied):
		// 1. sub classes are subsumed by every RHS concept.
		// 2. sub classes are disjoint from every RHS concept.
		// 3. obj classes are disjoint from every Filler Concept.
		return !container.isSubclassOf(subClasses, involvedRHSClasses) 
				&& !container.isDisjointFrom(subClasses, involvedRHSClasses)
				&& !container.isDisjointFrom(objClasses, fillerClass)
				;
	}
	
	

	public void visit(ExistentialRestrictionParser parser) {
		this.fillers = parser.getFillers();
		this.rhsConcepts = parser.getRHSConcepts();
	}


	public void visit(UniversalRestrictionParser parser) {}


	public void visit(MaxCardinalityRestrictionParser parser) {}


	public void visit(MinCardinalityRestrictionParser parser) {}


	public void visit(ExactCardinalityRestrictionParser parser) {}


	public void visit(ConceptParser parser) {}



	public void visit(QueryConceptParser parser) {
		// TODO Auto-generated method stub
		
	}

}
