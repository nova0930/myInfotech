package com.infotechsoft.integration.queryplan.semqa.msc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLOntology;

public class SemanticData {
	
	OWLOntology tbox;
	JavaRDD<OWLClass> rdd;
	JavaSparkContext sc;
	DistributedSubsumptionTest distributedEngine;
	private static SemanticData SD = null;
	
	public SemanticData getInstance(OWLOntology tbox, String sparkConfigFile) throws Exception {
		if (SD == null)
			SD = new SemanticData(tbox, sparkConfigFile);
		
		SD.setTbox(tbox);
		return SD;
	}
	
	public void setTbox(OWLOntology tbox) {
		this.tbox = tbox;
		// get all classes in the ontology
		Set<OWLClass> classes = tbox.getClassesInSignature();
		List<OWLClass> clist = new ArrayList<OWLClass>(classes);
		rdd = sc.parallelize(clist);
	}
	
	
	public List<OWLClass> getSubClasses(OWLClassExpression cls) {
		return this.distributedEngine.getSubClasses(cls, tbox, rdd);
	}
	
	
	public List<OWLClass> getSuperClasses(OWLClassExpression cls) {
		return this.distributedEngine.getSuperClasses(cls, tbox, rdd);
	}
	
	
	public List<OWLClass> getEquivClasses(OWLClassExpression cls) {
		return this.distributedEngine.getEquivClasses(cls, tbox, rdd);
	}
	
	
	private SemanticData(OWLOntology tbox, String sparkConfigFile) throws Exception {
		this.tbox = tbox;
		init(sparkConfigFile);
		this.distributedEngine = new DistributedSubsumptionTest();
		setTbox(tbox);
	}
	
	
	private void init(String sparkConfigFile) throws Exception {
		
		// set the spark configurations and create spark driver
		Configuration sparkConfig = new PropertiesConfiguration(sparkConfigFile);
		
		SparkConf sparkConf = new SparkConf();
		Iterator<?> it = sparkConfig.getKeys();
		while (it.hasNext()){
			String key = it.next().toString();
			sparkConf.set(key, sparkConfig.getString(key));
		}
		
		sc= new JavaSparkContext(sparkConf);
	}
	

}
