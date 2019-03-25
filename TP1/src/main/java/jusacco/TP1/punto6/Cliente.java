package jusacco.TP1.punto6;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;


public class Cliente {

	public static void main(String[] args) {
		try {
			
			Scanner sc = new Scanner(System.in);
			Registry clientRMI = LocateRegistry.getRegistry("localhost", 9000);
			System.out.println("Lista de servicios disponibles: ");
			String[] services = clientRMI.list();
			
			for (String service : services) {
				System.out.println(service);
			}
			IRemota cliStub = (IRemota) clientRMI.lookup("operacionVectores");
			int[] v1 = new int[3];
			int[] v2 = new int[3];
			System.out.println("Ingrese vector 1: ");
			for (int i = 0;i<3;i++){
				System.out.println("V["+i+"]: ");
				v1[i] = sc.nextInt();
			}
			System.out.println("Ingrese vector 2: ");
			for (int i = 0;i<3;i++){
				System.out.println("V["+i+"]: ");
				v2[i] = sc.nextInt();
			}
			int resultado[] = {0,0,0};
			resultado = cliStub.sumarVectores(v1, v2);
			System.out.println("Resultado suma: "+resultado[0]+","+resultado[1]+","+resultado[2]);
			resultado = cliStub.restarVectores(v1, v2);
			System.out.println("Resultado resta: "+resultado[0]+","+resultado[1]+","+resultado[2]);
			resultado = cliStub.sumarMal(v1, v2);
			System.out.println("Introduzca un error en su código que modifique los vectores recibidos por parámetro.");
			System.out.println("Resultado de sumar mal: "+resultado[0]+","+resultado[1]+","+resultado[2]);
			resultado = cliStub.restarMal(v1, v2);
			System.out.println("Resultado de restar mal: "+resultado[0]+","+resultado[1]+","+resultado[2]);
			System.out.println("¿Qué impacto se genera?\r\n"+"¿Qué conclusión saca sobre la forma de pasaje de parámetros en RMI? ");
			System.out.println("-");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
