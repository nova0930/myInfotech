package main;
import java.io.File;
import java.io.PrintWriter;
import java.util.*;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.VoidFunction;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.apache.hadoop.fs.*;

import um.ece.spark.cluster.util.HDFSUtil;
import um.ece.spark.cluster.util.SparkDriver;


public class TripleGenerator {
	
	public static void main(String args[]) throws Exception {
		
		PropertiesConfiguration conf = new PropertiesConfiguration(args[0]);
		String univss = conf.getString("univ.gen.number");
		String sparkConfigFile = conf.getString("spark.properties");
		final String ontology = conf.getString("ontology");
		final String output = conf.getString("ontology.output");
		final String hadoopConf = conf.getString("hadoop.conf.dir");
		
		List<Integer> ids = new ArrayList<Integer>();
		int univs = Integer.parseInt(univss);
		for (int i=0; i<univs; i++) 
			ids.add(i);

		
		JavaSparkContext sc = SparkDriver.sparkContext(sparkConfigFile);
		JavaRDD<Integer> rdd = sc.parallelize(ids, 400);
		
		rdd.foreach(new VoidFunction<Integer>() {
			private static final long serialVersionUID = 11212312L;
			public void call(Integer t) throws Exception {
				write(ontology, output, t, hadoopConf); 
			}
		});	
	}
	

	public static void write(String aboxFile, String destFile, int i, String hadoopConf) {
		
		try {
			
			FileSystem fs = HDFSUtil.getFS(hadoopConf + "/core-site.xml");
			Path p1 = new Path(destFile+"/ind/univ_" + i);
			Path p2 = new Path(destFile+"/rel/univ_" + i);
			
			PrintWriter pw1 = new PrintWriter(fs.create(p1), true);
			PrintWriter pw2 = new PrintWriter(fs.create(p2), true);
			
			File afile = new File(aboxFile);
			OWLOntology abox = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(afile);
			Set<OWLAxiom> aboxAx = abox.getABoxAxioms(false);
			
			String id = UUID.randomUUID().toString();
			System.out.println(id);
			//String prefix = "http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl";
			
			for (OWLAxiom ax : aboxAx) {
				if (ax.isOfType(AxiomType.CLASS_ASSERTION)) {
					OWLClassAssertionAxiom clsAx = (OWLClassAssertionAxiom) ax;
					String ind = clsAx.getIndividual().toString();
					ind = ind.substring(0, ind.length()-1) + "_" + id + ">";
					String cls = clsAx.getClassExpression().toString();
					String type = "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>";
					pw1.println(ind + " " + type + " " + cls);
				}
				else if (ax.isOfType(AxiomType.OBJECT_PROPERTY_ASSERTION)) {
					OWLObjectPropertyAssertionAxiom opAx = (OWLObjectPropertyAssertionAxiom) ax;
					String sub = opAx.getSubject().toString();
					String obj = opAx.getObject().toString();
					sub = sub.substring(0, sub.length()-1) + "_" + id + ">";
					obj = obj.substring(0, obj.length()-1) + "_" + id + ">";
					
					OWLObjectPropertyExpression op = opAx.getProperty();
					pw2.println(sub + " " + op + " " + obj);
				}
			}
			
			pw1.close();
			pw2.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
}
