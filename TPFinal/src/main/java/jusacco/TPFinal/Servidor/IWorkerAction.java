package jusacco.TPFinal.Servidor;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface IWorkerAction extends Remote{
	void helloServer(String worker) throws RemoteException;
	void checkStatus() throws RemoteException;
	Mensaje giveWorkToDo(String worker, ArrayList<String> realizedWorks) throws RemoteException;
}
