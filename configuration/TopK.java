package configuration;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Vector;

public class TopK {
	private Vector<ArrayList<Integer>> types;
	private Vector<ArrayList<Double>> resources;
	private HashMap<Integer,int[]> dis;
	private ArrayList<Integer> k;
	private int centerNum,typeNum;
	private int nodeNum;
	private ArrayList<Double> capacity;
	
	public TopK(Vector<ArrayList<Integer>> types,Vector<ArrayList<Double>> resources,HashMap<Integer,int[]> dis,
			ArrayList<Integer> k,int centerNum,int typeNum,ArrayList<Double> capacity){
		this.types = types;
		this.resources = resources;
		this.dis = dis;
		this.k = k;
		this.centerNum = centerNum;
		this.typeNum = typeNum;
		nodeNum = types.size();
		this.capacity = capacity;
	}
	
	public ConfigandDisResult configure(){
		HashMap<Integer,int[]> distribution = new HashMap<Integer,int[]>();
		Vector<int[]> configuration = new Vector<int[]>();
		for(int i=0;i<centerNum;i++)
			configuration.add(new int[k.get(i)]);
		
		int[][] recrequest = new int[centerNum][typeNum];
		//System.out.println("center num:"+centerNum+"   type num:"+typeNum);

		// count the number of the receiving of request in each kind of each center
		for(int i=0;i<nodeNum;i++){
			ArrayList<Integer> type = types.get(i);
			int[] allocation = dis.get(i);
			int requestNum = type.size();
			
			for(int j=0;j<requestNum;j++){
				int alloc = allocation[j];
				if(alloc == -1)
					continue;
				//System.out.println("alloc:"+alloc+"  j:"+j);
				recrequest[alloc][type.get(j)]++;
			}
		}
		
		// configure the center according the counting number
		for(int i=0;i<centerNum;i++){
			PriorityQueue<TypeandNum> queue = new PriorityQueue<TypeandNum>(TypeandNum.getDesComparator());
			for(int j=0;j<typeNum;j++)
				queue.add(new TypeandNum(j,recrequest[i][j]));
			for(int j=0;j<k.get(i);j++)
				configuration.get(i)[j] = queue.poll().getType();
		}
		
		// redistribute the request
		double[] centercapacity = new double[centerNum];
		for(int i=0;i<centerNum;i++)
			centercapacity[i] = capacity.get(i);
		
		for(int i=0;i<nodeNum;i++){
			ArrayList<Integer> type = types.get(i);
			ArrayList<Double> resource = resources.get(i);
			int[] allocation = dis.get(i);
			int requestNum = type.size();
			int[] reallocation = new int[requestNum];
			
			for(int j=0;j<requestNum;j++){
				int alloc_center = allocation[j];
				int req_type = type.get(j);
				if(alloc_center!=-1&&contain(configuration.get(alloc_center),req_type)){
					double req_demand = resource.get(j);
					if(centercapacity[alloc_center] >= req_demand){
						reallocation[j] = alloc_center;
						centercapacity[alloc_center] -= req_demand;
					}
					else{
						reallocation[j] = -1;
					}
				}
				else{
					reallocation[j] = -1;
				}
			}
			
			distribution.put(i, reallocation);
		}
		
		
		return new ConfigandDisResult(distribution,configuration);
	}
	
	private boolean contain(int[] a,int b){
		for(int x:a)
			if(x == b)
				return true;
		return false;
	}
	
}
