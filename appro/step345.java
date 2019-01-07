package appro;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

import scala.collection.generic.BitOperations.Int;

public class step345 {
	
	public void placeCloudlet(HashMap<Integer, Integer> map, Graph graph, Graph auxigraph){
		
		Node cloudlet_node;
		Node splitnode;
		int index;
		Iterator it = map.entrySet().iterator();
		//System.out.println(map);
		while (it.hasNext()){
			Map.Entry pairs = (Map.Entry)it.next();
			
			index = (Integer)pairs.getValue();
			splitnode = auxigraph.getNode(index);
			//System.out.println("step3: auxigraph index : "+index);
			index = splitnode.getAttribute("parent");
			cloudlet_node = graph.getNode(index);
			//System.out.println("step3: graph index : "+index);
			cloudlet_node.setAttribute("classify", "cloudlet_node");		
		}
		int[] cloudlet = network_topology_generator.findAllCloudletK(graph);
		//System.out.println("step3: cloudlet index : "+Arrays.toString(cloudlet));
	}  
	
	public HashMap<Integer, int[]> assignStep4(HashMap<Integer, Integer> map, Graph graph, Graph auxigraph){
		
		int[] cloudlet = new int[parameters_generator.CLOUDLET_NUM];
		cloudlet = network_topology_generator.findAllCloudletK(graph);
		
		HashMap<Integer, int[]> assign = new HashMap<Integer, int[]>();
		for(int i = 0; i < parameters_generator.AP_NUM; i++){
			assign.put(i, new int[parameters_generator.CLOUDLET_NUM]);
		}
		
		Node splitnode;
		Node cloudletnode;
		int keyindex,valindex,index = 0;
		
		Iterator it = map.entrySet().iterator();
		while (it.hasNext()){
			Map.Entry pairs = (Map.Entry)it.next();
			
			keyindex = (Integer)pairs.getKey();
			splitnode = auxigraph.getNode(keyindex);
			keyindex = splitnode.getAttribute("parent");
			
			valindex = (Integer)pairs.getValue();
			cloudletnode = auxigraph.getNode(valindex);
			valindex = cloudletnode.getAttribute("parent");
			
			for(int i = 0; i < cloudlet.length; i ++){
				if(cloudlet[i] == valindex){
					index = i;
					break;
				}
			}

			assign.get(keyindex)[index]++;
		}
		return assign;	
	}
	
	public HashMap<Integer, int[]> assignStep5(HashMap<Integer, int[]> map, Graph graph, int N){
		
		HashMap<Integer, int[]> assign = new HashMap<Integer, int[]>();
		for(int i = 0; i < parameters_generator.AP_NUM; i++){
			assign.put(i, new int[parameters_generator.CLOUDLET_NUM]);
		}
		
		for(int i = 0; i < parameters_generator.AP_NUM; i++){
			for(int j = 0; j < parameters_generator.CLOUDLET_NUM ; j++){
				assign.get(i)[j] = map.get(i)[j]*N;
			}
		}
		
		int[] cloudlet = new int[parameters_generator.CLOUDLET_NUM];
		cloudlet = network_topology_generator.findAllCloudletK(graph);
		//System.out.println("step5: cloudlet index : "+Arrays.toString(cloudlet));
		
		Node node;
		int w = 0 , rest = 0;
		int assignnum = 0, cloudletindex = 0, index=0;
		step345 step5 = new step345();
		for(int i = 0 ; i < graph.getNodeCount() ; i++){
			node = graph.getNode(i);
			w = step5.weightSum(node);  
			assignnum = step5.alreadyAssignNum(i, map);   
			rest = w - assignnum*N;
			if(rest > 0){
				cloudletindex = step5.nearestCloudlet(graph, i);   
				for(int j = 0; j < cloudlet.length; j ++){
					if(cloudlet[j] == cloudletindex){
						index = j;
						break;
					}
				}
				assign.get(i)[index] = assign.get(i)[index] + rest;
			}	
		}
		return assign;
	}
	
	private int alreadyAssignNum(int nodeindex, HashMap<Integer, int[]> map){
		int number = 0;
		for(int i = 0; i < parameters_generator.CLOUDLET_NUM; i ++)
			number = number + map.get(nodeindex)[i];
		return number;
	}
	
	private int weightSum(Node node){
		
		//double resource = 0;
		ArrayList<Double> resourcelist = new ArrayList<>();
		resourcelist = node.getAttribute("resource");
		//for(int i = 0; i < resourcelist.size(); i++)
		//	resource = resource + resourcelist.get(i);
		//System.out.println("weightSum: node request : "+ resourcelist.size());
		return resourcelist.size();
	}
	
	public int minWeightNode(Graph graph){
		
		Node node;
		int min = Integer.MAX_VALUE;
		for(int i=0; i < graph.getNodeCount(); i++){
			node = graph.getNode(i);
			if(weightSum(node)< min)
				min = weightSum(node);	
		}
		//System.out.println("N0 : "+ min);
		return min;
	}
	
	private int nearestCloudlet(Graph graph, int index){
		
		int[] cloudlet = new int[parameters_generator.CLOUDLET_NUM];
		cloudlet = network_topology_generator.findAllCloudletK(graph);
		
		double[][] shortestpath = new double[graph.getNodeCount()][graph.getNodeCount()];
		shortestpath = parameters_generator.netDelayMatrix(graph);  
		
		int cloudletindex = 0;
		double min = Double.MAX_VALUE;
		for(int i = 0; i < cloudlet.length; i++){
			if(shortestpath[index][cloudlet[i]] < min){
				min = shortestpath[index][cloudlet[i]];
				cloudletindex = cloudlet[i];
			}
		}
		return cloudletindex;	
	}
	
}
