package jusacco.TP2.punto2.conSync;


public class Banco {
	
	public static void main(String[] args) {
		Cuenta cuenta1 = new Cuenta(1000, "1111");
		Cliente cli1 = new Cliente(cuenta1,100,50,"Juan");
		Cliente cli2 = new Cliente(cuenta1,50,350,"Jose");
		
		
	}
}
