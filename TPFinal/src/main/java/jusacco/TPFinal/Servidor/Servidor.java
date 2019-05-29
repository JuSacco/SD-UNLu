package jusacco.TPFinal.Servidor;

import java.awt.image.BufferedImage;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;

import jusacco.TPFinal.Cliente.Imagen;
import jusacco.TPFinal.Servidor.Tools.*;

public class Servidor implements IClient{
	//General settings
	private final int CORTE = 10;
	Logger log = LoggerFactory.getLogger(Servidor.class);
	String myDirectory = System.getProperty("user.dir")+"\\ServerData\\";
	private String ip;
	private int rmiPort;
	
	//Ftp Related
	String myFTPDirectory;
	private int ftpPort;
	ServerFtp ftp;
	
	//Queue Related
	private ConnectionFactory queueConnectionFactory;
	private Connection queueConnection;
	private Channel queueChannel;
	String queueUser;
	String queuePwd;
	String queueIp;
	private String queueTrabajo = "queueTrabajo";
	private String queueTerminados = "queueTerminados";
	
	//Workers knowledge. Just a list, need ping to know if are on or off. Why I use this list? 
	private ArrayList<String> listaWorkers;
	private String queuePort;
	
	public Servidor() {
		try {
			readConfigFile();
			initialConfig();
			runRMIServer();
		}catch(RemoteException e){
			e.printStackTrace();
		}
	}
	
