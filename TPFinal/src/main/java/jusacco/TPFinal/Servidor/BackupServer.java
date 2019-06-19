package jusacco.TPFinal.Servidor;

import java.io.FileReader;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

public class BackupServer {
	private final int MAX_ATTEMPS = 1;
	Logger log = LoggerFactory.getLogger(BackupServer.class);
	String myDirectory = System.getProperty("user.dir")+"/ServerData/";
	private IWorkerAction stubServer;
	int serverPort;
	String serverIp;
	String myIp;
	
	public BackupServer() {
		while(true) {
			readConfigFile();
			getRMI(0);
			checkServerStatus();
		}
	}

	private void checkServerStatus() {
		while(true) {
			try {
				this.stubServer.checkStatus();
				log.info("Servidor principal: OK.");
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} catch (RemoteException e) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				log.error(e.getMessage());
				log.info("Servidor principal: Caido. Creando nuevo principal");
				createServer();
				break;
			}
			catch (NullPointerException nul) {
				break;
			}
		}
	}
	
	private void readConfigFile() {
		Gson gson = new Gson();
		Map config;
		try {
			config = gson.fromJson(new FileReader(myDirectory+"serverConfig.json"), Map.class);
			Map data = (Map) config.get("rmi");
			this.serverPort = Integer.valueOf(data.get("portSv").toString());
			data = (Map) config.get("server");
			this.serverIp = data.get("ip").toString();
			this.myIp = data.get("ipBak").toString();
		} catch (IOException e) {
			log.info("Error Archivo Config!");
			e.printStackTrace();
		} 
	}
	
	private void getRMI(int attemps) {
		try {
			Registry clienteRMI = LocateRegistry.getRegistry(this.serverIp,serverPort);
			log.info("Obteniendo servicios RMI.");
			log.info("Obteniendo stub...");
			log.info("Attemps "+attemps);
			this.stubServer = (IWorkerAction) clienteRMI.lookup("server");
		} catch (RemoteException | NotBoundException e) {
			if(attemps > this.MAX_ATTEMPS) {
				createServer();
			}else {
				log.error("RMI Error: "+e.getMessage());
				log.info("Re-intentando conectar a "+this.serverIp+":"+this.serverPort);
				for (int i = 5; i > 0; i--) {
					try {
						Thread.sleep(1000);
						log.info("Re-intentando en..."+i);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}
				getRMI(++attemps);
			}
		}
	}

	private void createServer() {
		log.info("Levantando servidor en: "+this.myIp);
		new Servidor(this.myIp);
	}
	
	public static void main(String[] args) {
		new BackupServer();
	}
}
