package jusacco.TP2.punto2.conSync;


public class DepositarDinero implements Runnable {
	Cuenta cuenta;
	int cantidad;
	String nombre;

	public DepositarDinero(Cuenta cuenta, int i, String nombre) {
		this.cuenta = cuenta;
		this.cantidad = i;
		this.nombre = nombre;
	}

	@Override
	public void run() {
		while(true) {
			//synchronized (this.cuenta) {
				System.out.println(this.nombre+" Despositando $"+this.cantidad+" en cuenta nro. "+this.cuenta.nroCuenta);
				this.cuenta.addDinero(this.cantidad, this.nombre);
			//}
		}
	}
}
