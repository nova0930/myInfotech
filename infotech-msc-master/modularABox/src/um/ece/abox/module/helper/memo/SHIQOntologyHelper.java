package um.ece.abox.module.helper.memo;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.OWLEntityCollector;

import um.ece.abox.module.helper.OntologyHelper;

/**
 * Ontology Helper for DL SHIQ.
 * @author Jia
 *
 */
public class SHIQOntologyHelper implements OntologyHelper {

	 
	public Set<OWLAxiom> getTBox(Set<? extends OWLAxiom> axioms) {
		
		return AxiomType.getAxiomsOfTypes(new TreeSet<OWLAxiom>(axioms), 
						AxiomType.DISJOINT_CLASSES, 
						AxiomType.DISJOINT_UNION,
						AxiomType.EQUIVALENT_CLASSES, 
						AxiomType.SUBCLASS_OF);
	}

	 
	public OWLOntology getTBoxAsOntology(Set<? extends OWLAxiom> axioms,
											OWLOntologyManager man) {
		
		Set<OWLAxiom> tbox = this.getTBox(axioms);
		try {
			return man.createOntology(tbox);
		} catch (OWLOntologyCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	 
	public Set<OWLAxiom> getRBox(Set<? extends OWLAxiom> axioms) {
		
		return AxiomType.getAxiomsOfTypes(new TreeSet<OWLAxiom>(axioms),
					AxiomType.ASYMMETRIC_OBJECT_PROPERTY, 
					AxiomType.DISJOINT_OBJECT_PROPERTIES,
					AxiomType.EQUIVALENT_OBJECT_PROPERTIES, 
					AxiomType.FUNCTIONAL_OBJECT_PROPERTY,
					AxiomType.INVERSE_FUNCTIONAL_OBJECT_PROPERTY,
					AxiomType.INVERSE_OBJECT_PROPERTIES,
					AxiomType.IRREFLEXIVE_OBJECT_PROPERTY,
					AxiomType.OBJECT_PROPERTY_DOMAIN,
					AxiomType.OBJECT_PROPERTY_RANGE,
					AxiomType.REFLEXIVE_OBJECT_PROPERTY,
					AxiomType.SUB_OBJECT_PROPERTY,
					AxiomType.SUB_PROPERTY_CHAIN_OF,
					AxiomType.SYMMETRIC_OBJECT_PROPERTY,
					AxiomType.TRANSITIVE_OBJECT_PROPERTY);
	}

	 
	public OWLOntology getRBoxAsOntology(Set<? extends OWLAxiom> axioms,
											OWLOntologyManager man) {
		Set<OWLAxiom> rbox = this.getRBox(axioms);
		try {
			return man.createOntology(rbox);
		} catch (OWLOntologyCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	 
	public Set<OWLAxiom> getABox(Set<? extends OWLAxiom> axioms) {
		
		return AxiomType.getAxiomsOfTypes(new TreeSet<OWLAxiom>(axioms), 
					AxiomType.CLASS_ASSERTION,
					AxiomType.DIFFERENT_INDIVIDUALS,
					AxiomType.OBJECT_PROPERTY_ASSERTION//,	
				);
	}

	 
	public OWLOntology getABoxAsOntology(Set<? extends OWLAxiom> axioms,
											OWLOntologyManager man) {
		Set<OWLAxiom> abox = this.getABox(axioms);
		try {
			return man.createOntology(abox);
		} catch (OWLOntologyCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	 
	public Set<OWLAxiom> getAxioms(Set<? extends OWLAxiom> axioms, OWLEntity e) {
		
		Set<OWLAxiom> result = new HashSet<OWLAxiom>();
		Set<OWLEntity> objects = new HashSet<OWLEntity>();
		OWLEntityCollector sigCollector = new OWLEntityCollector(objects);
		
		for (OWLAxiom ax : axioms) {
			objects.clear();
			sigCollector.reset(objects);
			ax.accept(sigCollector);
			if (objects.contains(e))
				result.add(ax);
		}
		return result;
	}

	 
	public Set<OWLAxiom> getAxioms(Set<? extends OWLAxiom> axioms, Set<? extends OWLEntity> es) {
		
		Set<OWLAxiom> result = new HashSet<OWLAxiom>();
		for (OWLEntity e : es)
			result.addAll(this.getAxioms(axioms, e));
		
		return result;	
	}

	 
	public Set<OWLAxiom> getAxioms(Set<? extends OWLAxiom> axioms, AxiomType<?>... types) {
		
		Set<OWLAxiom> result = new HashSet<OWLAxiom>();
		Set<AxiomType<?>> ts =  new HashSet<AxiomType<?>>();
		for (AxiomType<?> t : types)
			ts.add(t);
		
		for (OWLAxiom ax : axioms) 
			if (ts.contains(ax.getAxiomType()))
				result.add(ax);
		
		return result;
	}

}
