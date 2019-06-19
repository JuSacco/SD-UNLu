package jusacco.TPFinal.Servidor;

import java.awt.image.BufferedImage;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.NotBoundException;
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
import org.slf4j.MDC;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import jusacco.TPFinal.Cliente.Imagen;
import jusacco.TPFinal.Servidor.Tools.*;

public class Servidor implements IClient{
	//General settings
	Logger log = LoggerFactory.getLogger(Servidor.class);
	String myDirectory = System.getProperty("user.dir")+"\\ServerData\\";
	private String myIp;
	private String backupIp;
	private ArrayList<String> listaWorkers = new ArrayList<String>();
	private ArrayList<String> listaTrabajos = new ArrayList<String>();
	Map<String,LocalTime> workersLastPing = new HashMap<String,LocalTime>();
	//RMI
	private int rmiPortCli;
	private int rmiPortSv;
	Registry registryCli;
	Registry registrySv;
	private IClient remoteClient;
	private IFTPManager remoteFtpMan;
	private IWorkerAction remoteWorker;
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
	private String queuePort;
	private String queueTrabajo = "queueTrabajo";
	private String queueTerminados = "queueTerminados";
	

	
	public Servidor() {
		try {
			MDC.put("log.name", Servidor.class.getSimpleName().toString());
			readConfigFile();
			initialConfig();
			runRMIServer();
			while(true) {
				//Checkeo si se cayo un nodo
				for(String str : listaWorkers) {
					if((int)Duration.between(workersLastPing.get(str), LocalTime.now()).getSeconds() > 70) {
						synchronized (listaWorkers) {
							listaWorkers.remove(str);
							log.error("Eliminando al nodo "+str+". Motivo time-out de "+(int)Duration.between(workersLastPing.get(str), LocalTime.now()).getSeconds()+" segundos.");
						}
					}
				}
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}catch(RemoteException e){
			e.printStackTrace();
		}
	}
	public Servidor(String ip) {
		try {
			MDC.put("log.name", Servidor.class.getSimpleName().toString());
			readConfigFile();
			String primaryIp = this.myIp;
			this.myIp = ip;
			initialConfig();
			runRMIServer();
			while(true) {
				//Checkeo si se cayo un nodo
				for(String str : listaWorkers) {
					if((int)Duration.between(workersLastPing.get(str), LocalTime.now()).getSeconds() > 70) {
						listaWorkers.remove(str);
						log.error("Eliminando al nodo "+str+". Motivo time-out de "+(int)Duration.between(workersLastPing.get(str), LocalTime.now()).getSeconds()+" segundos.");
					}
				}
				try {
					Registry clienteRMI = LocateRegistry.getRegistry(primaryIp,this.rmiPortSv);
					IWorkerAction server = (IWorkerAction) clienteRMI.lookup("server");
					log.info("El servidor principal esta ON. Esperando estar listo para migrar al principal");
					while(true) {
						if(this.listaTrabajos.isEmpty()) {
							//Mato todo para terminar con este proceso
							listaTrabajos.add("1234567890exit");
							UnicastRemoteObject.unexportObject(registryCli,true);
							UnicastRemoteObject.unexportObject(registrySv,true);
							System.gc();
							break;
						}
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					break;
				} catch (RemoteException | NotBoundException e) {
					//Error porque el server principal no esta ON
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}
			}
		}catch(RemoteException e){
			e.printStackTrace();
		}
	}
	
	private void runRMIServer() throws RemoteException {
		System.setProperty("sun.security.ssl.allowUnsafeRenegotiation", "true"); // renegotiation process is disabled by default.. Without this can't run two clients rmi on same machine like worker and client.
		log.info("Levantando servidor RMI...");
		registryCli = LocateRegistry.createRegistry(this.rmiPortCli);
		registrySv = LocateRegistry.createRegistry(this.rmiPortSv);
		
		remoteFtpMan = (IFTPManager) UnicastRemoteObject.exportObject(new FTPManager(this.ftpPort, this.ftp),0);
		remoteClient = (IClient) UnicastRemoteObject.exportObject(this,0);
		remoteWorker = (IWorkerAction) UnicastRemoteObject.exportObject(new WorkerAction(this.listaWorkers, this.listaTrabajos, this.workersLastPing),0);
		
		registrySv.rebind("Acciones", remoteFtpMan);
		registrySv.rebind("server", remoteWorker);
		registryCli.rebind("client", remoteClient);
		log.info("Servidor RMI{");
		log.info("\t Client:"+registryCli.toString());
		log.info("\t Server:"+registrySv.toString()+"\n\t\t\t}");
	}
	
	private void readConfigFile() {
		Gson gson = new Gson();
		Map config;
		try {
			config = gson.fromJson(new FileReader(myDirectory+"serverConfig.json"), Map.class);
			
			Map data = (Map) config.get("server");
			this.myIp = data.get("ip").toString();
			this.backupIp =  data.get("ipBak").toString();
			
			data = (Map) config.get("rmi");
			this.rmiPortCli = Integer.valueOf(data.get("portCli").toString());
			this.rmiPortSv = Integer.valueOf(data.get("portSv").toString());
			
			data = (Map) config.get("ftp");
			this.ftpPort = Integer.valueOf(data.get("port").toString());
			this.myFTPDirectory = this.myDirectory + data.get("directory").toString();
			
			data = (Map) config.get("queue");
			this.queueIp = data.get("ip").toString();
			this.queuePort = data.get("port").toString();
			this.queueUser = data.get("user").toString();
			this.queuePwd = data.get("pass").toString();
			
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
			this.queueChannel.queueDelete(this.queueTrabajo);
		    this.queueChannel.queueDelete(this.queueTerminados);
			this.queueChannel.queueDeclare(this.queueTrabajo, false, false, false, null);
			this.queueChannel.queueDeclare(this.queueTerminados, false, false, true, null);
			log.info("RabbitMQ inicio correctamente.");
		} catch (IOException e) {
			e.printStackTrace();
			log.error("Error: Compruebe si RabbitMQ esta instalado en su equipo.");
		} catch (TimeoutException e) {
			log.error("Error: Time out.");
		}
		//FTP RELATED
		this.ftp = new ServerFtp(this.ftpPort, this.myFTPDirectory);
		log.info("FTP Configurado correctamente. Listo para usar en puerto:"+this.ftpPort+". Compartiendo carpeta: "+this.myFTPDirectory);
	}


	@Override
	public Imagen renderRequest(Mensaje msg) throws RemoteException {
		try {
			//Create a new Channel 
			Channel chThread;
			chThread = this.queueConnection.createChannel();
			BufferedImage respuesta = null;
			ThreadServer thServer = new ThreadServer(msg, listaWorkers, respuesta, chThread, this.queueConnection, this.listaTrabajos, this.workersLastPing);
			Thread th = new Thread(thServer);
			log.debug("-------------------------------------------------------------------------");
			log.debug("|### "+Thread.currentThread().toString()+" ###");
			log.debug("|Pedido de trabajo: "+msg.getName());
			log.debug("-------------------------------------------------------------------------");
			th.start();
			while(thServer.getRespuesta() == null) {
				try {
					Thread.sleep(400);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			log.debug("-------------------------------------------------------------------------");
			log.debug("|### "+Thread.currentThread().toString()+" ###");
			log.debug("|Lista trabajo: "+this.listaTrabajos.toString());
			log.debug("|Acaba de terminar: "+msg.getName());
			log.debug("|Eliminando... "+msg.getName());
			log.debug("-------------------------------------------------------------------------");
			this.listaTrabajos.remove(msg.getName()+":"+msg.ipCliente);
			th.interrupt();
			return new Imagen(thServer.getRespuesta());
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	@Override
	public String helloFromClient(String clientIp) throws RemoteException {
		log.info("Se conecto el cliente"+clientIp);
		return "OK";
	}
	

	public static void main(String[] args) {
		new Servidor();
	}
}
