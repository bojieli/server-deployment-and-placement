package appro;

public class Edgemcmf
{
	public int from, to, original;
	public double cap, cost, flow;
	
	public Edgemcmf(int u, int v, double c, double f, double w, int o) {
		from = u;
		to = v;
		cap = c;
		flow = f;
		cost = w;
		original = o;
	}
}