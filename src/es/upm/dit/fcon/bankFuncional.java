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
	
	public bankFuncional (int id) {
		this.clients = new HashMap <Long, Client>();
		this.bankId = id; 
	}
	private boolean createClient(Client client) {
		if (clients.containsKey(client.getAccountNumber())) {
			return false;
		} else {
			clients.put(client.getAccountNumber(), client);
			return true;
		}
	}
	
	private boolean deleteClient(Long id) {
		if (clients.containsKey(id)) {
			clients.remove(id);
			return true;
		} else {
			return false;
		}	
	}
	private HashMap<Long, Client> getClients() {
		return clients;
	}
	private boolean updateClient(Client client) {
		if (clients.containsKey(client.getAccountNumber())) {
			clients.put(client.getAccountNumber(), client);
			return true;
		} else {
			return false;
		}	
	}
	
	private Client readClient(Long id) {
		if (clients.containsKey(id)) {
			return clients.get(id);
		} else {
			return null;
		}	
	}
	
	private void sendCreateClient(Client client) {
		String operation = "";
		String type = "CREATE";
		String account_number = client.getAccountNumber().toString();
		String name = client.getName();
		String balance = Integer.toString(client.getBalance());
		operation = type + "," + account_number + "," + name + "," + balance;
		zkOperation zk = new zkOperation(bankId, operation);

		System.out.println(operation);
	}
	
	private void sendUpdateClient(Client client) {
		String operation = "";
		String type = "UPDATE";
		String account_number = client.getAccountNumber().toString();
		String name = client.getName();
		String balance = Integer.toString(client.getBalance());
		operation = type + "," + account_number + "," + name + "," + balance;
		System.out.println(operation);
	}
	
	private void sendDeleteClient(Long id) {
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
	
	private void printListOperations (List<String> list) {
		System.out.println("\nRemaining # operations:" + list.size());
		for (Iterator iterator = list.iterator(); iterator.hasNext();) {
			String string = (String) iterator.next();
			System.out.print(string + ", ");
			
		}
		System.out.println();
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		if (args[0] != null) {
			
			try {
				bankFuncional bank = new bankFuncional(Integer.parseInt(args[0]));
				System.out.println("Se ha creado el banco "+args[0]);
				Scanner sc = new Scanner(System.in);
				boolean salir = false;
				Long accNumber   = (long) 0;
				int balance = 0;
				Client client   = null;
				int menuKey = 0;
				String operation = "";
				Client c;
				
				while(!salir) {
					try {
						System. out .println(">>> Enter operation client.: 1) Create. 2) Read. 3) Update. 4) Delete. 5) ReadAll. 6) Exit");
						if (sc.hasNextInt()) {
							menuKey = sc.nextInt();
						} else {
							sc.next();
							System.out.println("The provised text provided is not an integer");
						}
						
						switch (menuKey) {
						
						case 1: // Create client
							c = bank.readScanner(sc);
							bank.sendCreateClient(c);
							bank.createClient(c);
							break;
						case 2: // Read client
							System. out .print(">>> Enter account number (int) = ");
							if (sc.hasNextInt()) {
								accNumber = (long) sc.nextInt();
								client = bank.readClient(accNumber);
								System.out.println(client);
							} else {
								System.out.println("The provised text provided is not an integer");
								sc.next();
							}
							break;
						case 3: // Update client
							c = bank.readScanner(sc);
							bank.sendUpdateClient(c);
							bank.updateClient(c);
		
							break;
						case 4: // Delete client
							System. out .print(">>> Enter account number (int) = ");
							if (sc.hasNextInt()) {
								accNumber = (long) sc.nextInt();
								String name = bank.getClients().get(accNumber).getName();
								bank.sendDeleteClient(accNumber);
								boolean status = bank.deleteClient(accNumber);
								if (status) {
									System.out.println("El cliente " +name+ " se ha borrado correctamente.");
								}
								else {
									System.out.println("Se ha producido un error. El cliente con id " + accNumber + " no existe.");
								}
							} else {
								System.out.println("The provised text provided is not an integer");
								sc.next();
							}
							break;
						case 5:
							String aux = bank.readAll();
							System.out.println(aux);
							break;
						case 6:
							salir = true;	
							//bank.close();
						default:
							break;
						}
					}
					catch (Exception e) {
						System.out.println("Exception at Main. Error read data");
					}
				}
				sc.close();
			}
			catch (Exception e) {
				System.out.println("Exception in bankid parameter");
			}
		}
	}
}