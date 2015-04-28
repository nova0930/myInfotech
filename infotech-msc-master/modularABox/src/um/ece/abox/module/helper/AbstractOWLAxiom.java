package um.ece.abox.module.helper;

import java.util.Set;

import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnonymousIndividual;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLAxiomVisitor;
import org.semanticweb.owlapi.model.OWLAxiomVisitorEx;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectVisitor;
import org.semanticweb.owlapi.model.OWLObjectVisitorEx;

public class AbstractOWLAxiom  implements OWLAxiom {

	private static final long serialVersionUID = -7463093930732262872L;
	OWLAxiom ax;
		
	public AbstractOWLAxiom(OWLAxiom ax) {
		this.ax = ax;
	}
	
	@Override
	public Set<OWLEntity> getSignature() {
		return ax.getSignature();
	}

	@Override
	public Set<OWLAnonymousIndividual> getAnonymousIndividuals() {
		return ax.getAnonymousIndividuals();
	}

	@Override
	public Set<OWLClass> getClassesInSignature() {
		return ax.getClassesInSignature();
	}

	@Override
	public Set<OWLDataProperty> getDataPropertiesInSignature() {
		return ax.getDataPropertiesInSignature();
	}

	@Override
	public Set<OWLObjectProperty> getObjectPropertiesInSignature() {
		return ax.getObjectPropertiesInSignature();
	}

	@Override
	public Set<OWLNamedIndividual> getIndividualsInSignature() {
		return ax.getIndividualsInSignature();
	}

	@Override
	public Set<OWLDatatype> getDatatypesInSignature() {
		return ax.getDatatypesInSignature();
	}

	@Override
	public Set<OWLClassExpression> getNestedClassExpressions() {
		return ax.getNestedClassExpressions();
	}

	@Override
	public void accept(OWLObjectVisitor visitor) {
		ax.accept(visitor);
	}

	@Override
	public <O> O accept(OWLObjectVisitorEx<O> visitor) {
		return ax.accept(visitor);
	}

	@Override
	public boolean isTopEntity() {
		return ax.isTopEntity();
	}

	@Override
	public boolean isBottomEntity() {
		return ax.isBottomEntity();
	}

	@Override
	public int compareTo(OWLObject o) {
		return ax.compareTo(o);
	}

	@Override
	public void accept(OWLAxiomVisitor visitor) {
		ax.accept(visitor);
	}

	@Override
	public <O> O accept(OWLAxiomVisitorEx<O> visitor) {
		return ax.accept(visitor);
	}

	@Override
	public Set<OWLAnnotation> getAnnotations() {
		return ax.getAnnotations();
	}

	@Override
	public Set<OWLAnnotation> getAnnotations(OWLAnnotationProperty annotationProperty) {
		return ax.getAnnotations(annotationProperty);
	}

	@Override
	public OWLAxiom getAxiomWithoutAnnotations() {
		return new AbstractOWLAxiom(ax.getAxiomWithoutAnnotations());
	}

	@Override
	public OWLAxiom getAnnotatedAxiom(Set<OWLAnnotation> annotations) {
		return ax.getAnnotatedAxiom(annotations);
	}

	@Override
	public boolean equalsIgnoreAnnotations(OWLAxiom axiom) {
		return ax.equalsIgnoreAnnotations(axiom);
	}

	@Override
	public boolean isLogicalAxiom() {
		return ax.isLogicalAxiom();
	}

	@Override
	public boolean isAnnotationAxiom() {
		return ax.isAnnotationAxiom();
	}

	@Override
	public boolean isAnnotated() {
		return ax.isAnnotated();
	}

	@Override
	public AxiomType<?> getAxiomType() {
		return ax.getAxiomType();
	}

	@Override
	public boolean isOfType(AxiomType<?>... axiomTypes) {
		return ax.isOfType(axiomTypes);
	}

	@Override
	public boolean isOfType(Set<AxiomType<?>> types) {
		return ax.isOfType(types);
	}

	@Override
	public OWLAxiom getNNF() {
		return new AbstractOWLAxiom(ax.getNNF());
	}
	
	@Override
	public String toString() {
		return ax.toString();
	}
	
	public int hashCode() {
		return ax.toString().hashCode();
	}
	
	@Override
	public boolean equals(Object ax) {	
		if (ax == null)
			return false;
		
//		if (ax instanceof AbstractOWLAxiom) 
//			return this.ax.equals(((AbstractOWLAxiom) ax).ax);
//		else
//			return this.ax.equals(ax);
		return this.ax.toString().equals(ax.toString());
	}

}
