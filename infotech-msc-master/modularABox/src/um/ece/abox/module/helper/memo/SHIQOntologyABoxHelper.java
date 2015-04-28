package um.ece.abox.module.helper.memo;

import java.util.*;

import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.OWLEntityCollector;

import um.ece.abox.module.helper.*;
import um.ece.abox.module.model.ABoxModule;

/**
 * An ontology ABox helper for description logic SHIQ.
 * It is a memory based ABox helper that assumes ABoxes can
 * be loaded into computer memories.
 * @author Jia
 *
 */
public class SHIQOntologyABoxHelper extends AbstractOntologyABoxHelper {
	
	// helpers for convenient ontology operations
	OntologyHelper ontHelper;
	
	// store of abox assertions.
	Set<OWLAxiom> abox;
	Map<OWLNamedIndividual, Set<OWLAxiom>> ind_assertions;
	OWLOntology ont;
	
	// individual collector from assertions. 
	OWLEntityCollector sigCollector;
	Set<OWLEntity> objects;
	
	// for connected components
	Map<OWLNamedIndividual, ABoxModule> modules;
	
	
	public SHIQOntologyABoxHelper(OWLOntology ont) {
		this.ont = ont;
		this.ontHelper = new SHIQOntologyHelper();
		this.abox = ont.getABoxAxioms(true);
		this.ind_assertions = new HashMap<OWLNamedIndividual, Set<OWLAxiom>>();
		
		this.objects = new HashSet<OWLEntity>(); 
		this.sigCollector = new OWLEntityCollector(this.objects);
		this.sigCollector.setCollectClasses(false);
		this.sigCollector.setCollectDataProperties(false);
		this.sigCollector.setCollectDatatypes(false);
		this.sigCollector.setCollectObjectProperties(false);
		
		this.modules = new HashMap<OWLNamedIndividual, ABoxModule>();
		
		preprocess();
	}
	
	private void preprocess() {
		
		for (OWLAxiom axiom : abox) {
			// check individuals in an property assertion
			this.objects.clear();
			this.sigCollector.reset(this.objects);
			axiom.accept(this.sigCollector);
			
			// put assertion into map for each individual
			for (OWLEntity o : objects) {
				Set<OWLAxiom> set = this.ind_assertions.get(o);
				if (set == null)
					set = new HashSet<OWLAxiom>();

				set.add(axiom);
				this.ind_assertions.put((OWLNamedIndividual)o, set);
			}
		}
	}


	public void loadABox(String url) {
		// cares are only needed for large ABoxes. 
		return; 
	}

	
	public Set<OWLNamedIndividual> getNamedIndividuals() {
		return this.ind_assertions.keySet();
	}


	public Set<OWLAxiom> getAssertions(OWLNamedIndividual ind) {
		return this.ind_assertions.get(ind);
	}


	public Set<OWLAxiom> getAssertions(OWLNamedIndividual ind, AxiomType<?>... types) {
		
		Set<OWLAxiom> assertions = this.getAssertions(ind);
		return iterable2Set(this.ontHelper.getAxioms(assertions, types));
	}


	public Set<OWLNamedIndividual> getNeighbors(OWLNamedIndividual ind) {
		
		// get all explicit role assertions of the given individual.
		Set<OWLAxiom> roleAssertions = this.getAssertions(ind, AxiomType.OBJECT_PROPERTY_ASSERTION);
		
		// get all the other individuals in the assertions.
		this.objects.clear();
		this.sigCollector.reset(this.objects);
		for (OWLAxiom ax : roleAssertions) 
			ax.accept(this.sigCollector);
		
		this.objects.remove(ind);
		Set<OWLNamedIndividual> inds = new HashSet<OWLNamedIndividual>();
		for (OWLEntity o : objects)
			inds.add((OWLNamedIndividual) o);
		
		return inds;
	}

	
	public Set<OWLNamedIndividual> getNeighbors(OWLNamedIndividual ind, OWLObjectPropertyExpression role) {
		
		// get all explicit role assertions of the given individual.
		Set<OWLAxiom> roleAssertions = this.getAssertions(ind, AxiomType.OBJECT_PROPERTY_ASSERTION);
		
		// get all the other individuals in the assertions.
		this.objects.clear();
		this.sigCollector.reset(this.objects);
		for (OWLAxiom ax : roleAssertions)  {
			// if the role assertion is the explicit R-assertion.
			if (this.getDirectedRole((OWLObjectPropertyAssertionAxiom)ax, ind).equals(role))
				ax.accept(this.sigCollector);
		}
		
		this.objects.remove(ind);
		Set<OWLNamedIndividual> inds = new HashSet<OWLNamedIndividual>();
		for (OWLEntity o : objects)
			inds.add((OWLNamedIndividual) o);
		
		return inds;
			
	}

	
	public ABoxModule getConnectedComponent(OWLNamedIndividual ind) {
		
		if (this.modules.get(ind) != null)
			return this.modules.get(ind);
		
		ABoxModule m = super.getConnectedComponent(ind);
		for (OWLNamedIndividual in : m.getSignature()) 
			this.modules.put(in, m);
		
		return m;
	}


	public Set<ABoxModule> getConnectedComponents() {
		
		Set<ABoxModule> components = new HashSet<ABoxModule>();
		
		Set<OWLNamedIndividual> allInds = this.getNamedIndividuals();
		for (OWLNamedIndividual ind : allInds)
			if (this.modules.get(ind) == null)
				this.getConnectedComponent(ind);
		
		components.addAll(this.modules.values());
		return components;
	}


	public Set<OWLClassExpression> getClasses(OWLNamedIndividual ind) {
		
		return ind.getTypes(this.ont);
	}
	
	

	public Set<OWLAxiom> getAssertions(AxiomType<?>... types) {
		return iterable2Set(this.ontHelper.getAxioms(this.abox, types));
	}
	
	
	private <T> Set<T> iterable2Set(Iterable<T> data) {
		
		Set<T> s = new HashSet<T>();
		for (T d : data)
			s.add(d);
		return s;
	}

}
