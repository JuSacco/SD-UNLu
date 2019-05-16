package jusacco.TP2.punto3;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ITarea extends Remote {
	int[] restar(int[]v1, int[]v2) throws RemoteException;
	int[] sumar(int[]v1, int[]v2) throws RemoteException;
}
