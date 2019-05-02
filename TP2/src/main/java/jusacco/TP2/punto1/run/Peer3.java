package jusacco.TP2.punto1.run;

import jusacco.TP2.punto1.Peer;

public class Peer3 {

	public static void main(String[] args) {
		/*		Inicio Peer2		*/
		new Thread(new Runnable() {
		    public void run() {
		    	Peer.main(8003,"peers/nodo3/");
		    }
		}).start();
	}

}
