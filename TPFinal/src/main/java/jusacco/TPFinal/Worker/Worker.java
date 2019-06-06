package jusacco.TPFinal.Worker;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.GetResponse;
import com.rabbitmq.client.MessageProperties;

import jusacco.TPFinal.Servidor.IFTPManager;
import jusacco.TPFinal.Servidor.IWorkerAction;
import jusacco.TPFinal.Servidor.Mensaje;
import jusacco.TPFinal.Worker.Tools.ClientFTP;
import jusacco.TPFinal.Worker.Tools.DirectoryTools;


/*TODO
 * 	
 * 	
 *  #Por linea de comandos realizar un renderizado y subirlo a la cola
 * 
 */
public class Worker {
	//Queue
	private String queueIp;
	private int queuePort;
	private String queueUsr;
	private String queuePwd;
	private ConnectionFactory queueConnectionFactory;
	private Connection queueConnection;
	private Channel queueChannel;
	private String queueTrabajo = "queueTrabajo";
	private String queueTerminados = "queueTerminados";
	//General
	Logger log = LoggerFactory.getLogger(Worker.class);
	String myDirectory = System.getProperty("user.dir")+"/WorkerData/";
	String myBlendDirectory;
	String myBlenderApp;
	String myRenderedImages;
	String localIp;
	IWorkerAction stubServer;
	//ftp
	IFTPManager stubFtp;
	ClientFTP cliFtp;
	int serverFTPPort;
	//server
	String serverIp;
	int serverPort;
	//Rendering CONST
	final int MIN_FRAME = 1;
	final int MAX_FRAME = 250;
	private ArrayList<String> realizedWorks = new ArrayList<String>();
	
	
	public Worker() {
		log.info("<-- [STEP 1] - LEYENDO ARCHIVO DE CONFIGURACION -->");
		readConfigFile();
		log.info("<-- [STEP 2] - PREPARANDO EL BANCO DE TRABAJO   -->");
		prepareWorkplace();
		log.info("<-- [STEP 3] - REALIZANDO CONEXION RMI		  -->");
		getRMI();
		log.info("<-- [STEP 4] - REALIZANDO CONEXION CON RABBITMQ -->");
		getQueueConn();
		log.info("<-- [STEP 5] - REVISANDO ARCHIVOS NECESARIOS    -->");
		if(checkNeededFiles()) {
			log.info("<-- [STEP 6] - ESPERANDO TRABAJOS		      -->");
			getWork();
			log.info("<-- [STEP 7] - QUEDAR OCIOSO			      -->");
		}else {
			log.debug("Error inesperado!");
		}
	}
	
