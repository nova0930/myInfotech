from __future__ import with_statement
from fabric.api import *
from fabric.contrib.console import confirm
from fabric.contrib.files import upload_template, exists, append
from collections import namedtuple
import os
from uuid import uuid4
import boto.ec2
import sys
import yaml

# hadoop distcp -i /mnt/data/faunus/graph/whole/job-2 s3://amp3-research/data/HDFS/Faunus/MG/whole/job-0
# hadoop distcp -i s3://amp3-research/data/HDFS/Faunus/MG/whole/job-0 /mnt/data/faunus/graph/whole/job-0
# hadoop distcp -i /hbase s3://amp3-research/data/HDFS/hbase
# s3n://AKIAICSJX2ASO7ROLBKA:8XW8Nf1l0ZyGto3dqKJ5Hai%2FvfnyZ6ZBPEjpTBcg@amp3-research/data/HDFS/Faunus/MG/whole/job-0
# fab -i ~/.ssh/genetegra.pem -P init keyless_ssh disableIPV6 install_java increase_limits install_hadoop install_scala install_spark install_hbase install_titan install_hazelcast

################# Environment Setup #################

env.data_loc = "/mnt"
env.tools_loc = "/msc"
env.server_cores = 32
env.master = "c3master"
env.slave = "c3data"
server_region = "us-east-1"
security_group = "msc"
env.s3PublicKey =  "AKIAJMXGUAEPME4MOXQQ"
env.s3PrivateKey = "5UwVESHmFN4F/75mGH5amjAJshEj2Ggtd6qd8zIY"

# Following properties are set automatically 
env.tools_folder = "%s/tools" % env.tools_loc
env.data_folder = "%s/data" % env.data_loc
env.data_tools = env.tools_folder
env.hadoopRoot = "%s/hadoop_data" % env.data_loc
env.tmpDir = "%s/tmp" % env.hadoopRoot
env.mapredJavaOpts = "-Xmx1000m"
env.reduceJavaOpts = "-Xmx1000m"
env.hadoopHeapSize = 1000
env.sparkDriverMem = "6G"
env.hbaseHeapSize = 8000
env.hazelcastHeapSize = 10240
env.mapredRoot = env.hadoopRoot
env.mapTasksMax = env.server_cores
env.reduceTasksMax = env.server_cores
env.dfsDataDir = env.hadoopRoot
env.nodeMemory = "64416"
env.user = "ubuntu"
env.inventory_file = '.inventory.yaml'
keylist = []
private_key = "~/.ssh/semantics.pem"


@task
# Always put this function ahead of any other functions that run on remote servers
# e.g. fab -i ~/.ssh/senzari-dev-oregon.pem init execmd:"ls ~/"
# e.g. fab -i ~/.ssh/senzari-dev-oregon.pem -P init inistall_mvn 
# e.g. fab -i ~/.ssh/senzari-dev-oregon.pem -P init:user="hadoop" inistall_hadoop
def init(user="ubuntu", iptype = "extip"):
    env.user = user
    env.roledefs["master"] = lookup_server(env.master, iptype=iptype)
    env.roledefs["slave"] = lookup_server(env.slave, iptype=iptype)
    env.roledefs["all"] = env.roledefs["master"] + env.roledefs["slave"] 
    print env.roledefs["master"]
    print env.roledefs["slave"]


@task
@roles("master", "slave")
def execmd(cmd):
    run(cmd)


################# Functions for Deployment #################
@task
def copy_dir(dir, dest="~/", role="all"):
    for item in env.roledefs[role]:
        target = "%s:%s" % (item, dest)
        local("scp -i %s -r %s %s" % (private_key, dir, target))

@task
def copy_file(src, dest="~/"):
    for item in env.roledefs["all"]:
        target = "%s:%s" % (item, dest)
        local("scp -i %s %s %s" % (private_key, src, target))        

@task
@roles("all")
def hazelcast_start():
    hazel = "%s/hazelcast/bin/" % env.tools_folder
    cmd = "./server.sh"
    with cd(hazel):
        run("dtach -n /tmp/hazelcast %s" % cmd)

@task
@roles("all")
def hazelcast_stop():
    pid = run("cat /tmp/hazelcast.pid")
    run("kill %s" % pid)       
        
