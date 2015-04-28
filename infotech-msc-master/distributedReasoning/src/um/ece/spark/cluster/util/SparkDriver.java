package um.ece.spark.cluster.util;

import java.util.Iterator;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;

public class SparkDriver {
	
	public static JavaSparkContext sparkContext(String sparkConfigFile) throws ConfigurationException {
		// Spark Driver
		org.apache.commons.configuration.Configuration sparkConfig = new PropertiesConfiguration(sparkConfigFile);
		// set the spark configurations
		SparkConf sparkConf = new SparkConf();
		Iterator<?> it = sparkConfig.getKeys();
		while (it.hasNext()){
			String key = it.next().toString();
			sparkConf.set(key, sparkConfig.getString(key));
		}
		
		return new JavaSparkContext(sparkConf);		
	}

}
