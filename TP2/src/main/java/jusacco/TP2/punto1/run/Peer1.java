package jusacco.TP2.punto1.run;

import jusacco.TP2.punto1.Peer;
public class Peer1 {

	public static void main(String[] args) {
		/*		Inicio Peer1		*/
		new Thread(new Runnable() {
		    public void run() {
		    	Peer.main(8001,"peers/nodo1/");
		    }
		}).start();
	}

}