@task
@serial
@roles("master")
def spark_start():
    # run the cluster lanuch scrript
    spark_sbin = "%s/spark/sbin" % env.tools_folder
    with cd(spark_sbin):
        run("./start-all.sh")

@task
@roles("master")
def spark_stop():
    spark_sbin = "%s/spark/sbin" % env.tools_folder
    with cd(spark_sbin):
        run("./stop-all.sh")
        
        
@task
@roles("master")
def hadoop_format():
    run("hadoop namenode -format")

@task
@serial
@roles("master")
def hadoop_start():
    run("start-all.sh")
    

@task
@roles("master")
def hadoop_stop():
    run("stop-all.sh")

@task
@roles("master")
def hadoop2_format():
    run("hadoop namenode -format")

@task
@serial
@roles("master")
def hadoop2_start():
    run("$HADOOP_HOME/sbin/start-dfs.sh")
    run("$HADOOP_HOME/sbin/start-yarn.sh")
    
@task
@roles("master")
def hadoop2_stop():
    run("$HADOOP_HOME/sbin/stop-dfs.sh")
    run("$HADOOP_HOME/sbin/stop-yarn.sh")

    
    
@task
@roles("master")
def hbase_start():
    run("start-hbase.sh")
    
@task
@roles("all")
def sync_time():
    sudo("ntpdate ntp.ubuntu.com")
    
@task
@roles("master")
def hbase_stop():
    run("stop-hbase.sh")
    
    

@task
def keyless_ssh():
    execute(sshkey_gen)
    execute(sshkey_flood)

@task
@serial
@roles("master", "slave")
def sshkey_gen():
    if not exists("~/.ssh/id_rsa.pub"):
        run("ssh-keygen -t rsa -P \"\" -f ~/.ssh/id_rsa")
    key = run("cat ~/.ssh/id_rsa.pub")
    keylist.append(key)
    if not exists("~/.ssh/config"):
        run("touch ~/.ssh/config")
    #run("echo \$'Host *\n\tStrictHostKeyChecking no' >> ~/.ssh/config")
    append("~/.ssh/config", "Host *", use_sudo = True)
    append("~/.ssh/config", "	StrictHostKeyChecking no", use_sudo = True)

    
@task
@parallel
@roles("master", "slave")
def sshkey_flood():
     for key in keylist:
        #run("echo %s >> ~/.ssh/authorized_keys" % key)
        append("~/.ssh/authorized_keys", key, use_sudo = True)


@task
@roles("master")
def faunus_run():
    faunusDir = "%s/faunus/bin" % env.data_tools
    with open("faunus.script") as f:
        content = f.readlines()
        cmd = ""
        for line in content:
            cmd = cmd + line.strip() + ";"
        with cd(faunusDir):
            upload_template('faunus.properties', "faunus.properties", context={}, use_jinja=True, template_dir=None, use_sudo=True)
            run("echo \'%s\' | ./gremlin.sh" % cmd)
        
             
################# Functions for Software Installation #################

@task
@roles("all")
def install_mvn():
	create_tools_folder()
	with cd(env.tools_folder):
		run("rm -rf maven*")
		run("wget http://apache.mirrors.hoobly.com/maven/maven-3/3.1.1/binaries/apache-maven-3.1.1-bin.tar.gz")
		run("tar xzf apache-maven-3.1.1-bin.tar.gz")
		run("rm apache-maven-3.1.1-bin.tar.gz")
		run("ln -s apache-maven-3.1.1 maven")
		#run("echo \'export PATH=%s/maven/bin:$PATH\' >> ~/.bashrc" % env.tools_folder)
        append("~/.bashrc", "export PATH=%s/maven/bin:$PATH" % env.tools_folder, use_sudo = True)
		#run("echo \'export PATH=%s/maven/bin:$PATH\' >> ~/.bash_profile" % env.tools_folder)
        append("~/.bash_profile", "export PATH=%s/maven/bin:$PATH" % env.tools_folder, use_sudo = True)


@task
@roles("master")
def install_graphlab():
        sudo("apt-get install git -y")
        sudo("apt-get install openmpi-bin -y")
        create_tools_folder()
        with cd(env.tools_folder):
        	run("rm -rf graphlab*")
        	run("git clone https://github.com/graphlab-code/graphlab")
        	sudo("apt-get install gcc g++ build-essential libopenmpi-dev default-jdk cmake zlib1g-dev -y")
        	with cd("graphlab"):
            		run("./configure")
            		with cd("release/toolkits/graph_analytics"):
                		run("make -j10")
            		with cd("release/toolkits/collaborative_filtering"):
                		run("make -j10")
                    	with cd("release/toolkits/clustering"):
                		run("make -j10")
                    	#with cd("release/toolkits/linear_solvers"):
                		#run("make -j10")
                    	with cd("release/toolkits/topic_modeling"):
                		run("make -j10")
        graphlab_config()

