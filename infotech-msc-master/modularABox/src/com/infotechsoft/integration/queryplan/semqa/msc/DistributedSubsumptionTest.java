package com.infotechsoft.integration.queryplan.semqa.msc;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import um.ece.abox.module.util.MyReasonerFactory;

public class DistributedSubsumptionTest implements java.io.Serializable {
	
	private static final long serialVersionUID = -1069936683284054251L;

	public static enum Hierarchy {SUBCLASS, SUPERCLASS, EQUIVALENT};
	
	public List<OWLClass> getSubClasses(final OWLClassExpression query, 
										final OWLOntology ont,
										JavaRDD<OWLClass> classes) {
		return getClasses(query, ont, classes, Hierarchy.SUBCLASS);		
	}
	
	public List<OWLClass> getSuperClasses(final OWLClassExpression query,
										  final OWLOntology ont,
										  JavaRDD<OWLClass> classes) {
		return getClasses(query, ont, classes, Hierarchy.SUPERCLASS);		
	}
	
	public List<OWLClass> getEquivClasses(final OWLClassExpression query,
										  final OWLOntology ont,
										  JavaRDD<OWLClass> classes) {
		return getClasses(query, ont, classes, Hierarchy.EQUIVALENT);		
	}
	
	protected List<OWLClass> getClasses(final OWLClassExpression query, 
										final OWLOntology ont,
										JavaRDD<OWLClass> classes, 
										final Hierarchy type) {
		
	return classes.mapPartitions(new FlatMapFunction<Iterator<OWLClass>, OWLClass>() {
			private static final long serialVersionUID = -3452814442897577973L;
			public Iterable<OWLClass> call(Iterator<OWLClass> clss) throws Exception {
				System.out.println("Creating reasoner...");
				OWLReasoner reasoner = MyReasonerFactory.getReasoner(ont);
				OWLDataFactory dataFactory = OWLManager.getOWLDataFactory();
				Set<OWLClass> results = new HashSet<OWLClass>();
				while (clss.hasNext()) {
					OWLClass c = clss.next();
					OWLAxiom ax; 
					if (type == Hierarchy.SUBCLASS)
						ax = dataFactory.getOWLSubClassOfAxiom(c, query);
					else if (type == Hierarchy.SUPERCLASS)
						ax = dataFactory.getOWLSubClassOfAxiom(query, c);
					else // if type == Hierarchy.EQUIVALENT
						ax = dataFactory.getOWLEquivalentClassesAxiom(query, c);
					
					if (reasoner.isEntailed(ax))
						results.add(c);
				}
				return results;
			}
		}).collect();
	}

}
