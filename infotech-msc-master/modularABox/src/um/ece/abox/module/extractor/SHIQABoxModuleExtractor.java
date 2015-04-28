package um.ece.abox.module.extractor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;

import um.ece.abox.module.condition.ConditionChecker;
import um.ece.abox.module.helper.OntologyABoxHelper;
import um.ece.abox.module.model.ABoxModule;
import um.ece.abox.module.model.SimpleABoxModule;

public class SHIQABoxModuleExtractor implements ABoxModuleExtractor {

	OntologyABoxHelper aboxHelper;
	ConditionChecker condChecker;
	PropertyModuleExtractor pmExtractor;
	Map<OWLNamedIndividual, ABoxModule> modules;
	
	public SHIQABoxModuleExtractor(OntologyABoxHelper ah, 
									ConditionChecker checker,
									PropertyModuleExtractor ppm) {
		this.aboxHelper = ah;
		this.condChecker = checker;
		this.pmExtractor = ppm;
		this.modules = new HashMap<OWLNamedIndividual, ABoxModule>();
	}
	
	
	public ABoxModule getABoxModule(OWLNamedIndividual ind) {
		
		if (this.modules.get(ind) != null)
			return this.modules.get(ind);
		
		ABoxModule module = _getABoxModule(ind);
		
		for (OWLNamedIndividual n : module.getSignature())
			this.modules.put(n, module);
		
		return module;
	}

	private ABoxModule _getABoxModule(OWLNamedIndividual ind) {
		
		// develop a BFS algorithm to compute the module
		Set<OWLNamedIndividual> signature = new HashSet<OWLNamedIndividual>();
		Set<OWLAxiom> module = new HashSet<OWLAxiom>();
		List<OWLNamedIndividual> queue = new LinkedList<OWLNamedIndividual>();
		queue.add(ind);
		
		while (!queue.isEmpty()) {
			
			OWLNamedIndividual n = queue.remove(0);
			// add n to the signature, and its assertion to the module
			signature.add(n);
			//module.addAll(this.pmExtractor.getPropertyPreservedModule(n));	
			//module.addAll(this.aboxHelper.getAssertions(n, AxiomType.CLASS_ASSERTION)); FIXME add assertions
			
			Iterable<OWLAxiom> roleAssertions = 
					this.aboxHelper.getAssertions(n, AxiomType.OBJECT_PROPERTY_ASSERTION);
			
			for (OWLAxiom ax : roleAssertions) {	
				OWLNamedIndividual neighbor = 
						this.aboxHelper.getNeighbor((OWLObjectPropertyAssertionAxiom)ax, n);		
				// TODO: check only those potential Role assertions, using pre-screening.
				if (!signature.contains(neighbor)
					&& this.condChecker.checkCondition((OWLObjectPropertyAssertionAxiom)ax, n)) 
					// && !signature.contains(n)
					queue.add(neighbor);
			}	
		}
		
		return new SimpleABoxModule(signature, module);
	}

	 
	public ABoxModule getABoxModule(Set<OWLNamedIndividual> sig) {
		
		ABoxModule module = new SimpleABoxModule();
		for (OWLNamedIndividual ind : sig)
			module.merge(this.getABoxModule(ind));
		
		return module;
	}

	 
	public Set<ABoxModule> ABoxModularize() {
		
		for (OWLNamedIndividual ind : aboxHelper.getNamedIndividuals()) 
			this.modules.put(ind, new SimpleABoxModule(ind, new HashSet<OWLAxiom>()));
		
		Iterable<OWLAxiom> roleAsserts = aboxHelper.getAssertions(AxiomType.OBJECT_PROPERTY_ASSERTION);
		
		for (OWLAxiom ax : roleAsserts) {
			OWLObjectPropertyAssertionAxiom ass = (OWLObjectPropertyAssertionAxiom) ax;
			OWLNamedIndividual sub = ass.getSubject().asOWLNamedIndividual();
			OWLNamedIndividual obj = ass.getObject().asOWLNamedIndividual();
			
			// if condition get satisfied, merge modules
			// TODO: check only those potential Role assertions, using pre-screening.
			if (this.condChecker.checkCondition(ass, sub)) {
				ABoxModule m1 = this.modules.get(sub);
				ABoxModule m2 = this.modules.get(obj);
				if (m1 != m2) {
					m1.merge(m2);
					for (OWLNamedIndividual ind : m1.getSignature())
						this.modules.put(ind, m1);
				}
			}
		} // end of for loop
		
		return new HashSet<ABoxModule>(this.modules.values());
	}

}
