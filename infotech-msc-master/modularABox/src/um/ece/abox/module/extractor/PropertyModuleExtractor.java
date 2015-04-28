package um.ece.abox.module.extractor;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLNamedIndividual;

public interface PropertyModuleExtractor {

	/**
	 * Extract a module for a given individual a, which preserves all
	 * role assertions R(a,b) for $a$, where b \not\approx a and R \in R^*.
	 * @param ind
	 * @return
	 */
	public Iterable<OWLAxiom> getPropertyPreservedModule(OWLNamedIndividual ind);
}
