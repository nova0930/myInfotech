package um.ece.abox.module.helper;

import java.util.*;

import org.semanticweb.owlapi.model.*;

import um.ece.abox.module.model.ABoxModule;

/**
 * An ontology ABox helper class that provides convenient service 
 * regarding operations on underlying ABox data, such as resource 
 * loading, individual retrieving, assertion retrieving, connected 
 * component computation, etc.
 * 
 * @author Jia
 *
 */
public interface OntologyABoxHelper {
	
	/**
	 * Load an ontology ABox from disk to memory,
	 * or initialize the connection to Dabatase if the ABox
	 * is too huge to be loaded.
	 * @param url
	 */
	public void loadABox(String url);
	
//	public void loadABox(Set<String> urls);
	
	/**
	 * Get all named individual in the underlying ABox data.
	 * @return
	 */
	public Iterable<OWLNamedIndividual> getNamedIndividuals();
	
	
	/**
	 * Get all assertions of the given types.
	 * @param types
	 * @return
	 */
	public Iterable<OWLAxiom> getAssertions(AxiomType<?>... types);
	
	/**
	 * Get all assertions of the given individual from the underlying
	 * ABox data.
	 * @param ind
	 * @return
	 */
	public Iterable<OWLAxiom> getAssertions(OWLNamedIndividual ind);
	
	
	/**
	 * Get all explicit assertions of a given type and a given individual,
	 * from the underlying ABox data.
	 * @param ind
	 * @param type
	 * @return
	 */
	public Iterable<OWLAxiom> getAssertions(OWLNamedIndividual ind, AxiomType<?>... type);
	
	/**
	 * Get all explicit neighbors (role-fillers) of a given individual. 
	 * No ABox reasoning is involved here.
	 * @param ind
	 * @return
	 */
	public Iterable<OWLNamedIndividual> getNeighbors(OWLNamedIndividual ind);
	
	/**
	 * Get all explicit R-neighbors of a given individual based on the ABox data.
	 * No ABox reasoning is involved. Role-hierarchy information is not used.
	 * @param ind
	 * @param role, indicates the R
	 * @return
	 */
	public Iterable<OWLNamedIndividual> getNeighbors(OWLNamedIndividual ind, OWLObjectPropertyExpression role);
	
	
	/**
	 * Get the neighbor of the given individual and the given axiom.
	 * @param ax
	 * @param ind
	 * @return
	 */
	public OWLNamedIndividual getNeighbor(OWLObjectPropertyAssertionAxiom ax, OWLNamedIndividual ind);
	
	
	/**
	 * Get all concepts in the class assertions of a given individual, based on
	 * the underlying ABox data.
	 * @param ind
	 * @return
	 */
	public Set<OWLClassExpression> getClasses(OWLNamedIndividual ind);
	
	/**
	 * Get assertions in the connected component in the ABox graph where the 
	 * given individual resides. The ABox graph here is induced by the role 
	 * assertions.
	 * 
	 * Assertions in a connected component include all ABox assertions of each
	 * individual in the component. They will be encapsulated and returned as an
	 * ABox module.
	 * 
	 * @param ind
	 * @return
	 */
	public ABoxModule getConnectedComponent(OWLNamedIndividual ind);
	
	
	/**
	 * Get sets of assertion in all connected components in the ABox graph.
	 * Return they as a set of ABox modules.
	 *  
	 * @return
	 */
	public Iterable<ABoxModule> getConnectedComponents();
	
	
	/**
	 * Get the directed role in the assertion, leading from the given individual. 
	 * to the neighbor.
	 * @param ax
	 * @param ind
	 * @return
	 */
	public OWLObjectPropertyExpression getDirectedRole(OWLObjectPropertyAssertionAxiom ax, OWLNamedIndividual ind);
	
	
} 
