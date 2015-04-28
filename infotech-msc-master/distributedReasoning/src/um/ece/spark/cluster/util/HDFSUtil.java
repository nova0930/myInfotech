package um.ece.spark.cluster.util;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

public class HDFSUtil {
	
	public static FileSystem getFS(String config) throws IOException {
		Configuration conf = new Configuration();
		conf.addResource(new Path(config));
		return FileSystem.get(conf);
	}
	
	public static void rmDir(FileSystem fs, String path) {
		try	{
			fs.delete(new Path(path), true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void mkDir(FileSystem fs, String path) {
		try {
			Path p = new Path(path);
			if (fs.exists(p))
				rmDir(fs, path);
			
			fs.mkdirs(p);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
