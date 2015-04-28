package main;

import java.util.Iterator;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.DefaultPrefixManager;

import um.ece.abox.module.helper.OntologyABoxHelper;
import um.ece.abox.module.helper.db.titan.*;
import um.ece.abox.module.helper.db.titan.TitanNamedIndividual;

public class TestTitan {
	
	public static void main(String args[]) {
		
		String prefix = "http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#";
		OWLDataFactory df = OWLManager.getOWLDataFactory();
		OntologyABoxHelper ah = new TitanABoxHelper(df, prefix, graphConfig());
		testGetNInds(ah);
		testGetAssertions(ah);
		testGetIndAssertion(ah, df, prefix);
		testGetIndNeighbors(ah, df, prefix);
		testGetIndClasses(ah, df, prefix);
	}
	
	public static Configuration graphConfig() {
		
		Configuration config = new PropertiesConfiguration();
		config.setProperty("storage.backend", "hbase");
	    config.setProperty("storage.tablename", "rdf");
	    config.setProperty("storage.hostname","10.104.5.186,10.108.121.76,10.70.234.39");
	    config.setProperty("cache.db-cache", "true");
	    config.setProperty("cache.db-cache-size", "0.5");
	    config.setProperty("cache.db-cache-time", "0");
	    return config;
	}
	
	public static void testGetIndClasses(OntologyABoxHelper ah, OWLDataFactory df, String prefix) {
		
		System.out.println("testGetIndClasses");
		PrefixManager prefixManager = new DefaultPrefixManager(prefix);
		String id = "";
		while (true) {
			id = System.console().readLine().trim();
			if (id.equals("exit"))
				break;
			OWLNamedIndividual ind = new TitanNamedIndividual(df, id, prefixManager);
			Iterable<OWLClassExpression> inds = ah.getClasses(ind);
			Iterator<OWLClassExpression> it = inds.iterator();
			while (it.hasNext()){
				System.out.println(it.next());
			}
		}
	}
	
	public static void testGetIndNeighbors(OntologyABoxHelper ah, OWLDataFactory df, String prefix) {
		
		System.out.println("testGetIndNeighbors");
		PrefixManager prefixManager = new DefaultPrefixManager(prefix);
		String id = "";
		while (true) {
			id = System.console().readLine().trim();
			if (id.equals("exit"))
				break;
			OWLNamedIndividual ind = new TitanNamedIndividual(df, id, prefixManager);
			Iterable<OWLNamedIndividual> inds = ah.getNeighbors(ind);
			Iterator<OWLNamedIndividual> it = inds.iterator();
			while (it.hasNext()){
				System.out.println(it.next());
			}
		}
	}
	
	public static void testGetIndAssertion(OntologyABoxHelper ah, OWLDataFactory df, String prefix) {
		
		System.out.println("testGetIndAssertion");
		PrefixManager prefixManager = new DefaultPrefixManager(prefix);
		String id = "";
		while (true) {
			id = System.console().readLine().trim();
			if (id.equals("exit"))
				break;
			OWLNamedIndividual ind = new TitanNamedIndividual(df, id, prefixManager);
			Iterable<OWLAxiom> ax = ah.getAssertions(ind);
			Iterator<OWLAxiom> it = ax.iterator();
			while (it.hasNext()){
				System.out.println(it.next());
			}
		}
	}
	
	public static void testGetAssertions(OntologyABoxHelper ah) {
		
		System.out.println("testGetAssertions");
		Iterable<OWLAxiom> edges = ah.getAssertions();
		Iterator<OWLAxiom> iterator = edges.iterator();
		int count = 0;
		while (iterator.hasNext()) {
			System.out.println(iterator.next());
			count++;
			if (count >= 10)
				break;
		}
	}
	
	public static void testGetNInds(OntologyABoxHelper ah) {
		
		System.out.println("testGetNInds");
		Iterable<OWLNamedIndividual> inds = ah.getNamedIndividuals();
		Iterator<OWLNamedIndividual> iterator = inds.iterator();
		
		int count = 0;
		while (iterator.hasNext()) {
			System.out.println(iterator.next());
			count++;
			if (count >= 10)
				break;
		}
	}

}
