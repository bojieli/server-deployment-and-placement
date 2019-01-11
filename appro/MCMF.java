package appro;

import java.util.*;
import java.io.*;
import java.lang.*;

public class MCMF {
	public static final double inf = 2000000;
	
	public List<Integer> from;
	public List<Integer> to;
	public List<Double> flow;
	
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
		vis = new boolean[n+1];
		net = new int[n+1];
		for(int i=0;i<=n;i++)
			net[i] = -1;
		pre = new int[n+1];
		cur = new int[n+1];
		dis = new double[n+1];
		E = new ArrayList<EDGE>();
		Q = new ArrayDeque<Integer>();
        from = new ArrayList<Integer>();
        to = new ArrayList<Integer>();
        flow = new ArrayList<Double>();
	}
	
	public void AddEdge(int u, int v, double cap, double cost){
		E.add(new EDGE(v, cap, cost, net[u], 1));
		net[u] = size++;
		E.add(new EDGE(u, 0, -cost, net[v], 0));
		net[v] = size++;	
	}
	
	private boolean adjust(){
		//System.exit(0);
		int v;
		double min = inf;
		for(int i = 0; i <= n; i++){
			if(!vis[i])
				continue;
			for(int j = net[i]; j != -1 ; j = E.get(j).next){
				v = E.get(j).v;
				if(Math.abs(E.get(j).cap) > 1e-8)
					flag = true;
				else
					flag = false;
				if(flag)
					if(!vis[v] && dis[v]-dis[i]+E.get(j).cost < min){
						min = dis[v] - dis[i] + E.get(j).cost;
					}
			}
		}
		if(Math.abs(min - inf) < 1e-8)
			return false;
		for(int i = 0 ; i <= n; i++)
			if(vis[i]){
				cur[i] = net[i];
				vis[i] = false;
				dis[i] = min+dis[i];
			}
		//System.out.println(min);
		return true;
	}
	
	private double augment(int i, double flow){
		if(i == en){
			mincost += dis[st] * flow;
			maxflow += flow;
			return flow;
		}
		vis[i] = true;
		for(int j = cur[i],v ; j != -1 ;j = E.get(j).next){
			v = E.get(j).v;
			if(Math.abs(E.get(j).cap) < 1e-8)
				continue;
			if(vis[v] || Math.abs(dis[v]+E.get(j).cost - dis[i]) > 1e-8)
				continue;
			double delta = augment(v, Math.min(flow, E.get(j).cap));
			if(Math.abs(delta) > 1e-8){
				E.get(j).cap -= delta;
				E.get(j^1).cap += delta;
				cur[i] = j;
				return delta;
			}
		}
		return 0;
	}
	
	private void spfa(){
		//System.out.println("spfa start");
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
				if(Math.abs(E.get(i).cap) < 1e-8||dis[v] <= dis[u]+E.get(i).cost)
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
		//System.out.println("spfa done");
	}
	
	private void transOutput(){
	    for(int i = 0 ; i <= n ; i++){
	        int nodeId = i;
	        int edgeId = net[nodeId];
	        while(edgeId!=-1){
	            int rightId = E.get(edgeId).v;
	            double edgeflow = inf - E.get(edgeId).cap;
	            if(E.get(edgeId).original == 1 && Math.abs(edgeflow)>1e-8){
	                from.add(nodeId);
	                to.add(rightId);
	                flow.add(edgeflow);
	                //System.out.println(nodeId + ":" + rightId + ":" + edgeflow);
	            }
	            edgeId = E.get(edgeId).next;
	        }
	    }
	}
	
	public double calMCMF(int s, int t){
		st = s;
		en = t;
		spfa();
		mincost = maxflow = 0;
		//System.out.println("calMCMF start");
		for(int i = 0 ; i <= n; i++){
			vis[i] = false;
			cur[i] = net[i];
		}
		do{
			while(Math.abs(augment(st,inf)) > 1e-8){
				for(int i = 0 ; i <vis.length; i++){
					vis[i] = false;	
				}
			}		
		}while(adjust());
		//System.out.println("calMCMF done!");
		transOutput();
		return mincost;
	}
	
	public double MincostMaxflow(int s,int t){
		return calMCMF(s,t);
	}
	
	public static void main(String arg[]){
		MCMF augraph = new MCMF();      
		augraph.init(5); 
		
		augraph.AddEdge(1 ,2 ,10, 4);
		augraph.AddEdge(1 ,3 ,8, 1);
		augraph.AddEdge(3 ,2 ,5, 2);
		augraph.AddEdge(3 ,4 ,10,3);
		augraph.AddEdge(2 ,4 ,6, 2);
		augraph.AddEdge(2 ,5 ,7, 1);
		augraph.AddEdge(4 ,5 ,4, 2);
		
		System.out.println(augraph.MincostMaxflow(1, 5));
		
	}

}