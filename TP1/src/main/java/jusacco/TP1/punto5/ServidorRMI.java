package jusacco.TP1.punto5;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class ServidorRMI {
	public static void main(String[] args) {
		try {	
			Clima c1 = new Clima("Buenos Aires");
			
			Registry serverRmiRegistry = LocateRegistry.createRegistry(9000);
			System.out.println("Servidor RMI levantado en el puerto 9000");
			
			IRemota serverStub = (IRemota) UnicastRemoteObject.exportObject(c1, 8000);
			System.out.println("Objetos correctamente publicados a traves de IRemota");
			
			serverRmiRegistry.rebind("infoClima", serverStub);
			System.out.println("Se ha bindeado el nombre con el servicio");
			
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
}
