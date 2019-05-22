package jusacco.TP2.punto4.distributedImproved;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.Thread.State;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeoutException;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;

public class Maestro implements ITarea{
	int port;
	final String USERNAME = "guest";
	final String PWD = "guest";
	final String IP = "localhost";
	final int CANT_WORKER = 500;
	final int CANT_CORTES = 10;
	Logger log = LoggerFactory.getLogger(Maestro.class);
	String ip;
	String username;
	String password;
	
	ConnectionFactory queueConnectionFactory;
	Connection queueConnection;
	Channel queueChannel;
	String queueTrabajos = "queueTrabajos";
	String queueEnProceso = "queueEnProceso";
	String queueTerminados = "queueTerminados";
	
	public Maestro (int port) {
		initialConfig(port);
		try {
			runRMIServer();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	private void instantiateWorkers() {
		for (int i = 0; i < this.CANT_WORKER; i++) {
			Worker worker = new Worker(this.queueChannel, this.queueConnection);
			Thread thWorker = new Thread(worker);
			thWorker.start();
		}
	}

	private void runRMIServer() throws RemoteException {
		Remote remote = UnicastRemoteObject.exportObject(this,0);
		Registry registry = LocateRegistry.createRegistry(this.port);
		registry.rebind("Tarea", remote);
	}
	
	public Imagen convertirImg(Imagen img) throws RemoteException {
		try {
			LocalTime initTime = LocalTime.now();
			this.queueChannel.queuePurge(this.queueTrabajos);
			this.queueChannel.queuePurge(this.queueTerminados);
			this.queueChannel.queuePurge(this.queueEnProceso);
			
			BufferedImage bImage = img.getImage();
			ManipularImagen imgHandler = new ManipularImagen(bImage,this.CANT_CORTES);
			boolean termino = false;
			ArrayList<Imagen> arrayImg = imgHandler.cortar();
			ArrayList<Imagen> arrayImgConFiltro = new ArrayList<Imagen>(); 
			ArrayList<BufferedImage> buffImgOrdenada = new ArrayList<BufferedImage>(); 
		    for (Imagen imgCutted : arrayImg) {
				this.queueChannel.basicPublish("", this.queueTrabajos, MessageProperties.PERSISTENT_TEXT_PLAIN, Imagen.imagenToByteArr(imgCutted));
			}
			log.info("Cola queueTrabajos: "+(int)this.queueChannel.messageCount(this.queueTrabajos));
			instantiateWorkers();
			int cortes = (int)Math.pow(((int)Math.sqrt(CANT_CORTES)),2);
		    while(!termino) {
		    	int terminados = (int) this.queueChannel.messageCount(this.queueTerminados);
		    	if(terminados == cortes){//Cant Cortes Real
		    		termino = true;
		    	}
		    }
	    	synchronized (this.queueConnection) {
		    	for (int i = 0; i < arrayImg.size(); i++) {
		    		byte[] data = this.queueChannel.basicGet(this.queueTerminados, false).getBody();
					arrayImgConFiltro.add(Imagen.ByteArrToImagenObj(data));
				}
			}
	    	/*
	    	 * Ordenar el array de partes
	    	 */
	    	Collections.sort(arrayImgConFiltro,(o1, o2) -> Integer.compare(o1.getNroImage(),o2.getNroImage()));
	    	
			for(Imagen i : arrayImgConFiltro) {
				buffImgOrdenada.add(i.getImage());
			}
		    BufferedImage result = imgHandler.unirImagen(buffImgOrdenada);
		    LocalTime endTime = LocalTime.now();
			
		    /*
		     * Eliminio las colas y las vuelvo a crear para borrar total
		     */
		   
		    this.queueChannel.queueDelete(this.queueTrabajos);
		    this.queueChannel.queueDelete(this.queueTerminados);
		    this.queueChannel.queueDelete(this.queueEnProceso);
			
			this.queueChannel.queueDeclare(this.queueTrabajos, true, false, false, null);
			this.queueChannel.queueDeclare(this.queueEnProceso, true, false, false, null);
			this.queueChannel.queueDeclare(this.queueTerminados, true, false, false, null);
		    
			log.info("Master: Enviando el resultado");
		    log.info("Init time:"+initTime.toString());
		    log.info("End time:"+endTime.toString());
		    log.info("Delta time:"+Duration.between(initTime, endTime));
		    
			return new Imagen(result);
		
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private void initialConfig(int port) {
		this.port = port;
		this.username = this.USERNAME;
		this.password = this.PWD;
		this.ip = this.IP;

		try {
			// [STEP 0] - FACTORIA DE CONEXION
			this.queueConnectionFactory = new ConnectionFactory();
			this.queueConnectionFactory.setHost(this.ip);
			this.queueConnectionFactory.setUsername(this.username);
			this.queueConnectionFactory.setPassword(this.password);
			
			// [STEP 1] - QueueConnection
			this.queueConnection = this.queueConnectionFactory.newConnection();
			// [STEP 2] - ChannelConnection
			this.queueChannel = this.queueConnection.createChannel();
			// [STEP 3] - Create the queues
			//queueDeclare(String queue, boolean durable, boolean exclusive, boolean autoDelete, Map<String, Object> arguments)
			this.queueChannel.queueDeclare(this.queueTrabajos, true, false, false, null);
			this.queueChannel.queueDeclare(this.queueEnProceso, true, false, false, null);
			this.queueChannel.queueDeclare(this.queueTerminados, true, false, false, null);
			
		} catch (IOException | TimeoutException e) {
			e.printStackTrace();
		}
		log.info("Master: RabbitMQ Queue System has started correctly");
	}
	public static void main(String[] args) {
		new Maestro(9000);
	}
}
