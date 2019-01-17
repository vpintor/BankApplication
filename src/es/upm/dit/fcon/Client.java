package es.upm.dit.fcon;

import java.io.Serializable;

public class Client implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private Long accountNumber;
	private String name;
	private int balance;
	
	public Client (Long accountNumber, String name, int balance) {
		this.accountNumber = accountNumber;
		this.name          = name;
		this.balance       = balance;
	}

	public Long getAccountNumber() {
		return accountNumber;
	}

	public void setAccountNumber(Long accountNumber) {
		this.accountNumber = accountNumber;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getBalance() {
		return balance;
	}

	public void setBalance(int balance) {
		this.balance = balance;
	}

	@Override
	public String toString() {
		return "[" + accountNumber + ", " + name + ", " + balance + "]";
	}
}