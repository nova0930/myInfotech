package um.ece.abox.module.helper;

import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

public class AbstractOWLObjectPropertyAssertionAxiom extends AbstractOWLAxiom implements OWLObjectPropertyAssertionAxiom {

	private static final long serialVersionUID = 7418980815451284703L;

	public AbstractOWLObjectPropertyAssertionAxiom(OWLObjectPropertyAssertionAxiom ax) {
		super(ax);
	}
	
	@Override
	public OWLObjectPropertyAssertionAxiom getAxiomWithoutAnnotations() {
		return new AbstractOWLObjectPropertyAssertionAxiom(((OWLObjectPropertyAssertionAxiom)ax).getAxiomWithoutAnnotations());
	}
	
	@Override
	public OWLIndividual getSubject() {
		return ((OWLObjectPropertyAssertionAxiom)ax).getSubject();
	}

	@Override
	public OWLObjectPropertyExpression getProperty() {
		return ((OWLObjectPropertyAssertionAxiom)ax).getProperty();
	}

	@Override
	public OWLIndividual getObject() {
		return ((OWLObjectPropertyAssertionAxiom)ax).getObject();
	}

	@Override
	public OWLSubClassOfAxiom asOWLSubClassOfAxiom() {
		return ((OWLObjectPropertyAssertionAxiom)ax).asOWLSubClassOfAxiom();
	}

	@Override
	public OWLObjectPropertyAssertionAxiom getSimplified() {
		return ((OWLObjectPropertyAssertionAxiom)ax).getSimplified();
	}

	@Override
	public boolean isInSimplifiedForm() {
		return ((OWLObjectPropertyAssertionAxiom)ax).isInSimplifiedForm();
	}
	

//	public int hashCode() {
//		System.out.println("AbstractOWLObjectPropertyAssertionAxiom.hashCode() is called");
//		return ax.toString().hashCode();
//	}

}
