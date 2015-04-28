package um.ece.abox.module.model;

import java.util.*;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLNamedIndividual;

/**
 * A simple implementation of the ABoxModule that supports quick
 * combination of ABox modules, using the java.util.Vector.
 * 
 * Its sets of signature and assertions may contain duplicates.
 * 
 * @author Jia
 *
 */
public class SimpleABoxModule implements ABoxModule {
	
	protected Set<OWLNamedIndividual> signature;
	protected Set<OWLAxiom> assertions;
	
	public SimpleABoxModule() {
		this.signature = new HashSet<OWLNamedIndividual>();
		this.assertions = new HashSet<OWLAxiom>();
	}
	
	public SimpleABoxModule(Set<OWLNamedIndividual> sig, Set<OWLAxiom> assertions) {
		this.signature = sig;
		this.assertions = assertions;
	}
	
	public SimpleABoxModule(OWLNamedIndividual ind, Set<OWLAxiom> assertions) {
		this.signature = new HashSet<OWLNamedIndividual>();
		this.signature.add(ind);
		this.assertions = assertions;
	}

	 
	public Collection<OWLNamedIndividual> getSignature() {
		
		return this.signature;
	}

	 
	public Collection<OWLAxiom> getAssertions() {
		
		return this.assertions;
	}

	 
	public ABoxModule merge(ABoxModule m) {
		
		if (this == m)
			return this;

		this.signature.addAll(m.getSignature());
		this.assertions.addAll(m.getAssertions());
		return this;
	}
}
