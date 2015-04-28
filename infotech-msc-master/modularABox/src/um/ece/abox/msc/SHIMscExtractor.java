package um.ece.abox.msc;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;

import um.ece.abox.module.condition.ConditionChecker;
//import um.ece.abox.module.extractor.PropertyModuleExtractor;
import um.ece.abox.module.helper.OntologyABoxHelper;

public class SHIMscExtractor implements MostSpecificConcept {
	
	OWLDataFactory dataFactory;
	OntologyABoxHelper aboxHelper;
	//PropertyModuleExtractor pmExtractor;
	ConditionChecker condChecker;
	
	public SHIMscExtractor(OntologyABoxHelper ah, 
							//PropertyModuleExtractor pm, 
							ConditionChecker cc, 
							OWLDataFactory df) {
		this.aboxHelper = ah;
		//this.pmExtractor = pm;
		this.condChecker = cc;
		this.dataFactory = df;
	}
	 
	public OWLClassExpression getMSC(OWLNamedIndividual ind) {
		return _getMsc(ind, new HashSet<OWLNamedIndividual>(), new HashSet<OWLAxiom>(), new HashSet<OWLNamedIndividual>(), 0).concept;
	}
	
	public OWLConcept getMSC(OWLNamedIndividual ind, int i) {
		return _getMsc(ind, new HashSet<OWLNamedIndividual>(), new HashSet<OWLAxiom>(), new HashSet<OWLNamedIndividual>(), 0);
	}

	OWLConcept _getMsc(OWLNamedIndividual ind, HashSet<OWLNamedIndividual> visitedInds,
						HashSet<OWLAxiom> visitedAxioms, HashSet<OWLNamedIndividual> markedInds, int depth) {
		
		if (visitedInds.contains(ind)) {
			markedInds.add(ind);
			return new OWLConcept(this.dataFactory.getOWLObjectOneOf(ind), 0, 1);
		}

		visitedInds.add(ind);
		int maxDepth =1;
		int maxLength = 1;
		
		// get all classes of x from its class assertions
		Set<OWLClassExpression> classes = aboxHelper.getClasses(ind);
		
		// get all incident edges of node x
		Iterable<OWLAxiom> edges = aboxHelper.getAssertions(ind, AxiomType.OBJECT_PROPERTY_ASSERTION);
		
		for (OWLAxiom e : edges) {
			
			OWLObjectPropertyAssertionAxiom ax = (OWLObjectPropertyAssertionAxiom) e;
			if (visitedAxioms.contains(ax) || !this.condChecker.checkCondition(ax, ind))
				continue;
			
			visitedAxioms.add(ax);
			OWLObjectPropertyExpression role = aboxHelper.getDirectedRole(ax, ind);
			OWLNamedIndividual neighbor = aboxHelper.getNeighbor(ax, ind);
			
			// recursively call the roll up for the neighbor
			OWLConcept filler = _getMsc(neighbor, visitedInds, visitedAxioms, markedInds, depth+1);
			OWLClassExpression restriction = this.dataFactory.getOWLObjectSomeValuesFrom(role, filler.concept);
			classes.add(restriction);
			
			// record the max depth and width
			maxDepth = Math.max(maxDepth, filler.roleDepth + 1);
			maxLength = Math.max(maxLength, filler.andBranches);
		}
		
		if (classes.size() < 1)
			classes.add(this.dataFactory.getOWLThing());
		
		// handle head of a circle
		if (markedInds.contains(ind)) 
			classes.add(this.dataFactory.getOWLObjectOneOf(ind));
		
		if (classes.size() == 1)
			return new OWLConcept(classes.iterator().next(), 1, 1);
					
		return new OWLConcept(this.dataFactory.getOWLObjectIntersectionOf(classes), 
								maxDepth, Math.max(maxLength,classes.size()));
	}

}
	
