package um.ece.abox.module.helper;

import java.util.Set;

import org.semanticweb.owlapi.model.*;
/**
 * An ontology helper that assist several operations over a given ontology 
 * in certain Description Logic, such as SHIQ.
 * 
 * @author Jia
 *
 */
public interface OntologyHelper {
	
	
	/**
	 * Extract TBox axioms, i.e. general concept inclusion axioms, out
	 * of a given set of axioms.
	 * @param axioms
	 * @return
	 */
	public Iterable<OWLAxiom> getTBox(Set<? extends OWLAxiom> axioms);
	
	/**
	 * Extract TBox axioms, i.e. general concept inclusion axioms, out
	 * of a given set of axioms as an ontology.
	 * @param axioms
	 * @param man
	 * @return
	 */
	public OWLOntology getTBoxAsOntology(Set<? extends OWLAxiom> axioms, 
											OWLOntologyManager man)
											throws OWLOntologyCreationException;
	
	
	/**
	 * Extract RBox axioms, i.e. general role inclusion axioms, out
	 * of a given set of axioms.
	 * @param axioms
	 * @return
	 */
	public Iterable<OWLAxiom> getRBox(Set<? extends OWLAxiom> axioms);
	
	/**
	 * Extract RBox axioms, i.e. general role inclusion axioms, out
	 * of a given set of axioms as an ontology.
	 * @param axioms
	 * @param man
	 * @return
	 */
	public OWLOntology getRBoxAsOntology(Set<? extends OWLAxiom> axioms, 
											OWLOntologyManager man)
											throws OWLOntologyCreationException;

	
	/**
	 * Extract ABox assertions out of a given set of axioms.
	 * @param axioms
	 * @return
	 */
	public Iterable<OWLAxiom> getABox(Set<? extends OWLAxiom> axioms);
	
	
	/**
	 * Extract ABox assertions out of a given set of axioms as an ontology.
	 * @param axioms
	 * @param man
	 * @return
	 */
	public OWLOntology getABoxAsOntology(Set<? extends OWLAxiom> axioms, 
											OWLOntologyManager man)
											throws OWLOntologyCreationException;
	
	
	/**
	 * Retrieve axioms that are related to a given entity 
	 * (i.e. entity occurred in the axioms).
	 * @param axioms
	 * @param e
	 * @return
	 */
	public Iterable<OWLAxiom> getAxioms(Set<? extends OWLAxiom> axioms, OWLEntity e);
	
	/**
	 * Retrieve axioms that are related to a given set of entities. 
	 * (i.e. any entity in the set occurred in the axioms).
	 * @param axioms
	 * @param es
	 * @return
	 */
	public Iterable<OWLAxiom> getAxioms(Set<? extends OWLAxiom> axioms, Set<? extends OWLEntity> es);

	
	/**
	 * Retrieve axioms of the given set of types. 
	 * Description Logic with different expressivity 
	 * may support different types of axioms.
	 * @param axioms
	 * @param types
	 * @return
	 */
	public Iterable<OWLAxiom> getAxioms(Set<? extends OWLAxiom> axioms, AxiomType<?>... types);
	
}
