package jusacco.TP2.punto1;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PeerServer implements Runnable{
	String peerIp;
	int peerPort;
	ArrayList<Archivo> liArchivos;
	private final Logger log = LoggerFactory.getLogger(Config.class);
	String directory;
	
	public PeerServer (String ip, int port, ArrayList<Archivo> liArchivos, String directory) {
		this.peerPort = port;
		this.peerIp = ip;
		this.liArchivos = liArchivos;
		this.directory = directory;
	}


	@Override
	public void run() {
		try {
			ServerSocket ss = new ServerSocket (this.peerPort);
			log.info("Server Peer en puerto: "+peerPort);
			int counter = 0;
			while (true) {
				Socket client = ss.accept();
				counter++;
				log.info("Server Peer conexion con el cliente nro: "+counter);
				//Thread: Serve petition
				PeerServerWorker worker = new PeerServerWorker (this.liArchivos,client, directory);
				Thread tWorker= new Thread (worker);
				tWorker.start();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println(" Socket on port "+peerPort+" is used ");
		}
	}
	
}
