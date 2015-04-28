package um.ece.spark.cluster.util;

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
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.util.DefaultPrefixManager;

import um.ece.abox.module.condition.SyntacticConditionChecker;
import um.ece.abox.module.helper.db.titan.TitanABoxHelper;
import um.ece.abox.module.helper.db.titan.TitanNamedIndividual;
import um.ece.abox.module.helper.memo.SHIQOntologyRBoxHelper;
import um.ece.abox.module.parser.ontology.SHIQOntologyParser;
import um.ece.abox.msc.SHIMscExtractor;

public class SparkMSCJava {
	
	public RDD<TitanNamedIndividual> runMsc(RDD<String> ids, 
									            final String ontPrefix,
									            final OWLOntology tbox, 
									            final Map<String, Object> titanConfig,
									            final OWLClassExpression query,
									            final String hadoopConf, 
									            final String logPath)  {
		
		FlatMapFunction<Iterator<String>, TitanNamedIndividual> f = 
			new FlatMapFunction<Iterator<String>, TitanNamedIndividual>() {
			private static final long serialVersionUID = 2809994240827324027L;
			public Iterable<TitanNamedIndividual> call(Iterator<String> t) throws Exception {
				
				// connect to HDFS log file
		        FileSystem fs = HDFSUtil.getFS(hadoopConf + "/core-site.xml");
				Path p = new Path(logPath+"/" + UUID.randomUUID().toString());
				PrintWriter pw = new PrintWriter(fs.create(p));
		        
				// create a stopwatch to measure the timing
				long startTime = System.currentTimeMillis();
				
				// initializing TBox analyzer and MSC calculator
				DefaultPrefixManager prefixManager = new DefaultPrefixManager(ontPrefix);
		        OWLDataFactory df = OWLManager.getOWLDataFactory();
		        TitanABoxHelper aboxHelper = new TitanABoxHelper(df, ontPrefix, new MapConfiguration(titanConfig));
		        SHIQOntologyRBoxHelper rboxHelper = new SHIQOntologyRBoxHelper(tbox);
		        SyntacticConditionChecker condChecker = new SyntacticConditionChecker(tbox, aboxHelper, rboxHelper);
		        SHIQOntologyParser ontParser = new SHIQOntologyParser(tbox);
		        ontParser.parseOntology();
		        ontParser.accept(condChecker);
		        SHIMscExtractor msc = new SHIMscExtractor(aboxHelper, condChecker, df);
		        OWLReasoner reasoner = condChecker.getReasoner();
						  
		        pw.println("Initiation Time :\t" + (System.currentTimeMillis() - startTime));
		        pw.println("MSC Time\tReasoning Time");
		        
		        // use MSC for instance checking
		        ArrayList<TitanNamedIndividual> result = new ArrayList<TitanNamedIndividual>();
				while (t.hasNext()) {
					String id = t.next();
					startTime = System.currentTimeMillis();
					TitanNamedIndividual ind = new TitanNamedIndividual(df, id, prefixManager);
				    OWLClassExpression cls = msc.getMSC(ind); 
				    long mscTime = System.currentTimeMillis() - startTime;
				    startTime = System.currentTimeMillis();
				    OWLAxiom ax = df.getOWLSubClassOfAxiom(cls, query);
				    boolean b = reasoner.isEntailed(ax);
				    long reasoningTime = System.currentTimeMillis() - startTime;
				    pw.println(mscTime + "\t" + reasoningTime);
				    if (b) result.add(ind);
				}
		        
				return result;
			}
			
		};
		
		return ids.toJavaRDD().mapPartitions(f, true).rdd();
	}
}
