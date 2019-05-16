package jusacco.TP2.punto3.run;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import jusacco.TP2.punto3.Balanceador;

public class BalanceadorInstance {

	public static void main(String[] args) {
		new Thread(new Runnable() {
		    public void run() {
		    	try {
					Balanceador.main();
				} catch (RemoteException | NotBoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    }
		}).start();
	}

}
