package jusacco.TP2.punto2.sinSync;


public class Banco {
	
	public static void main(String[] args) {
		Cuenta cuenta1 = new Cuenta(1000, "1111");
		Cliente cli1 = new Cliente(cuenta1,100,150,"Juan");
		Cliente cli2 = new Cliente(cuenta1,50,350,"Jose");
		Cliente cli3 = new Cliente(cuenta1,200,500,"Pedro");
		
		Thread cli1Thread = new Thread (cli1);
		Thread cli2Thread = new Thread (cli2);
		Thread cli3Thread = new Thread (cli3);
		cli1Thread.start();		
		cli2Thread.start();
		cli3Thread.start();
		
	}
}