	private void runRMIServer() throws RemoteException {
		log.info("Levantando servidor RMI...");
		Registry registry = LocateRegistry.createRegistry(this.rmiPort);
		IFTPManager remote = (IFTPManager) UnicastRemoteObject.exportObject(new FTPManager(this.ftpPort, this.ftp),0);
		IClient remoteClient = (IClient) UnicastRemoteObject.exportObject(this,0);
		registry.rebind("Acciones", remote);
		registry.rebind("client", remoteClient);
		log.info("Servidor RMI: ON");
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void readConfigFile() {
		Gson gson = new Gson();
		Map config;
		try {
			config = gson.fromJson(new FileReader(myDirectory+"serverConfig.json"), Map.class);
			Map data = (Map) config.get("rmi");
			this.rmiPort = Integer.valueOf(data.get("port").toString());
			
			data = (Map) config.get("ftp");
			this.ftpPort = Integer.valueOf(data.get("port").toString());
			this.myFTPDirectory = this.myDirectory + data.get("directory").toString();
			
			data = (Map) config.get("queue");
			this.queueIp = data.get("ip").toString();
			this.queuePort = data.get("port").toString();
			this.queueUser = data.get("user").toString();
			this.queuePwd = data.get("pass").toString();
			
			ArrayList<String> workerData = (ArrayList) config.get("workers");
			this.listaWorkers = workerData;
			
		} catch (IOException e) {
			log.info("Error Archivo Config!");
		} 
	}

	private void initialConfig() {
		//QUEUE RELATED
		try {
			// [STEP 0] - FACTORIA DE CONEXION
			this.queueConnectionFactory = new ConnectionFactory();
			this.queueConnectionFactory.setHost(this.queueIp);
			this.queueConnectionFactory.setPort(Integer.valueOf(this.queuePort));
			this.queueConnectionFactory.setUsername(this.queueUser);
			this.queueConnectionFactory.setPassword(this.queuePwd);
			// [STEP 1] - QueueConnection
			this.queueConnection = this.queueConnectionFactory.newConnection();
			// [STEP 2] - ChannelConnection
			this.queueChannel = this.queueConnection.createChannel();
			// [STEP 3] - Create the queues
			this.queueChannel.queueDeclare(this.queueTrabajo, true, false, false, null);
			this.queueChannel.queueDeclare(this.queueTerminados, true, false, false, null);
			log.info("RabbitMQ inicio correctamente.");
		} catch (IOException e) {
			log.error("Error: Compruebe si RabbitMQ esta instalado en su equipo.");
		} catch (TimeoutException e) {
			log.error("Error: Time out.");
		}
		
		/*
	     * Eliminio las colas y las vuelvo a crear para borrar total
	     */
	    try {
			this.queueChannel.queueDelete(this.queueTrabajo);
		    this.queueChannel.queueDelete(this.queueTerminados);
			this.queueChannel.queueDeclare(this.queueTrabajo, true, false, false, null);
			this.queueChannel.queueDeclare(this.queueTerminados, true, false, false, null);
	    } catch (IOException e1) {
			e1.printStackTrace();
		}
		
		//FTP RELATED
		System.out.println(this.myFTPDirectory);
		this.ftp = new ServerFtp(this.ftpPort, this.myFTPDirectory);
		log.info("FTP Configurado correctamente. Corriendo en "+this.ftpPort+". Compartiendo "+this.myFTPDirectory);
	}

	public static void main(String[] args) {
		new Servidor();
	}

	@Override
	public Imagen renderRequest(Mensaje msg) throws RemoteException {
	    /*
	     * Eliminio las colas y las vuelvo a crear para borrar total
	     */
	    try {
			this.queueChannel.queueDelete(this.queueTrabajo);
		    this.queueChannel.queueDelete(this.queueTerminados);
			this.queueChannel.queueDeclare(this.queueTrabajo, true, false, false, null);
			this.queueChannel.queueDeclare(this.queueTerminados, true, false, false, null);
	    } catch (IOException e1) {
			e1.printStackTrace();
		}
		boolean salir = false;
		ArrayList<BufferedImage> renderedImages = new ArrayList<BufferedImage>();
		LocalTime initTime = LocalTime.now();
		boolean porSamples = msg.cantidadSamples > 0;
		if(porSamples)
			log.info("Obteniendo trabajo. Modalidad: Por samples. Cantidad: "+msg.cantidadSamples);
		else
			log.info("Obteniendo trabajo. Modalidad: Por tiempo. Cantidad: "+msg.tiempoLimite+" segundos");
		log.info("Tiempo inicio:\t"+initTime.toString());
		try {
			this.queueChannel.basicPublish("", this.queueTrabajo, MessageProperties.PERSISTENT_TEXT_PLAIN, msg.getBytes());
			if(porSamples) {//Entonces cada worker hace X cantidad de samples
				Map<String,Integer> workers = new HashMap<String,Integer>();
				while(!salir) {
					try {
						byte[] data = this.queueChannel.basicGet(this.queueTerminados, false).getBody();
			    		Mensaje m = Mensaje.getMensaje(data);
			    		if(m.name.contentEquals(msg.name)) {
			    			renderedImages.add(Imagen.ByteArrToBuffImg(m.bufferedImg));
			    			if(workers.containsKey(m.from)) {
			    				int count = workers.get(m.from);
			    				workers.put(m.from, count + 1);
			    			}
			    		}
			    		for (String key: workers.keySet()) {
			    		    System.out.println("key : " + key);
			    		    System.out.println("value : " + workers.get(key));
			    		    if(workers.get(key) == msg.cantidadSamples) {
			    		    	workers.remove(key);
			    		    }
			    		}
			    		if(renderedImages.size()>0 && workers.isEmpty()) {
			    			salir = true;
			    		}
					}catch (Exception e) {
						
					}
				}
			}else {//Cada worker opera X cantidad de tiempo
				try {
					Thread.sleep(msg.tiempoLimite * 1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				int terminados = (int) this.queueChannel.messageCount(this.queueTerminados);
				synchronized (this.queueConnection) {
			    	for (int i = 0; i < terminados; i++) {
			    		byte[] data = this.queueChannel.basicGet(this.queueTerminados, false).getBody();
			    		Mensaje m = Mensaje.getMensaje(data);
			    		if(m.name.contentEquals(msg.name))
			    			renderedImages.add(Imagen.ByteArrToBuffImg(m.bufferedImg));
					}
				}
			}
			log.info("Cantidad de imagenes:\t"+renderedImages.size());
			BufferedImage result = ImageStacker.aplicarFiltroStack(renderedImages);
			log.info("endTime aplicarFilto:\t"+LocalTime.now().toString());
		    log.info("Delta time aplicarFilto:\t"+Duration.between(initTime, LocalTime.now()));
			return new Imagen(result);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}
