package jusacco.TPFinal.Cliente;

import jusacco.TPFinal.Cliente.controller.Controller;
import jusacco.TPFinal.Cliente.view.*;
import jusacco.TPFinal.Cliente.*;

public class RunCliente {
	
	public static void main(String[] args) {
		Cliente modelo = new Cliente();
		Controller controlador = new Controller(modelo);
		GUICliente iniciar = new GUICliente(controlador);
	}
}
