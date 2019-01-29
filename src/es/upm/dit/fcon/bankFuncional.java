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
import java.util.Collections;
import java.util.Arrays;
import java.util.Iterator;
import java.io.IOException;
import java.util.Random;
import java.nio.ByteBuffer;

public class bankFuncional {

	private HashMap <Long, Client> clients; 
	private int bankId; 
	private int last_operation_db;
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
		this.last_operation_db = -1;
		
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
		try {
			Stat globalNode = zk.exists(rootState+aglobal, null);
			
			
			if (atenderPeticion(globalNode)) {
				if (clients.containsKey(id)) {
					return clients.get(id);
				} else {
					return null;
				}
			}
			else {
				System.out.println("El servidor no puede atender la petición ahora mismo, está desactualizado");
				System.out.println("Va a proceder a actualizarse. Espere un momento");
				actualizarServidor(globalNode);
				return null; 

			}
		} catch (KeeperException e) {
			System.out.println("The operation read failes. KeeperException");
			return null;
		} catch (InterruptedException e) {
			System.out.println("The operation read failes. InterruptedException");
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

	}
	
	public void sendUpdateClient(Client client) {
		String operation = "";
		String type = "UPDATE";
		String account_number = client.getAccountNumber().toString();
		String name = client.getName();
		String balance = Integer.toString(client.getBalance());
		operation = type + "," + account_number + "," + name + "," + balance;
		this.createOperation(operation);
	}
	
