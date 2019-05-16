package jusacco.TP2.punto3;

import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;


public class NodoService implements Runnable {
	Registry registry;
	final static Logger log = LoggerFactory.getLogger(NodoService.class);
	int port;
	
	public NodoService(int port){
		this.port = port;
		String logName = NodoService.class.getSimpleName().toString()+"-"+Thread.currentThread().getId();
		MDC.put("log.name",logName);
		try {
			registry = LocateRegistry.createRegistry(port);
			log.info("NODO SERVICE escuchando en PUERTO "+port);
			

		} catch (RemoteException e) {
			log.info(e.getMessage());
		}
		MDC.remove(logName);
	}
		
	@Override
	public void run(){
		String logName = NodoService.class.getSimpleName().toString()+"-"+Thread.currentThread().getId();
		MDC.put("log.name",logName);
		try {
			Remote remote = UnicastRemoteObject.exportObject(new ITarea() {

				@Override
				public int[] restar(int[] v1, int[] v2) {
					for (int i = 0; i < v1.length; i++) {
						v1[i] = v1[i] - v2[i];
					}
					try {
						Thread.sleep(20000);
					} catch (InterruptedException e) {
						log.info(e.getMessage());
					}
					return v1;
				}

				@Override
				public int[] sumar(int[] v1, int[] v2) throws RemoteException {
					for (int i = 0; i < v1.length; i++) {
						v1[i] = v1[i] + v2[i];
					}
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						log.info(e.getMessage());
					}
					return v1;
				}
				
			},0);
			
			Remote remoteControl = UnicastRemoteObject.exportObject(new IControl() {
				
				@Override
				public void detenerSv() throws RemoteException {
					try {
						registry.unbind("Tarea");
						registry.unbind("Control");
						UnicastRemoteObject.unexportObject(registry, true);
						log.info("NODOService Detenido!"+port);
					} catch (NotBoundException e) {
						log.info(e.getMessage());
					}
				}
			},0);
			
			registry.bind("Control", remoteControl);
			registry.bind("Tarea", remote);
		} catch (RemoteException | AlreadyBoundException e) {
			log.info(e.getMessage());
		}
		
	}
}