@task
@roles("master")        
def graphlab_config():
    # deploy the mpi and graphlab
    masters = lookup_server_ip(env.master, iptype="ip")
    slaves = lookup_server_ip(env.slave, iptype="ip")
    run("rm -f ~/machines")
    run("touch ~/machines")
    for item in masters:
        run("echo %s >> ~/machines" % item)
    for item in slaves:
        run("echo %s >> ~/machines" % item)
    #copy graphlab to all mpi machines
    #with cd("%s/graphlab/release/toolkits" % env.tools_folder):
        #run("%s/graphlab/scripts/mpirsync" % env.tools_folder)
    #with cd("%s/graphlab/deps/local" % env.tools_folder):
        #run("%s/graphlab/scripts/mpirsync" % env.tools_folder)
        

@task
@roles("master", "slave")
def install_scala():
    create_tools_folder()
    with cd (env.tools_folder):
        run("rm -rf scala*")
        run("wget http://www.scala-lang.org/files/archive/scala-2.10.3.tgz")
        run("tar xzf scala-2.10.3.tgz")
        run("rm scala-2.10.3.tgz")
        run("ln -s scala-2.10.3 scala")
        #run("echo \'export SCALA_HOME=%s/scala\' >> ~/.bashrc" % env.tools_folder)
        append("~/.bashrc", "export SCALA_HOME=%s/scala" % env.tools_folder, use_sudo = True)
        #run("echo \'export SCALA_HOME=%s/scala\' >> ~/.bash_profile" % env.tools_folder)
        append("~/.bash_profile", "export SCALA_HOME=%s/scala" % env.tools_folder, use_sudo = True)
        #run("echo \'export PATH=%s/scala/bin:$PATH\' >> ~/.bashrc" % env.tools_folder)
        append("~/.bashrc", "export PATH=%s/scala/bin:$PATH" % env.tools_folder, use_sudo = True)
        #run("echo \'export PATH=%s/scala/bin:$PATH\' >> ~/.bash_profile" % env.tools_folder)
        append("~/.bash_profile", "export PATH=%s/scala/bin:$PATH" % env.tools_folder, use_sudo = True)


@task
@roles("all")
def install_hazelcast():
    create_tools_folder()
    sudo("apt-get install zip -y")
    sudo("apt-get install dtach -y")
    with cd (env.tools_folder):
        sudo("rm -rf hazelcast*")
        run("wget https://www.dropbox.com/s/jh6yfvuh6djvk6w/hazelcast.zip --no-check-certificate")
        run("unzip hazelcast.zip")
        run("rm hazelcast.zip")
        run("rm -rf __MACOSX")
    hazelcast_config()

@task
@roles("all")
def hazelcast_config():
    hazelcast_conf = "%s/hazelcast/bin/" % env.tools_folder
    masters = lookup_server_ip(env.master, "ip")
    slaves = lookup_server_ip(env.slave, "ip")
    machines = masters[0]
    for item in slaves:
    	machines = machines + "," + item
    #machines = machines[1:]
    context = {
		'ec2PublicKey': env.s3PublicKey,
		'ec2PrivateKey': env.s3PrivateKey,
        'serverRegion': server_region,
        'securityGroup': security_group,
		'heapSize': env.hazelcastHeapSize,
        'hazelcastMembers': machines
	}
    with cd(hazelcast_conf):
        upload_template('template.hazelcast.xml', "hazelcast.xml", context=context, use_jinja=True, template_dir=None, use_sudo=True)
        
