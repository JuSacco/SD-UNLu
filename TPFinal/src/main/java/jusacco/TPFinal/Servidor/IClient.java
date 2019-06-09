package jusacco.TPFinal.Servidor;

import java.rmi.Remote;
import java.rmi.RemoteException;

import jusacco.TPFinal.Cliente.Imagen;


public interface IClient extends Remote{

	public Imagen renderRequest(Mensaje msg) throws RemoteException;
	public String helloFromClient(String clientIp)throws RemoteException;
}
