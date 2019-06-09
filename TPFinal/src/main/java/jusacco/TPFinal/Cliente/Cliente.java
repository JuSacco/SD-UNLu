package jusacco.TPFinal.Cliente;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.google.gson.Gson;

import jusacco.TPFinal.Servidor.IClient;
import jusacco.TPFinal.Servidor.Mensaje;

public class Cliente{
	static Logger log = LoggerFactory.getLogger(Cliente.class);
	IClient stub;
	File file;
	byte[] fileContent;
	int tipoRender = 0;
	private String serverIp;
	private Integer serverPort;
	
	public Cliente() {
		readConfigFile();
		MDC.put("log.name", Cliente.class.getSimpleName().toString());
	}
	
	public void connectRMI(String ip, int port) {
		Registry clienteRMI;
		try {
			clienteRMI = LocateRegistry.getRegistry(ip,port);
			this.stub = (IClient) clienteRMI.lookup("client");
		} catch (RemoteException | NotBoundException e) {
			log.error("No se pudo conectar al servidor.Revise ip/puerto");
			try {
				Thread.sleep(5000);
				log.info("Intentando reconectar...");
				connectRMI(ip, port);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
	}
	public void setFile(File f) {
		try {
			this.file = f;
			this.fileContent = Files.readAllBytes(file.toPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void setTipoRender(int tipo) {
		this.tipoRender = tipo;
	}
	public String enviarFile(int i, int noFrame) {
		connectRMI(serverIp, serverPort);
		if(this.file != null) {
			if(this.tipoRender == 0) { //0 = por sample ; 1 = por tiempo
				log.info("Enviando el archivo: "+this.file.getName());
				String myIp = "";
				try {
					myIp = Inet4Address.getLocalHost().getHostAddress();
					log.info("Conexion con el servidor: "+this.stub.helloFromClient(myIp));
				} catch (UnknownHostException | RemoteException e1) {
					e1.printStackTrace();
				}
				Mensaje m = new Mensaje(this.fileContent, file.getName(),i, noFrame,myIp);
				log.debug("Cree el mensaje: "+m.getName());
				try {
					log.debug("Enviando el mensaje....");
					Imagen returned = this.stub.renderRequest(m);
					returned.persistImg("./resultado.png");
					return new File("./resultado.png").getAbsolutePath();
				} catch (RemoteException e) {
					log.error("Error en la conexion con el servidor..");
					log.error("Error: "+e.getMessage());
					try {
						Thread.sleep(5000);
						log.info("Reintentando enviar "+this.file.getName());
						enviarFile(i, noFrame);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}
			}else {
				log.info("Enviando el archivo: "+this.file.getName());
				String myIp = "";
				try {
					myIp = Inet4Address.getLocalHost().getHostAddress();
				} catch (UnknownHostException e1) {
					e1.printStackTrace();
				}
				Mensaje m = new Mensaje(this.fileContent, i, file.getName(),noFrame,myIp);
				try {
					Imagen returned = this.stub.renderRequest(m);
					returned.persistImg("./resultado.png");
					return new File("./resultado.png").getAbsolutePath();
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		}else {
			log.error("Error: Archivo no cargado");
			return "Error";
		}
		return "Error";
	}
	public boolean isReady() {
		if(this.file != null && this.fileContent != null)
			return true;
		else return false;
	}
	
	private void readConfigFile() {
		Gson gson = new Gson();
		Map config;
		try {
			config = gson.fromJson(new FileReader("clienteConfig.json"), Map.class);
			Map server = (Map) config.get("server");
			this.serverIp = server.get("ip").toString();
			this.serverPort = Integer.valueOf(server.get("port").toString());
		} catch (IOException e) {
			log.info("Error Archivo Config!");
		} 
	}
	
}

