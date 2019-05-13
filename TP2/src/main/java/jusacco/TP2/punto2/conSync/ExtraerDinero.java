package jusacco.TP2.punto2.conSync;


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
			if (this.cantidad <= this.cuenta.getCurrentBalance()) {
				
				//synchronized (this.cuenta) {
					System.out.println(this.nombre+" Extrayendo $"+this.cantidad+" en cuenta nro. "+this.cuenta.nroCuenta);
					this.cuenta.discDinero(this.cantidad,this.nombre);
				//}
			}else{
				System.err.println("[ERROR] - No hay suficiente dinero disponible - "+this.cuenta.getCurrentBalance());
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
