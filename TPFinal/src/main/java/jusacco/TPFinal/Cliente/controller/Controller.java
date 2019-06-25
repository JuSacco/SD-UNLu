package jusacco.TPFinal.Cliente.controller;


import java.io.File;

import jusacco.TPFinal.Cliente.Cliente;

public class Controller{
	Cliente cliente;
	
	public Controller(Cliente modelo) {
		this.cliente = modelo;
	}
	
	void connectRMI(String ip, int port) {
		this.cliente.connectRMI(ip, port, 0);
	}
	public void setFile(File f) {
		this.cliente.setFile(f);
	}
	public void setTipoRender(int tipo) {
		this.cliente.setTipoRender(tipo);
	}
	public void setHighRender(boolean b) {
		this.cliente.setHighRender(b);
	}
	public String enviarFile(int value, int noFrame) {
		return this.cliente.enviarFile(value, noFrame);
	}
	public boolean isReady() {
		return this.cliente.isReady();
	}
}
