package um.ece.abox.module.condition;


import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;

import um.ece.abox.module.helper.OntologyABoxHelper;
import um.ece.abox.module.helper.OntologyRBoxHelper;

public abstract class AbstractConditionChecker implements ConditionChecker{

	protected OntologyABoxHelper aboxHelper;
	protected OntologyRBoxHelper rboxHelper;
	
	
	public AbstractConditionChecker(OntologyABoxHelper ah, OntologyRBoxHelper rh) {
		this.aboxHelper = ah;
		this.rboxHelper = rh;
	}
	

	public boolean checkCondition(OWLObjectPropertyAssertionAxiom assertion,
												OWLNamedIndividual target) {
		
		OWLObjectPropertyExpression role = assertion.getProperty();
		OWLNamedIndividual sub = assertion.getSubject().asOWLNamedIndividual();
		OWLNamedIndividual obj = assertion.getObject().asOWLNamedIndividual();
		
		OWLNamedIndividual neighbor = (sub.equals(target)) ? obj : sub;
		OWLObjectPropertyExpression  directedRole = (sub.equals(target)) 
													? role : role.getInverseProperty().getSimplified();
		
		return checkCondition(directedRole, target, neighbor) 
			//	|| checkCondition(directedRole.getInverseProperty(), neighbor, target)
				;	
	}
	
	
	/**
	 * Checking condition. Various optimizations can be applied in this function,
	 * which should be implemented by concrete classes.
	 * @param role
	 * @param sub
	 * @param obj
	 * @return
	 */
	public abstract boolean checkCondition(OWLObjectPropertyExpression role, 
									OWLNamedIndividual sub, OWLNamedIndividual obj);
}
