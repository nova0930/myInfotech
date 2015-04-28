package main;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.util.DefaultPrefixManager;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.infotechsoft.integration.queryplan.semqa.GraphPattern;
import com.infotechsoft.integration.queryplan.semqa.Join;
import com.infotechsoft.integration.queryplan.semqa.QAQuery;
import com.infotechsoft.integration.queryplan.semqa.TriplePattern;
import com.infotechsoft.integration.queryplan.semqa.msc.SemqaMSC;
import com.infotechsoft.integration.queryplan.semqa.sparql.SPARQLToSemQA;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.configuration.MapConfiguration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.rdd.RDD;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;


import um.ece.abox.module.condition.SyntacticConditionChecker;
import um.ece.abox.module.helper.db.titan.TitanABoxHelper;
import um.ece.abox.module.helper.db.titan.TitanNamedIndividual;
import um.ece.abox.module.helper.memo.SHIQOntologyABoxHelper;
import um.ece.abox.module.helper.memo.SHIQOntologyRBoxHelper;
import um.ece.abox.module.parser.ontology.SHIQOntologyParser;
import um.ece.abox.msc.SHIMscExtractor;
import um.ece.abox.module.condition.ConditionChecker;
import um.ece.abox.module.helper.OntologyABoxHelper;
import um.ece.abox.module.helper.OntologyRBoxHelper;
import um.ece.abox.module.helper.memo.*;
import um.ece.abox.module.parser.ontology.OntologyParser;
import um.ece.abox.msc.OWLConcept;
import um.ece.spark.cluster.util.MemoMSC;

public class TestRealMSC {
	
	public static void main(String args[]) throws Exception {
		
		String kbFile = "/home/zhangda/Desktop/Protege_5.0_beta/university.owl";
		
		 OWLDataFactory df = OWLManager.getOWLDataFactory();
		 
			OWLOntology tbox = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(new File(kbFile));
			OWLOntology abox = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(new File(kbFile));
			SHIQOntologyABoxHelper ah = new SHIQOntologyABoxHelper(abox); 
			OntologyRBoxHelper rh = new SHIQOntologyRBoxHelper(tbox);
			SyntacticConditionChecker condChecker = new SyntacticConditionChecker(tbox, ah, rh);
			OntologyParser ontParser = new SHIQOntologyParser(tbox);
			
			  ontParser.parseOntology();
		      ontParser.accept(condChecker);
		      SHIMscExtractor msc = new SHIMscExtractor(ah, condChecker, df);
		      OWLReasoner reasoner = condChecker.getReasoner();
		      Set<OWLNamedIndividual> inds = new HashSet<OWLNamedIndividual>();

String prefixes
= "PREFIX xsd:  <http://www.w3.org/2001/xmlschema#> \n"
+ "PREFIX rdf:    <http://www.w3.org/2002/07/owl#> \n"
+ "PREFIX rdfs:   <http://www.w3.org/2000/01/rdf-schema#> \n"
+ "PREFIX owl:    <http://www.w3.org/2002/07/owl#> \n"
+ "PREFIX university: <http://www.semanticweb.org/zhangda/ontologies/2015/1/university#> \n";
		
		
String sparql = prefixes + "\n"
		  + "SELECT ?g \n"
		  + "WHERE { \n"
		  + "?g rdf:type  university:UV_000008 .\n"
		 // + "?g university:UV_000058 ?c .\n"
		  + "?c rdf:type university:UV_000026 . \n"
		  + "}"	
	;
        
        
        Query Query = QueryFactory.create(sparql);
		QAQuery qaq = SPARQLToSemQA.getInstance().sparqlToSemQA(Query); 
		System.out.println("QAQ for " + qaq);
		System.out.println("**********************************************************" );
		GraphPattern gp = qaq.getGraphPattern();
		System.out.println(gp);

		Set<Node> nodes = new HashSet<Node>();
		
		for (GraphPattern p : ((Join) gp).getOperands()) {
				TriplePattern tp = (TriplePattern) p;
				Node sub = tp.getSubject();
				Node obj = tp.getObject();
				if (sub.isVariable()) 
					nodes.add(sub);
				if (obj.isVariable())
					nodes.add(obj);
		}
		
		SemqaMSC MSC = new SemqaMSC();
		
		for (Node n : nodes) {
			System.out.println();
			System.out.println("MSC for " + n);
			System.out.println(MSC.getMsc(gp, n));
			
			OWLClassExpression query = MSC.getMsc(gp, n);
			System.out.println("The query is : "+query);
			//OWLAxiom ax = df.getOWLSubClassOfAxiom(cls, query);
			for (OWLNamedIndividual ind : ah.getNamedIndividuals()) {
				//ah.getClasses(ind);
				
				OWLClassExpression cls = msc.getMSC(ind); 
				System.out.println("The MSC concept for ind "+ind+"is  : "+ah.getClasses(ind));
				OWLAxiom ax = df.getOWLSubClassOfAxiom(cls, query);
				
				boolean b = reasoner.isEntailed(ax);
			    
			
			    if (b) 
			    	{
			    	System.out.println("The included individual is : "+ind);
			    	inds.add((OWLNamedIndividual) ind);
			    	}

			}
			
			
		}
		
		
		
		
		
		
		
		
	}
	

	

}
