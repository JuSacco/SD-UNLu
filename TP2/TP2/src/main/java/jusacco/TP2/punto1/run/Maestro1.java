package jusacco.TP2.punto1.run;

import jusacco.TP2.punto1.ServerMaster;

public class Maestro1 {

	public static void main(String[] args) {
		/*		Inicio Master		*/
		new Thread(new Runnable() {
		    public void run() {
		    	ServerMaster.main(9000);
		    }
		}).start();
	}

}
