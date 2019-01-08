#include <math.h>
#include <vector>
#include <queue>

using namespace std;

#define ZERO 1e-8

class EDGE {
	public:
		int v, next;   //----------------------cap,cost: int?
		double cap,cost;
		int original;
		EDGE()
		:v(0),
		 next(0),
		 cap(0.0),
		 cost(0.0),
		 original(0.0) {}
	
		EDGE(int a, double b, double c,int d,int e)
		:v(a),
		 cap(b),
		 cost(c),
		 next(d),
		 original(e) {}
};

class MCMF {
	public:
		constexpr static double inf = 2000000.0;
	
		vector<int> from;
		vector<int> to;
		vector<double> flow;

		void init(int _n){
			n = _n;
			size = 0;
			vis.reserve(n+1);
			net.reserve(n+1);
			for(int i=0;i<=n;i++)
				net[i] = -1;
			pre.reserve(n+1);
			cur.reserve(n+1);
			dis.reserve(n+1);
		}
	
		/**
		 * u & v are source vertex and destination vertex
		 * cap are the edge capacity
		 * cost are the edge cost of a unit flow
		 */
		void AddEdge(int u, int v, double cap, double cost){
			E.emplace_back(v, cap, cost, net[u], 1);
			net[u] = size++;
			E.emplace_back(u, 0, -cost, net[v], 0);
			net[v] = size++;	
		}

		double calMCMF(int s, int t){
			st = s;
			en = t;
			spfa();
			mincost = maxflow = 0;
			for(int i = 0 ; i <= n; i++){
				vis[i] = false;
				cur[i] = net[i];
			}
			do{
				while(fabs(augment(st,inf)) > ZERO){
					for(int i = 0 ; i <vis.size(); i++){
						vis[i] = false;	
					}
				}		
			}while(adjust());
			transOutput();
			return mincost;
		}

		double MincostMaxflow(int s,int t){
			return calMCMF(s,t);
		}

	private:
		int size, n;
		int st, en;
		double maxflow, mincost;
		vector<bool> vis;
		vector<int> net, pre, cur;
		vector<double> dis;
		vector<EDGE> E;
		bool flag;
	
		queue<int> Q;
	
		bool adjust(){
			int v;
			double min = inf;
			for(int i = 0; i <= n; i++){
				if(!vis[i])
					continue;
				for(int j = net[i]; j != -1 ; j = E[j].next){
					v = E[j].v;
					if(fabs(E[j].cap) > ZERO)
						flag = true;
					else
						flag = false;
					if(flag)
						if(!vis[v] && dis[v]-dis[i]+E[j].cost < min){
							min = dis[v] - dis[i] + E[j].cost;
						}
				}
			}
			if(fabs(min - inf) < ZERO)
				return false;
			for(int i = 0 ; i <= n; i++)
				if(vis[i]){
					cur[i] = net[i];
					vis[i] = false;
					dis[i] = min+dis[i];
				}
			return true;
		}
	
		double augment(int i, double flow){
			if(i == en){
				mincost += dis[st] * flow;
				maxflow += flow;
				return flow;
			}
			vis[i] = true;
			for(int j = cur[i],v ; j != -1 ;j = E[j].next){
				v = E[j].v;
				if(fabs(E[j].cap) < ZERO)
					continue;
				if(vis[v] || fabs(dis[v]+E[j].cost - dis[i]) > ZERO)
					continue;
				double delta = augment(v, flow < E[j].cap ? flow : E[j].cap);
				if(fabs(delta) > ZERO){
					E[j].cap -= delta;
					E[j^1].cap += delta;
					cur[i] = j;
					return delta;
				}
			}
			return 0;
		}
	
		void spfa(){
			int u,v;
			for(int i = 0; i <=n ; i++){
				vis[i] = false;
				dis[i] = inf;
			}
			dis[st] = 0;
			Q.emplace(st);
			vis[st] = true;
			while(!Q.empty()){
				u = Q.front();
				Q.pop();
				vis[u] = false;
				for(int i = net[u]; i != -1; i = E[i].next){
					v = E[i].v;
					if(fabs(E[i].cap) < ZERO||dis[v] <= dis[u]+E[i].cost)
						continue;
						
					dis[v] = dis[u]+E[i].cost;
					if(!vis[v]){
						vis[v] = true;
						Q.emplace(v);
					}
				}
			}
			for(int i = 0 ; i <= n ;i++)
				dis[i] = dis[en]-dis[i];
		}
	
		void transOutput(){
	    for(int i = 0 ; i <= n ; i++){
	        int nodeId = i;
	        int edgeId = net[nodeId];
	        while(edgeId!=-1){
	            int rightId = E[edgeId].v;
	            double edgeflow = inf - E[edgeId].cap;
	            if(E[edgeId].original == 1 && fabs(edgeflow)>ZERO){
	                from.emplace_back(nodeId);
	                to.emplace_back(rightId);
	                flow.emplace_back(edgeflow);
	            }
	            edgeId = E[edgeId].next;
	        }
	    }
		}
};
/*
int main() {
		MCMF augraph;     
		augraph.init(5); 
		
		augraph.AddEdge(1 ,2 ,10, 4);
		augraph.AddEdge(1 ,3 ,MCMF::inf, 1);
		augraph.AddEdge(3 ,2 ,5, 2);
		augraph.AddEdge(3 ,4 ,10,3);
		augraph.AddEdge(2 ,4 ,6, 2);
		augraph.AddEdge(2 ,5 ,7, 1);
		augraph.AddEdge(4 ,5 ,4, 2);
		
		augraph.MincostMaxflow(1, 5);
		return 0;
}
*/