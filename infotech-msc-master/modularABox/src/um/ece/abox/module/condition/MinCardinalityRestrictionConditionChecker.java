package um.ece.abox.module.condition;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;

import com.google.common.collect.Iterables;

import um.ece.abox.module.helper.OntologyABoxHelper;
import um.ece.abox.module.helper.OntologyRBoxHelper;
import um.ece.abox.module.parser.concept.ConceptParser;
import um.ece.abox.module.parser.concept.ExactCardinalityRestrictionParser;
import um.ece.abox.module.parser.concept.ExistentialRestrictionParser;
import um.ece.abox.module.parser.concept.MaxCardinalityRestrictionParser;
import um.ece.abox.module.parser.concept.MinCardinalityRestrictionParser;
import um.ece.abox.module.parser.concept.QueryConceptParser;
import um.ece.abox.module.parser.concept.UniversalRestrictionParser;

public class MinCardinalityRestrictionConditionChecker extends AbstractConditionChecker {

	protected Map<OWLObjectPropertyExpression, Set<OWLClassExpression>> fillers;
	protected Map<OWLObjectPropertyExpression, Set<OWLClassExpression>> rhsConcepts;
	protected Map<OWLObjectPropertyExpression, Integer> minCardinalities;
	ConditionChecker container;
	
	public MinCardinalityRestrictionConditionChecker(OntologyABoxHelper ah,
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
		
		// check if the subject has enough R-neighbors
		boolean hasEnoughNeighbors = false;
		for (OWLObjectPropertyExpression r : involvedRoles) {
			int neighbors = Iterables.size(this.aboxHelper.getNeighbors(sub, r));
			int minCard = this.minCardinalities.get(r);
			if (neighbors >= minCard) {
				hasEnoughNeighbors = true;
				break;
			}
		}
		
		if (!hasEnoughNeighbors)
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
		// 4. sub does not have enough neighbors
		return !container.isSubclassOf(subClasses, involvedRHSClasses)
				&& !container.isDisjointFrom(subClasses, involvedRHSClasses)
				&& !container.isDisjointFrom(objClasses, fillerClass)
				;	
	}


	public void visit(ExistentialRestrictionParser parser) {}


	public void visit(UniversalRestrictionParser parser) {}


	public void visit(MaxCardinalityRestrictionParser parser) {}

	 
	public void visit(MinCardinalityRestrictionParser parser) {
		this.fillers = parser.getFillers();
		this.rhsConcepts = parser.getRHSConcepts();
		this.minCardinalities = parser.getMinCardinality();
	}

	 
	public void visit(ExactCardinalityRestrictionParser parser) {}

	 
	public void visit(ConceptParser parser) {}

	 
	public void visit(QueryConceptParser parser) {
		// TODO Auto-generated method stub
		
	}
}
