package jusacco.TP2.punto2.sinSync;

import java.time.LocalTime;

public class Cuenta {
	int dinero;
	String nroCuenta;
	
	public Cuenta(int dinero, String nroCuenta) {
		this.dinero = dinero;
		this.nroCuenta = nroCuenta;
	}

	public int getCurrentBalance() {
		return this.dinero;
	}
	
	public void addDinero (int value, String persona) {
	    LocalTime time = LocalTime.now();
		System.out.println("----\nInicio operacion (addDinero sumo: "+value+")\nCuenta: "+this.nroCuenta+"\nTiempo: "+time+"\nDinero disponible: "+this.dinero+"\nAutor:"+persona+"\n----");
		try {
			Thread.sleep(400);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		this.dinero += value;
	    time = LocalTime.now();
		System.out.println("----\nFin operacion (addDinero sumé: "+value+")\nCuenta: "+this.nroCuenta+"\nTiempo: "+time+"\nDinero disponible: "+this.dinero+"\nAutor:"+persona+"\n----");
	}
	
	public void discDinero (int value, String persona) {
	    LocalTime time = LocalTime.now();
		System.err.println("----\nInicio operacion (discDinero resto: "+value+")\nCuenta: "+this.nroCuenta+"\nTiempo: "+time+"\nDinero disponible: "+this.dinero+"\nAutor:"+persona+"\n----");
		try {
			Thread.sleep(800);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		this.dinero-=value;
	    time = LocalTime.now();
		System.err.println("----\nFin operacion (discDinero resté: "+value+")\nCuenta: "+this.nroCuenta+"Tiempo: "+time+"\nDinero disponible: "+this.dinero+"\nAutor:"+persona+"\n----");
	}
	
}
