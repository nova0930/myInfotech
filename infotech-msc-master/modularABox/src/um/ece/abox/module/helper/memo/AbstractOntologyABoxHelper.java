package um.ece.abox.module.helper.memo;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;

import um.ece.abox.module.helper.OntologyABoxHelper;
import um.ece.abox.module.model.ABoxModule;
import um.ece.abox.module.model.SimpleABoxModule;



public abstract class AbstractOntologyABoxHelper implements OntologyABoxHelper {

	
	public ABoxModule getConnectedComponent(OWLNamedIndividual ind) {

		Set<OWLAxiom> component = new HashSet<OWLAxiom>();
		Set<OWLNamedIndividual> visisted = new HashSet<OWLNamedIndividual>();
		List<OWLNamedIndividual> queue = new LinkedList<OWLNamedIndividual>();
		queue.add(ind);
		visisted.add(ind);
		
		while (!queue.isEmpty()) {
			
			OWLNamedIndividual in = queue.remove(0); 
			Set<OWLAxiom> axioms = (Set<OWLAxiom>) this.getAssertions(in);
			component.addAll(axioms);
		
			// get adjacent nodes
			Set<OWLNamedIndividual> neighbors = (Set<OWLNamedIndividual>) this.getNeighbors(in);
			// remove visited entities.
			neighbors.removeAll(visisted);
			queue.addAll(neighbors);
			visisted.addAll(neighbors);
		}
		
		ABoxModule m = new SimpleABoxModule(visisted, component);		
		return m;
	}
	
	
	public OWLObjectPropertyExpression getDirectedRole(
			OWLObjectPropertyAssertionAxiom ax, OWLNamedIndividual ind) {

		OWLObjectPropertyExpression role = ax.getProperty();
		OWLNamedIndividual sub = ax.getSubject().asOWLNamedIndividual();
		return (sub.equals(ind)) ? role : role.getInverseProperty().getSimplified();
	}

	public OWLNamedIndividual getNeighbor(
			OWLObjectPropertyAssertionAxiom ax, OWLNamedIndividual ind) {
		
		OWLNamedIndividual sub = ax.getSubject().asOWLNamedIndividual();
		OWLNamedIndividual obj = ax.getObject().asOWLNamedIndividual();
		return (sub.equals(ind)) ? obj : sub;
	}

	
}
