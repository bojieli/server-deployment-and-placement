package greedyheuristic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

import appro.MCMF;
import appro.parameters_generator;

public class optdispaching {
	
	public HashMap<Integer,int[]> heuristicdispaching(Graph graph, Vector<Integer> cloudlet, ArrayList<Double> cloudletCapacity, double[][] shortestpath){
		
		
		MCMF augraph = new MCMF();      
		int num_augraph = parameters_generator.AP_NUM + cloudlet.size() + 1;
		augraph.init(num_augraph+2);
		
		double capacity = MCMF.inf;
		double cost = 0;
		Node node;
		
		for(int i = 0 ; i < parameters_generator.AP_NUM; i++){
			for(int j = 0 ; j < cloudlet.size(); j++){
				cost = shortestpath[i][cloudlet.get(j)];
				capacity = MCMF.inf;
				augraph.AddEdge(i+1, j+1+parameters_generator.AP_NUM, capacity, cost);	
			}
		}
		
		for(int i = 0 ; i < parameters_generator.AP_NUM; i++){
			cost = parameters_generator.INTERNET[i];
			capacity = MCMF.inf;
			augraph.AddEdge(i+1, num_augraph, capacity, cost);	
		}
		
		for(int i = 0 ; i < parameters_generator.AP_NUM; i++){
			cost = 0;
			node = graph.getNode(i);
			capacity = apResource(node);
			augraph.AddEdge(num_augraph+1, i+1, capacity, cost);	
		}
		
		for(int j = 0 ; j < cloudlet.size(); j++){
			cost = 0;
			capacity = cloudletCapacity.get(j);
			augraph.AddEdge(j+1+parameters_generator.AP_NUM, num_augraph+2, capacity, cost);	
		}
		
		augraph.AddEdge(num_augraph, num_augraph+2, MCMF.inf, 0);
		
        augraph.calMCMF(num_augraph+1,num_augraph+2);
		
        List<Integer> from = augraph.from;
		List<Integer> to = augraph.to;
		List<Double> flow = augraph.flow;
		/*
		for(int i=0;i<from.size();i++){
			System.out.println(from.get(i)+":"+to.get(i)+":"+flow.get(i));
		}
		*/
		
		
		
		HashMap<Integer,int[]> result = new HashMap<Integer,int[]>();
		HashMap<Integer,int[]> preresult = new HashMap<Integer,int[]>();
		ArrayList<Integer> type;
		double num = 0;
		int apindex;
		int cloudletindex;
		
		for(int i = 0 ; i < parameters_generator.AP_NUM; i++){
			node = graph.getNode(i);
			type = node.getAttribute("type");
			result.put(i, new int[type.size()]);
			preresult.put(i, new int[cloudlet.size()+1]);
		}
		
		for(int i = 0 ; i < from.size(); i++){
			if(!((from.get(i) < to.get(i)) &&( to.get(i) <= num_augraph)))
				continue;
			num = flow.get(i);
			apindex = from.get(i)-1;
			if(to.get(i) == num_augraph)
				cloudletindex = cloudlet.size();
			else	
				cloudletindex = to.get(i)-(1+parameters_generator.AP_NUM);
			preresult.get(apindex)[cloudletindex] += (int)num;	
		}
		
		//for(int i = 0 ; i < result.size() ;i++)
		//	System.out.println("prekey :"+ Arrays.toString(preresult.get(i)));
		
		Iterator it = preresult.entrySet().iterator();
		while (it.hasNext()){
			Map.Entry pairs = (Map.Entry)it.next();
			apindex = (Integer)pairs.getKey();
			int k = 0;
			for(int i = 0 ; i < preresult.get(apindex).length-1 ; i++){
				num = preresult.get(apindex)[i];
				for(int j = 0 ;j < num ; j ++){
					result.get(apindex)[k] = i;
					k++;
				}
			}
			num = preresult.get(apindex)[cloudlet.size()];
			for(int j = 0 ;j < num ; j ++){
				result.get(apindex)[k] = -1;
				k++;
			}
		}
		return result;	
	}
	
	public HashMap<Integer,int[]> translation(HashMap<Integer,int[]> flowmatrix, Graph graph){
		
		HashMap<Integer,int[]> result = new HashMap<Integer,int[]>();
		ArrayList<Integer> type;
		int num = 0;
		int index;
		Node node;
		for(int i = 0 ; i < parameters_generator.AP_NUM; i++){
			node = graph.getNode(i);
			type = node.getAttribute("type");
			result.put(i, new int[type.size()]);
			index = 0;
			for(int j = 0 ; j < flowmatrix.get(i).length ; j++){
				num = flowmatrix.get(i)[j];
				for(int k = 0; k < num; k++){
					result.get(i)[index] = j;
					index ++;
				}	
			}
		}	
		return result;	
	}
	
	private double apResource(Node node){
		double resource = 0;
		ArrayList<Double> resourcelist = new ArrayList<>();
		resourcelist = node.getAttribute("resource");
	    resource = resourcelist.size();
		return resource;
	}
	
	public double benefit(HashMap<Integer, int[]> distribution, double[][] shortestpath, Vector<Integer> cloudlet, Graph graph){
		double benefit = 0 ;
		int apindex; 
		int[] server;
		Iterator it = distribution.entrySet().iterator();
		while (it.hasNext()){
			Map.Entry pairs = (Map.Entry)it.next();
			apindex = (Integer)pairs.getKey();
            server = (int[])pairs.getValue();
			for(int i = 0 ; i < server.length; i++){
				if(server[i] == -1){
					benefit = benefit+0;
					parameters_generator.write("caseB_delay",parameters_generator.INTERNET[apindex]+","+1+"\n");
					number++;
				}
				else{
					benefit += parameters_generator.INTERNET_DELAY - shortestpath[apindex][cloudlet.get(server[i])];
					parameters_generator.write("caseB_delay",shortestpath[apindex][cloudlet.get(server[i])]+","+1+"\n");
					number++;
				}
			}	
		}
		//System.out.println("number : "+number);
		return benefit;
	}
	
	private int number = 0;
	

}
