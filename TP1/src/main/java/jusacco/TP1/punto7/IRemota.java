package jusacco.TP1.punto7;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IRemota extends Remote {
	public Object accion_ejecutar(ITarea t) throws RemoteException;
}
