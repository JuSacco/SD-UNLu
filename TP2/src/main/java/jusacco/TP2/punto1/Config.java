package jusacco.TP2.punto1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Config {
	String archivo;
	private final Logger log = LoggerFactory.getLogger(Config.class);
	
	public Config (String path) {
		this.archivo = path;
	}
	
	
	public ArrayList<String[]> doConfig(String masterIp,int masterPort){
		log.info("Configurando al master "+masterIp+":"+masterPort+ archivo);
		ArrayList<String[]> result = new ArrayList<String[]>();
		File tempFile = new File(this.archivo);
		FileWriter fw;
		String sNodo = masterIp+":"+masterPort;
		if (tempFile.exists()) {
			try (BufferedReader br = new BufferedReader(new FileReader(this.archivo))) {
				String s;
				boolean esta = false;
				while ((s = br.readLine()) != null) {
					if(s.contentEquals(sNodo)) {
						log.info("El nodo Maestro ya esta registrado en las configuraciones.");
						esta = true;
					}
					if(!s.contentEquals(sNodo) && !s.contentEquals("")) {
						log.info("Encontre otro nodo maestro "+ s);
						result.add(s.split(":"));
					}
				}
				if (!esta) {
					log.info("Registrando el nodo en configuraciones.");
					fw = new FileWriter(tempFile, true);
					fw.write(sNodo+"\r\n");
					fw.close();
				}
				return result;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}else {
			try {
				log.info("El archivo de mensajes no existe...\nCreandolo en "+ tempFile.getCanonicalPath());
				tempFile.createNewFile();
				log.info("Registrando el nodo en configuraciones.");
				fw = new FileWriter(tempFile, true);
				fw.write(sNodo+"\r\n");
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
}
