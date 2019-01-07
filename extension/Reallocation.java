package extension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Vector;

public class Reallocation {
	private int n;
	private int[] center;
	private double[][] cost;
	private HashMap<Integer,int[]> initial_alloc;
	private Vector<ArrayList<Integer>> virtual_request;
	
	public Reallocation(int n,int[] center,double[][] cost
			,HashMap<Integer,int[]> initial_alloc
			,Vector<ArrayList<Integer>> virtual_request){
		this.n = n;
		this.center = center;
		this.cost = cost;
		this.initial_alloc = initial_alloc;
		this.virtual_request = virtual_request;
	}
	
	public HashMap<Integer,int[]> realloc(){
		HashMap<Integer,int[]> alloc = new HashMap<Integer,int[]>();
		
		for(int i=0;i<n;i++){
			ArrayList<Integer> request = virtual_request.get(i);
			int[] allocation = initial_alloc.get(i);
			int[] re_allocation = new int[request.size()];
			
			int allocindex = 0;
			int residue = allocation[allocindex];
			//System.out.println(Arrays.toString(allocation));
			while(residue == 0){
				residue = allocation[++allocindex];
			}
			for(int j=0;j<request.size();j++){
				residue -= request.get(j);
				if(residue >= 0){
					re_allocation[j] = allocindex;
					while(residue == 0){
						++allocindex;
						if(allocindex == allocation.length)
							break;
						residue = allocation[allocindex];
					}
				}
				else{
					Vector<Integer> b = new Vector<Integer>();
					b.add(allocindex);
					while(residue < 0){
						++allocindex;
						if(allocindex == allocation.length)
							break;
						while(allocation[allocindex] == 0){
							++allocindex;
							if(allocindex == allocation.length)
								break;
						}
						residue += allocation[allocindex];
						b.add(allocindex);
					}
					while(residue == 0){
						++allocindex;
						if(allocindex == allocation.length)
							break;
						residue = allocation[allocindex];
					}
					double max = 0;
					int selectindex = b.get(0);
					for(int candidateindex: b){
						double c = cost[center[candidateindex]][i];
						if(c > max){
							max = c;
							selectindex = candidateindex;
						}
					}
					re_allocation[j] = selectindex;
				}// else
			}// for
			alloc.put(i, re_allocation);
		}// for
		
		return alloc;
	}
}
