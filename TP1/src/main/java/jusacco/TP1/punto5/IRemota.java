package jusacco.TP1.punto5;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IRemota extends Remote{
	public String getDatos() throws RemoteException;
}
