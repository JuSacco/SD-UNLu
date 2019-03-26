package jusacco.TP1.punto7;

import java.rmi.RemoteException;

public class Resolutor implements IRemota{

	@Override
	public Object accion_ejecutar(ITarea t) throws RemoteException{
		return t.ejecutar();
	}
}
