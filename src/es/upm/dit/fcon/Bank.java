package es.upm.dit.fcon;

import java.util.Scanner;
import java.util.HashMap;

public class Bank {

	private HashMap <Long, Client> clients; 
	
	public Bank () {
		this.clients = new HashMap <Long, Client>();
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
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Bank bank = new Bank();
		Scanner sc = new Scanner(System.in);
		boolean salir = false;
		Long accNumber   = (long) 0;
		int balance     = 0;
		Client client   = null;
		int     menuKey = 0;
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
					bank.createClient(bank.readScanner(sc));
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
					
					bank.updateClient(bank.readScanner(sc));
					break;
				case 4: // Delete client
					System. out .print(">>> Enter account number (int) = ");
					if (sc.hasNextInt()) {
						accNumber = (long) sc.nextInt();
						String name = bank.getClients().get(accNumber).getName();
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
}
