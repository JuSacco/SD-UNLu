package jusacco.TP1.punto5;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;


public class Cliente {

	public static void main(String[] args) {

			try {
				Registry clientRMI = LocateRegistry.getRegistry("localhost", 9000);
				
				String[] services = clientRMI.list();
				
				for (String service : services) {
					System.out.println(" SERV: "+service);
				}
				IRemota cliStub = (IRemota) clientRMI.lookup("getClima");
				
				System.out.println(cliStub.getDatos());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
	}

}
