package main;

import java.io.File;
import java.util.Iterator;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.DefaultPrefixManager;

import um.ece.abox.module.condition.ConditionChecker;
import um.ece.abox.module.condition.SyntacticConditionChecker;
import um.ece.abox.module.helper.OntologyABoxHelper;
import um.ece.abox.module.helper.OntologyRBoxHelper;
import um.ece.abox.module.helper.db.titan.EfficientTitanABoxHelper;
import um.ece.abox.module.helper.db.titan.TitanABoxHelper;
import um.ece.abox.module.helper.db.titan.TitanNamedIndividual;
import um.ece.abox.module.helper.memo.SHIQOntologyABoxHelper;
import um.ece.abox.module.helper.memo.SHIQOntologyRBoxHelper;
import um.ece.abox.module.parser.ontology.OntologyParser;
import um.ece.abox.module.parser.ontology.SHIQOntologyParser;
import um.ece.abox.msc.SHIMscExtractor;
import um.ece.abox.msc.OWLConcept;

public class TestTitanMSC {
	
	public static void main(String args[]) throws OWLOntologyCreationException {
		
		String prefix = "http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#";
		//String tfile = "../../Ont3/data/univ.owl"; 
		String tfile = "/home/zhangda/Desktop/Infotech/univ.owl"; 
		String afile = "/home/zhangda/Desktop/Infotech/lubm1.owl";
		
		OWLDataFactory df = OWLManager.getOWLDataFactory();
		
		OWLOntology tbox = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(new File(tfile));
		//OWLOntology abox = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(new File(afile));
		TitanABoxHelper ah = new TitanABoxHelper(df, prefix, graphConfig()); // new SHIQOntologyABoxHelper(abox);// 
		OntologyRBoxHelper rh = new SHIQOntologyRBoxHelper(tbox);
		ConditionChecker condChecker = new SyntacticConditionChecker(tbox, ah, rh);
		OntologyParser ontParser = new SHIQOntologyParser(tbox);
		ontParser.parseOntology();
		ontParser.accept(condChecker);
		SHIMscExtractor msc = new SHIMscExtractor(ah, condChecker, df);
		PrefixManager prefixManager = new DefaultPrefixManager(prefix);
		
		
		
		int count = 0;
		int maxDepth = 0, maxWidth = 0;
		
		for (OWLNamedIndividual ind : ah.getNamedIndividuals()) {
			count++;
			ah.getClasses(ind);
//			System.out.println(ah.getClasses(ind).iterator().next());
//			OWLConcept cls = msc.getMSC(ind, 1);
//			if (cls.getRoleDepth() > 1) {
//				count++;
//				if (cls.getRoleDepth() > maxDepth)
//					maxDepth = cls.getRoleDepth();
//				if (cls.getAndBranches() > maxWidth)
//					maxWidth = cls.getAndBranches();
//				System.out.println(cls + "role depth " + cls.getRoleDepth() + " and branch " + cls.getAndBranches());
				//System.out.println(cls.getConcept() + "\n");
//			}
		}
		
		Iterator<Long> it = ah.getNamedIndividualIDs();
		Long id  = 0L; 
		try {
			while (it.hasNext()) {
				id = it.next();
				OWLNamedIndividual ind = new TitanNamedIndividual(df, id, prefixManager);
				System.out.println(ah.getClasses(ind).iterator().next());
			}
		} catch (Exception e) {
			System.err.println("ID " + id);
			e.printStackTrace();
		}
		
		
		
		System.out.println(count);
		System.out.println("Max Depth " + maxDepth);
		System.out.println("Max Width " + maxWidth);
//		PrefixManager prefixManager = new DefaultPrefixManager(prefix);
//		
//		String id = "";
//		while (true) {
//			id = System.console().readLine().trim();
//			if (id.equals("exit"))
//				break;
//			OWLNamedIndividual ind = new TitanNamedIndividual(df, id, prefixManager);
//			OWLClassExpression cls = msc.getMSC(ind);
//			System.out.println(cls);
//		}
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

}
