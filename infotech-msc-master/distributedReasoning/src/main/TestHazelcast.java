package main;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

public class TestHazelcast {
	
	public static void main( String[] args ) {
        ClientConfig clientConfig = new ClientConfig();
        HazelcastInstance client = HazelcastClient.newHazelcastClient( clientConfig );
        IMap<?, ?> map = client.getMap( "ids" );
        java.util.concurrent.locks.Lock lock = client.getLock("key");
        lock.lock();
        System.out.println("Map size is " + map.size());
        String cmd = "";
        //map.clear();
        while (!cmd.equals("exit")) {
        	if (cmd.equals("remove"))
        		map.clear();
        		System.out.println("Map size is now " + map.size());
        		cmd = System.console().readLine().trim();
        }
        client.shutdown();
    }
}
