package um.ece.abox.module.model;

import java.util.Collection;

import org.semanticweb.owlapi.model.*;


/**
 * This interface is for an ABox modules consisting of 
 * ABox assertions that preserve both explicit and implicit
 * facts of a given signature (individuals).
 * @author Jia
 *
 */
public interface ABoxModule {
	
	/**
	 * Get signature of this ABox module, which may not 
	 * include all of the individuals that occur in the module.
	 * @return
	 */
	public Collection<OWLNamedIndividual> getSignature();
	
	/**
	 * Get all ABox assertions that are contained in this ABox module.
	 * @return
	 */
	public Collection<? extends OWLAxiom> getAssertions();
	
	/**
	 * Merge another ABox module into this one.
	 * @param m
	 * @return
	 */
	public ABoxModule merge(ABoxModule m);
	
	
	
}
