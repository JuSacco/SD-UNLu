package jusacco.TP1.punto5;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;



public class Cliente {

	public static void main(String[] args) {
				try {
					Registry clientRMI = LocateRegistry.getRegistry("localhost", 9000);
					System.out.println("Lista de servicios disponibles: ");
					String[] services = clientRMI.list();
					
					for (String service : services) {
						System.out.println(service);
					}
					IRemota cliStub = (IRemota) clientRMI.lookup("infoClima");
					
					System.out.println(cliStub.getClima());
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			
	}

}
