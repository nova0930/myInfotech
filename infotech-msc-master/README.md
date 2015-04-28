# Steps for Testing MSC with Spark on AWS

###Cluster Creation and Initialization

1. Create instances in AWS
2. Set proper parameters in fabric script, including master/data node names, cpus, memories, etc.
3. In your pc command line, under fabric folder
    
    ```
    bash> rm .inventory.yaml
    bash> fab init
    ```
    
4. You should see the master node IP and data node IPs in the command line output.
5. Install softwares

    ```
    fab -i ~/.ssh/genetegra.pem -P init keyless_ssh disableIPV6 install_java increase_limits install_hadoop install_scala install_spark install_hbase install_titan install_hazelcast
    ```
    
6. Commands to start different softwares. Notice that, **hadoop_format** should only run in the first time; later to start Hadoop, just use **hadoop_start**.

    ```
    Hadoop:    fab -i ~/.ssh/genetegra.pem init hadoop_format hadoop_start
    Spark:     fab -i ~/.ssh/genetegra.pem init spark_start
    HBase:     fab -i ~/.ssh/genetegra.pem -P init sync_time hbase_start
    Hazelcast: fab -i ~/.ssh/genetegra.pem -P init hazelcast_start    
    ```
    
7. Commands to stop different softwares.

    ```
    Hadoop:    fab -i ~/.ssh/genetegra.pem init hadoop_stop
    Spark:     fab -i ~/.ssh/genetegra.pem init spark_stop
    HBase:     fab -i ~/.ssh/genetegra.pem init hbase_stop
    Hazelcast: fab -i ~/.ssh/genetegra.pem -P init hazelcast_stop   
    ```
    
8. Check proper services are running in the cluster.

    ```
    fab -i ~/.ssh/genetegra.pem init execmd:jps
    ```


###Data Generation



###Data Ingestion into HBase


###HBase Migration from S3 to HDFS

1. Create an AWS cluster and install all necessary softwares
2. Start Hadoop using fabric
3. Login to the master of the cluster
4. In the master, run command to download data from S3 to HDFS, for example
    
    ```
    hadoop distcp -i s3://infotech-msc/data/one-billion/hbase /hbase
    hadoop distcp -i s3://infotech-msc/data/one-billion/univ_id /mnt/data/univ_id
    ```
    
5. Start HBase using fabric
6. HBase may take a while (say 5-10 minutes) to gather all the table information in the master, and the Titan graph data is only accessible after this procedure. To speed up this procedure, go to /mnt/tools/hbase/bin, and run the HBase shell using ``` ./hbase shell``` . In the HBase shell, try to run the following command
     
     ```
     hbase> describe 'univ'
     hbase> count 'univ'
     ```
  
7. Let the counting run for a couple of minutes. Meanwhile, using the Titan API to check if the data in HBase is ready.
     
    ```
    ubuntu> cd /mnt/tools/titan/bin
    ubuntu> ./gremlin.sh
    gremlin> g = TitanFactory.open("hbase.properties") 
    ```
    
8. If the graph data in HBase is accessible through gremlin, the migration of HBase data from S3 to HDFS is then done and successful.

###Run the MSC test using Spark

1. After **hbase** and **univ_id** files are loaded into HDFS, it is almost ready to run the test, and the only thing left now is to set the properties files and input data for the Java program.

2. Set **hbase.properties**: 

    The thing that needs to be changed there is the **storage.hostname**.

    ```
    ubuntu> cat /msc/tools/hbase/conf/hbase-site.xml
    ```
Copy the server IPs printed in console and paste it into the **hbase.properties** file as **storage.hostname**.

3. Prepare data input: 
    
    Repartition **univ_id** files if necessary using **spark-shell**. For example:
    
    ```
    ubuntu> ./spark-shell --master <master_url> --driver-memory 20G --executor-memory 20G
    scala> val t1 = sc.textFile("hdfs://ip:8020/mnt/data/univ_id");
    scala> val t2 = t1.repartition(1000); 
    scala> t2.saveAsTextFile("hdfs://ip:8020/mnt/data/test/univ_id");
    ```
    
    Copy (part of) **univ_id** files into a proper position in HDFS, which will be used as the data input for the Spark program.

4. Set correct data input & output paths in **msc.properties** file.

5. Run the java command for Spark program.

   ```
   ubuntu> java -cp distributedReasoning.0.0.1.jar main.TestSparkMSC msc.properties
   ```