package jusacco.TP2.punto3.run;

import jusacco.TP2.punto3.Cliente;

public class Cliente1 {

	public static void main(String[] args) {
		new Thread(new Runnable() {
		    public void run() {
		    	Cliente.main(new String[]{"localhost","9000","Juani"});
		    }
		}).start();
	}

}
