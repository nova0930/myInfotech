package main;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.rdd.RDD;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.apibinding.OWLManager;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

import um.ece.spark.cluster.util.RDFLoader;
import um.ece.spark.cluster.util.SparkDriver;


public class RDFLoad {

	public static void main(String args[]) throws Exception {
	    
		Configuration conf = new PropertiesConfiguration(args[0]);
		String tboxFile = conf.getString("ontology.tbox");
		String hbaseConfFile = conf.getString("hbase.conf");
		String indFile = conf.getString("ontology.abox.inds");
		String relFile = conf.getString("ontology.abox.rels");
		
	    HashMap<String,Object> config = configHBase(hbaseConfFile);//new HashMap<>();

	    File tfile = new File(tboxFile);//File("univ.owl");
	    OWLOntology tbox = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(tfile);
	    RDFLoader loader = new RDFLoader();
	    loader.createTypes(tbox, config);
	    
	    JavaSparkContext sc = SparkDriver.sparkContext(args[1]);
	    
	    // connect to DHT
	    ClientConfig clientConfig = new ClientConfig();
        HazelcastInstance client = HazelcastClient.newHazelcastClient( clientConfig );
        IMap<?, ?> map = client.getMap( "ids" );
	    
	    for (int i=0; i<=10; i++) { 
		    RDD<String> classAssertions = sc.textFile(indFile + "univ_" + i + "*", 320).rdd();
		    RDD<String>	roleAssertions = sc.textFile(relFile + "univ_" + i + "*", 320).rdd();
		    
		    // clear the map for efficiency
		    map.clear();
		    
		    // load nodes and edges into graph db
		    loader.loadNodesWithIDs(classAssertions, config, "/mnt/tools/hadoop/conf/", "/mnt/data/univ_id/");
		    loader.loadEdges(roleAssertions, config);
	    }
	    System.out.println("Press Enter to finish the program...");
		System.console().readLine();
	    
	}
	
	public static HashMap<String,Object> configHBase(String confFile) throws ConfigurationException {
		// Spark Driver
		Configuration hbaseConfig = new PropertiesConfiguration(confFile);
		// set the spark configurations
		HashMap<String,Object> conf = new HashMap<String,Object>();
		Iterator<?> it = hbaseConfig.getKeys();
		while (it.hasNext()){
			String key = it.next().toString();
			conf.put(key, hbaseConfig.getString(key));
		}
		
		return conf;		
	}
	
}
