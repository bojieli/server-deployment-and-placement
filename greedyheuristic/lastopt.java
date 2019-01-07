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
import appro.base_one;
import appro.network_topology_generator;
import appro.parameters_generator;

public class lastopt {
	
	private int number = 0;
	
	public double lastoptdispatching(Graph graph, Vector<Integer> cloudlet, ArrayList<Double> cloudletCapacity, Vector<int[]> configuration, double[][] shortestpath){
				
		//split Ap node
		int splitnode = 0;
		Node ap_node;
		for(int i = 0 ; i < parameters_generator.AP_NUM ; i++ ){
			ap_node = graph.getNode(i);
			splitnode = splitnode + base_one.splitApNode(ap_node, graph);
		}
				
	    if(splitnode != (graph.getNodeCount()-parameters_generator.AP_NUM)){
			System.out.println("Wrong!!!");
			System.exit(0);
		}
		//construct auxiliary graph
		MCMF augraph = new MCMF();      // 
		int num_augraph = splitnode + parameters_generator.CLOUDLET_NUM + 1;
		augraph.init(num_augraph + 2); //
				
		double capacity = MCMF.inf;
		double cost = 0;
		int type;
		int parent;
		Node node;
		double resource;
				
		for(int i = parameters_generator.AP_NUM; i < graph.getNodeCount(); i++){
			node = graph.getNode(i);
			type = node.getAttribute("type");
			parent = node.getAttribute("parent");
			for(int j = 0; j < parameters_generator.CLOUDLET_NUM; j++){
				int[] config = configuration.get(j);
				if(contain(config,type)){
					capacity = MCMF.inf;
					cost = shortestpath[parent][cloudlet.get(j)];
					augraph.AddEdge(i+1-parameters_generator.AP_NUM , graph.getNodeCount()-parameters_generator.AP_NUM+1+j, capacity, cost);
				}
			}
		}
				
		for(int i = parameters_generator.AP_NUM; i < graph.getNodeCount(); i++){
			capacity = MCMF.inf;
			node = graph.getNode(i);
			parent = node.getAttribute("parent");
			cost = parameters_generator.INTERNET[parent];
			augraph.AddEdge(i+1-parameters_generator.AP_NUM ,num_augraph , capacity, cost);
		}
				
		for(int i = parameters_generator.AP_NUM; i < graph.getNodeCount(); i++){
				node = graph.getNode(i);
				capacity = node.getAttribute("resource");
				cost = 0;
				augraph.AddEdge(num_augraph+1 , i+1-parameters_generator.AP_NUM, capacity, cost);	
		}
				
		for(int j = 0; j < parameters_generator.CLOUDLET_NUM; j++){
			cost = 0;
			capacity = cloudletCapacity.get(j);
			augraph.AddEdge(graph.getNodeCount()-parameters_generator.AP_NUM+1+j , num_augraph+2, capacity, cost);	
		}
				
		augraph.AddEdge(num_augraph , num_augraph+2, MCMF.inf, 0);
				
		augraph.calMCMF(num_augraph+1,num_augraph+2); 
			
	    List<Integer> from = augraph.from;
	    List<Integer> to = augraph.to;
		List<Double> flow = augraph.flow;	
		/*
		for(int i=0;i<from.size();i++){
			System.out.println(from.get(i)+":"+to.get(i)+":"+flow.get(i));
		}
		*/
		//System.out.println("graph :"+graph.getNodeCount());
		
		double benefit = 0;
		int ap;
		int cloudleti;
		for(int i = 0 ; i < from.size() ; i++){
			if(((from.get(i) < to.get(i)) && (to.get(i) == num_augraph))){
				ap = from.get(i)+parameters_generator.AP_NUM-1;
				node = graph.getNode(ap);
				ap = node.getAttribute("parent");
				parameters_generator.write("caseC_delay",parameters_generator.INTERNET[ap]+","+flow.get(i)+"\n");
				number+=flow.get(i);
			}
			if(!((from.get(i) < to.get(i)) && (to.get(i) < num_augraph)))
				continue;
			ap = from.get(i)+parameters_generator.AP_NUM-1;
			//System.out.println(ap);
			node = graph.getNode(ap);
			ap = node.getAttribute("parent");
			//System.out.println(ap);
			//System.out.println("ap index"+(to.get(i)-splitnode-1));
			cloudleti = cloudlet.get(to.get(i)-splitnode-1);
			//System.out.println("cloudlet index"+cloudleti);
			benefit += flow.get(i)*(parameters_generator.INTERNET_DELAY - shortestpath[ap][cloudleti]); 
			parameters_generator.write("caseC_delay",shortestpath[ap][cloudleti]+","+flow.get(i)+"\n");
			number+=flow.get(i);
		}
		//System.out.println("number :" + number);
		return benefit;
			
	}
	
	private boolean contain(int[] a,int b){
		for(int x:a)
			if(x == b)
				return true;
		return false;
	}

}