@task
@roles("master", "slave")
def install_spark(hadoop="1.2.1"):
    create_tools_folder()
    sudo("apt-get install libgfortran3 -y")
    with cd (env.tools_folder):
        run("rm -rf spark*")
        #run("wget http://d3kbcqa49mib13.cloudfront.net/spark-1.0.0-bin-hadoop1.tgz")
        run("wget http://d3kbcqa49mib13.cloudfront.net/spark-1.0.0-bin-hadoop1.tgz")
        run("tar xzf spark-1.0.0-bin-hadoop1.tgz")
        run("rm spark-1.0.0-bin-hadoop1.tgz")
        run("ln -s spark-1.0.0-bin-hadoop1 spark")
        append("~/.bashrc", "export SPARK_HOME=%s/spark" % env.tools_folder, use_sudo = True)
        append("~/.bash_profile", "export SPARK_HOME=%s/spark" % env.tools_folder, use_sudo = True)
    spark_config()

 
# refer to http://spark.apache.org/docs/1.0.0/building-with-maven.html
# for building Spark with other versions of Hadoop    
def spark_build(hadoop="1.2.1"):
    sudo("apt-get install unzip -y")
    with cd (env.tools_folder):
        with cd ("spark"):
            run("export MAVEN_OPTS=\"-Xmx2g -XX:MaxPermSize=512M -XX:ReservedCodeCacheSize=512m\"; mvn -Dhadoop.version=%s -DskipTests clean package" % hadoop)

@task
@roles("all")
def spark_config():
    create_data_folder()
    masters = lookup_server_ip(env.master, iptype="ip")
    slaves = lookup_server_ip(env.slave, iptype="ip")
    spark_conf = "%s/spark/conf" % env.tools_folder
    localIp = run("ifconfig | perl -nle'/dr:(\\S+)/ && print $1' | grep -v \"127.0.0.1\"")
    context = {
		'master': masters[0],
		'local_dir': "%s/spark" % env.data_folder,
        	'local_ip': localIp,
		'driver_mem': env.sparkDriverMem
	}
    # configure the slaves for spark
    if not exists(spark_conf):
        abort("Aborting, Spark does not exists")
    with cd(spark_conf):
        run("rm -f slaves")
        run("touch slaves")
        for item in slaves:
            run("echo %s >> slaves" % item)
        upload_template('template.spark-env.sh', "spark-env.sh", context=context, use_jinja=True, template_dir=None, use_sudo=True)
        sudo("chmod +x spark-env.sh")

            

@task
@roles("master", "slave")
def install_hadoop():
    create_tools_folder()
    sudo("mkdir -p %s" % env.hadoopRoot)
    sudo("chown \`whoami\` %s" % env.hadoopRoot) 
    with cd(env.tools_folder):
        run("rm -rf hadoop*")
        run("wget http://apache.mirrors.tds.net/hadoop/common/hadoop-1.2.1/hadoop-1.2.1-bin.tar.gz")
        run("tar xzf hadoop-1.2.1-bin.tar.gz")
        run("rm hadoop-1.2.1-bin.tar.gz")
        run("ln -s hadoop-1.2.1 hadoop")
    hadoop_config()

@task
@roles("all")
def hadoop_env_config():
    append("~/.bashrc", "export HADOOP_HOME=%s/hadoop" % env.tools_folder, use_sudo = True)
    append("~/.bash_profile", "export HADOOP_HOME=%s/hadoop" % env.tools_folder, use_sudo = True)
    append("~/.bashrc", "export PATH=%s/hadoop/bin:$PATH" % env.tools_folder, use_sudo = True)
    append("~/.bash_profile", "export PATH=%s/hadoop/bin:$PATH" % env.tools_folder, use_sudo = True)
    append("~/.bashrc", "export HADOOP_CONF_DIR=%s/hadoop/conf" % env.tools_folder, use_sudo = True)
    append("~/.bash_profile", "export HADOOP_CONF_DIR=%s/hadoop/conf" % env.tools_folder, use_sudo = True)
    hadoop_env = "%s/hadoop/conf/hadoop-env.sh" % env.tools_folder
    java_home = run("echo $JAVA_HOME")
    append(hadoop_env, 'export JAVA_HOME=%s' % java_home, use_sudo = True)
    append(hadoop_env, 'export HADOOP_HEAPSIZE=%s' % env.hadoopHeapSize, use_sudo = True)

