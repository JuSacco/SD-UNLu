package jusacco.TP2.punto3;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IClientServices extends Remote{

	public String realizarTarea(String name) throws RemoteException;
	
}
