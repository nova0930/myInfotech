package um.ece.abox.module.extractor;

import java.util.Set;

import org.semanticweb.owlapi.model.OWLNamedIndividual;

import um.ece.abox.module.model.ABoxModule;

public interface ABoxModuleExtractor {

	/**
	 * Extract an ABox module for the given individual, which should preserve
	 * all its role assertions and atomic class assertions (both explicit and implicit).
	 * @param ind
	 * @return
	 */
	public ABoxModule getABoxModule(OWLNamedIndividual ind);
	
	/**
	 * Extract an ABox module for the given signature, which should preserve
	 * all role assertions and atomic class assertions (both explicit and implicit)
	 * of individuals in the given signature.
	 * @param ind
	 * @return
	 */
	public ABoxModule getABoxModule(Set<OWLNamedIndividual> sig);
	
	/**
	 * Modularize the given ABox, with each module preserves complete information of 
	 * associated signature.
	 * @return
	 */
	public Set<ABoxModule> ABoxModularize();
}
