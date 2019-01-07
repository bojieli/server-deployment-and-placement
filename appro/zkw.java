package appro;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Queue;

public class zkw {
	
	public static final int maxn = 70000;   //the max node 
	public static final int maxm = 19000000;
	public static final double inf = Double.MAX_VALUE;
	
	int size, n;
	int st, en;
	double maxflow, mincost;
	boolean[] vis;
	int[] net, pre, cur;
	double[] dis;
	ArrayList<EDGE> E;
	boolean flag;
	
	Queue<Integer> Q;
	
	public void init(int _n){
		n = _n;
		size = 0;
		vis = new boolean[n];
		net = new int[n];
		pre = new int[n];
		cur = new int[n];
		dis = new double[n];
		E = new ArrayList<EDGE>();
		Q = new ArrayDeque<Integer>();
	}
	
	public void add(int u, int v, double cap, double cost){
		E.add(new EDGE(v, cap, cost, net[u]));
		net[u] = size++;
		E.add(new EDGE(u, 0, -cost, net[v]));
		net[v] = size++;	
	}
	
	public boolean adjust(){
		int v;
		double min = inf;
		for(int i = 0; i <= n; i++){
			if(!vis[i])
				continue;
			for(int j = net[i]; j != -1 ; j = E.get(j).next){
				v = E.get(j).v;
				if(E.get(j).cap != 0)
					flag = true;
				else
					flag = false;
				if(flag)
					if(!vis[v] && dis[v]-dis[i]+E.get(j).cost < min)
						min = dis[v] - dis[i] + E.get(j).cost;
			}
		}
		if(min == inf)
			return false;
		for(int i = 0 ; i <= n; i++)
			if(vis[i]){
				cur[i] = net[i];
				vis[i] = false;
				dis[i] = min+dis[i];
			}
		return true;
	}
	
	public double augment(int i, double flow){
		if(i == en){
			mincost += dis[st] * flow;
			maxflow += flow;
			return flow;
		}
		vis[i] = true;
		for(int j = cur[i],v ; j != -1 ;j = E.get(j).next){
			v = E.get(j).v;
			if(E.get(j).cap == 0)
				continue;
			if(vis[v] || dis[v]+E.get(j).cost != dis[i])
				continue;
			double delta = augment(v, Math.min(flow, E.get(j).cap));
			if(delta != 0){
				E.get(j).cap -= delta;
				E.get(j^1).cap += delta;
				cur[i] = j;
				return delta;
			}
		}
		return 0;
	}
	
	public void spfa(){
		int u,v;
		for(int i = 0; i <=n ; i++){
			vis[i] = false;
			dis[i] = inf;
		}
		dis[st] = 0;
		Q.add(st);
		vis[st] = true;
		while(!Q.isEmpty()){
			u = Q.poll();
			vis[u] = false;
			for(int i = net[u]; i != -1; i = E.get(i).next){
				v = E.get(i).v;
				if(E.get(i).cap != 0||dis[v] <= dis[u]+E.get(i).cost)
					continue;
					
				dis[v] = dis[u]+E.get(i).cost;
				if(!vis[v]){
					vis[v] = true;
					Q.add(v);
				}
			}
		}
		for(int i = 0 ; i <= n ;i++)
			dis[i] = dis[en]-dis[i];
	}
	
	public double run(int s, int t, int need){
		st = s;
		en = t;
		spfa();
		mincost = maxflow = 0;
		for(int i = 0 ; i <= n; i++){
			vis[i] = false;
			cur[i] = net[i];
		}
		do{
			while(augment(st,inf) != 0){
				for(int i = 0 ; i <vis.length; i++)
					vis[i] = false;	
			}		
		}while(adjust());
		if(maxflow < need)
			return -1;
		return mincost;
	}
}
