package jusacco.TP2.punto1;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class PeerServer implements Runnable{
	String peerIp;
	int peerPort;
	ArrayList<Archivo> liArchivos;
	private final Logger log = LoggerFactory.getLogger(PeerServer.class);
	String directory;
	
	public PeerServer (String ip, int port, ArrayList<Archivo> liArchivos, String directory) {
		this.peerPort = port;
		this.peerIp = ip;
		this.liArchivos = liArchivos;
		this.directory = directory;
	}


	@Override
	@SuppressWarnings("resource")
	public void run() {
		try {
			MDC.put("log.name", PeerServer.class.getSimpleName().toString()+"-"+this.peerPort+"-"+Thread.currentThread().getId());
			ServerSocket ss = new ServerSocket (this.peerPort);
			log.info("[SERVER PEER-"+this.peerPort+"]: ON");
			int counter = 0;
			while (true) {
				Socket client = ss.accept();
				counter++;
				log.info("[SERVER PEER-"+this.peerPort+"] Establecio conexion con el cliente nro: "+counter);
				log.info("[SERVER PEER-"+this.peerPort+"] Info Cliente: "+client.getInetAddress()+":"+client.getPort());
				//Thread: Serve petition
				PeerServerWorker worker = new PeerServerWorker (this.liArchivos,client, directory);
				Thread tWorker= new Thread (worker);
				tWorker.start();
			}
		} catch (IOException e) {
			System.out.println(" Socket on port "+peerPort+" is used ");
		}
	}
	
}
