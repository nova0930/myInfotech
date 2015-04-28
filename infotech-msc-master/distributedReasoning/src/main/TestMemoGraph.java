package main;


import java.io.File;
import java.util.*;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.infotechsoft.integration.queryplan.semqa.*;
import com.infotechsoft.integration.queryplan.semqa.msc.SemqaMSC;
import com.infotechsoft.integration.queryplan.semqa.msc.TriplePatternHelper;
import com.infotechsoft.integration.queryplan.semqa.msc.TriplePatternMSC;
import com.infotechsoft.integration.queryplan.semqa.sparql.SPARQLToSemQA;




public class TestMemoGraph {
	
	public static void main(String ars[]) throws Exception {
		
		/*
		String sparql = "PREFIX ub: <http://swat.cse.lehigh.edu/onto/univ-bench.owl#> \n"
				  + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"
				//  + "PREFIX db: <urn:its:anoGam1:test/tbox#> \n"
				  + "SELECT ?X \n"
				  + "WHERE {\n"
				  + "?X a ?professor1 . \n"
				  //+ "?X rdf:type ?professor1 . \n"
				  + "?professor1 ub:teacherOf ub:GraduateCourse0 . \n"
				  + "}"	
			;
		 
		*/
		
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
	    //private static final File kbFile = new File("/home/zhangda/Desktop/Protege_5.0_beta/", "university.owl");
/*
final String prefixes
= "PREFIX xsd:  <http://www.w3.org/2001/xmlschema#> \n"
+ "PREFIX rdf:    <http://www.w3.org/2002/07/owl#> \n"
+ "PREFIX rdfs:   <http://www.w3.org/2000/01/rdf-schema#> \n"
+ "PREFIX owl:    <http://www.w3.org/2002/07/owl#> \n"
+ "PREFIX university: <http://www.semanticweb.org/zhangda/ontologies/2015/1/university#> \n";
*/
//SELECT (COUNT(*) AS ?no) { ?s ?p ?o  }

// Simple SPARQL query
/*String query = prefixes + "\n"
+ "SELECT ?x \n"
+ "WHERE\n"
+ "{ \n"
+ "?x a university:UV_000008 .\n"
+ "}";*/
		Query q = QueryFactory.create(sparql);
		QAQuery qaq = SPARQLToSemQA.getInstance().sparqlToSemQA(q); 
		GraphPattern gp = qaq.getGraphPattern();
		System.out.println(gp);

		Set<Node> inds = new HashSet<Node>();
		
		for (GraphPattern p : ((Join) gp).getOperands()) {
				TriplePattern tp = (TriplePattern) p;
				Node sub = tp.getSubject();
				Node obj = tp.getObject();
				if (sub.isVariable()) 
					inds.add(sub);
				if (obj.isVariable())
					inds.add(obj);
		}
		
		SemqaMSC msc = new SemqaMSC();
		
		for (Node n : inds) {
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

