package es.upm.dit.fcon;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper; 
import org.apache.zookeeper.data.Stat;

import java.util.List;
import java.util.Iterator;
import java.io.IOException;
import java.util.Random;

public class zkOperation {
	
	private static final int SESSION_TIMEOUT = 5000;
	private ZooKeeper zk;
	
	private static String rootOperations = "/operations";
	private static String aoperation = "/operation-";
	private String myId;
	
	public zkOperation() {
		
		try {
			
			if (zk == null) {
				zk = new ZooKeeper("0.0.0.0:2181", SESSION_TIMEOUT, cWatcher);
				try {
					// Wait for creating the session. Use the object lock
					wait();
					//zk.exists("/",false);
				} catch (Exception e) {
					// TODO: handle exception
				}
			}
		} catch (Exception e) {
			System.out.println("Error");
		}
		

		if (zk != null) {
			// Create a folder for members and include this process/server
			try {
				// Create a folder, if it is not created
				String response = new String();
				Stat s = zk.exists(rootOperations, watcherOperation); //this);
				if (s == null) {
					// Created the znode, if it is not created.
					response = zk.create(rootOperations, new byte[0], 
							Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
					System.out.println(response);
				}

				// Create a znode for registering as member and get my id
				myId = zk.create(rootOperations + aoperation, new byte[0], 
						Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);

				myId = myId.replace(rootOperations + "/", "");

				List<String> list = zk.getChildren(rootOperations, watcherOperation, s); //this, s);
				System.out.println("Created znode operation id:"+ myId );
				printListOperations(list);
			} catch (KeeperException e) {
				System.out.println("The session with Zookeeper failes. Closing");
				return;
			} catch (InterruptedException e) {
				System.out.println("InterruptedException raised");
			}

		}
	}
	
	private Watcher cWatcher = new Watcher() {
		public void process (WatchedEvent e) {
			System.out.println("Created session");
			System.out.println(e.toString());
			notify();
		}
	};
	
	// Notified when the number of children in /member is updated
	private Watcher  watcherOperation = new Watcher() {
		public void process(WatchedEvent event) {
			System.out.println("------------------Watcher Operation------------------\n");		
			try {
				System.out.println("        Update!!");
				List<String> list = zk.getChildren(rootOperations,  watcherOperation); //this);
				printListOperations(list);
			} catch (Exception e) {
				System.out.println("Exception: wacherOperation");
			}
		}
	};
	
	
	public void process(WatchedEvent event) {
		try {
			System.out.println("Unexpected invocated this method. Process of the object");
			List<String> list = zk.getChildren(rootOperations, watcherOperation); //this);
			printListOperations(list);
		} catch (Exception e) {
			System.out.println("Unexpected exception. Process of the object");
		}
	}
	
	private void printListOperations (List<String> list) {
		System.out.println("Remaining # operations:" + list.size());
		for (Iterator iterator = list.iterator(); iterator.hasNext();) {
			String string = (String) iterator.next();
			System.out.print(string + ", ");				
		}
		System.out.println();
	}
	
}
