package jusacco.TP2.punto3;

import java.rmi.RemoteException;
import java.util.Random;

public class NodoTarea {
	public String realizarTarea(String name) throws RemoteException{
		Random rnd = new Random();
		String res = "Trabajando para "+name+" haciendo una contraseña fuerte.\nContraseña: ";
		for (int i = 0; i < 20; i++) {
			try {
				Thread.sleep(2000);
				res +=Character.toString((char) (33 + rnd.nextInt(93)));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return res;
	}

}
