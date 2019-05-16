package jusacco.TP2.punto3;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Random;

public class Cliente{
	
	public static void main() throws NotBoundException {
		try {
			Random rnd = new Random();
			int opcion = rnd.nextInt(2);
			int[] v1 = new int[3];
			int[] v2 = new int[3];
			int[] resultado;
			Registry clienteRMI = LocateRegistry.getRegistry("127.0.0.1",9000);
			ITarea cliStub = (ITarea) clienteRMI.lookup("Tarea");
			
			for (int i = 0; i < 3; i++) {//Genero 2 vectores rnd
				v1[i] = new Random().nextInt(9);
				v2[i] = new Random().nextInt(9);
			}
			if(opcion == 0) {
				
					resultado = cliStub.restar(v1, v2);
				
				System.out.printf("Resta de Vectores: v1[%d,%d,%d] y v2[%d,%d,%d]\n",v1[0],v1[1],v1[2],v2[0],v2[1],v2[2]);
			}else {
				resultado = cliStub.sumar(v1, v2);
				System.out.printf("Suma de Vectores: v1[%d,%d,%d] y v2[%d,%d,%d]\n",v1[0],v1[1],v1[2],v2[0],v2[1],v2[2]);
			}
			System.out.printf("Resultado: [%d,%d,%d]\n",resultado[0],resultado[1],resultado[2]);
			
		} catch (RemoteException e) {
		}
	}

}
