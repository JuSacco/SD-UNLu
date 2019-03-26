package jusacco.TP1.punto7;

import java.io.Serializable;

public class NumeroPrimo implements ITarea,Serializable {
	
	private static final long serialVersionUID = 5757474154079590585L;
	int numero;
	
	@Override
	public Object ejecutar() {
		int contador = 2;
		  boolean primo=true;
		  while ((primo) && (contador!=this.numero)){
		    if (this.numero % contador == 0)
		      primo = false;
		    contador++;
		  }
		  return primo;
	}

}