	public void sendDeleteClient(Long id) {
		String operation = "";
		String type = "DELETE";
		String account_number = id.toString();
		operation = type + "," + account_number;
		this.createOperation(operation);
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
				List<String> list = zk.getChildren(rootOperations, watcherOperations); //this);

				if (event.getType()!=Watcher.Event.EventType.NodeDeleted) {
					System.out.println("        Update!!");
					System.out.println("Se ha producido un evento de tipo "+event.getType());
					System.out.println("El valor de event.getPath del parent es "+event.getPath());
					List<String> list1 = zk.getChildren(rootOperations, watcherOperations); //this);

					printListOperations(list1);
					
					
					Stat globalNode = zk.exists(rootState+aglobal, null);
					String global_string = new String(zk.getData(rootState+aglobal, null, globalNode));
		
					Stat nodo_operacion = zk.exists(rootOperations+aoperation+global_string,watcherOperations);
					if(nodo_operacion != null) {
						System.out.println("\nSe ha creado un nuevo nodo operación:  " + rootOperations+aoperation+global_string);
						byte [] datos = zk.getData(rootOperations+aoperation+global_string, null, nodo_operacion);
						System.out.println("La operación es la siguiente: "+new String(datos));
						
						
						if (atenderWatcher(globalNode)) {
							actualizarServidor(globalNode);
							
						}
											
	
					}
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
			limpiarNodos();
			if (atenderPeticion(globalNode)) {
				String global_string = new String(zk.getData(rootState+aglobal, null, globalNode));
				Stat nodo_operacion = zk.exists(rootOperations+aoperation+global_string, null);
				if(nodo_operacion == null) {
					// Crear el nodo para la operacion pasada como parametro
					myId = zk.create(rootOperations + aoperation, new byte[0], 
							Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
	
					myId = myId.replace(rootOperations +"/", "");
					zk.setData(rootOperations+"/"+myId, operation.getBytes(), -1);
					
					
					byte [] datos = zk.getData(rootState+aglobal, null, globalNode);
					String[] numOp = myId.split("-");
					String datos_str = new String(datos);
					//System.out.println("El valor de datos str es "+datos_str);
					int numero = Integer.parseInt(datos_str);
					numero = numero +1;
				    String glob = String.format("%10s", Integer.toString(numero))
				    	    .replace(' ', '0');
					
				    actualizarGlobal(glob);
				    //zk.setData(rootState+aglobal, glob.getBytes(), -1);
					//wait(1000);

				    Stat s = zk.exists(rootOperations, null); //this);
					Stat opa = zk.exists(rootOperations+"/"+myId, watcherOperations);
	
					List<String> list = zk.getChildren(rootOperations, null, s); //this, s);
					System.out.println("Created znode operation id:"+ myId );
					printListOperations(list);
				}
			}
			else {
				System.out.println("El servidor no puede atender la petición ahora mismo, está desactualizado");
				System.out.println("Va a proceder a actualizarse. Espere un momento");
				actualizarServidor(globalNode);

			}
		} catch (KeeperException e) {
			System.out.println("The operation creation with Zookeeper failes. Closing");
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
	
	private boolean atenderWatcher(Stat globalNode) {
		try {
			Thread.sleep(1000);

			byte [] datos_global = zk.getData(rootState+aglobal, null, globalNode);
			String datos_str_glob = new String(datos_global);
			//System.out.println("El valor de datos str es "+datos_str);
			int global = Integer.parseInt(datos_str_glob);
			System.out.println("ATENDER WATCHER CON LAST_OPERATION "+last_operation_db+" Y GLOBAL "+global);
			if (last_operation_db <= global -1) {
				return true; 
			}
		} catch (KeeperException e) {
			System.out.println("The operation creation with Zookeeper failes. Closing");
			return false;
		} catch (InterruptedException e) {
			System.out.println("InterruptedException raised");
			return false;
		}
		return false;
	}
	
	private boolean atenderPeticion(Stat globalNode) {
		try {
			
			byte [] datos_global = zk.getData(rootState+aglobal, null, globalNode);
			String datos_str_glob = new String(datos_global);
			//System.out.println("El valor de datos str es "+datos_str);
			int global = Integer.parseInt(datos_str_glob);
			
			if (last_operation_db == global -1) {
				return true; 
			}
		} catch (KeeperException e) {
			System.out.println("The operation creation with Zookeeper failes. Closing");
			return false;
		} catch (InterruptedException e) {
			System.out.println("InterruptedException raised");
			return false;
		}
		return false;
	}
	
	private void actualizarServidor (Stat globalNode) {
		try {		
			byte [] datos_global = zk.getData(rootState+aglobal, null, globalNode);
			String datos_str_glob = new String(datos_global);
			//System.out.println("El valor de datos str es "+datos_str);
			int global = Integer.parseInt(datos_str_glob);
			System.out.println("El valor de last_operation_db es "+last_operation_db);
			System.out.println("El valor de global es "+global);
			if (last_operation_db <= global -1) {
				System.out.println("Entra en el if de actualizarServidor");

				for(int i=last_operation_db+1; i<global; i++) {
				    String i_formatted = String.format("%10s", Integer.toString(i)).replace(' ', '0');
					System.out.println("Valor de i es "+i);
					System.out.println("Valor de i_formatted es "+i_formatted);

					Stat nodo_operacion = zk.exists(rootOperations+aoperation+i_formatted,null);
					byte [] datos_op = zk.getData(rootOperations+aoperation+i_formatted, null, nodo_operacion);
					String datos_str = new String(datos_op);
					System.out.println("Valor de datos_str es "+datos_str);

					execOperation(datos_str);
					System.out.println("Operación "+ datos_str+" realizada en la base de datos");

					last_operation_db++;
					
					sincronizarStateSx();
					
				}
			}
			else {
				return;
			}
		} catch (KeeperException e) {
			System.out.println("Error actualizando servidor. KeeperException");
		} catch (InterruptedException e) {
			System.out.println("Error actualizando servidor. InterruptedException raised");
		}
	}
	
	private void sincronizarStateSx() {
		try {
			String Server = aserver+bankId;
			Stat server = zk.exists(rootState+Server, null);
			String server_string = new String(zk.getData(rootState+Server, null, server));
			int state_sx = Integer.parseInt(server_string);
			while (state_sx != last_operation_db) {
				
			    String sx_update = String.format("%10s", Integer.toString(last_operation_db))
			    	    .replace(' ', '0');
				zk.setData(rootState+Server, sx_update.getBytes(), -1);
				
				server_string = new String(zk.getData(rootState+Server, null, server));
				state_sx = Integer.parseInt(server_string);
			}
			
			
		} catch (KeeperException e) {
			System.out.println("Error sincronizarStateSx. KeeperException");
		} catch (InterruptedException e) {
			System.out.println("Error sincronizarStateSx. InterruptedException raised");
		}
	}
	
	private void actualizarGlobal(String glob) {
		try {
			Stat global = zk.exists(rootState+aglobal, null);
			String global_string = new String(zk.getData(rootState+aglobal, null, global));
			System.out.println("Entra en actualizar global");
			System.out.println("El valor de global_string es "+global_string);
			System.out.println("El valor de glob  es "+glob);

//			while (!global_string.equals(glob)) {
//				zk.setData(rootState+aglobal, glob.getBytes(), -1);
//				System.out.println("Entra en el while de actualizar global y cambia el valor a "+glob);
//
//				global_string = new String(zk.getData(rootState+aglobal, null, global));
//			}
			do {
				zk.setData(rootState+aglobal, glob.getBytes(), -1);
				System.out.println("Entra en el while de actualizar global y cambia el valor a "+glob);

				global_string = new String(zk.getData(rootState+aglobal, null, global));
	        } while (!global_string.equals(glob));
			
		} catch (KeeperException e) {
			System.out.println("Error actualizando global. KeeperException");
		} catch (InterruptedException e) {
			System.out.println("Error actualizando global. InterruptedException raised");
		}
	}
	
	private void limpiarNodos() {
		try {
			
			List<String> list_op = zk.getChildren(rootOperations,  null); //this);
			int num_op = list_op.size();
			List<Integer> list_op_int = new ArrayList<Integer>();
			
			for (int i=0; i<list_op.size();i++) {
				String[] Node_split = list_op.get(i).split("-");
				int oper = Integer.parseInt(Node_split[1]);
				list_op_int.add(oper);
				
			}
			
			if (num_op>=6) {
				List<String> list_states = zk.getChildren(rootState,  null); //this);
				int num_sx = list_states.size()-1;
				List<Integer> states = new ArrayList<Integer>(); 
				
				for(int i=1; i<num_sx+1; i++) {
					System.out.println("El valor i del bucle es "+i);
					System.out.println("Accedemos al servidor "+rootState+aserver+i);
					Stat sxNode = zk.exists(rootState+aserver+i, null);
					System.out.println("Llega 1");
					String sx_string = new String(zk.getData(rootState+aserver+i, null, sxNode));
					System.out.println("Llega 2");

					int sx_int = Integer.parseInt(sx_string);
					System.out.println("Llega 3");

					states.add(sx_int);
					System.out.println("Llega 4");

				}
				System.out.println("Llega 5");

				int minSx = (int)Collections.min(states);
				System.out.println(list_op.get(0));
				System.out.println("Llega 6. Valor de minSx es "+minSx);
				//String[] firstNode_split = list_op.get(0).split("-");
				//int firstNode = Integer.parseInt(firstNode_split[1]);
				int firstNode = Collections.min(list_op_int);
				//Eliminar desde list_op(0) hasta min -1
				System.out.println("LLega antes del meollo. El valor de firstNode es "+firstNode);
				for (int i=firstNode; i<minSx-1;i++) {
					System.out.println("El valor del bucle meollo es "+i);
					String operation_node_str = String.format("%10s", Integer.toString(i))
				    	    .replace(' ', '0');
					System.out.println("Llega 7. Valor de operation_node_str"+operation_node_str);
					Stat operation_node = zk.exists(rootOperations+aoperation+operation_node_str, null);
					System.out.println("Llega 8. Valor de string "+rootOperations+aoperation+operation_node_str);

					if (operation_node !=null) {
						System.out.println("Llega 8.");

						zk.delete(rootOperations+aoperation+operation_node_str, 1);;
					}	
				}
			}	
			
			
		} catch (KeeperException e) {
			System.out.println("Error limpiando nodos. KeeperException");
		} catch (InterruptedException e) {
			System.out.println("Error actualizando global. InterruptedException raised");
		}
	}
	
}