	private void prepareWorkplace() {
		log.info("Borrando los archivos ya existentes en las carpetas temporales.");
		borrarTemporales();
		if(!(new File(this.myDirectory+"defaultRenderOptions.py").exists())) {
			log.info("Creando Script Python...");
			try {
				// File template
			    File file = new File(this.myDirectory+"scriptTemplate.txt"); 
			    // File script.py
		    	PrintWriter script;
				script = new PrintWriter(new FileWriter(this.myDirectory+"defaultRenderOptions.py"));
			    Scanner sc = new Scanner(file);
			    while (sc.hasNextLine()) {
			    	String line = sc.nextLine();
			    	if(line.contains("$myPath")) {
			    		script.println("bpy.data.scenes[\"Scene\"].render.filepath = '"+this.myRenderedImages.replace("\\", "\\\\")+"'");
			    	}else {
				    	script.println(line);
			    	}
			    }
			    script.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void borrarTemporales() {
		for(String toErase : getFiles(myBlendDirectory)) {
			File f = new File(myBlendDirectory+"/"+toErase);
			 if(f.delete()){
				 System.out.println(myBlendDirectory+toErase+" -> Eliminado.");
		     }else {
		    	 System.out.println(myBlendDirectory+toErase+" -> No existe, o no se puede eliminar.");
		     }
		}	
		for(String toErase : getFiles(myRenderedImages)) {
			File f = new File(myRenderedImages+"/"+toErase);
			 if(f.delete()){
				 System.out.println(myRenderedImages+toErase+" -> Eliminado.");
		     }else {
		    	 System.out.println(myRenderedImages+toErase+" -> No existe, o no se puede eliminar.");
		     }
		}
	}

	private void getQueueConn() {
		//QUEUE RELATED
		try {
			// [STEP 0] - FACTORIA DE CONEXION
			this.queueConnectionFactory = new ConnectionFactory();
			this.queueConnectionFactory.setHost(this.queueIp);
			this.queueConnectionFactory.setPort(this.queuePort);
			this.queueConnectionFactory.setUsername(this.queueUsr);
			this.queueConnectionFactory.setPassword(this.queuePwd);
			// [STEP 1] - QueueConnection
			this.queueConnection = this.queueConnectionFactory.newConnection();
			// [STEP 2] - ChannelConnection
			this.queueChannel = this.queueConnection.createChannel();
			log.info("RabbitMQ: Conexion creada con exito.");
		} catch (IOException e) {
			log.error("Error: Compruebe el Usuario/Contraseña/direccion IP de RabbitMQ");
		} catch (TimeoutException e) {
			log.error("Error: RabbitMQ Connection Time out.");
		}
	}
/* OLD
	private void getWork() {
		boolean salir = false;
		try {
			while(!salir) {
				if(this.queueChannel.messageCount(this.queueTrabajo) > 0) {
					GetResponse gr = this.queueChannel.basicGet(this.queueTrabajo, false);
					Mensaje msg = Mensaje.getMensaje(gr.getBody());
					this.queueChannel.basicNack(gr.getEnvelope().getDeliveryTag(), false, true);
					persistBlendFile(msg.getBlend(), msg.getName());
					if(msg.getCantidadSamples() > 0)
						startRenderSamples(msg.getName(), msg.getCantidadSamples(),msg.getFrameToRender());
					else
						startRenderTime(msg.getName(), msg.getTiempoLimite(),msg.getFrameToRender());
					salir = true;
					log.debug("Salio linea 174");
				}
			}
		} catch (Exception e) {
			//e.printStackTrace();
			try {
				Thread.sleep(1000);
				getWork();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			
		}
	}
*/

	private void getWork() {
		boolean salir;
		while(true) {
			try {
				salir = false;
				String trabajo = this.stubServer.giveWorkToDo(localIp, this.realizedWorks);//Deberia quedar colgado aca hasta tener un nuevo trabajo
				while(!salir) {
					if(this.queueChannel.messageCount(this.queueTrabajo) > 0) {
						GetResponse gr = this.queueChannel.basicGet(this.queueTrabajo, false);
						Mensaje msg = Mensaje.getMensaje(gr.getBody());
						this.queueChannel.basicNack(gr.getEnvelope().getDeliveryTag(), false, true);
						if(trabajo.split(":")[0].contentEquals(msg.getName())) {
							persistBlendFile(msg.getBlend(), msg.getName());
							if(msg.getCantidadSamples() > 0)
								startRenderSamples(msg.getName(), msg.getCantidadSamples(),msg.getFrameToRender());
							else
								startRenderTime(msg.getName(), msg.getTiempoLimite(),msg.getFrameToRender());
							this.realizedWorks.add(trabajo);
							Thread.sleep(30000);
							borrarTemporales();
							salir = true;
						}
					}
				}
			} catch (Exception e) {
				//e.printStackTrace();
				try {
					Thread.sleep(1000);
					getWork();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				
			}
		}
	}
	
	private void persistBlendFile(byte[] byteBlend, String name) {
		File folder = new File(this.myBlendDirectory);
		if(folder.exists() && folder.isDirectory()) {
				File blend = new File(folder.getAbsolutePath()+"/"+name);
				try (FileOutputStream fos = new FileOutputStream(blend)) {
					   fos.write(byteBlend);
				}catch (Exception e) {
					log.error("ERROR: "+e.getMessage());
				}
		}else {
			folder.mkdir();
			File blend = new File(folder.getAbsolutePath()+"/"+name);
			try (FileOutputStream fos = new FileOutputStream(blend)) {
				   fos.write(byteBlend);
			}catch (Exception e) {
				log.error("ERROR: "+e.getMessage());
			}
		}
		try {
			// TODO Buscar otra forma de asegurarse que el archivo esta completamente listo para ser modificado.
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	private void startRenderSamples(String name, int cantidadSamples, int nroFrame) {
		// blender -b file_name.blend -x 1 -o //file -F AVI_JPEG -s 001 -e 250 -S scene_name -a
		int i = 1;
		String blendToRender = this.myBlendDirectory+"/"+getFiles(this.myBlendDirectory).get(0);
		String frames = "-f "+nroFrame;
		//String fromTo = "-s "+this.MIN_FRAME+" -e "+maxSample+" -a";
		String useExtension = "-x 1";
		String pyScript = "-P "+this.myDirectory+"defaultRenderOptions.py";
		String pyRandScript = "-P "+this.myDirectory+"/randomSeed.py";
		
		File finishedWorkFolder = new File(this.myRenderedImages);
		if(finishedWorkFolder.exists() && finishedWorkFolder.isDirectory()) {
			//First configure default settings to .blend 
			log.info("Pre-configurando el archivo .blend");
			String cmd = " -b \""+blendToRender+"\" "+pyScript;
			File f = new File (this.myBlenderApp+cmd);//Normalize backslashs and slashs
			

			String cmdRnd = " -b \""+blendToRender+"\" "+pyRandScript;
			File fRnd = new File (this.myBlenderApp+cmdRnd);//Normalize backslashs and slashs
			
			System.out.println("CMD: "+ f.getPath());
			ejecutar(f.getPath());
			//Start render
			while(i<=cantidadSamples) {
				//Calculo una semilla random para tener diferente nivel de ruido.
				log.info("Calculando Seed Random");
				ejecutar(fRnd.getPath());
				//---------------------
				String output = "-o \""+this.myRenderedImages+"/"+i+" from frame \"";
				cmd = " -b \""+blendToRender+"\" "+output+" "+useExtension+" "+frames;
				i++;
				f = new File (this.myBlenderApp+cmd);//Normalize backslashs and slashs
				System.out.println("CMD: "+ f.getPath());
				ejecutar(f.getPath());
				ArrayList<String> imgTerminadas = getFiles(finishedWorkFolder.getPath());
				try {
					File imgRendered = new File(finishedWorkFolder.getPath()+"/"+imgTerminadas.get(imgTerminadas.size()-1));
					BufferedImage image = ImageIO.read(imgRendered);
					this.queueChannel.basicPublish("", this.queueTerminados, MessageProperties.PERSISTENT_TEXT_PLAIN, new Mensaje(image,name,this.localIp).getBytes());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			log.info("==========Termine=========");
		}else {
			finishedWorkFolder.mkdir();
			startRenderSamples(name,cantidadSamples,nroFrame);
		}
	}

	private void startRenderTime(String name, int timeLimit, int frameToRender) {
		// blender -b file_name.blend -x 1 -o //file -F AVI_JPEG -s 001 -e 250 -S scene_name -a
		
		String blendToRender = this.myBlendDirectory+"/"+getFiles(this.myBlendDirectory).get(0);
		String frames = "-f "+frameToRender;
		String useExtension = "-x 1";
		String pyScript = "-P "+this.myDirectory+"/defaultRenderOptions.py";
		String pyRandScript = "-P "+this.myDirectory+"/randomSeed.py";
		int i = 1;
		File finishedWorkFolder = new File(this.myRenderedImages);
		if(finishedWorkFolder.exists() && finishedWorkFolder.isDirectory()) {
			//First configure default settings to .blend 
			log.info("Pre-configurando el archivo .blend");
			String cmd = " -b \""+blendToRender+"\" "+pyScript;
			File f = new File (this.myBlenderApp+cmd);//Normalize backslashs and slashs
			
			String cmdRnd = " -b \""+blendToRender+"\" "+pyRandScript;
			File fRnd = new File (this.myBlenderApp+cmdRnd);//Normalize backslashs and slashs
			
			System.out.println("CMD: "+ f.getPath());
			ejecutar(f.getPath());
			log.info("Iniciando el renderizado...");
			LocalTime init = LocalTime.now();
			long estimatedRenderTime = 0;
			boolean termino = false;
			while(!termino) {
				//Calculo una semilla random para tener diferente nivel de ruido.
				log.info("Calculando Seed Random");
				ejecutar(fRnd.getPath());
				//------------------
				String output = "-o \""+this.myRenderedImages+"/"+i+" from frame \"";
				cmd = " -b \""+blendToRender+"\" "+output+" "+useExtension+" "+frames;
				i++;
				f = new File (this.myBlenderApp+cmd);//Normalize backslashs and slashs
				LocalTime now = LocalTime.now();
				ejecutar(f.getPath());
				if(estimatedRenderTime == 0) {
					estimatedRenderTime = Duration.between(now,LocalTime.now()).getSeconds();
					log.info("Tiempo estimado de renderizado: "+estimatedRenderTime+" segundos.");
				}
				ArrayList<String> imgTerminadas = getFiles(finishedWorkFolder.getPath());
				try {
					File imgRendered = new File(finishedWorkFolder.getPath()+"/"+imgTerminadas.get(imgTerminadas.size()-1));
					BufferedImage image = ImageIO.read(imgRendered);
					this.queueChannel.basicPublish("", this.queueTerminados, MessageProperties.PERSISTENT_TEXT_PLAIN, new Mensaje(image,name,this.localIp).getBytes());
				} catch (Exception e) {
					e.printStackTrace();
				}
				if(Duration.between(init, LocalTime.now()).getSeconds()+estimatedRenderTime > timeLimit) {
					termino = true;
				}else {
					log.info("Falta :"+(timeLimit - Duration.between(init, LocalTime.now()).getSeconds())+" segundos para terminar.");
				}
			}
		}else {
			finishedWorkFolder.mkdir();
			startRenderSamples(name, timeLimit,frameToRender);
		}
		
	}
	
    public ArrayList<String> getFiles( String path ) {
        File f = new File( path );
        if ( f.isDirectory()) {
            ArrayList<String> res   = new ArrayList<String>();
            File[] arr_content = f.listFiles();
            int size = arr_content.length;
            for ( int i = 0; i < size; i ++ ) {
                if ( arr_content[ i ].isFile( ))
                res.add( arr_content[ i ].getName());
            }
            return res;
        } else {
        	f.mkdir();  
            return null;      	
        }
    }
	
	private void ejecutar(String cmd) {
		
		try {
			Process p = Runtime.getRuntime().exec(cmd);
			BufferedReader input = new BufferedReader (new InputStreamReader (p.getInputStream()));
			String line = "";
			while (true) {
				line = input.readLine();
				if (line == null) break;
				System.out.println("Line: "+line);
			}
			p.waitFor();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
		
		
	
	
	
	private ClientFTP connectFTP() {
		try {
			log.info("Iniciando servidor FTP.");
			this.serverFTPPort = this.stubFtp.getFTPPort();
			ClientFTP cliFtp = null;
			if(this.stubFtp.startFTPServer() > 0) {
				log.info("El servidor FTP fue iniciado correctamente");
				cliFtp = new ClientFTP(serverIp, serverFTPPort);
			}else{
				boolean recuperado = this.stubFtp.resumeFTPServer();
				log.error("El servidor FTP ya estaba iniciado. Intentando establecer comunicacion: "+recuperado);
				if(recuperado) {
					cliFtp = new ClientFTP(serverIp, serverFTPPort);
				}
			}
			return cliFtp;
		} catch (Exception e) {
			log.error("Hubo un error inesperado al intentar conectarse al servidor FTP.");
			log.error("Error:"+e.getMessage());
			return null;
		}
	}

	private void getRMI() {
		try {
			Registry clienteRMI = LocateRegistry.getRegistry(this.serverIp,serverPort);
			log.info("Obteniendo servicios RMI.");
			log.info("Obteniendo stub...");
			this.stubFtp = (IFTPManager) clienteRMI.lookup("Acciones"); 
			this.stubServer = (IWorkerAction) clienteRMI.lookup("server");
			//DEBUG
			log.debug(this.localIp);
			this.stubServer.helloServer(this.localIp);
		} catch (RemoteException | NotBoundException e) {
			log.error("RMI Error: "+e.getMessage());
			log.info("Re-intentando conectar a "+this.serverIp+":"+this.serverPort);
			for (int i = 5; i > 0; i--) {
				try {
					Thread.sleep(1000);
					log.info("Re-intentando en..."+i);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
			getRMI();
		}
	}

	/*
	 * Verifica si el worker tiene las carpetas necesarias para trabajar,
	 * en caso de no tenerlas las descarga por ftp.
	 */
	private boolean checkNeededFiles() {
		File fRoot = new File(myDirectory);
		if (fRoot.isDirectory()) {
			log.info(fRoot.getAbsolutePath()+" - [Es un directorio]");
       		File fApp = new File(myDirectory+"\\Blender-app\\");
       		if (fApp.isDirectory()) {
       			log.info(fApp.getAbsolutePath()+" - [Es un directorio]");
       			try {
       			    long size = DirectoryTools.getFolderSize(fApp);
       				log.info("Obteniendo tamanio de: "+fApp.getAbsolutePath()+" MB:"+(size/1024));
       				if(size < 30000000) {
       					downloadBlenderApp(fApp.getAbsolutePath());       					
       				}else {
       					log.info("Tengo el blender en condiciones de funcionar");
       					return true;
       				}
       			}catch (Exception e) {
       				log.info("Error: "+fApp.getAbsolutePath()+" No es un directorio.");
       				if(fApp.mkdir()) {
           				downloadBlenderApp(fApp.getAbsolutePath());
           				return true;
       				}else {
       	    			log.error("Hubo un error inesperado al intentar crear la carpeta: "+fApp.getAbsolutePath());
       					return false;
       				}
       			}
       		}else{
   				log.info("Error: "+fApp.getAbsolutePath()+" No es un directorio.");
            	if(fApp.mkdir()) {
       				downloadBlenderApp(fApp.getAbsolutePath());
       				return true;
       			}else {
        			log.error("Hubo un error inesperado al intentar crear la carpeta: "+fApp.getAbsolutePath());
       				return false;
       			}
       		}
        }else{
			log.info("Error: "+fRoot.getAbsolutePath()+" No es un directorio.");
        	if(fRoot.mkdir()) {
           		File fApp = new File(myDirectory+"/Blender-app/");
           		if(fApp.mkdir()) {
       				downloadBlenderApp(fApp.getAbsolutePath());
       				return true;
   				}else {
   	    			log.error("Hubo un error inesperado al intentar crear la carpeta: "+fApp.getAbsolutePath());
   					return false;
   				}
        	}else{
    			log.error("Hubo un error inesperado al intentar crear la carpeta: "+fRoot.getAbsolutePath());
        		return false;
        	}
        }
		return false;
	}
	

	@SuppressWarnings("static-access")
	private void downloadBlenderApp(String myAppDir) {
		log.info("La carpeta BlenderApp esta corrupta o no existe. Descargandola desde el servidor FTP");
		this.cliFtp = connectFTP();
		try {
			if(this.cliFtp != null) {
				log.info("Intentando descargar...Porfavor espere, este proceso podria tardar varios minutos...");
				if(System.getProperty("os.name").toLowerCase().contains("windows")) {
					String so = "windows";
					this.cliFtp.downloadDirectory(this.cliFtp.getClient(), "/blender-"+so+"/", "",myAppDir);
				}else {
					String so = "linux";
					this.cliFtp.downloadDirectory(this.cliFtp.getClient(), "/blender-"+so+"/", "",myAppDir);
				}
				this.cliFtp.closeConn();
				this.stubFtp.stopFTPServer();
			}else {
				log.error("Hubo un problema con el servidor FTP");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("rawtypes")
	private void readConfigFile() {
		Gson gson = new Gson();
		Map config;
		try {
			this.localIp = Inet4Address.getLocalHost().getHostAddress();
			
			config = gson.fromJson(new FileReader(this.myDirectory+"workerConfig.json"), Map.class);
			Map server = (Map) config.get("server");
			this.serverIp = server.get("ip").toString();
			this.serverPort = Integer.valueOf(server.get("port").toString());
			this.serverFTPPort = Integer.valueOf(server.get("ftp").toString());
			
			Map queue = (Map) config.get("queue");
			this.queueIp = queue.get("ip").toString();
			this.queuePort = Integer.valueOf(queue.get("port").toString());
			this.queueUsr = queue.get("user").toString();
			this.queuePwd = queue.get("pass").toString();
			
			Map paths = (Map) config.get("paths");
			this.myBlendDirectory = this.myDirectory + paths.get("myBlendDir").toString();
			this.myBlenderApp = this.myDirectory + paths.get("myBlenderApp");
			this.myRenderedImages = this.myDirectory + paths.get("myFinishedWorks");
			
		} catch (IOException e) {
			log.info("Error Archivo Config!");
		} 
	}
	
	public static void main(String[] args) {
		new Worker();
	}
}
