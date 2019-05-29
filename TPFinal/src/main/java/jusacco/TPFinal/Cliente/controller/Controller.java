package jusacco.TPFinal.Cliente.controller;


import java.io.File;

import jusacco.TPFinal.Cliente.Cliente;

public class Controller{
	Cliente cliente;
	
	public Controller(Cliente modelo) {
		this.cliente = modelo;
	}
	
	void connectRMI(String ip, int port) {
		this.cliente.connectRMI(ip, port);
	}
	public void setFile(File f) {
		this.cliente.setFile(f);
	}
	public void setTipoRender(int tipo) {
		this.cliente.setTipoRender(tipo);
	}
	public String enviarFile(int i) {
		return this.cliente.enviarFile(i);
	}
	public boolean isReady() {
		return this.cliente.isReady();
	}
}
