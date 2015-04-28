package um.ece.spark.cluster.util

import com.hazelcast.client.config.ClientConfig
import com.hazelcast.client.HazelcastClient
import com.hazelcast.core.HazelcastInstance
import com.hazelcast.core.IMap
import org.apache.spark.rdd.RDD
import org.apache.commons.configuration.MapConfiguration
import com.thinkaurelius.titan.core.TitanFactory
import org.semanticweb.owlapi.model.OWLOntology
import java.util.HashSet
import collection.JavaConverters._
import org.apache.hadoop.fs.FileSystem
import org.apache.hadoop.fs.Path
import org.apache.hadoop.conf.Configuration
import java.util.UUID
import java.io.PrintWriter

public class RDFLoader {
  
  
  def createTypes(tbox : OWLOntology, titanConfig : java.util.Map[String,Any]) {
    
      // connect to Titan database
      val conf = new MapConfiguration(titanConfig);
      val titanGraph = TitanFactory.open(conf);
    
      // create node properties: name & type
      titanGraph.makeKey("name").dataType(classOf[String]).make();
      titanGraph.makeKey("type").dataType(classOf[String]).make();
      
      // create edge labels: from Ontology roles
      val roles = tbox.getObjectPropertiesInSignature(true);
      roles.asScala.foreach(r => {
          var label = r.toString();
          label = label.substring(label.lastIndexOf('#')+1, label.length()-1);
          titanGraph.makeLabel(label).directed().make();
      })
      
      titanGraph.commit();
  }
  
  
  /**
   * Load Class Assertions in RDF Set.
   */
  def loadNodes(nodeRdfs : RDD[String], titanConfig : java.util.Map[String,Any]) {
    
      nodeRdfs.foreachPartition(rdfs => {
        // connect to Titan database
        val conf = new MapConfiguration(titanConfig);
        val titanGraph = TitanFactory.open(conf);
        
        // connect to DHT
        val clientConfig = new ClientConfig();
        val client = HazelcastClient.newHazelcastClient(clientConfig);
        val map = client.getMap[String, java.lang.Long]("ids");
        //val idGenerator = client.getAtomicLong("counter");
        
        rdfs.foreach(r => {
            val items = r.split(" ");
            val indName = items(0).substring(1, items(0).length-1);
            val clsName = items(2).substring(items(2).lastIndexOf('#')+1, items(2).length-1);
            
            val v = titanGraph.addVertex(null);
            v.setProperty("name", indName);
            v.setProperty("type", clsName);
            map.put(indName, v.getId().toString.toLong);
            //println("map size " + map.size());

        }); // end of rdfs.forewach()
        
        client.shutdown();
        titanGraph.commit();
      });
  }
  
  /**
	   * Load Class Assertions in RDF Set, and save all the assigned DB IDs into HDFS
	   */
	def loadNodesWithIDs(nodeRdfs : RDD[String], 
									titanConfig : java.util.Map[String,Object], hadoopConf : String, logPath : String) {
	  
	  	nodeRdfs.foreachPartition(rdfs => {
        // connect to Titan database
        val conf = new MapConfiguration(titanConfig);
        val titanGraph = TitanFactory.open(conf);
        
        // connect to DHT
        val clientConfig = new ClientConfig();
        val client = HazelcastClient.newHazelcastClient(clientConfig);
        val map = client.getMap[String, java.lang.Long]("ids");
        //val idGenerator = client.getAtomicLong("counter");
        
        // open file in hdfs
		    val fs = HDFSUtil.getFS(hadoopConf + "/core-site.xml");
		    val p = new Path(logPath+"/" + UUID.randomUUID().toString());
		    val pw = new PrintWriter(fs.create(p), true);
        
        rdfs.foreach(r => {
            val items = r.split(" ");
            val indName = items(0).substring(1, items(0).length-1);
            val clsName = items(2).substring(items(2).lastIndexOf('#')+1, items(2).length-1);
            
            val v = titanGraph.addVertex(null);
            v.setProperty("name", indName);
            v.setProperty("type", clsName);
            map.put(indName, v.getId().toString.toLong);
            pw.println(v.getId().toString);
            //println("map size " + map.size());

        }); // end of rdfs.forewach()
        
        pw.close();
        client.shutdown();
        titanGraph.commit();
      });
	}
  
  /**
   * Load Role Assertions in the RDF Set.
   */
  @throws(classOf[Exception])
  def loadEdges(edgeRdfs : RDD[String], titanConfig : java.util.Map[String,Any]) {
     
      edgeRdfs.foreachPartition(rdfs => {
        
        // connect to Titan database
        val conf = new MapConfiguration(titanConfig);
        val titanGraph = TitanFactory.open(conf);
        
        // connect to DHT
        val clientConfig = new ClientConfig();
        val client = HazelcastClient.newHazelcastClient(clientConfig);
        val map = client.getMap[String, java.lang.Long]("ids");
        
        rdfs.foreach(r => {
          val items = r.split(" ");
          val subName = items(0).substring(1, items(0).length-1);
          val objName = items(2).substring(1, items(2).length-1);
          val predicate = items(1).substring(items(1).lastIndexOf('#')+1, items(1).length-1);
          
          val subId = map.get(subName);
          val objId = map.get(objName);
          
          if (subId != null && objId != null) {
              val subV = titanGraph.getVertex(subId);
              val objV = titanGraph.getVertex(objId);
              titanGraph.addEdge(null, subV, objV, predicate);
          } else 
              throw new Exception("ID not found in distributed map. Map size is " + map.size());
        }); // end of rdfs.foreawch()
        
        client.shutdown();
        titanGraph.commit();
      });
  }
  
}