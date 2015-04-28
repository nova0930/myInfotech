package um.ece.abox.module.helper.memo;

import java.util.Iterator;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import um.ece.abox.module.helper.OntologyRBoxHelper;
import um.ece.abox.module.util.MyReasonerFactory;

/**
 * Ontology RBox helper for DL SHIQ.
 * @author Jia
 *
 */
public class SHIQOntologyRBoxHelper implements OntologyRBoxHelper {

	OWLOntology rbox;
	OWLOntologyManager man;
	OWLReasoner reasoner;
	
	public SHIQOntologyRBoxHelper(OWLOntology ont) {
		Set<OWLAxiom> axioms = ont.getRBoxAxioms(true);
		man = OWLManager.createOWLOntologyManager();
		this.rbox = new SHIQOntologyHelper().getRBoxAsOntology(axioms, man);
		this.reasoner = MyReasonerFactory.getNonBufferingReasoner(rbox);
		this.reasoner.precomputeInferences(InferenceType.OBJECT_PROPERTY_HIERARCHY);
	}
	
	 
	public Set<OWLObjectPropertyExpression> getParentRoles(
							OWLObjectPropertyExpression role, boolean named) {
		
		Set<OWLObjectPropertyExpression> parents = 
					reasoner.getSuperObjectProperties(role, false).getFlattened();
		Set<OWLObjectPropertyExpression> equivalencies = 
					reasoner.getEquivalentObjectProperties(role).getEntities();
		parents.addAll(equivalencies);
		parents.add(role.getSimplified());
		
		if (!named)
			return parents;
		
		// filter out anonymous roles
		Iterator<OWLObjectPropertyExpression> it = parents.iterator();
		while (it.hasNext())
			if (it.next().isAnonymous())
				it.remove();
		
		return parents;
	}

	 
	public Set<OWLObjectPropertyExpression> getTransParentRoles(
							OWLObjectPropertyExpression role, boolean named) {
		
		Set<OWLObjectPropertyExpression> parents = this.getParentRoles(role, named);
		
		// filter out non-transitive roles
		Iterator<OWLObjectPropertyExpression> it = parents.iterator();
		while (it.hasNext()) {
			OWLObjectPropertyExpression r = it.next();
			if (!this.isTransitive(r) || r.isTopEntity())
				it.remove();
		}
		
		return parents;
	}

	 
	public boolean isSubRoleOf(OWLObjectPropertyExpression child,
								OWLObjectPropertyExpression parent) {
		
		OWLDataFactory df = man.getOWLDataFactory();
		OWLAxiom ax = df.getOWLSubObjectPropertyOfAxiom(child, parent);
	//	System.out.println(ax);
		try {
			return this.reasoner.isEntailed(ax);
		} catch (Exception e) {
			//e.printStackTrace();
		//	System.err.println("Exception in isSubRoleOf.");
		//	System.err.println(ax);
			return false;
		} 	
	}

	 
	public boolean isTransitive(OWLObjectPropertyExpression role) {
		return !rbox.getTransitiveObjectPropertyAxioms(role).isEmpty();
	}

	 
	public boolean isFunctional(OWLObjectPropertyExpression role) {
		Set<OWLObjectPropertyExpression> parents = this.getParentRoles(role, false);
		for (OWLObjectPropertyExpression r : parents)
			if (this._isFunctional(r))
				return true;
		return false;
	}

	 
	public boolean isInverseFunctional(OWLObjectPropertyExpression role) {
		Set<OWLObjectPropertyExpression> parents = this.getParentRoles(role, false);
		for (OWLObjectPropertyExpression r : parents)
			if (this._isInverseFunctional(r))
				return true;
		return false;
	}
	
	private boolean _isFunctional(OWLObjectPropertyExpression op) {
		return !this.rbox.getFunctionalObjectPropertyAxioms(op).isEmpty();
	}
	
	private boolean _isInverseFunctional(OWLObjectPropertyExpression op) {
		return !this.rbox.getInverseFunctionalObjectPropertyAxioms(op).isEmpty();
	}

}
