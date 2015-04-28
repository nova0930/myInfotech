package um.ece.abox.module.helper.db.rel;


import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;

/**
 * A marker interface, wraps OWLNamedIndividual.
 * @author Jia
 *
 */
public interface DBOWLNamedIndividual extends OWLNamedIndividual {
	
	public boolean isFullyLoaded();
	
	public void setFullyLoaded();
	
	public void addOWLClassAssertion(OWLClassAssertionAxiom ax);
	
	public void addOWLClassAssertions(Set<OWLClassAssertionAxiom> axs);
	
	public void addOWLObjectPropertyAssertion(OWLObjectPropertyAssertionAxiom ax);
	
	public void addOWLObjectPropertyAssertions(Set<OWLObjectPropertyAssertionAxiom> axs);
	
	public Set<OWLAxiom> getOWLClassAssertions();
	
	public Set<OWLAxiom> getOWLObjectPropertyAssertions();
	
	public Set<OWLIndividual> getObjectPropertyValues();

	public Set<OWLIndividual> getObjectPropertyValues(OWLObjectPropertyExpression arg0);
	
	public Set<OWLClassExpression> getTypes();
	
	public String getDataBaseIRI();
	
	public String getDataBaseTableName();
	
	public String getDatabaseID();
}
