package jusacco.TP2.punto2.conSync;

public class Cliente {
	Cuenta cuenta;
	int deposito;
	int extraigo;
	String nombre;
	public Cliente(Cuenta cuenta, int i, int j,String nombre) {
		this.cuenta = cuenta;
		this.deposito = i;
		this.extraigo = j;
		this.nombre = nombre;
		DepositarDinero thDepositar = new DepositarDinero(this.cuenta, this.deposito, this.nombre);
		Thread depoThread = new Thread (thDepositar);
		ExtraerDinero thExtraer = new ExtraerDinero (this.cuenta, this.extraigo, this.nombre);
		Thread extrThread = new Thread (thExtraer);
		depoThread.start();		
		extrThread.start();
	}

}
