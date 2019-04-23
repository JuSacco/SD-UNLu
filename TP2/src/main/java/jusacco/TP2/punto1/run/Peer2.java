package jusacco.TP2.punto1.run;

import jusacco.TP2.punto1.Peer;

public class Peer2 {

	public static void main(String[] args) {
		/*		Inicio Peer2		*/
		new Thread(new Runnable() {
		    public void run() {
		    	Peer.main(8002,"peers/nodo2/");
		    }
		}).start();
	}

}
