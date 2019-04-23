package jusacco.TP2.punto1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Peer {
	private final String CONF_PATH = "./config.txt";
	private final Logger log = LoggerFactory.getLogger(Config.class);
	String peerIp;
	int peerPort;
	
	PeerClient peerClient;
	PeerServer peerServer;
	
	Socket connMaestro;
	String masterIp;
	int masterPort;
	
	ArrayList<Archivo> liArchivos;
	
	
	public Peer (String ip,int port,String directory) {
		//STEP 0: Configuro mis parametros
		this.peerIp = ip;
		this.peerPort = port;
		loadConfig();
		try {
			//STEP 1: Cargo mis archivos disponibles
			this.liArchivos = getArchivos(directory);
			//STEP 2: Creo una nueva conexion socket 
			this.connMaestro = new Socket(masterIp,masterPort);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		//STEP 3: Creo mis Threads cliente y servidor
		this.peerServer = new PeerServer(this.peerIp,this.peerPort, this.liArchivos, directory);
		this.peerClient = new PeerClient(this.connMaestro,this.peerPort, this.liArchivos, directory);
		Thread tSv = new Thread(this.peerServer);
		Thread tCli = new Thread(this.peerClient);
		//STEP 4: Lanzo los Threads cliente y servidor
		tSv.start();
		tCli.start();
		try {
			tCli.join();
			tSv.start();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private void loadConfig() {
		File tempFile = new File(this.CONF_PATH);
		String[] primaryMaster;
		if (tempFile.exists()) {
			try (BufferedReader br = new BufferedReader(new FileReader(this.CONF_PATH))) {
				String s;
				while ((s = br.readLine()) != null) {
					primaryMaster = s.split(":");
					this.masterIp = primaryMaster[0];
					this.masterPort = Integer.valueOf(primaryMaster[1]);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}else {
			log.info("El archivo de inicializacion no existe.");
		}		
	}
	
	private ArrayList<Archivo> getArchivos(String directory) throws FileNotFoundException {
		String[] res = getFiles(directory);
		ArrayList<Archivo> liArchivo = new ArrayList<Archivo>();
		if ( res != null ) {
            int size = res.length;
            for ( int i = 0; i < size; i ++ ) {
                System.out.println( res[ i ] );
                if (res[i] != null) {
                	Archivo a = new Archivo(res[ i ]);
                	liArchivo.add(a);
                }
            }
        }
		return liArchivo;
	}

    public String[] getFiles( String dir_path ) {
        String[] arr_res = null;
        File f = new File( dir_path );
        this.log.info(f.getAbsolutePath());
        if ( f.isDirectory( )) {
            List<String> res   = new ArrayList<>();
            File[] arr_content = f.listFiles();
            int size = arr_content.length;
            for ( int i = 0; i < size; i ++ ) {
                if ( arr_content[ i ].isFile( ))
                res.add( arr_content[ i ].getName());
            }
            arr_res = res.toArray( new String[ 0 ] );
        } else
           log.error("Path Invalido");
        return arr_res;
    }


	public static void main( int port,String folder) {
		new Peer("localhost",port,folder);
	}

}
