package jusacco.TP2.punto4.distributedImproved;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ITarea extends Remote {
	public Imagen convertirImg(Imagen img) throws RemoteException;
}
