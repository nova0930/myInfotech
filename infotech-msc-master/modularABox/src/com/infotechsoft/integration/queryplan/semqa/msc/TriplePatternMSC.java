package com.infotechsoft.integration.queryplan.semqa.msc;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;

import com.hp.hpl.jena.graph.Node;
import com.infotechsoft.integration.queryplan.semqa.TriplePattern;

public class TriplePatternMSC {
	
	OWLDataFactory dataFactory;
	TriplePatternHelper tpHelper;
	
	public TriplePatternMSC(OWLDataFactory df, TriplePatternHelper sh) {
		this.dataFactory = df;
		this.tpHelper = sh;
	}
	
	/**
	 * Compute the Most Specfic Concept for a given variable node in SPARQL query.
	 * @param ind: the variable node
	 * @return
	 */
	public OWLClassExpression getMsc(Node ind) {
		return _getMsc(ind, new HashSet<TriplePattern>());
	}
	
	public void setTPHelper(TriplePatternHelper sh) {
		this.tpHelper = sh;
	}
	
	/** Assume no circles in SPARQL query*/
	OWLClassExpression _getMsc(Node ind, HashSet<TriplePattern> visitedAxioms) {

		// get all classes of x from its class assertions
		Set<OWLClassExpression> classes = tpHelper.getClasses(ind);
		
		// get all incident edges of node x
		Iterable<TriplePattern> edges = tpHelper.getRoleAssertions(ind);
		
		for (TriplePattern e : edges) {
			
			if (visitedAxioms.contains(e))
				continue;
			
			visitedAxioms.add(e);
			OWLObjectPropertyExpression role = tpHelper.getDirectedRole(e, ind);
			Node neighbor = tpHelper.getNeighbor(e, ind);
			
			// recursively call the roll up for the neighbor
			OWLClassExpression filler = _getMsc(neighbor, visitedAxioms);
			OWLClassExpression restriction = this.dataFactory.getOWLObjectSomeValuesFrom(role, filler);
			classes.add(restriction);
		}
		
		if (classes.size() < 1)
			classes.add(this.dataFactory.getOWLThing());
		
		if (classes.size() == 1)
			return classes.iterator().next();
				
		return this.dataFactory.getOWLObjectIntersectionOf(classes);
	}
	
	

}
