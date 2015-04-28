package um.ece.abox.msc;

import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLNamedIndividual;

/**
 * The class that is used to extract the Most Specific Concept (MSC) for a given named 
 * individual in the ontology ABox.
 * @author Jia
 *
 */
public interface MostSpecificConcept {

	public OWLClassExpression getMSC(OWLNamedIndividual x);
}
