package main;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import com.infotechsoft.integration.queryplan.semqa.msc.DistributedSubsumptionTest;

import um.ece.abox.module.util.MyReasonerFactory;
import um.ece.abox.module.util.TBoxModuleExtractor;


public class TestDistributedSubsumption {
	
	public static void main(String args[]) {
		
		String sparkConfigFile = "spark.properties";//args[0];
		String tboxFile = "go-plus.owl.xml";
		String aboxFile = "output.txt";
		OWLOntology tbox;
		OWLOntology abox;
		TBoxModuleExtractor t_extractor;
		
		try {
			File tfile = new File(tboxFile);
			File afile = new File(aboxFile);
			
			long time = System.currentTimeMillis();
			System.out.println("Loading Ontology (T & A)...");
			tbox = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(tfile);
			//abox = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(afile);
			
			// remove non-logical axioms
			OWLOntologyManager man = tbox.getOWLOntologyManager();
			Set<OWLAxiom> logicAxioms = new java.util.HashSet<OWLAxiom>();
			for (OWLAxiom ax : tbox.getAxioms()) {
				if (ax.isLogicalAxiom()) {
					logicAxioms.add(ax);
				}
			}
			
			OWLOntology TBox = man.createOntology(logicAxioms);
			t_extractor = new TBoxModuleExtractor(TBox);
			
			System.out.println("Ontology loaded");
			System.out.println("Time: " + (System.currentTimeMillis()-time) + "\n");
			time = System.currentTimeMillis();
			
			OWLDataFactory df = OWLManager.getOWLDataFactory();
			
			// make a query concept
			OWLClassExpression cls = randomClass(TBox);
			OWLObjectPropertyExpression role = randomRole(TBox);
			final OWLClassExpression query = df.getOWLObjectSomeValuesFrom(role, cls);
			
			// extract tbox module
			System.out.println("Extracting TBox module...");
			final OWLOntology module = t_extractor.extractModuleAsOntology(query.getSignature());
			System.out.println("Time: " + (System.currentTimeMillis()-time) + "\n");
			time = System.currentTimeMillis();
			Set<OWLClass> classes = module.getClassesInSignature();
			System.out.println("Module axioms " + module.getAxiomCount());
			System.out.println("Module classes " + classes.size());
			
			/** !!! The essential part for distributed subsumption test **/
			// Spark Driver
			Configuration sparkConfig = new PropertiesConfiguration(sparkConfigFile);
			// set the spark configurations
			SparkConf sparkConf = new SparkConf();
			Iterator<?> it = sparkConfig.getKeys();
			while (it.hasNext()){
				String key = it.next().toString();
				sparkConf.set(key, sparkConfig.getString(key));
			}
			JavaSparkContext sc = new JavaSparkContext(sparkConf);
			List<OWLClass> clist = new ArrayList<OWLClass>(classes);
			JavaRDD<OWLClass> rdd = sc.parallelize(clist, 96);
			System.out.println("Spark driver initiated.");
			System.out.println("Time: " + (System.currentTimeMillis()-time) + "\n");
			time = System.currentTimeMillis();
			
			List<OWLClass> subs = new DistributedSubsumptionTest().getSubClasses(query, module, rdd);

			System.out.println("Subsumption tests done.");
			System.out.println("#subclass " + subs.size());
			System.out.println("Time: " + (System.currentTimeMillis()-time) + "\n");
			time = System.currentTimeMillis();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static OWLClassExpression expandQuery(OWLClassExpression query, 
													OWLReasoner reasoner, OWLDataFactory df) {
		
		Set<OWLClass> supers = reasoner.getSubClasses(query, false).getFlattened(); 
		return df.getOWLObjectUnionOf(supers);
	}
	
	public static OWLClassExpression randomClass(OWLOntology tbox) {
		
		List<OWLClass> clss = new ArrayList<OWLClass>(tbox.getClassesInSignature());
		
		int index = (int)(Math.random()*Integer.MAX_VALUE) % clss.size();
		return clss.get(index);
	}
	
	public static OWLObjectPropertyExpression randomRole(OWLOntology tbox) {
		List<OWLObjectProperty> roles = new ArrayList<OWLObjectProperty>(tbox.getObjectPropertiesInSignature());
		int index = (int)(Math.random()*Integer.MAX_VALUE) % roles.size();
		return roles.get(index);
	}
	
}
