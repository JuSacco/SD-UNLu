package jusacco.TP2.punto3;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class NodoServicio implements Runnable, ILoadBalancerServices{
	int port;
	String ip;
	int activeClients;
	NodoTarea nt = new NodoTarea();
	private final Logger log = LoggerFactory.getLogger(NodoServicio.class);
	
	public NodoServicio(String ip, int port) {
		this.port = port;
		this.ip = ip;
		this.activeClients = 0;
		MDC.put("log.name", NodoServicio.class.getSimpleName().toString()+"-"+this.port);
	}
	
	@Override
	public void run() {
		registerServices();
	}

	private void registerServices() {
		try {				
			Registry serverRmiRegistry = LocateRegistry.createRegistry(this.port);
			log.info("[NodoServicio"+"-"+this.port+"]: Servidor RMI levantado en el puerto "+this.port);
			
			ILoadBalancerServices serverStub = (ILoadBalancerServices) UnicastRemoteObject.exportObject(this, this.port);
			log.info("[NodoServicio"+"-"+this.port+"]: Objetos correctamente publicados a traves de ILoadBalancerServices");
			
			serverRmiRegistry.rebind(this.ip+":"+this.port, serverStub);
			log.info("[NodoServicio"+"-"+this.port+"]: Se ha bindeado el nombre con el servicio");
			
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	public String realizarTarea(String name) throws RemoteException{
		this.activeClients++;
		log.info("Nodo: Cantidad de clientes activos "+this.activeClients);
		String res = this.nt.realizarTarea(name);
		this.activeClients--;
		return res;
	}

	public int getActiveClients() {
		return this.activeClients;
	}
}
