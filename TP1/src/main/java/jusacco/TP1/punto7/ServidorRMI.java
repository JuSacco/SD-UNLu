package jusacco.TP1.punto7;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class ServidorRMI {
	
	
	
	public static void main(String[] args) {
		try {	
			
			Resolutor r = new Resolutor();
			
			Registry serverRmiRegistry = LocateRegistry.createRegistry(9000);
			System.out.println("Servidor RMI levantado en el puerto 9000");
			
			IRemota serverStub = (IRemota) UnicastRemoteObject.exportObject(r, 8000);
			serverRmiRegistry.rebind("accion", serverStub);
			System.out.println("Binding realizado");
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}


}