@task
@roles("all")    
def hadoop_config(iptype="ip"):
    hadoop_env_config()
    hadoop_conf = "%s/hadoop/conf" % env.tools_folder
    hadoop_core_site = "%s/core-site.xml" % hadoop_conf
    hadoop_hdfs_site = "%s/hdfs-site.xml" % hadoop_conf
    hadoop_mapred_site = "%s/mapred-site.xml" % hadoop_conf
    masters = lookup_server_ip(env.master, iptype)
    slaves = lookup_server_ip(env.slave, iptype)
    context = {
		'master': masters[0],
		's3PublicKey': env.s3PublicKey,
		's3PrivateKey': env.s3PrivateKey,
		'tmpDir': env.tmpDir,
		'mapredJavaOpts': env.mapredJavaOpts,
		'reduceJavaOpts': env.reduceJavaOpts,
		'mapredRoot': env.mapredRoot,
		'mapTasksMax': env.mapTasksMax,
		'reduceTasksMax': env.reduceTasksMax,
		'dfsDataDir': env.dfsDataDir,
		'totalTasks' : len(env.roledefs["slave"]) * env.reduceTasksMax
	}
    upload_template('template.core-site.xml', hadoop_core_site, context=context, use_jinja=True, template_dir=None, use_sudo=True)
    upload_template('template.hdfs-site.xml', hadoop_hdfs_site, context=context, use_jinja=True, template_dir=None, use_sudo=True)
    upload_template('template.mapred-site.xml', hadoop_mapred_site, context=context, use_jinja=True, template_dir=None, use_sudo=True)
    with cd(hadoop_conf):
        run("rm -f masters")
        run("touch masters")
        run("echo %s >> masters" % masters[0])
        run("rm -f slaves")
        run("touch slaves")
        for item in slaves:
                run("echo %s >> slaves" % item)


 
def create_tools_folder():
	sudo("mkdir -p %s" % env.tools_folder)
	sudo("chown \`whoami\` %s" % env.tools_folder)


def create_data_folder():
    	sudo("mkdir -p %s" % env.data_folder)
    	sudo("chown \`whoami\` %s" % env.data_folder)

@task
@roles("all")
def increase_limits():
	append("/etc/security/limits.conf", "root soft nofile 65535", use_sudo = True)
	append("/etc/security/limits.conf", "root hard nofile 65535", use_sudo = True)
	append("/etc/security/limits.conf", "ubuntu soft nofile 655350", use_sudo = True)
	append("/etc/security/limits.conf", "ubuntu hard nofile 655350", use_sudo = True)


@task
@roles("master", "slave")
def install_hadoop2():
    create_tools_folder()
    sudo("mkdir -p %s" % env.hadoopRoot)
    sudo("chown \`whoami\` %s" % env.hadoopRoot) 
    with cd(env.tools_folder):
        run("rm -rf hadoop*")
        run("wget http://mirror.nexcess.net/apache/hadoop/common/hadoop-2.4.1/hadoop-2.4.1.tar.gz")
        run("tar xzf hadoop-2.4.1.tar.gz")
        run("rm hadoop-2.4.1.tar.gz")
        run("ln -s hadoop-2.4.1 hadoop")  
        append("~/.bashrc", "export HADOOP_HOME=%s/hadoop" % env.tools_folder, use_sudo = True)
        append("~/.bash_profile", "export HADOOP_HOME=%s/hadoop" % env.tools_folder, use_sudo = True)
	append("~/.bashrc", "export HADOOP_MAPRED_HOME=%s/hadoop" % env.tools_folder, use_sudo = True)
        append("~/.bash_profile", "export HADOOP_MAPRED_HOME=%s/hadoop" % env.tools_folder, use_sudo = True)
	append("~/.bashrc", "export HADOOP_COMMON_HOME=%s/hadoop" % env.tools_folder, use_sudo = True)
        append("~/.bash_profile", "export HADOOP_COMMON_HOME=%s/hadoop" % env.tools_folder, use_sudo = True)
	append("~/.bashrc", "export HADOOP_HDFS_HOME=%s" % env.dfsDataDir, use_sudo = True)
        append("~/.bash_profile", "export HADOOP_HDFS_HOME=%s" % env.dfsDataDir, use_sudo = True)
	append("~/.bashrc", "export YARN_HOME=%s/hadoop" % env.tools_folder, use_sudo = True)
        append("~/.bash_profile", "export YARN_HOME=%s/hadoop" % env.tools_folder, use_sudo = True)
	append("~/.bashrc", "export HADOOP_CONF_DIR=%s/hadoop/etc/hadoop" % env.tools_folder, use_sudo = True)
        append("~/.bash_profile", "export HADOOP_CONF_DIR=%s/hadoop/etc/hadoop" % env.tools_folder, use_sudo = True)
	append("~/.bashrc", "export HADOOP_COMMON_LIB_NATIVE_DIR=%s/hadoop/lib/native" % env.tools_folder, use_sudo = True)
        append("~/.bash_profile", "export HADOOP_COMMON_LIB_NATIVE_DIR=%s/hadoop/lib/native" % env.tools_folder, use_sudo = True)
	append("~/.bashrc", "export HADOOP_OPTS=\"-Djava.library.path=%s/hadoop/lib\"" % env.tools_folder, use_sudo = True)
        append("~/.bash_profile", "export HADOOP_OPTS=\"-Djava.library.path=%s/hadoop/lib\"" % env.tools_folder, use_sudo = True)
        append("~/.bashrc", "export PATH=%s/hadoop/bin:$PATH" % env.tools_folder, use_sudo = True)
        append("~/.bash_profile", "export PATH=%s/hadoop/bin:$PATH" % env.tools_folder, use_sudo = True)
    hadoop2_config()
    disableIPV6()

