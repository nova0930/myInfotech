package um.ece.abox.module.helper;

import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.EntityType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnonymousIndividual;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLEntityVisitor;
import org.semanticweb.owlapi.model.OWLEntityVisitorEx;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLIndividualVisitor;
import org.semanticweb.owlapi.model.OWLIndividualVisitorEx;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLNamedObjectVisitor;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectVisitor;
import org.semanticweb.owlapi.model.OWLObjectVisitorEx;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.PrefixManager;

public abstract class AbstractOWLNamedIndividual implements OWLNamedIndividual {
	
	private static final long serialVersionUID = -7324817020310388392L;
	protected OWLNamedIndividual ind;	// OWL object representation of the data item
	
	public AbstractOWLNamedIndividual(OWLNamedIndividual ind) {
		this.ind = ind;
	}
	
	
	public static OWLNamedIndividual create(OWLDataFactory df, String name, PrefixManager pm) {
		return df.getOWLNamedIndividual(name, pm);
	}
	
	public boolean isNamed() {
		return this.ind.isNamed();
	}

	public boolean isAnonymous() {
		return false;
	}

	public OWLNamedIndividual asOWLNamedIndividual() {
		return this;//.ind.asOWLNamedIndividual();
	}

	public OWLAnonymousIndividual asOWLAnonymousIndividual() {
		return null;
	}

	public Set<OWLClassExpression> getTypes(OWLOntology ontology) {
		return this.ind.getTypes(ontology);
	}

	public Set<OWLClassExpression> getTypes(Set<OWLOntology> ontologies) {
		return this.ind.getTypes(ontologies);
	}

	public Map<OWLObjectPropertyExpression, Set<OWLIndividual>> getObjectPropertyValues(
			OWLOntology ontology) {
		return this.ind.getObjectPropertyValues(ontology);
	}

	public Set<OWLIndividual> getObjectPropertyValues(
			OWLObjectPropertyExpression property, OWLOntology ontology) {
		return this.ind.getObjectPropertyValues(property, ontology);
	}

	public boolean hasObjectPropertyValue(OWLObjectPropertyExpression property,
			OWLIndividual individual, OWLOntology ontology) {
		return this.ind.hasObjectPropertyValue(property, individual, ontology);
	}

	public boolean hasDataPropertyValue(OWLDataPropertyExpression property,
			OWLLiteral value, OWLOntology ontology) {
		return this.ind.hasDataPropertyValue(property, value, ontology);
	}

	public boolean hasNegativeObjectPropertyValue(
			OWLObjectPropertyExpression property, OWLIndividual individual,
			OWLOntology ontology) {
		return this.ind.hasNegativeObjectPropertyValue(property, individual, ontology);
	}

	public Map<OWLObjectPropertyExpression, Set<OWLIndividual>> getNegativeObjectPropertyValues(
			OWLOntology ontology) {
		return this.ind.getNegativeObjectPropertyValues(ontology);
	}

	public Map<OWLDataPropertyExpression, Set<OWLLiteral>> getDataPropertyValues(
			OWLOntology ontology) {
		return this.ind.getDataPropertyValues(ontology);
	}

	public Set<OWLLiteral> getDataPropertyValues(
			OWLDataPropertyExpression property, OWLOntology ontology) {
		return this.ind.getDataPropertyValues(property, ontology);
	}

	public Map<OWLDataPropertyExpression, Set<OWLLiteral>> getNegativeDataPropertyValues(
			OWLOntology ontology) {
		return this.ind.getNegativeDataPropertyValues(ontology);
	}

	public boolean hasNegativeDataPropertyValue(
			OWLDataPropertyExpression property, OWLLiteral literal,
			OWLOntology ontology) {
		return this.ind.hasNegativeDataPropertyValue(property, literal, ontology);
	}

	public Set<OWLIndividual> getSameIndividuals(OWLOntology ontology) {
		return this.ind.getSameIndividuals(ontology);
	}

	public Set<OWLIndividual> getDifferentIndividuals(OWLOntology ontology) {
		return this.ind.getDifferentIndividuals(ontology);
	}

	public String toStringID() {
		return this.ind.toStringID();
	}

	public void accept(OWLIndividualVisitor visitor) {
		this.ind.accept(visitor);
	}

	public <O> O accept(OWLIndividualVisitorEx<O> visitor) {
		return this.ind.accept(visitor);
	}

	public Set<OWLEntity> getSignature() {
		return this.ind.getSignature();
	}

