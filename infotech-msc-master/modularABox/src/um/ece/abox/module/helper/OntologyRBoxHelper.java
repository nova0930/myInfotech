package um.ece.abox.module.helper;

import java.util.Set;

import org.semanticweb.owlapi.model.*;


/**
 * An ontology RBox helper that provides convenient RBox services, such as 
 * computation of role hierarchies, query of sub-role relations, transitive
 * roles, or functional roles, etc.
 * 
 * @author Jia
 *
 */
public interface OntologyRBoxHelper {
	

	/**
	 * Get parent roles of a given one, based on the underlying ontology RBox.
	 * i.e. To get all R's, such that role \sqsubseteq R.
	 * 
	 * @param role
	 * @param named, indicate if the parent role is named or not.
	 * @return
	 */
	public Set<OWLObjectPropertyExpression> getParentRoles(OWLObjectPropertyExpression role, boolean named);

	
	/**
	 * Get parent roles of a given one, based on the underlying ontology RBox,
	 * which is also transitive.
	 * i.e. To get all R's, such that role \squsubseteq R and Trans(R).
	 * 
	 * @param role
	 * @param named, indicate if the parent role is named or not.
	 * @return
	 */
	public Set<OWLObjectPropertyExpression> getTransParentRoles(OWLObjectPropertyExpression role, boolean named);
	
	
	/**
	 * Indicate if one of the given roles is a sub-role of the other.
	 * Reasoning is invoked to indicate both explicit and implicit info.
	 * 
	 * @param child
	 * @param parent
	 * @return
	 */
	public boolean isSubRoleOf(OWLObjectPropertyExpression child, OWLObjectPropertyExpression parent);
	
	/**
	 * Indicate if a given role is transitive or not.
	 * @param role
	 * @return
	 */
	public boolean isTransitive(OWLObjectPropertyExpression role);
	
	
	/**
	 * Indicate if a given role is functional or not.
	 * A role is functional, either if it is directly asserted to be, 
	 * or any of its parents roles are functional.
	 * 
	 * @param role
	 * @return
	 */
	public boolean isFunctional(OWLObjectPropertyExpression role);
	
	
	/**
	 * Indicate if a given role is inverse functional or not.
	 * A role is inverse functional, either if it is directly asserted to be,
	 * or any of its parent roles are inverse functional.
	 * 
	 * @param role
	 * @return
	 */
	public boolean isInverseFunctional(OWLObjectPropertyExpression role);
}