@task
@roles("all")
def disableIPV6():
     append("/etc/sysctl.conf", "net.ipv6.conf.all.disable_ipv6 = 1", use_sudo = True)
     append("/etc/sysctl.conf", "net.ipv6.conf.default.disable_ipv6 = 1", use_sudo = True)
     append("/etc/sysctl.conf", "net.ipv6.conf.lo.disable_ipv6 = 1", use_sudo = True)
     sudo("sysctl -p")

  
def hadoop2_env_config():
    hadoop_env = "%s/hadoop/etc/hadoop/hadoop-env.sh" % env.tools_folder
    java_home = run("echo $JAVA_HOME")
    append(hadoop_env, 'export JAVA_HOME=%s' % java_home, use_sudo = True)
    append(hadoop_env, 'export HADOOP_HEAPSIZE=%s' % env.hadoopHeapSize, use_sudo = True)
    append(hadoop_env, 'export HADOOP_CLASSPATH=%s/hadoop/share/hadoop/hdfs/*:$HADOOP_CLASSPATH' % env.tools_folder)
    append(hadoop_env, 'export HADOOP_CLASSPATH=%s/hadoop/share/hadoop/hdfs/lib/*:$HADOOP_CLASSPATH' % env.tools_folder)
    append(hadoop_env, 'export HADOOP_CLASSPATH=%s/hadoop/share/hadoop/tools/lib/*:$HADOOP_CLASSPATH' % env.tools_folder)


@task
@roles("all")    
def hadoop2_config(iptype="ip"):
    hadoop2_env_config()
    hadoop_conf = "%s/hadoop/etc/hadoop" % env.tools_folder
    hadoop_core_site = "%s/core-site.xml" % hadoop_conf
    hadoop_hdfs_site = "%s/hdfs-site.xml" % hadoop_conf
    hadoop_mapred_site = "%s/mapred-site.xml" % hadoop_conf
    hadoop_yarn_site = "%s/yarn-site.xml" % hadoop_conf
    masters = lookup_server_ip(env.master, iptype)
    slaves = lookup_server_ip(env.slave, iptype)
    context = {
		'master': masters[0],
		's3PublicKey': env.s3PublicKey,
		's3PrivateKey': env.s3PrivateKey,
		'tmpDir': env.tmpDir,
		'mapredJavaOpts': env.mapredJavaOpts,
		'reduceJavaOpts': env.reduceJavaOpts,
        'nodeMem': env.nodeMemory, 
		'mapredRoot': env.mapredRoot,
		'mapTasksMax': env.mapTasksMax,
		'reduceTasksMax': env.reduceTasksMax,
		'dfsDataDir': env.dfsDataDir,
		'totalTasks' : len(env.roledefs["slave"]) * env.reduceTasksMax
	}
    upload_template('template2.core-site.xml', hadoop_core_site, context=context, use_jinja=True, template_dir=None, use_sudo=True)
    upload_template('template2.hdfs-site.xml', hadoop_hdfs_site, context=context, use_jinja=True, template_dir=None, use_sudo=True)
    upload_template('template2.mapred-site.xml', hadoop_mapred_site, context=context, use_jinja=True, template_dir=None, use_sudo=True)
    upload_template('template2.yarn-site.xml', hadoop_yarn_site, context=context, use_jinja=True, template_dir=None, use_sudo=True)
    with cd(hadoop_conf):
        if exists("masters"):
            run("rm -f masters")
        run("touch masters")
        append("masters", '%s' % masters[0], use_sudo = True)
        run("rm -f slaves")
        run("touch slaves")
        for item in slaves:
                append("slaves", '%s' % item, use_sudo = True)


