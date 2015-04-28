package um.ece.abox.module.parser.ontology;

import java.util.*;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import um.ece.abox.module.condition.ConditionChecker;
import um.ece.abox.module.helper.memo.SHIQOntologyHelper;
import um.ece.abox.module.parser.concept.*;

public class SHIQOntologyParser implements OntologyParser{

	Map<ClassExpressionType, ConceptParser> conceptParsers;
	OWLOntology ont;
	Set<OWLAxiom> tbox;
	
	public SHIQOntologyParser(OWLOntology ont) {
		
		this.ont = ont;
		this.tbox = new SHIQOntologyHelper().getTBox(this.ont.getAxioms()); //Get all axioms in Tbox
		this.conceptParsers = new HashMap<ClassExpressionType, ConceptParser>();
		
		conceptParsers.put(ClassExpressionType.OBJECT_SOME_VALUES_FROM, 
								new ExistentialRestrictionParser());
		conceptParsers.put(ClassExpressionType.OBJECT_ALL_VALUES_FROM, 
								new UniversalRestrictionParser());
		conceptParsers.put(ClassExpressionType.OBJECT_MIN_CARDINALITY, 
								new MinCardinalityRestrictionParser());
		conceptParsers.put(ClassExpressionType.OBJECT_MAX_CARDINALITY, 
								new MaxCardinalityRestrictionParser());
	}
	
	 
	public void parseOntology() {
		
		// TODO: handle role range & domain
		
		// handle (inverse) Functional roles
		Set<OWLObjectProperty> roles = this.ont.getObjectPropertiesInSignature();
		OWLDataFactory df = OWLManager.createOWLOntologyManager().getOWLDataFactory();
		OWLClassExpression topEntity = df.getOWLThing();
		
		for (OWLObjectProperty role : roles) 
			if (role.isFunctional(this.ont)) {
				OWLClassExpression concept = df.getOWLObjectMaxCardinality(1, role);
				OWLAxiom ax = df.getOWLSubClassOfAxiom(topEntity, concept);
				tbox.add(ax);
			} else if (role.isInverseFunctional(this.ont)) {
				OWLClassExpression concept = df.getOWLObjectMaxCardinality(1, 
																role.getInverseProperty());
				OWLAxiom ax = df.getOWLSubClassOfAxiom(topEntity, concept);
				tbox.add(ax);
			}
		
		// handle GCIs in the TBox, which means that general Tbox only has subclass of or equivalant class properties
		for (OWLAxiom ax : tbox) {
			if (ax.getAxiomType().equals(AxiomType.SUBCLASS_OF))
				parseSubclassAxiom((OWLSubClassOfAxiom)ax);  //ax is a general axiom in Tbox
			else if (ax.getAxiomType().equals(AxiomType.EQUIVALENT_CLASSES))
				parseEqualClassAxiom((OWLEquivalentClassesAxiom) ax);
		}
		
	}

	

	 
	public void accept(ConditionChecker checker) {
		
		Collection<ConceptParser> parsers = this.conceptParsers.values();
		for (ConceptParser p : parsers)
			p.accept(checker);
	}
	
	
	public void registerConceptParser(ClassExpressionType type, ConceptParser parser) {
		this.conceptParsers.put(type, parser);
	}
	
	public void dropConceptParser(ClassExpressionType type) {
		this.conceptParsers.remove(type);
	}
	
	private void parseEqualClassAxiom(OWLEquivalentClassesAxiom ax) {
		Set<OWLSubClassOfAxiom> cs = ax.asOWLSubClassOfAxioms();
		for (OWLSubClassOfAxiom a : cs)
			parseSubclassAxiom(a);	
	}

	private void parseSubclassAxiom(OWLSubClassOfAxiom ax) {  // for general axiom ax, it can be analized by getting its superclass and subclass
		
		OWLClassExpression subclass = ax.getSubClass();
		OWLClassExpression superclass = ax.getSuperClass();
		
		// if both lhs and rhs are atomic concept, do nothing.
		if (!subclass.isAnonymous() && !superclass.isAnonymous())
			return;
		
		// TODO: to handle exact_cardinality_restriction
		
		// handle complex concepts in lhs of a GCI. 在属于符号左边的那些值，并且把这些值转换成concept的形式
		if (subclass.isAnonymous()) {
			Set<OWLClassExpression> juncts = ConceptParser.getConceptJuncts(subclass);
			for (OWLClassExpression c : juncts) {
				ConceptParser parser = this.conceptParsers.get(c.getClassExpressionType());
				if (parser != null)
					parser.parseAsLHSConcept(c, ax);
			}
		}
		
		// handle complex concepts in rhs of a GCI
		if (superclass.isAnonymous()) {
			Set<OWLClassExpression> juncts = ConceptParser.getConceptJuncts(superclass);
			for (OWLClassExpression c : juncts) {
				ConceptParser parser = this.conceptParsers.get(c.getClassExpressionType());
				if (parser != null)
					parser.parseAsRHSConcept(c, ax);
			}
		}
	}
	
	
	
}
