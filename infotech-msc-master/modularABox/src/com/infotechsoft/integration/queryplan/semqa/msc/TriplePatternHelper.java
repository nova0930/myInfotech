package com.infotechsoft.integration.queryplan.semqa.msc;

import java.util.*;

import com.hp.hpl.jena.graph.Node;
import com.infotechsoft.integration.queryplan.semqa.TriplePattern;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;

public class TriplePatternHelper {
	
	public static String IS_A = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
	Map<Node, Set<TriplePattern>> triples;
	OWLDataFactory df;
	
	public TriplePatternHelper(Set<TriplePattern> ts, OWLDataFactory dataFactory) {
		this.triples = new HashMap<Node, Set<TriplePattern>>();
		this.df = dataFactory;
		preprocess(ts);
	}
	
	private void preprocess(Set<TriplePattern> ts) {	
		
		if (ts == null)
			return;
		
		for (TriplePattern t : ts) {
			Node sub = t.getSubject();
			Node obj = t.getObject();
			
			if (sub.isVariable()) {
				Set<TriplePattern> subTriples = triples.get(sub);
				if (subTriples == null) 
					subTriples = new HashSet<TriplePattern>();
				subTriples.add(t);
				triples.put(sub, subTriples);
			}
			
			if (obj.isVariable()) {
				Set<TriplePattern> objTriples = triples.get(obj);
				if (objTriples == null) 
					objTriples = new HashSet<TriplePattern>();
				objTriples.add(t);
				triples.put(obj, objTriples);
			}
		}
	}


	public Set<OWLClassExpression> getClasses(Node ind) {
		
		Set<OWLClassExpression> classes = new HashSet<OWLClassExpression>();
		Set<TriplePattern> ts = triples.get(ind);
		if (ts == null)
			return classes;
		
		for (TriplePattern t : ts) {
			Node sub = t.getSubject();
			Node obj = t.getObject();
			Node pred = t.getProperty();
			if (!sub.equals(ind) || !obj.isURI())
				continue;
			
			if (pred.getURI().equals(TriplePatternHelper.IS_A))
				classes.add(this.df.getOWLClass(IRI.create(obj.getURI())));
		}
		return classes;
	}


	public Iterable<TriplePattern> getRoleAssertions(Node ind) {
		
		Set<TriplePattern> assertions = new HashSet<TriplePattern>();
		Set<TriplePattern> ts = triples.get(ind);
		if (ts == null)
			return assertions;
		
		for (TriplePattern t : ts) {
			Node pred = t.getProperty();
			if (pred.isVariable() || pred.isBlank() || pred.getURI().equals(TriplePatternHelper.IS_A))
				continue;
			
			assertions.add(t);
		}
		
		return assertions;
	}


	public OWLObjectPropertyExpression getDirectedRole(TriplePattern t, Node ind) {
		
		if (t == null || ind == null)
			return null;
		
		Node sub = t.getSubject();
		Node pred = t.getProperty();
		OWLObjectPropertyExpression role = this.df.getOWLObjectProperty(IRI.create(pred.getURI()));
		
		return (sub.equals(ind)) ? role : role.getInverseProperty() ;
	}


	public Node getNeighbor(TriplePattern t, Node ind) {
		
		if (t == null || ind == null)
			return null;
		
		Node sub = t.getSubject();
		Node obj = t.getObject();
		
		return sub.equals(ind) ? obj : sub;
	}



}
