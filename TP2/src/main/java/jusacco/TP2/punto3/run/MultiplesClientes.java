package jusacco.TP2.punto3.run;

import java.rmi.NotBoundException;

import jusacco.TP2.punto3.Cliente;

public class MultiplesClientes {
	
	public void newClient() {
		new Thread(new Runnable() {
		    public void run() {
		    	try {
					Cliente.main();
				} catch (NotBoundException e) {
					e.printStackTrace();
				}
		    }
		}).start();
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			System.err.println("Nope");
		}
	}
	
	public MultiplesClientes() {
		for (int i = 0; i < 55; i++) {
			newClient();
		}
	}
	
	
	
	public static void main(String[] args) {
		new MultiplesClientes();
	}

}
