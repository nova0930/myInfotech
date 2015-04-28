package main;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.semanticweb.owlapi.apibinding.OWLManager;

import um.ece.abox.module.helper.db.titan.TitanABoxHelper;
import um.ece.spark.cluster.util.SparkDriver;

public class LoadTitanIDs {
	
	public static void main(String args[]) throws ConfigurationException {
		
		String sparkConfigFile = "spark.properties";
		final String prefix = "http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#"; 
		
		TitanABoxHelper ah = new TitanABoxHelper(OWLManager.getOWLDataFactory(), prefix, graphConfig());
		Iterator<Long> ids = ah.getNamedIndividualIDs();
		ArrayList<Long> idList = new ArrayList<Long>();
		while (ids.hasNext()) {
			idList.add(ids.next());
			if (idList.size() % 10000 == 0)
				System.out.println(idList.size());
		}
		System.out.println("Total IDs " + idList.size());
		
		JavaSparkContext sc = SparkDriver.sparkContext(sparkConfigFile);
		JavaRDD<Long> rdd = sc.parallelize(idList, 16);
		rdd.saveAsTextFile("hdfs://10.104.5.186:8020/user/ubuntu/univ/ids2");
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
