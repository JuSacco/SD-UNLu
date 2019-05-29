package jusacco.TPFinal.Worker.Tools;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
  
public class ClientFTP {
	final String USER = "worker";
	final String PWD = "workerpwd";
	String ip;
	int port;
	FTPClient client;
	boolean logged = false;
	public ClientFTP(String ip, int port) throws java.net.ConnectException {
		try {
			this.client = new FTPClient();
			this.client.connect(ip,port);
			this.logged = client.login(USER, PWD);
			if (this.logged) {
				System.out.println("Conexion FTP Establecida.");
			} else {
			    System.out.println("Conexion FTP Fallida, revise si el usr/pwd es correcto: "+USER+"/"+PWD);
			}
		} catch (IOException e) {
			e.printStackTrace();
			try {
				this.client = new FTPClient();
				this.logged = client.login(USER, PWD);
				if (this.logged) {
					System.out.println("Conexion FTP Establecida.");
				} else {
				    System.out.println("Conexion FTP Fallida, revise si el usr/pwd es correcto: "+USER+"/"+PWD);
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}
	     
	public FTPClient getClient() {
		return this.client;
	}
	
	public ArrayList<String> showFiles() {
		ArrayList<String> result = new ArrayList<String>();
		if(this.logged) {
			try {
				FTPFile[] files = null;
				files = this.client.listFiles();
				for (FTPFile f : files) {
					result.add(f.getName());
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	public void closeConn() {
		if(this.logged) {
			try {
				boolean logout = this.client.logout();
				if(logout) {
					this.logged = false;
					this.client.disconnect();
					System.out.println("Cerrando conexion FTP..");
				}else {
					System.err.println("Error al cerrar la conexion!");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	/*
	 *-----------------------------------------------
	 *|					FTP UTILS					|
	 *-----------------------------------------------
	 */
	public static boolean downloadSingleFile(FTPClient ftpClient,
	        String remoteFilePath, String savePath) throws IOException {
	    File downloadFile = new File(savePath);
	     
	    File parentDir = downloadFile.getParentFile();
	    if (!parentDir.exists()) {
	        parentDir.mkdir();
	    }
	         
	    OutputStream outputStream = new BufferedOutputStream(
	            new FileOutputStream(downloadFile));
	    try {
	        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
	        return ftpClient.retrieveFile(remoteFilePath, outputStream);
	    } catch (IOException ex) {
	        throw ex;
	    } finally {
	        if (outputStream != null) {
	            outputStream.close();
	        }
	    }
	}
	public static void downloadDirectory(FTPClient ftpClient, String parentDir,
	        String currentDir, String saveDir) throws IOException {
	    String dirToList = parentDir;
	    if (!currentDir.equals("")) {
	        dirToList += "/" + currentDir;
	    }
	 
	    FTPFile[] subFiles = ftpClient.listFiles(dirToList);
	 
	    if (subFiles != null && subFiles.length > 0) {
	        for (FTPFile aFile : subFiles) {
	            String currentFileName = aFile.getName();
	            if (currentFileName.equals(".") || currentFileName.equals("..")) {
	                // skip parent directory and the directory itself
	                continue;
	            }
	            String filePath = parentDir + "/" + currentDir + "/"
	                    + currentFileName;
	            if (currentDir.equals("")) {
	                filePath = parentDir + "/" + currentFileName;
	            }
	 
	            String newDirPath = saveDir + parentDir + File.separator
	                    + currentDir + File.separator + currentFileName;
	            if (currentDir.equals("")) {
	                newDirPath = saveDir + parentDir + File.separator
	                          + currentFileName;
	            }
	 
	            if (aFile.isDirectory()) {
	                // create the directory in saveDir
	                File newDir = new File(newDirPath);
	                boolean created = newDir.mkdirs();
	                if (created) {
	                    System.out.println("CREATED the directory: " + newDirPath);
	                } else {
	                    System.out.println("COULD NOT create the directory: " + newDirPath);
	                }
	 
	                // download the sub directory
	                downloadDirectory(ftpClient, dirToList, currentFileName,
	                        saveDir);
	            } else {
	                // download the file
	                boolean success = downloadSingleFile(ftpClient, filePath,
	                        newDirPath);
	                if (success) {
	                    System.out.println("DOWNLOADED the file: " + filePath);
	                } else {
	                    System.out.println("COULD NOT download the file: "
	                            + filePath);
	                }
	            }
	        }
	    }
	}
}
