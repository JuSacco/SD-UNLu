package jusacco.TP2.punto1.run;

import jusacco.TP2.punto1.Peer;

public class Peer4 {

	public static void main(String[] args) {
		/*		Inicio Peer4		*/
		new Thread(new Runnable() {
		    public void run() {
		    	Peer.main(8004,"peers/nodo4/");
		    }
		}).start();
	}

}