@task
@roles("master", "slave")
def install_hbase():
    create_tools_folder()
    with cd(env.tools_folder):
        run("rm -rf hbase*")
        #run("wget http://mirror.reverse.net/pub/apache/hbase/hbase-0.94.12/hbase-0.94.12.tar.gz")
        run("wget https://archive.apache.org/dist/hbase/hbase-0.94.12/hbase-0.94.12.tar.gz")
        run("tar xzf hbase-0.94.12.tar.gz")
        run("rm hbase-0.94.12.tar.gz")
        run("ln -s hbase-0.94.12 hbase")
    hbase_config()

    
@task
@roles("master", "slave")
def hbase_env_config():
    append("~/.bashrc", "export HBASE_HOME=%s/hbase" % env.tools_folder, use_sudo = True)
    append("~/.bash_profile", "export HBASE_HOME=%s/hbase" % env.tools_folder, use_sudo = True)
    append("~/.bashrc", "export PATH=%s/hbase/bin:$PATH" % env.tools_folder, use_sudo = True)
    append("~/.bash_profile", "export PATH=%s/hbase/bin:$PATH" % env.tools_folder, use_sudo = True)
    hbase_env = "%s/hbase/conf/hbase-env.sh" % env.tools_folder
    java_home = run("echo $JAVA_HOME")
    append(hbase_env, 'export JAVA_HOME=%s' % java_home, use_sudo = True)
    append(hbase_env, 'export HBASE_HEAPSIZE=%s' % env.hbaseHeapSize, use_sudo = True)
    append(hbase_env, 'export HBASE_MANAGES_ZK=true', use_sudo = True)
    append(hbase_env, 'export HBASE_REGIONSERVER_OPTS=\"$HBASE_REGIONSERVER_OPTS -Xms8G -Xmx8g\"', use_sudo = True)
    

@task
@roles("all")    
def hbase_config(iptype="ip"):
    hbase_env_config()
    hbase_conf = "%s/hbase/conf" % env.tools_folder
    hbase_site = "%s/hbase-site.xml" % hbase_conf
    hbase_hdfs_site = "%s/hdfs-site.xml" % hbase_conf
    masters = lookup_server_ip(env.master, iptype)
    slaves = lookup_server_ip(env.slave, iptype)
    machines = masters[0]
    for item in slaves:
    	machines = machines + "," + item
    context = {
		'master': masters[0],
		's3PublicKey': env.s3PublicKey,
		's3PrivateKey': env.s3PrivateKey,
		'tmpDir': env.tmpDir,
		'mapredJavaOpts': env.mapredJavaOpts,
		'reduceJavaOpts': env.reduceJavaOpts,
		'mapredRoot': env.mapredRoot,
		'mapTasksMax': env.mapTasksMax,
		'reduceTasksMax': env.reduceTasksMax,
		'dfsDataDir': env.dfsDataDir,
		'totalTasks' : len(env.roledefs["slave"]) * env.reduceTasksMax,
		'DataDir' : env.data_folder,
		'machines' : machines
	}
    upload_template('template.hdfs-site.xml', hbase_hdfs_site, context=context, use_jinja=True, template_dir=None, use_sudo=True)
    upload_template('template.hbase-site.xml', hbase_site, context=context, use_jinja=True, template_dir=None, use_sudo=True)
    with cd(hbase_conf):
        run("rm -f regionservers")
        run("touch regionservers")
        for item in slaves:
                run("echo %s >> regionservers" % item)


                
@task
@roles("all")
def install_java():
    sudo("apt-get update")
    sudo("apt-get install openjdk-7-jdk -y")
    append("~/.bash_profile", "export JAVA_HOME=/usr/lib/jvm/java-7-openjdk-amd64", use_sudo = True)
    append("~/.bash_profile", "export PATH=$JAVA_HOME/bin:$PATH", use_sudo = True)
    append("~/.bashrc", "export JAVA_HOME=/usr/lib/jvm/java-7-openjdk-amd64", use_sudo = True)
    append("~/.bashrc", "export PATH=$JAVA_HOME/bin:$PATH", use_sudo = True)

