package jusacco.TP1.punto7;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;


public class Cliente {

	public static void main(String[] args) {
		try {
			NumeroRandom rnd = new NumeroRandom();
			NumeroPrimo primo = new NumeroPrimo();
			
			Registry clientRMI = LocateRegistry.getRegistry("localhost", 9000);
			System.out.println("Lista de servicios disponibles: ");
			String[] services = clientRMI.list();
			
			for (String service : services) {
				System.out.println(service);
			}
			IRemota cliStub = (IRemota) clientRMI.lookup("accion");
			
			System.out.println("El numero random es: "+cliStub.accion_ejecutar(rnd));
			primo.numero = 50;
			System.out.println("El numero "+primo.numero+" es primo: "+cliStub.accion_ejecutar(primo));
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
