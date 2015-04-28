package main;


import java.util.*;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.infotechsoft.integration.queryplan.semqa.*;
import com.infotechsoft.integration.queryplan.semqa.msc.SemqaMSC;
import com.infotechsoft.integration.queryplan.semqa.msc.TriplePatternHelper;
import com.infotechsoft.integration.queryplan.semqa.msc.TriplePatternMSC;
import com.infotechsoft.integration.queryplan.semqa.sparql.SPARQLToSemQA;


public class TestSPARQLRollup {
	
	public static void main(String ars[]) throws Exception {
		
		
	
		
		
		String sparql = "PREFIX ann: <http://www.semanticweb.org/target/ontologies/disease#> \n"
				  + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"
				  + "PREFIX db: <urn:its:anoGam1:test/tbox#> \n"
				  + "SELECT ?gene \n"
				  + "WHERE { \n"
				  + "?gene rdf:type ann:Disease003 .\n"
				  + "?gene ann:AnnotationID ?disease1 .\n"
				  + "?disease1 rdf:type ann:Disease001 . \n"
				  + "}"	
			;
			
		Query q = QueryFactory.create(sparql);
		QAQuery qaq = SPARQLToSemQA.getInstance().sparqlToSemQA(q); 
		System.out.println("QAQ for " + qaq);
		System.out.println("**********************************************************" );
		GraphPattern gp = qaq.getGraphPattern();
		System.out.println(gp);

		Set<Node> inds = new HashSet<Node>();
		
		for (GraphPattern p : ((Join) gp).getOperands()) {
				TriplePattern tp = (TriplePattern) p;
				System.out.println(tp);
				Node sub = tp.getSubject();
				Node obj = tp.getObject();
				if (sub.isVariable()) 
					inds.add(sub);
				if (obj.isVariable())
					inds.add(obj);
		}
		
		SemqaMSC msc = new SemqaMSC();
		
		for (Node n : inds) {
			System.out.println();
			System.out.println("MSC for " + n);
			System.out.println(msc.getMsc(gp, n));
		}
		
		
		
		/**
	
		OWLDataFactory df = OWLManager.getOWLDataFactory();
		
		TriplePatternHelper sh = new TriplePatternHelper(triples, df);
		
		for (Node n : inds) {
			System.out.println(n);
			Set<OWLClassExpression> cls = sh.getClasses(n);
			Iterable<TriplePattern> roles = sh.getRoleAssertions(n);
			for (TriplePattern tp : roles) {
				System.out.println("directed role " + sh.getDirectedRole(tp, n));
				System.out.println("neighbor " + sh.getNeighbor(tp, n));
			}
			System.out.println();
		}
		
		TriplePatternMSC msc = new TriplePatternMSC(df, sh);
		for (Node n : inds) {
			System.out.println("MSC for " + n);
			System.out.println(msc.getMsc(n));
		}
		*/
	}

}
