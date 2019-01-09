package appro;

public class EDGE {
	
	public int v, next;   //----------------------cap,cost: int?
	public double cap,cost;
	public int original;
	public double reserve;
	public EDGE(){}
	
	public EDGE(int a, double b, double c,int d,int e){
		v = a;
		cap = b;
		cost = c;
		next = d;
		original = e;
		reserve = cap;
	}

}