	public Set<OWLAnonymousIndividual> getAnonymousIndividuals() {
		return this.ind.getAnonymousIndividuals();
	}

	public Set<OWLClass> getClassesInSignature() {
		return this.ind.getClassesInSignature();
	}

	public Set<OWLDataProperty> getDataPropertiesInSignature() {
		return this.ind.getDataPropertiesInSignature();
	}

	public Set<OWLObjectProperty> getObjectPropertiesInSignature() {
		return this.ind.getObjectPropertiesInSignature();
	}

	public Set<OWLNamedIndividual> getIndividualsInSignature() {
		return this.ind.getIndividualsInSignature();
	}

	public Set<OWLDatatype> getDatatypesInSignature() {
		return this.ind.getDatatypesInSignature();
	}

	public Set<OWLClassExpression> getNestedClassExpressions() {
		return this.ind.getNestedClassExpressions();
	}

	public void accept(OWLObjectVisitor visitor) {
		this.ind.accept(visitor);
	}

	public <O> O accept(OWLObjectVisitorEx<O> visitor) {
		return this.ind.accept(visitor);
	}

	public boolean isTopEntity() {
		return this.ind.isTopEntity();
	}

	public boolean isBottomEntity() {
		return this.ind.isBottomEntity();
	}

	public int compareTo(OWLObject o) {
		return this.ind.compareTo(o);
	}

	public EntityType<?> getEntityType() {
		return this.ind.getEntityType();
	}

	@SuppressWarnings("deprecation")
	public <E extends OWLEntity> E getOWLEntity(EntityType<E> entityType) {
		return this.ind.getOWLEntity(entityType);
	}

	public boolean isType(EntityType<?> entityType) {
		return this.ind.isType(entityType);
	}

	public Set<OWLAnnotation> getAnnotations(OWLOntology ontology) {
		return this.ind.getAnnotations(ontology);
	}

	public Set<OWLAnnotation> getAnnotations(OWLOntology ontology,
			OWLAnnotationProperty annotationProperty) {
		return this.ind.getAnnotations(ontology, annotationProperty);
	}

	public Set<OWLAnnotationAssertionAxiom> getAnnotationAssertionAxioms(
			OWLOntology ontology) {
		return this.getAnnotationAssertionAxioms(ontology);
	}

	public boolean isBuiltIn() {
		return this.ind.isBuiltIn();
	}

	public boolean isOWLClass() {
		return this.ind.isOWLClass();
	}

	public OWLClass asOWLClass() {
		return this.ind.asOWLClass();
	}

	public boolean isOWLObjectProperty() {
		return this.ind.isOWLObjectProperty();
	}

	public OWLObjectProperty asOWLObjectProperty() {
		return this.ind.asOWLObjectProperty();
	}

	public boolean isOWLDataProperty() {
		return this.ind.isOWLDataProperty();
	}

	public OWLDataProperty asOWLDataProperty() {
		return this.ind.asOWLDataProperty();
	}

	public boolean isOWLNamedIndividual() {
		return this.ind.isOWLNamedIndividual();
	}

	public boolean isOWLDatatype() {
		return this.ind.isOWLDatatype();
	}

	public OWLDatatype asOWLDatatype() {
		return this.ind.asOWLDatatype();
	}

	public boolean isOWLAnnotationProperty() {
		return this.ind.isOWLAnnotationProperty();
	}

	public OWLAnnotationProperty asOWLAnnotationProperty() {
		return this.ind.asOWLAnnotationProperty();
	}

	public Set<OWLAxiom> getReferencingAxioms(OWLOntology ontology) {
		return this.ind.getReferencingAxioms(ontology);
	}

	public Set<OWLAxiom> getReferencingAxioms(OWLOntology ontology,
			boolean includeImports) {
		return this.ind.getReferencingAxioms(ontology);
	}

	public void accept(OWLEntityVisitor visitor) {
		this.ind.accept(visitor);
	}

	public <O> O accept(OWLEntityVisitorEx<O> visitor) {
		return this.ind.accept(visitor);
	}

	public IRI getIRI() {
		return this.ind.getIRI();
	}

	public void accept(OWLNamedObjectVisitor visitor) {
		this.ind.accept(visitor);
	}
	
	public abstract Object getId();
	
	@Override
	public boolean equals(Object ind) {		
		if (ind == null)
			return false;

		return toString().equals(ind.toString());
	}
	
	public String toString() {
		return this.ind.toString();
	}
	
	public int hashCode() {
		return this.toString().hashCode();
	}

}
