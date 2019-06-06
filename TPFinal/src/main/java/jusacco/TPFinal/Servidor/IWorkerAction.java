package jusacco.TPFinal.Servidor;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IWorkerAction extends Remote{
	void helloServer(String worker) throws RemoteException;
	String giveWorkToDo(String worker) throws RemoteException;
}
