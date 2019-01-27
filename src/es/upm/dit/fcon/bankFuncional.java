package es.upm.dit.fcon;

import java.util.Scanner;
import java.util.HashMap;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper; 
import org.apache.zookeeper.data.Stat;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper; 
import org.apache.zookeeper.data.Stat;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Iterator;
import java.io.IOException;
import java.util.Random;
import java.nio.ByteBuffer;

public class bankFuncional {

	private HashMap <Long, Client> clients; 
	private int bankId; 
	
	private static final int SESSION_TIMEOUT = 5000;
	private ZooKeeper zk;
	private static String rootOperations = "/operations";
	private static String rootState = "/state";
	private static String aoperation = "/operation-";
	private static String aserver = "/server-";
	private static String aglobal = "/global";
	private String myId;
	
	public bankFuncional (int id) {
		this.clients = new HashMap <Long, Client>();
		this.bankId = id; 
		
	}
	
	public int getBankId() {
		return this.bankId;
	}
	
	public boolean createClient(Client client) {
		if (clients.containsKey(client.getAccountNumber())) {
			return false;
		} else {
			clients.put(client.getAccountNumber(), client);
			return true;
		}
	}
	
	public boolean deleteClient(Long id) {
		if (clients.containsKey(id)) {
			clients.remove(id);
			return true;
		} else {
			return false;
		}	
	}
	public HashMap<Long, Client> getClients() {
		return clients;
	}
	public boolean updateClient(Client client) {
		if (clients.containsKey(client.getAccountNumber())) {
			clients.put(client.getAccountNumber(), client);
			return true;
		} else {
			return false;
		}	
	}
	
	public Client readClient(Long id) {
		if (clients.containsKey(id)) {
			return clients.get(id);
		} else {
			return null;
		}	
	}
	
	public void sendCreateClient(Client client) {
		String operation = "";
		String type = "CREATE";
		String account_number = client.getAccountNumber().toString();
		String name = client.getName();
		String balance = Integer.toString(client.getBalance());
		operation = type + "," + account_number + "," + name + "," + balance;
		this.createOperation(operation);

		System.out.println(operation);
	}
	
	public void sendUpdateClient(Client client) {
		String operation = "";
		String type = "UPDATE";
		String account_number = client.getAccountNumber().toString();
		String name = client.getName();
		String balance = Integer.toString(client.getBalance());
		operation = type + "," + account_number + "," + name + "," + balance;
		System.out.println(operation);
	}
	
	public void sendDeleteClient(Long id) {
		String operation = "";
		String type = "DELETE";
		String account_number = id.toString();
		operation = type + "," + account_number;
		System.out.println(operation);
	}
	
	public Client readScanner(Scanner sc) {
		Long accNumber = (long) 0;
		String name   = null;
		int balance   = 0;
		
		System. out .print(">>> Enter account number (long) = ");
		if (sc.hasNextInt()) {
			accNumber = (long)(sc.nextInt());
		} else {
			System.out.println("The provised text provided is not an integer");
			sc.next();
			return null;
		}

		System. out .print(">>> Enter name (String) = ");
		name = sc.next();

		System. out .print(">>> Enter balance (int) = ");
		if (sc.hasNextInt()) {
			balance = sc.nextInt();
		} else {
			System.out.println("The provised text provided is not an integer");
			sc.next();
			return null;
		}
		return new Client(accNumber, name, balance);
	}
	
	public String readAll() {
		String aux = new String();

		for (HashMap.Entry <Long, Client>  entry : clients.entrySet()) {
			aux = aux + entry.getValue().toString() + "\n";
		}
		return aux;
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
			System.out.println("\n------------------Watcher Operations------------------\n");		
			try {
				System.out.println("        Update!!");
				 System.out.println("Se ha producido un evento de tipo "+event.getType());
				 System.out.println("El valor de event.getPath del parent es "+event.getPath());
				List<String> list = zk.getChildren(rootOperations,  null); //this);
				printListOperations(list);
				
				Stat globalNode = zk.exists(rootState+aglobal, null);
				String global_string = new String(zk.getData(rootState+aglobal, null, globalNode));
	
				Stat nodo_operacion = zk.exists(rootOperations+aoperation+global_string, null);
				if(nodo_operacion != null) {
					System.out.println("\nSe ha creado un nuevo nodo operación:  " + rootOperations+aoperation+global_string);
					byte [] datos = zk.getData(rootOperations+aoperation+global_string, null, nodo_operacion);
					System.out.println("La operación es la siguiente: "+new String(datos));
					execOperation(new String(datos));
				}
				
			} catch (Exception e) {
				System.out.println("Exception: wacherOperations");
			}
			System.out.println("------------------------------------------------------\n");		
		}
	};
	
