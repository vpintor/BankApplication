package es.upm.dit.fcon;

import java.util.Scanner;

public class BankMain {
	private bankFuncional bank; 
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		if (args[0] != null) {
			
			try {
				bankFuncional bank = new bankFuncional(Integer.parseInt(args[0]));
				System.out.println("Se ha creado el banco "+args[0]);
				bank.initZoo(bank.getBankId());

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
						System.out.println("###################################################################################################");
						System. out .println("\n>>> Enter operation client.: 1) Create.  2) Read.  3) Update.  4) Delete.  5) ReadAll.  6) Exit\n");
						System.out.println("###################################################################################################");

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
							//bank.createClient(c);
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
							//bank.updateClient(c);
		
							break;
						case 4: // Delete client
							System. out .print(">>> Enter account number (int) = ");
							if (sc.hasNextInt()) {
								accNumber = (long) sc.nextInt();
								//String name = bank.getClients().get(accNumber).getName();
								bank.sendDeleteClient(accNumber);
//								boolean status = bank.deleteClient(accNumber);
//								if (status) {
//									System.out.println("El cliente " +name+ " se ha borrado correctamente.");
//								}
//								else {
//									System.out.println("Se ha producido un error. El cliente con id " + accNumber + " no existe.");
//								}
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
