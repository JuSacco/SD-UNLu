package jusacco.TP2.punto3.run;

import jusacco.TP2.punto3.Balanceador;

public class BalanceadorInstance {

	public static void main(String[] args) {
		new Thread(new Runnable() {
		    public void run() {
		    	Balanceador.main("localhost",9000);
		    }
		}).start();
	}

}
