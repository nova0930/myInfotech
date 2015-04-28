package um.ece.abox.module.extractor;

import java.util.*;

import org.semanticweb.owlapi.model.*;

import com.google.common.collect.Iterables;

import um.ece.abox.module.helper.OntologyABoxHelper;
import um.ece.abox.module.helper.OntologyRBoxHelper;

public class SHIQPropertyModuleExtractor implements PropertyModuleExtractor {

	OntologyABoxHelper ah;
	OntologyRBoxHelper rh;
	Map<OWLNamedIndividual, Iterable<OWLAxiom>> modules;
	
	public SHIQPropertyModuleExtractor(OntologyABoxHelper ah, OntologyRBoxHelper rh) {
		this.ah = ah;
		this.rh = rh;
		this.modules = new HashMap<OWLNamedIndividual, Iterable<OWLAxiom>>();
	}
	
	 
	public Iterable<OWLAxiom> getPropertyPreservedModule(OWLNamedIndividual ind) {
		
		if (this.modules.get(ind) != null) 
			return this.modules.get(ind);
		
		// get all explicit role assertions of the given individual
		Iterable<OWLAxiom> roleAssts = ah.getAssertions(ind, AxiomType.OBJECT_PROPERTY_ASSERTION);
		
		// get all transitive parent roles in the obtained role assertions.
		// If it is R(a,b), then get transitive parents of R.
		// If it is R(b,a), then get transitive parents of R^-.
		Set<OWLObjectPropertyExpression> tranRoles = new HashSet<OWLObjectPropertyExpression>();
		for (OWLAxiom ax : roleAssts) {
			OWLObjectPropertyExpression r = ah.getDirectedRole((OWLObjectPropertyAssertionAxiom)ax, ind);
			tranRoles.addAll(rh.getTransParentRoles(r, false));
		}
		
		// for every transitive role incident to the given individual,
		// flood the ABox graph to find reachable role assertions.
		for (OWLObjectPropertyExpression role : tranRoles) {
			Set<OWLAxiom> reachable = flood(role, ind);
			//roleAssts.addAll(reachable);
			roleAssts = Iterables.concat(roleAssts, reachable);
		}
		
		this.modules.put(ind, roleAssts);
		return roleAssts;
	}

	
	/**
	 * Find reachable role assertions Ri(a,b) in the graph, where Ri \sqsubseteq R.
	 * @param R
	 * @param ind, the root point.
	 * @return
	 */
	private Set<OWLAxiom> flood(OWLObjectPropertyExpression R, OWLNamedIndividual ind) {
		
		Set<OWLAxiom> result = new HashSet<OWLAxiom>();
		List<OWLNamedIndividual> queue = new LinkedList<OWLNamedIndividual>();
		Set<OWLNamedIndividual> visited = new HashSet<OWLNamedIndividual>();
		queue.add(ind);
		visited.add(ind);
		
		while (!queue.isEmpty()) {
			OWLNamedIndividual n = queue.remove(0);
			Iterable<OWLAxiom> roleAssertions = ah.getAssertions(n, AxiomType.OBJECT_PROPERTY_ASSERTION);
			
			for (OWLAxiom ax : roleAssertions) {
				if (result.contains(ax))
					continue;
				
				OWLObjectPropertyExpression directedR = 
									ah.getDirectedRole((OWLObjectPropertyAssertionAxiom)ax, n);
				OWLNamedIndividual neighbor = 
									ah.getNeighbor((OWLObjectPropertyAssertionAxiom)ax, n);
				
				if (!rh.isSubRoleOf(directedR, R)) 
					continue;
				
				result.add(ax);
				if (!visited.contains(neighbor)) {
					queue.add(neighbor);
					visited.add(neighbor);
				}	
			} // end of for loop
		}
		return result;
	}

}
