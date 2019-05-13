package jusacco.TP2.punto2.sinSync;


public class ExtraerDinero implements Runnable {
	Cuenta cuenta;
	int cantidad;
	String nombre;

	public ExtraerDinero(Cuenta cuenta, int i, String nombre) {
		this.cuenta = cuenta;
		this.cantidad = i;
		this.nombre = nombre;
	}

	@Override
	public void run() {
		while(true) {
			synchronized (this.cuenta) {
				if (this.cantidad <= this.cuenta.getCurrentBalance()) {
					System.out.println(this.nombre+" Extrayendo $"+this.cantidad+" en cuenta nro. "+this.cuenta.nroCuenta+"\n");
					this.cuenta.discDinero(this.cantidad,this.nombre);
				}
			}
			if(this.cantidad > this.cuenta.getCurrentBalance()) {
				System.err.println("[ERROR] - No hay suficiente dinero disponible - "+this.cuenta.getCurrentBalance());
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
