package jusacco.TP2.punto3.run;

import jusacco.TP2.punto3.Cliente;

public class MultiplesClientes {
	
	public MultiplesClientes() {
		new Thread(new Runnable() {
		    public void run() {
		    	Cliente.main(new String[]{"localhost","9000","Juani"});
		    }
		}).start();
		new Thread(new Runnable() {
		    public void run() {
		    	Cliente.main(new String[]{"localhost","9000","Jose"});
		    }
		}).start();
		new Thread(new Runnable() {
		    public void run() {
		    	Cliente.main(new String[]{"localhost","9000","Luis"});
		    }
		}).start();
		new Thread(new Runnable() {
		    public void run() {
		    	Cliente.main(new String[]{"localhost","9000","Juan"});
		    }
		}).start();
		new Thread(new Runnable() {
		    public void run() {
		    	Cliente.main(new String[]{"localhost","9000","Rodrigo"});
		    }
		}).start();
		new Thread(new Runnable() {
		    public void run() {
		    	Cliente.main(new String[]{"localhost","9000","Maria"});
		    }
		}).start();
		new Thread(new Runnable() {
		    public void run() {
		    	Cliente.main(new String[]{"localhost","9000","Tete"});
		    }
		}).start();
		new Thread(new Runnable() {
		    public void run() {
		    	Cliente.main(new String[]{"localhost","9000","Roro"});
		    }
		}).start();
		new Thread(new Runnable() {
		    public void run() {
		    	Cliente.main(new String[]{"localhost","9000","Mili"});
		    }
		}).start();
		new Thread(new Runnable() {
		    public void run() {
		    	Cliente.main(new String[]{"localhost","9000","Rerere"});
		    }
		}).start();
		new Thread(new Runnable() {
		    public void run() {
		    	Cliente.main(new String[]{"localhost","9000","Menganito"});
		    }
		}).start();
		new Thread(new Runnable() {
		    public void run() {
		    	Cliente.main(new String[]{"localhost","9000","Fulanito"});
		    }
		}).start();
		new Thread(new Runnable() {
		    public void run() {
		    	Cliente.main(new String[]{"localhost","9000","Tenganito"});
		    }
		}).start();
		new Thread(new Runnable() {
		    public void run() {
		    	Cliente.main(new String[]{"localhost","9000","Rolanguito"});
		    }
		}).start();
	}
	
	
	
	public static void main(String[] args) {
		new MultiplesClientes();
	}

}
