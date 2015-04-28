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



################# Environment Setup #################

env.tools_folder = "/mnt/tools"
env.user = "ubuntu"
env.master = "Test Hadoop Master"
env.slave = "Test Hadoop Data"
private_key = "~/.ssh/senzari-dev-oregon.pem"
server_region = "us-west-2"
keylist = []

@task
# Always put this function ahead of any other functions that run on remote servers
# e.g. fab -i ~/.ssh/senzari-dev-oregon.pem -P init execmd:"ls ~/"
# e.g. fab -i ~/.ssh/senzari-dev-oregon.pem -P init inistall_mvn 
def init(iptype = "extip"):    
    env.roledefs["master"] = lookup_server(env.master, iptype=iptype)
    env.roledefs["slave"] = lookup_server(env.slave, iptype=iptype)
    env.roledefs["all"] = env.roledefs["master"] + env.roledefs["slave"] 
    print env.roledefs


@task
@roles("all")
def execmd(cmd):
    run(cmd)


################# Functions for Deployment #################
@task
def copy_dir(dir, dest="~/"):
    for item in env.roledefs["all"]:
        target = "%s:%s" % (item, dest)
        local("scp -i %s -r %s %s" % (private_key, dir, target))

@task
def copy_file(src, dest="~/"):
    for item in env.roledefs["all"]:
        target = "%s:%s" % (item, dest)
        local("scp -i %s %s %s" % (private_key, src, target))        
        
@task
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
@roles("master")
def hadoop_start():
    run("start-all.sh")
    

@task
@roles("master")
def hadoop_stop():
    run("stop-all.sh")
     

@task
def keyless_ssh():
    execute(sshkey_gen)
    execute(sshkey_flood)

@task
# This function should run sequentially.
@serial
@roles("master", "slave")
def sshkey_gen():
    if not exists("~/.ssh/id_rsa.pub"):
        run("ssh-keygen -t rsa -P \"\" -f ~/.ssh/id_rsa")
    key = run("cat ~/.ssh/id_rsa.pub")
    keylist.append(key)

@task
@parallel
@roles("master", "slave")
def sshkey_flood():
    for key in keylist:
        #run("echo %s >> ~/.ssh/authorized_keys" % key)
        append("~/.ssh/authorized_keys", key, use_sudo = True)


        
################# Functions for Software Installation #################

@task
@roles("all")
def install_mvn():
	create_tools_folder()
	with cd(env.tools_folder):
		run("rm -rf maven")
		run("wget http://apache.mirrors.hoobly.com/maven/maven-3/3.1.1/binaries/apache-maven-3.1.1-bin.tar.gz")
		run("tar xzf apache-maven-3.1.1-bin.tar.gz")
		run("rm apache-maven-3.1.1-bin.tar.gz")
		run("ln -s apache-maven-3.1.1 maven")
		#run("echo \'export PATH=%s/maven/bin:$PATH\' >> ~/.bashrc" % env.tools_folder)
        append("~/.bashrc", "export PATH=%s/maven/bin:$PATH" % env.tools_folder, use_sudo = True)
		#run("echo \'export PATH=%s/maven/bin:$PATH\' >> ~/.bash_profile" % env.tools_folder)
        append("~/.bash_profile", "export PATH=%s/maven/bin:$PATH" % env.tools_folder, use_sudo = True)
		

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
@roles("master", "slave")
def install_spark(hadoop="1.2.1"):
    create_tools_folder()
    with cd (env.tools_folder):
        run("rm -rf spark*")
        run("wget http://d3kbcqa49mib13.cloudfront.net/spark-1.0.0.tgz")
        run("tar xzf spark-1.0.0.tgz")
        run("rm spark-1.0.0.tgz")
        run("ln -s spark-1.0.0 spark")
        #run("echo \'export SPARK_HOME=%s/spark\' >> ~/.bashrc" % env.tools_folder)
        append("~/.bashrc", "export SPARK_HOME=%s/spark" % env.tools_folder, use_sudo = True)
        #run("echo \'export SPARK_HOME=%s/spark\' >> ~/.bash_profile" % env.tools_folder)
        append("~/.bash_profile", "export SPARK_HOME=%s/spark" % env.tools_folder, use_sudo = True)
    spark_build(hadoop)
    spark_config()


