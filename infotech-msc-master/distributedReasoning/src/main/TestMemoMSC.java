package main;

import java.io.File;
import java.util.*;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.spark.rdd.RDD;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;

import um.ece.abox.module.helper.db.titan.TitanNamedIndividual;
import um.ece.abox.msc.OWLConcept;
import um.ece.spark.cluster.util.MemoMSC;


public class TestMemoMSC {
	
	public static void main(String args[]) throws Exception {
		
		//final String prefix = "http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#"; 
		String prefix = "http://swat.cse.lehigh.edu/onto/univ-bench.owl";
		//String prefix = "http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#";
		//String tfile = "../../Ont3/data/univ.owl"; 
		String tfile = "/home/zhangda/Desktop/Infotech/univ.owl"; 
		String afile = "/home/zhangda/Desktop/Infotech/lubm1.owl";
        OWLDataFactory df = OWLManager.getOWLDataFactory();
		OWLOntology tbox = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(new File(tfile));
		OWLOntology abox = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(new File(afile));
	
		
		
		
		
	
		
		MemoMSC memoMsc = new MemoMSC();
		Object[] concepts = tbox.getClassesInSignature().toArray();
		int i =0;
		
		for (i =0; i<=concepts.length-1;i++)
		{
			System.out.println("Number: "+i+ " "+concepts[i]);
		}
		
		System.out.println("After Analyzing Tbox, Thereare "+i+" Classes");
		
		String cmd = "";
		int count = 33;
		int num = 0;
		
			
				OWLClassExpression query = (OWLClassExpression) concepts[count];
				Set<OWLNamedIndividual> result = memoMsc.runMsc(prefix, tbox, abox, query);
				System.out.println("The query is "+query);
				//result.saveAsTextFile(output+"/"+count+"/query-result");
				for (OWLNamedIndividual ind : result) {
				num++;	
			     }
			
				
				System.out.println("Query concept " + query);
				System.out.println("Result count: " + num);
				System.out.println("Query is finished.");
				System.out.println("Enter command to continue...");
				//cmd = System.console().readLine().trim();
			//}
			
			
			
		}
	
	
	
	public static Map<String, Object> graphConfig(String confFile) throws ConfigurationException {
		
		HashMap<String, Object> conf = new HashMap<String, Object>();
		Configuration config = new PropertiesConfiguration(confFile);
		Iterator<?> keys = config.getKeys();
		while (keys.hasNext()) {
			String key = keys.next().toString();
			conf.put(key, config.getString(key));
		}
	    conf.put("cache.db-cache", "true");
	    conf.put("cache.db-cache-size", "0.5");
	    conf.put("cache.db-cache-time", "0");
	    return conf;
	}
	

}
