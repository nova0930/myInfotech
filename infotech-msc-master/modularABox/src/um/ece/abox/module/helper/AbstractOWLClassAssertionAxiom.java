package um.ece.abox.module.helper;

import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

public class AbstractOWLClassAssertionAxiom extends AbstractOWLAxiom implements OWLClassAssertionAxiom {

	private static final long serialVersionUID = 7687704769509929901L;

	public AbstractOWLClassAssertionAxiom(OWLClassAssertionAxiom ax) {
		super(ax);
	}
	
	@Override
	public OWLClassAssertionAxiom getAxiomWithoutAnnotations() {
		return new AbstractOWLClassAssertionAxiom(((OWLClassAssertionAxiom) ax).getAxiomWithoutAnnotations());
	}
	
	@Override
	public OWLSubClassOfAxiom asOWLSubClassOfAxiom() {
		return ((OWLClassAssertionAxiom) ax).asOWLSubClassOfAxiom();
	}

	@Override
	public OWLIndividual getIndividual() {
		return ((OWLClassAssertionAxiom) ax).getIndividual();
	}

	@Override
	public OWLClassExpression getClassExpression() {
		return ((OWLClassAssertionAxiom) ax).getClassExpression();
	}

//	public String toString() {
//		return ax.toString();
//	}
//	
//	public int hasCode() {
//		return ax.toString().hashCode();
//	}
//	

}