# refer to http://spark.apache.org/docs/1.0.0/building-with-maven.html
# for building Spark with other versions of Hadoop    
def spark_build(hadoop="1.2.1"):
    with cd (env.tools_folder):
        with cd ("spark"):
            #run("SPARK_HADOOP_VERSION=%s sbt/sbt assembly" % hadoop)
            run("export MAVEN_OPTS=\"-Xmx2g -XX:MaxPermSize=512M -XX:ReservedCodeCacheSize=512m\"")
            run("mvn -Dhadoop.version=%s -DskipTests clean package" % hadoop)

def spark_config():
    slaves = lookup_server_ip(env.slave, iptype="ip")
    spark_conf = "%s/spark/conf" % env.tools_folder
    # configure the slaves for spark
    if not exists(spark_conf):
        abort("Aborting, Spark does not exists")
    with cd(spark_conf):
        run("rm slaves")
        run("touch slaves")
        for item in slaves:
            run("echo %s >> slaves" % item)
            

@task
@roles("master", "slave")
def install_hadoop():
    create_tools_folder()
    with cd(env.tools_folder):
        run("rm -rf hadoop*")
        run("wget http://mirrors.ibiblio.org/apache/hadoop/common/hadoop-1.2.1/hadoop-1.2.1-bin.tar.gz")
        run("tar xzf hadoop-1.2.1-bin.tar.gz")
        run("rm hadoop-1.2.1-bin.tar.gz")
        run("ln -s hadoop-1.2.1 hadoop")
        #run("echo \'export HADOOP_HOME=%s/hadoop\' >> ~/.bashrc" % env.tools_folder)
        append("~/.bashrc", "export HADOOP_HOME=%s/hadoop" % env.tools_folder, use_sudo = True)
        #run("echo \'export Hadoop_HOME=%s/hadoop\' >> ~/.bash_profile" % env.tools_folder)
        append("~/.bash_profile", "export HADOOP_HOME=%s/hadoop" % env.tools_folder, use_sudo = True)
        #run("echo \'export PATH=%s/hadoop/bin:$PATH\' >> ~/.bashrc" % env.tools_folder)
        append("~/.bashrc", "export PATH=%s/hadoop/bin:$PATH" % env.tools_folder, use_sudo = True)
        #run("echo \'export PATH=%s/hadoop/bin:$PATH\' >> ~/.bash_profile" % env.tools_folder)
        append("~/.bash_profile", "export PATH=%s/hadoop/bin:$PATH" % env.tools_folder, use_sudo = True)
    hadoop_env_config()
    hadoop_config()


def hadoop_env_config():
    hadoop_env = "%s/hadoop/conf/hadoop-env.sh" % env.tools_folder
    java_home = run("echo $JAVA_HOME")
    append(hadoop_env, 'export JAVA_HOME=%s' % java_home, use_sudo = True)

@task
@roles("all")    
def hadoop_config():
    hadoop_conf = "%s/hadoop/conf" % env.tools_folder
    hadoop_core_site = "%s/core-site.xml" % hadoop_conf
    hadoop_hdfs_site = "%s/hdfs-site.xml" % hadoop_conf
    hadoop_mapred_site = "%s/mapred-site.xml" % hadoop_conf
    masters = lookup_server_ip(env.master, iptype="ip")
    slaves = lookup_server_ip(env.slave, iptype="ip")
    context = {
		'master': masters[0],
		'replica': 2,
	}
    upload_template('template.core-site.xml', hadoop_core_site, context=context, use_jinja=True, template_dir=None, use_sudo=True)
    upload_template('template.hdfs-site.xml', hadoop_hdfs_site, context=context, use_jinja=True, template_dir=None, use_sudo=True)
    upload_template('template.mapred-site.xml', hadoop_mapred_site, context=context, use_jinja=True, template_dir=None, use_sudo=True)
    with cd(hadoop_conf):
        run("rm masters")
        run("touch masters")
        run("echo %s >> masters" % masters[0])
        run("rm slaves")
        run("touch slaves")
        for item in slaves:
            run("echo %s >> slaves" % item)


        
