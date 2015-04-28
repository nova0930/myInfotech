package um.ece.abox.module.condition;

import java.util.*;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import um.ece.abox.module.helper.*;
import um.ece.abox.module.helper.memo.SHIQOntologyHelper;
import um.ece.abox.module.parser.concept.*;
import um.ece.abox.module.util.MyReasonerFactory;

public class SyntacticConditionChecker extends AbstractConditionChecker {

	Map<Integer, ConditionChecker> checkers;
	Set<OWLObjectPropertyExpression> roles;
	OWLOntologyManager man;
	OWLOntology tbox;
	OWLReasoner reasoner;
	OWLDataFactory df;
	
	public SyntacticConditionChecker(OWLOntology ont, OntologyABoxHelper ah, OntologyRBoxHelper rh) {
		
		super(ah, rh);
		
		this.man = OWLManager.createOWLOntologyManager();
		this.tbox = new SHIQOntologyHelper().getTBoxAsOntology(ont.getAxioms(), man);
		this.reasoner = MyReasonerFactory.getNonBufferingReasoner(tbox);
		this.reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
		this.df = man.getOWLDataFactory();
		this.roles = new HashSet<OWLObjectPropertyExpression>();
		
		this.checkers = new HashMap<Integer, ConditionChecker>();
		
		this.checkers.put(ConditionChecker.EXIST_RESTRICTION, 
							new ExistentialRestrictionConditionChecker(ah, rh, this));
		this.checkers.put(ConditionChecker.FORALL_RESTRICTION, 
							new UniversalRestrictionConditionChecker(ah, rh, this));
		this.checkers.put(ConditionChecker.MIN_CARDINALITY_RESTRICTION, 
							new MinCardinalityRestrictionConditionChecker(ah, rh, this));
		this.checkers.put(ConditionChecker.MAX_CARDINALITY_RESTRICTION, 
							new MaxCardinalityRestrictionConditionChecker(ah, rh, this));
		this.checkers.put(ConditionChecker.QUERY_SPECIFIC, new QueryConceptChecker(ah, rh, this));
	}
	
	
	
	public boolean checkCondition(OWLObjectPropertyAssertionAxiom assertion,
											OWLNamedIndividual target) {
				
		if (!this.roles.contains(assertion.getProperty().getSimplified())
				&& !this.roles.contains(assertion.getProperty().getInverseProperty().getSimplified()))
			return false;

		// use all registered condition-checker to check
		for (ConditionChecker c : this.checkers.values())
			if (c.checkCondition(assertion, target))
				return true;
		
		return false;
	}
	
	public Set<OWLObjectPropertyExpression> getInvolvedParentRoles(OWLObjectPropertyExpression role,
			Set<OWLObjectPropertyExpression> roles) {

		Set<OWLObjectPropertyExpression> result = new HashSet<OWLObjectPropertyExpression>();
		Set<OWLObjectPropertyExpression> parents = this.rboxHelper.getParentRoles(role, false);

		for (OWLObjectPropertyExpression p : parents)
			if (roles.contains(p))
				result.add(p);
		return result;
	}
	
	public Set<OWLClassExpression> getInvolvedClasses(Set<OWLObjectPropertyExpression> roles,
			Map<OWLObjectPropertyExpression, Set<OWLClassExpression>> map) {
		
		Set<OWLClassExpression> result = new HashSet<OWLClassExpression>();
		for (OWLObjectPropertyExpression r : roles)
			result.addAll(map.get(r));
		return result;
	}

	/**
	 * Test if the conjunction of class set A is subsumed by conjunction of class set B.
	 * i.e.
	 * 				(\sqcap A_i) \sqsubseteq (\sqcap B_i)
	 * @param A
	 * @param B
	 * @return
	 */
	public boolean isSubclassOf(Set<OWLClassExpression> A, Set<OWLClassExpression> B) {
		
		OWLObjectIntersectionOf sub = df.getOWLObjectIntersectionOf(A);
		OWLObjectIntersectionOf sup = df.getOWLObjectIntersectionOf(B);
		OWLAxiom ax = df.getOWLSubClassOfAxiom(sub, sup);
		boolean isEntailed = this.reasoner.isEntailed(ax);
		return isEntailed;
	}
	
	/**
	 * Test if the conjunction of class set A is subsumed by conjunction of negation 
	 * of every class in B. i.e. 
	 *  				(\sqcap A_i) \sqsubseteq (\sqcap \neg B_i)
	 * @param A
	 * @param B
	 * @return
	 */
	public boolean isDisjointFrom(Set<OWLClassExpression> A, Set<OWLClassExpression> B) {
		
		OWLObjectIntersectionOf sub = df.getOWLObjectIntersectionOf(A);
		OWLObjectUnionOf sup = df.getOWLObjectUnionOf(B);
		OWLAxiom ax = df.getOWLSubClassOfAxiom(sub, sup.getComplementNNF());
		boolean isEntailed = this.reasoner.isEntailed(ax);
		return isEntailed;
	}
	
	
	public void registerChecker(int type, ConditionChecker checker) {
		this.checkers.put(type, checker);
	}
	
	public void dropChecker(int type) {
		this.checkers.remove(type);
	}
	
	
	public void clearConditions() {
		
	}

	 
	public void visit(UniversalRestrictionParser parser) {
		ConditionChecker c = this.checkers.get(ConditionChecker.FORALL_RESTRICTION);
		if (c != null) 
			c.visit(parser);
		this.roles.addAll(parser.getFillers().keySet());
	}

	 
	public void visit(ExistentialRestrictionParser parser) {
		ConditionChecker c = this.checkers.get(ConditionChecker.EXIST_RESTRICTION);
		if (c != null)
			c.visit(parser);
		this.roles.addAll(parser.getFillers().keySet());
	}

	 
	public void visit(MaxCardinalityRestrictionParser parser) {
		ConditionChecker c = this.checkers.get(ConditionChecker.MAX_CARDINALITY_RESTRICTION);
		if (c != null)
			c.visit(parser);
		this.roles.addAll(parser.getFillers().keySet());
	}

	 
	public void visit(MinCardinalityRestrictionParser parser) {
		ConditionChecker c = this.checkers.get(ConditionChecker.MIN_CARDINALITY_RESTRICTION);
		if (c != null)
			c.visit(parser);
		this.roles.addAll(parser.getFillers().keySet());
	}

	 
	public void visit(ExactCardinalityRestrictionParser parser) {
		return;
	}
	
	 
	public void visit(QueryConceptParser parser) {
		ConditionChecker c = this.checkers.get(ConditionChecker.QUERY_SPECIFIC);
		if (c != null)
			c.visit(parser);
		this.roles.addAll(parser.getFillers().keySet());
	}

	 
	public void visit(ConceptParser parser) {
		return;
	}


	 
	public boolean checkCondition(OWLObjectPropertyExpression role,
			OWLNamedIndividual sub, OWLNamedIndividual obj) {
		// TODO Auto-generated method stub
		return false;
	}
	
	public OWLReasoner getReasoner() {
		return this.reasoner;
	}


	

	
}
