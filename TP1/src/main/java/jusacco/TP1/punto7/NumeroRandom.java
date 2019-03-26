package jusacco.TP1.punto7;

import java.io.Serializable;
import java.util.Random;

public class NumeroRandom implements ITarea,Serializable {
	private static final long serialVersionUID = 7349437558762397983L;
	Random rnd;
	@Override
	public Object ejecutar() {
		return new Random().nextInt(100);
	}
}
