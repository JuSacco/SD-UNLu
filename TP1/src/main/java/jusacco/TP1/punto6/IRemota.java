package jusacco.TP1.punto6;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IRemota extends Remote{
	public int[] sumarVectores(int[] v1, int[] v2) throws RemoteException;
	public int[] restarVectores(int[] v1, int[] v2) throws RemoteException;
	public int[] sumarMal(int[] v1, int[] v2) throws RemoteException;
}
