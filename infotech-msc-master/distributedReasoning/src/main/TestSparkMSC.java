package main;

import java.io.File;
import java.util.*;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.rdd.RDD;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLOntology;

import um.ece.abox.module.helper.db.titan.TitanNamedIndividual;
import um.ece.spark.cluster.util.HDFSUtil;
import um.ece.spark.cluster.util.SparkDriver;
import um.ece.spark.cluster.util.SparkMSC;

public class TestSparkMSC {
	
	public static void main(String args[]) throws Exception {
		
		//final String prefix = "http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#"; 
		
		PropertiesConfiguration conf = new PropertiesConfiguration(args[0]);
		String sparkConfigFile = conf.getString("spark.properties");
		final String tfile = conf.getString("ontology.tbox");
		final String prefix = conf.getString("ontology.uri.prefix");
		final String hadoopConf = conf.getString("hadoop.conf.dir");
		final String hbaseConf = conf.getString("hbase.properties");
		final String output = conf.getString("output.path");
		final String input = conf.getString("input.path");
		final String splits = conf.getString("data.split");
		int dataSplit = Integer.parseInt(splits.trim());
		
		final OWLOntology tbox = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(new File(tfile));
		final Map<String, Object> titanConfig = graphConfig(hbaseConf);
		JavaSparkContext sc = SparkDriver.sparkContext(sparkConfigFile);
		JavaRDD<String> rdd = sc.textFile(input);
		
		// remove output folder in hdfs
		FileSystem fs = HDFSUtil.getFS(hadoopConf + "/core-site.xml");
		HDFSUtil.rmDir(fs, output);
		
		// always re-partition the input into specified pieces
//		String temp = output + "/tmp";
//		rdd.repartition(dataSplit).saveAsTextFile(temp);
//		JavaRDD<String> rdd2 = sc.textFile(temp, dataSplit);
		
		SparkMSC sparkMsc = new SparkMSC();
		Object[] concepts = tbox.getClassesInSignature().toArray();
		
		String cmd = "";
		SparkMSCJavaint count = 0;
		while (!cmd.equals("exit")) { //count < concepts.length
			//int index = (int) (Math.random() * 1000) % concepts.length;
			OWLClassExpression query = (OWLClassExpression) concepts[count];
			RDD<TitanNamedIndividual> result = sparkMsc.runMsc(rdd.rdd(), prefix, tbox, titanConfig, query, hadoopConf, output+"/"+count+"/time.log");
			//result.saveAsTextFile(output+"/"+count+"/query-result");
			long num = result.count();
			count++;
			System.out.println("Query concept " + query);
			System.out.println("Result count: " + num);
			System.out.println("Query is finished.");
			System.out.println("Enter command to continue...");
			cmd = System.console().readLine().trim();
		}
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
