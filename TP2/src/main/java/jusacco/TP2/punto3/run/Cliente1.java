package jusacco.TP2.punto3.run;

import java.rmi.NotBoundException;

import jusacco.TP2.punto3.Cliente;

public class Cliente1 {

	public static void main(String[] args) {
		new Thread(new Runnable() {
		    public void run() {
		    	try {
					Cliente.main();
				} catch (NotBoundException e) {
					e.printStackTrace();
				}
		    }
		}).start();
	}

}
