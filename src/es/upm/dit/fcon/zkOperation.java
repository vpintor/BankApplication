package es.upm.dit.fcon;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper; 
import org.apache.zookeeper.data.Stat;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.io.IOException;
import java.util.Random;

public class zkOperation {
	
	private static final int SESSION_TIMEOUT = 5000;
	private ZooKeeper zk;
	
	private static String rootOperations = "/operations";
	private static String rootState = "/state";

	private static String aoperation = "/operation-";
	private static String aserver = "/server-";
	private static String aglobal = "/global";

	private String myId;
	
	public zkOperation(int serverId, String operation) {
		
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
				Stat s = zk.exists(rootOperations, watcherOperations); //this);
				Stat state = zk.exists(rootState, watcherOperations);
				
				if (s == null) {
					// Crear el nodo raiz de operations
					response = zk.create(rootOperations, new byte[0], 
							Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
					System.out.println("Crear el nodo raiz operations");
					System.out.println(response);
				}
				if (state == null) {
					// Crear el nodo raiz state
					response = zk.create(rootState, new byte[0], 
							Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
					System.out.println("Crear el nodo raiz state");
					System.out.println(response);
				}
				
				Stat globalNode = zk.exists(rootState+aglobal, null);
				
				if (globalNode == null) {
					// Crear nodo para el estado global
					response = zk.create(rootState+aglobal, new byte[0], 
							Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
					System.out.println("Crear el nodo global");
					System.out.println(response);
					zk.setData(rootState+aglobal, "0".getBytes(), -1);

				}
				
				String stringServer = aserver+serverId;
				Stat server = zk.exists(rootState+stringServer, null);
				
				if (server == null) {
					// Crear nodo para el servidor pasado como id
					response = zk.create(rootState+stringServer, new byte[0], 
							Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
					System.out.println("Crear el nodo server x");
					System.out.println(response);
					zk.setData(rootState+stringServer, "-1".getBytes(), -1);

				}
				String global_string = new String(zk.getData(rootState+aglobal, null, globalNode));
				Stat nodo_operacion = zk.exists(rootOperations+aoperation+global_string, watcherIdOperation);
				// Crear el nodo para la operacion pasada como parametro
				myId = zk.create(rootOperations + aoperation, new byte[0], 
						Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);

				myId = myId.replace(rootOperations +"/", "");
				Stat op = zk.exists(rootOperations+"/"+myId, watcherIdOperation);
				
				String[] numOp = myId.split("-");
				zk.setData(rootOperations+"/"+myId, operation.getBytes(), -1);
				zk.setData(rootState+aglobal, numOp[1].getBytes(), -1);

				// byte [] data = zk.getData(rootOperations+"/"+myId, watcherOperations, op);
				// String data_string = new String(data);
				

				//System.out.println("El valor del nodo "+myId+" es " +data_string);
				List<String> list = zk.getChildren(rootOperations, watcherOperations, s); //this, s);
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
			System.out.println("------------------Watcher Session------------------\n");
			System.out.println("Created session");
			System.out.println(e.toString());
			System.out.println("---------------------------------------------------\n");
			notify();
		}
	};
	
	// Notified when the number of children in /member is updated
	private Watcher  watcherOperations = new Watcher() {
		public void process(WatchedEvent event) {
			System.out.println("------------------Watcher Operations------------------\n");		
			try {
				System.out.println("        Update!!");
				 System.out.println("El valor de event.getType es "+event.getType());
				 System.out.println("El valor de event.getPath es "+event.getPath());
				List<String> list = zk.getChildren(rootOperations,  watcherOperations); //this);
				printListOperations(list);
			} catch (Exception e) {
				System.out.println("Exception: wacherOperations");
			}
			System.out.println("------------------------------------------------------\n");		
		}
	};
	
	
//	@Override
//	public void process(WatchedEvent event) {
//		try {
//			System.out.println("Unexpected invocated this method. Process of the object");
//			List<String> list = zk.getChildren(rootOperations, watcherOperations); //this);
//			printListOperations(list);
//		} catch (Exception e) {
//			System.out.println("Unexpected exception. Process of the object");
//		}
//	}
	
	
	private Watcher  watcherIdOperation = new Watcher() {
		public void process(WatchedEvent event) {
			System.out.println("------------------Watcher Id Operation------------------\n");		
			try {
				System.out.println("        Update!!");
				System.out.println("El watcher id operation da"+event.getPath());
				System.out.println("Ahora si funciona");

				
			} catch (Exception e) {
				System.out.println("Exception: wacherIdOperation");
			}
			System.out.println("-------------------------------------------------\n");		

		}
	};
	
	
	private void printListOperations (List<String> list) {
		System.out.println("\nRemaining # operations:" + list.size());
		for (Iterator iterator = list.iterator(); iterator.hasNext();) {
			String string = (String) iterator.next();
			System.out.print(string + ", ");
			
		}
		System.out.println();
	}
	
}
