package greedyheuristic;

import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Vector;

public class Heuristic {
	private double[][] cost;
	private ArrayList<Double> capacity,request;
	private int k,n;
	
	public Heuristic(double[][] cost,ArrayList<Double> capacity,ArrayList<Double> request,int n,int k){
		this.cost = cost;
		this.capacity = capacity;
		this.request = request;
		this.n = n;
		this.k = k;
	}
	
	public Vector<Integer> run(){
		Vector<Integer> L = new Vector<Integer>();
		Vector<Integer> S = new Vector<Integer>();
		for(int i=0;i<n;i++){
			S.add(i);
		}
		
		PriorityQueue<ListNode> capacityQueue = new PriorityQueue<ListNode>(ListNode.getDesComparator());
		for(int i=0;i<k;i++){
			capacityQueue.add(new ListNode(capacity.get(i),i));
		}
		
		for(int i=0;i<k;i++){
			ListNode Ci = capacityQueue.poll();
			PriorityQueue<ListNode> D = new PriorityQueue<ListNode>(ListNode.getComparator());
			for(int u:S){
				PriorityQueue<ListNode> d = new PriorityQueue<ListNode>(ListNode.getComparator());
				for(int j=0;j<n;j++){
					d.add(new ListNode(cost[u][j],j));
				}
				
				Vector<Integer> points = new Vector<Integer>();
				for(int j=0;j<n;j++)
					points.add(j);
				
				double assignresource = 0;
				double delay = 0;
				ListNode rnode = new ListNode();
				while(assignresource < capacity.get(Ci.getKey()) && d.size() != 0){
					rnode = d.poll();
					delay += rnode.getValue();
					assignresource += request.get(rnode.getKey());
					points.removeElement(rnode.getKey());
				}
				
				if(assignresource > capacity.get(Ci.getKey())){
					delay -= rnode.getValue();
					assignresource -= request.get(rnode.getKey());
					points.add(rnode.getKey());
				}
				
				if(assignresource < capacity.get(Ci.getKey())){
					PriorityQueue<ListNode> rd = new PriorityQueue<ListNode>(ListNode.getComparator());
					for(int residue:points){
						rd.add(new ListNode(request.get(rnode.getKey()),residue));
					}
					while(rd.size() != 0){
						rnode = rd.poll();
						if(assignresource + rnode.getValue() < capacity.get(Ci.getKey())){
							delay += cost[u][rnode.getKey()];
							assignresource += rnode.getValue();
						}
						else{
							break;
						}
					}
				}
				
				D.add(new ListNode(delay,u));				
			}
			
			ListNode selectcenter = D.peek();
			L.add(selectcenter.getKey());
			S.removeElement(selectcenter.getKey());
			
		}
		
		return L;
		
	}
	
	public static void main(String[] args){
		Vector<Integer> v = new Vector<Integer>();
		v.add(1);
		v.add(3);
		System.out.println(v);
		v.removeElement(3);
		System.out.println(v);
	}

}
