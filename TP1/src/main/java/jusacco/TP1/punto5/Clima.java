package jusacco.TP1.punto5;

import java.rmi.RemoteException;
import java.util.Random;

public class Clima implements IRemota {
	String lugar;
	int temperatura;
	
	public Clima (String lugar) {
		this.temperatura = new Random().nextInt(30);
		this.lugar = lugar;
	}
	
	public String getClima() throws RemoteException{
		return "La temperatura es "+this.temperatura+"°C en "+this.lugar;
	}
}
