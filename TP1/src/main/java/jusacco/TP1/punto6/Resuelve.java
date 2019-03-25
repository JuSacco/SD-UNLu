package jusacco.TP1.punto6;

import java.rmi.RemoteException;

public class Resuelve implements IRemota {
	
	public Resuelve () {
		
	}

	public int[] sumarVectores(int[] v1, int[]v2) throws RemoteException{
		int[] res = new int[3];
		for (int i = 0;i<3;i++){
			res[i] = v1[i]+v2[i];
		}
		return res;
	}
	public int[] restarVectores(int[] v1, int[]v2) throws RemoteException{
		int[] res = new int[3];
		for (int i = 0;i<3;i++){
			res[i] = v1[i]-v2[i];
		}
		return res;
	}


	public int[] sumarMal(int[] v1, int[]v2) throws RemoteException{
		int[] res = new int[3];
		v1[0] = 0;
		v1[1] = 0;
		v1[2] = 0;
		for (int i = 0;i<3;i++){
			res[i] = v1[i]+v2[i];
		}
		return res;
	}
	public int[] restarMal(int[] v1, int[]v2) throws RemoteException{
		int[] res = new int[3];
		v1[0] = 0;
		v1[1] = 0;
		v1[2] = 0;
		for (int i = 0;i<3;i++){
			res[i] = v1[i]-v2[i];
		}
		return res;
	}
	
}