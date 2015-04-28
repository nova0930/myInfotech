package um.ece.spark.cluster.util

import org.apache.spark.rdd.RDD
import org.semanticweb.owlapi.model.OWLOntology
import org.semanticweb.owlapi.util.DefaultPrefixManager
import org.semanticweb.owlapi.apibinding.OWLManager
import um.ece.abox.module.helper.db.titan.TitanABoxHelper
import org.apache.commons.configuration.MapConfiguration
import um.ece.abox.module.helper.memo.SHIQOntologyRBoxHelper
import um.ece.abox.module.condition.SyntacticConditionChecker
import um.ece.abox.module.parser.ontology.SHIQOntologyParser
import um.ece.abox.msc.SHIMscExtractor
import org.semanticweb.owlapi.model.OWLClassExpression
import um.ece.abox.module.helper.db.titan.TitanNamedIndividual
import org.apache.hadoop.fs.Path
import java.io.PrintWriter
import java.util.UUID
import com.google.common.base.Stopwatch
import java.util.concurrent.TimeUnit
import java.util.LinkedList
import collection.JavaConverters
import scala.collection.mutable.ArrayBuffer

class SparkMSC {
  
  def runMsc(ids : RDD[String], 
              ontPrefix : String,
              tbox : OWLOntology, 
              titanConfig : java.util.Map[String,Any],
              query : OWLClassExpression,
              hadoopConf : String, 
              logPath : String) : RDD[TitanNamedIndividual] = {

ids.mapPartitions(ds => {


          val fs = HDFSUtil.getFS(hadoopConf + "/core-site.xml");
		      val p = new Path(logPath+"/" + UUID.randomUUID().toString());
		      val pw = new PrintWriter(fs.create(p));
        
		      // create a stopwatch to measure the timing
		      var startTime = System.currentTimeMillis();
		        
		      // initializing TBox analyzer and MSC calculator
          val prefixManager = new DefaultPrefixManager(ontPrefix);
          val df = OWLManager.getOWLDataFactory();
          val aboxHelper = new TitanABoxHelper(df, ontPrefix, new MapConfiguration(titanConfig));
          val rboxHelper = new SHIQOntologyRBoxHelper(tbox);
          val condChecker = new SyntacticConditionChecker(tbox, aboxHelper, rboxHelper);
          val ontParser = new SHIQOntologyParser(tbox);
          ontParser.parseOntology();
				  ontParser.accept(condChecker);
				  val msc = new SHIMscExtractor(aboxHelper, condChecker, df);
				  val reasoner = condChecker.getReasoner();
				  
				  pw.println("Initiation Time :\t" + (System.currentTimeMillis() - startTime));
				  pw.println("MSC Time\tReasoning Time");
				  
				  // use MSC for instance checking
				  val result = ArrayBuffer[TitanNamedIndividual]();
				  ds.foreach(id => {
              startTime = System.currentTimeMillis();
				      val ind = new TitanNamedIndividual(df, id, prefixManager);
				      val cls = msc.getMSC(ind); 
				      val mscTime = System.currentTimeMillis() - startTime;
				      startTime = System.currentTimeMillis();
				      val ax = df.getOWLSubClassOfAxiom(cls, query);
				      val b = reasoner.isEntailed(ax);
				      val reasoningTime = System.currentTimeMillis() - startTime;
				      pw.println(mscTime + "\t" + reasoningTime);
				      if (b) result += ind;
				  });
				  
				  pw.close();
				  result.iterator;
      }, true);
  }

}