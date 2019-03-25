package jusacco.TP1.punto5;

import java.rmi.RemoteException;
import java.util.Random;

public class Clima implements IRemota {
	String lugar;
	int temperatura;
	
	public String getLugar() {
		return lugar;
	}
	public void setLugar(String lugar) {
		this.lugar = lugar;
	}
	public int getTemperatura() {
		return temperatura;
	}

	public Clima (String lugar) {
		this.temperatura = new Random().nextInt(30);
		this.lugar = lugar;
	}
	
	public String getDatos() throws RemoteException{
		return "La temperatura es "+this.temperatura+"°C en "+this.lugar;
	}
}