@task
@roles("master")
def install_faunus():
#	if not exists(env.data_tools):
	sudo("mkdir -p %s" % env.data_tools)
	sudo("chown %s -R %s" % (env.user, env.data_tools))
	sudo("chgrp %s -R %s" % (env.user, env.data_tools))
    
	with cd(env.data_tools):
		run("rm -rf faunus*")
 		run("wget http://s3.thinkaurelius.com/downloads/faunus/faunus-0.4.4.zip")
		sudo("apt-get install unzip -y")
		run("unzip faunus-0.4.4.zip")
		run("rm faunus-0.4.4.zip")
		run("ln -s faunus-0.4.4 faunus")
	faunusDir = "%s/faunus" % env.data_tools
	append("~/.bashrc", "export PATH=%s/bin:$PATH" % faunusDir, use_sudo = True)
	append("~/.bashrc", "export FAUNUS_HOME=%s" % faunusDir, use_sudo = True)
	append("~/.bash_profile", "export PATH=%s/bin:$PATH" % faunusDir, use_sudo = True)
	append("~/.bash_profile", "export FAUNUS_HOME=%s" % faunusDir, use_sudo = True)
	upload_template('faunus.properties', "%s/bin/faunus.properties" % faunusDir, context={}, use_jinja=True, template_dir=None, use_sudo=True)

@task
@roles("master")
def install_titan():
#	if not exists(env.data_tools):
	sudo("mkdir -p %s" % env.data_tools)
	sudo("chown %s -R %s" % (env.user, env.data_tools))
	sudo("chgrp %s -R %s" % (env.user, env.data_tools))
    
	with cd(env.data_tools):
		run("rm -rf titan*")
		run("wget http://s3.thinkaurelius.com/downloads/titan/titan-all-0.4.4.zip")
		sudo("apt-get install unzip -y")
		run("unzip titan-all-0.4.4.zip")
		run("rm titan-all-0.4.4.zip")
		run("ln -s titan-all-0.4.4 titan")
	titanDir = "%s/titan" % env.data_tools
	append("~/.bashrc", "export PATH=%s/bin:$PATH" % titanDir, use_sudo = True)
	append("~/.bashrc", "export TITAN_HOME=%s" % titanDir, use_sudo = True)
	append("~/.bash_profile", "export PATH=%s/bin:$PATH" % titanDir, use_sudo = True)
	append("~/.bash_profile", "export TITAN_HOME=%s" % titanDir, use_sudo = True)
	upload_template('cassandra.properties', "%s/bin/cassandra.properties" % titanDir, context={}, use_jinja=True, template_dir=None, use_sudo=True)


################# Functions for Server Inventory Lookup #################

@task
def lookup_server(name, user=env.user, iptype="extip"):
    inventory = get_inventory()
    return ['%s@%s' % (user, item[iptype]) for item in inventory[name] if item[iptype] is not None]

@task
def lookup_server_ip(name, iptype="extip"):
    inventory = get_inventory()
    return [item[iptype] for item in inventory[name] if item[iptype] is not None]

@task
def get_inventory(region=server_region, inventory_file=env.inventory_file):
    if not os.path.exists(inventory_file):
        return update_inventory(region, inventory_file)
    inv_file = open(inventory_file, 'r')
    inventory = yaml.load(inv_file)
    inv_file.close()
    return inventory

@task
def update_inventory(region=server_region, inventory_file=env.inventory_file):
    aws_access_key_id = env.s3PublicKey
    aws_secret_access_key = env.s3PrivateKey

    ec2_conn = boto.ec2.connect_to_region(region,
                                              aws_access_key_id=aws_access_key_id,
                                              aws_secret_access_key=aws_secret_access_key)
    reservations = ec2_conn.get_all_reservations()
    instance_lookup = {}
    for reservation in reservations:
        for instance in reservation.instances:
            name = instance.tags.get('Name')
            if name:
                instance_lookup.setdefault(name, []).append(
                    {'ip': instance.private_ip_address,
                      'extip': instance.ip_address,
                      'state': instance.state,
                      'is_master': str(True),
                      'is_data': str(True)}
                )
    out = open(inventory_file, 'w')
    yaml.dump(instance_lookup, out, default_flow_style=False)
    out.close()
    return instance_lookup