//	private Watcher  watcherIdOperation = new Watcher() {
//		public void process(WatchedEvent event) {
//			System.out.println("------------------Watcher Id Operation------------------\n");		
//			try {
//				System.out.println("        Update!!");
//				System.out.println("El watcher id operation da"+event.getPath());
//				System.out.println("Ahora si funciona");
//				
////				Stat globalNode = zk.exists(rootState+aglobal, null);
////				String global_string = new String(zk.getData(rootState+aglobal, null, globalNode));
////				Stat nodo_operacion = zk.exists(rootOperations+aoperation+global_string, watcherIdOperation);
////				byte [] datos = zk.getData(rootOperations+aoperation+global_string, watcherIdOperation, nodo_operacion);
////				System.out.println(new String(datos));
//				
//			} catch (Exception e) {
//				System.out.println("Exception: wacherIdOperation");
//			}
//			System.out.println("-------------------------------------------------\n");		
//
//		}
//	};
	
	private void printListOperations (List<String> list) {
		System.out.println("\nNumero total de operaciones: " + list.size());
		System.out.println("Lista de operaciones");
		for (Iterator iterator = list.iterator(); iterator.hasNext();) {
			String string = (String) iterator.next();
			System.out.println(string);
			
		}
		System.out.println();
	}
	
	public void initZoo(int serverId) {
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
					zk.setData(rootState+aglobal, "0000000000".getBytes(), -1);

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
	
				Stat nodo_operacion = zk.exists(rootOperations+aoperation+global_string, watcherOperations);
				
			} catch (KeeperException e) {
				System.out.println("The session with Zookeeper failes. Closing");
				return;
			} catch (InterruptedException e) {
				System.out.println("InterruptedException raised");
			}

		}
	}
	
	public void createOperation(String operation) {
		try {
			Stat globalNode = zk.exists(rootState+aglobal, null);
			String global_string = new String(zk.getData(rootState+aglobal, null, globalNode));
			Stat nodo_operacion = zk.exists(rootOperations+aoperation+global_string, null);
			if(nodo_operacion == null) {
				// Crear el nodo para la operacion pasada como parametro
				myId = zk.create(rootOperations + aoperation, new byte[0], 
						Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
					
				myId = myId.replace(rootOperations +"/", "");
				//Stat op = zk.exists(rootOperations+"/"+myId, watcherIdOperation);
				byte [] datos = zk.getData(rootState+aglobal, null, globalNode);
				String[] numOp = myId.split("-");
				String datos_str = new String(datos);
				System.out.println("El valor de datos str es "+datos_str);
				int numero = Integer.parseInt(datos_str);
				numero = numero +1;
			    String glob = String.format("%10s", Integer.toString(numero))
			    	    .replace(' ', '0');
				
				zk.setData(rootOperations+"/"+myId, operation.getBytes(), -1);
				zk.setData(rootState+aglobal, glob.getBytes(), -1);
	
				Stat s = zk.exists(rootOperations, watcherOperations); //this);
	
				List<String> list = zk.getChildren(rootOperations, null, s); //this, s);
				System.out.println("Created znode operation id:"+ myId );
				printListOperations(list);
			}
		} catch (KeeperException e) {
			System.out.println("The operation creaation with Zookeeper failes. Closing");
			return;
		} catch (InterruptedException e) {
			System.out.println("InterruptedException raised");
		}
	}
	
	private void execOperation (String operation) {
		String[] opSplit = operation.split(",");
		long account_number;
		String name;
		int balance;
		Client client;
		switch (opSplit[0]) {
		case "CREATE": 
			account_number = Long.parseLong(opSplit[1]);
			name = opSplit[2];
			balance = Integer.parseInt(opSplit[3]);
			client = new Client(account_number, name, balance);
			this.createClient(client);
			break;
		case "DELETE":
			account_number = Long.parseLong(opSplit[1]);
			String name_client = this.getClients().get(account_number).getName();
			boolean status = this.deleteClient(account_number);
			if (status) {
				System.out.println("El cliente " +name_client+ " se ha borrado correctamente.");
			}
			else {
				System.out.println("Se ha producido un error. El cliente con id " + account_number + " no existe.");
			}
			break;
		case "UPDATE":
			account_number = Long.parseLong(opSplit[1]);
			name = opSplit[2];
			balance = Integer.parseInt(opSplit[3]);
			client = new Client(account_number, name, balance);
			this.updateClient(client);
			break;
		default:
			break;
		}
	}
	
	
}