def create_tools_folder():
	sudo("mkdir -p %s" % env.tools_folder)
	sudo("chown \`whoami\` %s" % env.tools_folder)


@task
@roles("all")
def install_java(user="ubuntu", version="jdk-7u51", url="http://download.oracle.com/otn-pub/java/jdk/7u51-b13", javaExtractedFolderName="jdk1.7.0_51"):
	javaFile = "%s-linux-x64.tar.gz" % version
	downloadDir = "/tmp/java_%s" % version
	completeUrl = "%s/%s" % (url, javaFile)

	sudo("sudo apt-get update")
	sudo("sudo apt-get purge openjdk-\* -y")
	if exists("/usr/local/java"):
		sudo("rm -rf /usr/local/java")
	sudo("mkdir -p  /usr/local/java")
	sudo("chown \`whoami\` /usr/local/java")

	run("mkdir -p %s" % downloadDir)
	run("rm -rf %s/*" % downloadDir)	
	with cd(downloadDir):
		result = run("wget --no-cookies --no-check-certificate --header \"Cookie: oraclelicense=accept-securebackup-cookie\" %s -O %s/%s" % (completeUrl, downloadDir, javaFile))		
		if result.failed:
			abort("Could not download java");
		run("cp %s /usr/local/java" % javaFile)
	with cd("/usr/local/java"):
		sudo("tar xvf %s" % javaFile)
	sudo("sudo echo export JAVA_HOME=/usr/local/java/%s >> /etc/profile" % javaExtractedFolderName)	
	sudo("sudo update-alternatives --install '/usr/bin/java' 'java' '/usr/local/java/%s/bin/java' 1" % javaExtractedFolderName)
	sudo("sudo update-alternatives --install '/usr/bin/javac' 'javac' '/usr/local/java/%s/bin/javac' 1" % javaExtractedFolderName)
	sudo("sudo update-alternatives --install '/usr/bin/javaws' 'javaws' '/usr/local/java/%s/bin/javaws' 1" % javaExtractedFolderName)
	sudo("sudo update-alternatives --install '/usr/bin/javah' 'javah' '/usr/local/java/%s/bin/javah' 1" % javaExtractedFolderName)
	sudo("sudo update-alternatives --install '/usr/bin/jar' 'jar' '/usr/local/java/%s/bin/jar' 1" % javaExtractedFolderName)
	sudo("sudo update-alternatives --set java '/usr/local/java/%s/bin/java'" % javaExtractedFolderName)
	sudo("sudo update-alternatives --set javac '/usr/local/java/%s/bin/javac'" % javaExtractedFolderName)
	sudo("sudo update-alternatives --set javaws '/usr/local/java/%s/bin/javaws'" % javaExtractedFolderName)
	sudo("sudo update-alternatives --set javah '/usr/local/java/%s/bin/javah'" % javaExtractedFolderName)
	sudo("sudo update-alternatives --set jar '/usr/local/java/%s/bin/jar'" % javaExtractedFolderName)

    

################# Functions for Server Inventory Lookup #################

@task
def lookup_server(name, user=env.user, iptype="extip"):
    inventory = get_inventory()
    return ['%s@%s' % (user, item[iptype]) for item in inventory[name]]

@task
def lookup_server_ip(name, iptype="extip"):
    inventory = get_inventory()
    return [item[iptype] for item in inventory[name]]

@task
def get_inventory(region=server_region, inventory_file='.inventory.yaml'):
    if not os.path.exists(inventory_file):
        return update_inventory(region, inventory_file)
    inv_file = open(inventory_file, 'r')
    inventory = yaml.load(inv_file)
    inv_file.close()
    return inventory

@task
def update_inventory(region, inventory_file):
    aws_access_key_id = os.getenv('AWS_ACCESS_KEY_ID')
    aws_secret_access_key = os.getenv('AWS_SECRET_ACCESS_KEY')

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




