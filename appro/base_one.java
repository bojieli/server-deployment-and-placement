package appro;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.graphstream.graph.ElementNotFoundException;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.GraphParseException;

import tpds.simulation;

public class base_one {
	public int[] cloudlet;
	
	private int number = 0;
	
	/* randomly place cloudlet, according to the increasing capacity
	 * 		input : network topology, cloudlet capacity array
	 * */
	public void randomPlace(Graph graph, ArrayList<Double> cap){
		
		Node ap_node;
		int index = 0;
		String classify = "ap_node";
		for(int i = 0; i < parameters_generator.CLOUDLET_NUM ; i++){
			Random ran = new Random();
			index = ran.nextInt(parameters_generator.AP_NUM);
			ap_node = graph.getNode(index);
			if(classify.equals(ap_node.getAttribute("classify"))){
				ap_node.setAttribute("classify", "cloudlet_node");
			}
			else
				i--;
		}
		
		//int[] cloudlet = new int[parameters_generator.CLOUDLET_NUM];
		cloudlet = network_topology_generator.findAllCloudletK(graph);
		//System.out.println("the cloudlet index : "+Arrays.toString(cloudlet));
	}
	
	/* randomly config
	 * 		input : network topology
	 * */
	public void randomConfig(Graph graph,ArrayList<Integer> typeK){
		
		//int[] cloudlet = network_topology_generator.findAllCloudletK(graph);
		int index = 0;
		Node node;
		
		for(int i = 0; i < cloudlet.length; i++){
			ArrayList<Integer> config = new ArrayList<>();
			for(int j = 0 ; j < typeK.get(cloudlet[i]); j++){
				Random ran = new Random();
				index = ran.nextInt(parameters_generator.TYPE_SUM);
				if(config.contains(index))
					j--;
				else
					config.add(index);	
			}
			node = graph.getNode(cloudlet[i]);
			node.setAttribute("configuration", config);	
		}
		//System.out.println(node.getIndex()+" : "+ config);	 
	}
    
	public double optimalDispatch(Graph graph){

		// find all cloudlet 
		//int[] cloudlet = new int[parameters_generator.CLOUDLET_NUM];
		cloudlet = network_topology_generator.findAllCloudletK(graph);
		
		//compute shortest path
		double[][] shortestpath = new double[graph.getNodeCount()][graph.getNodeCount()];
		shortestpath = parameters_generator.netDelayMatrix(graph);   
		
		//split Ap node
		int splitnode = 0;
		Node ap_node;
		for(int i = 0 ; i < parameters_generator.AP_NUM ; i++ ){
			ap_node = graph.getNode(i);
			splitnode = splitnode + base_one.splitApNode(ap_node, graph);
		}
		
		//System.out.println("AP: "+parameters_generator.AP_NUM);
		//System.out.println("split: "+splitnode);
		//System.out.println("count: "+graph.getNodeCount());
		
		if(splitnode != (graph.getNodeCount()-parameters_generator.AP_NUM)){
			System.out.println("Wrong!!!");
			System.exit(0);
		}
		//construct auxiliary graph
		MCMF augraph = new MCMF();      // 
		int num_augraph = splitnode + parameters_generator.CLOUDLET_NUM + 1;
		augraph.init(num_augraph + 2); //
		
		double capacity = 0;
		double cost = 0;
		int type;
		int parent;
		Node node;
		double resource;
		//int[] config = new int[parameters_generator.CONFIG_NUM];
		ArrayList<Integer> config = new ArrayList<>();
		
		for(int i = parameters_generator.AP_NUM; i < graph.getNodeCount(); i++){
			node = graph.getNode(i);
			type = node.getAttribute("type");
			parent = node.getAttribute("parent");
			for(int j = 0; j < parameters_generator.CLOUDLET_NUM; j++){
				config = graph.getNode(cloudlet[j]).getAttribute("configuration");
				if(config.contains(type)){
					capacity = MCMF.inf;
					cost = shortestpath[parent][cloudlet[j]];
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
			node = graph.getNode(cloudlet[j]);
			capacity = node.getAttribute("capacity");
			augraph.AddEdge(graph.getNodeCount()-parameters_generator.AP_NUM+1+j , num_augraph+2, capacity, cost);	
		}
		
		augraph.AddEdge(num_augraph , num_augraph+2, MCMF.inf, 0);
		
		augraph.calMCMF(num_augraph+1,num_augraph+2); 
		//System.out.println("done!");
		
		List<Integer> from = augraph.from;
		List<Integer> to = augraph.to;
		List<Double> flow = augraph.flow;
		/*
		for(int i=0;i<from.size();i++){
			System.out.println(from.get(i)+":"+to.get(i)+":"+flow.get(i));
		}
		*/
		return outputDelay(graph, from, to, flow ,cloudlet,shortestpath);	
	}
	
	private double outputDelay(Graph graph, List<Integer> from, List<Integer> to, List<Double> flow ,int[] cloudlet,double[][] shortestpath){
		
		double delay = 0;
		double weight = 0;
		double curflow;
		int parent;
		int cloudletindex;
		Node node = null;
		
		for(int i = 0 ; i < from.size() ; i++){
			//System.out.println("from.size : "+from.size());
			if(!((from.get(i) < to.get(i)) &&( to.get(i) <= (graph.getNodeCount()-parameters_generator.AP_NUM + cloudlet.length +1))))
				continue;
			if(to.get(i) == (graph.getNodeCount()-parameters_generator.AP_NUM + cloudlet.length +1)){
				//System.out.println(i +":"+from.get(i)+"->"+to.get(i)+"->"+flow.get(i));
				delay = delay +0;
				parent = from.get(i) + parameters_generator.AP_NUM-1;
				node = graph.getNode(parent);
				parent = node.getAttribute("parent");
				parameters_generator.write("caseA_delay", parameters_generator.INTERNET[parent]+","+flow.get(i)+"\n");
				number= (int) (number + flow.get(i));;
			}
			else{
				curflow = flow.get(i);
				parent = from.get(i) + parameters_generator.AP_NUM-1;
				//System.out.println(i +":"+from.get(i)+"->"+to.get(i)+"->"+flow.get(i));
				node = graph.getNode(parent);
				parent = node.getAttribute("parent");
				cloudletindex = to.get(i)-(graph.getNodeCount()-parameters_generator.AP_NUM+1);
				weight = shortestpath[parent][cloudlet[cloudletindex]];
				parameters_generator.write("caseA_delay",weight+","+curflow+"\n");
				delay = delay + curflow*(parameters_generator.INTERNET_DELAY-weight);
				number= (int) (number + curflow);
			}
		}
		//System.out.println("number: " + number);
		return delay;	
	}
	
	/* split node according to type
	 * input : node, graph
	 * output : the number of split_node
	 * 		Id : node index + index
	 *      parent : node index
	 *      classify : split_node
	 *      type : type
	 *      resource : resource
	 * */
	public static int splitApNode(Node node, Graph graph){
		
		int type = 0;
		int splitnum = 0;
		double resource = 0;
		Node splitnode;
		ArrayList<Integer> pretypelist = new ArrayList<>();    //the node arraylist
		ArrayList<Integer> typelist = new ArrayList<>();  
		ArrayList<Integer> typenum= new ArrayList<>();
		ArrayList<Double> resourcelist = new ArrayList<>();
		
		resourcelist = node.getAttribute("resource");
		pretypelist = node.getAttribute("type");
		typelist = (ArrayList<Integer>)pretypelist.clone(); 
		
		//find all request type of the node
		for(int i = 0; i < typelist.size(); i++){
			type = typelist.get(i);
			if(!typenum.contains(type))
				typenum.add(type);
		}
		splitnum = typenum.size();
		
		//split node
		for(int i = 0; i < typenum.size(); i++){
			type = typenum.get(i);
			
			//add a splited node to graph
			graph.addNode(node.getIndex()+" "+i);
			splitnode = graph.getNode(node.getIndex()+" "+i);
			splitnode.setAttribute("parent", node.getIndex());
			splitnode.setAttribute("classify", "split_node");
			splitnode.setAttribute("type", type);
			for(int j = 0; j < typelist.size(); j++){
				if(type == typelist.get(j))
					resource = resource +1;
			}
			splitnode.setAttribute("resource", resource);
			resource = 0;
			//System.out.println(splitnode.getIndex()+" : "+splitnode.getAttribute("resource"));
		}	
		return splitnum;
	}
	
	public static boolean searchArray(int[] a, int key){
		for(int i = 0; i < a.length ; i++){
			if(key == a[i])
				return true;
		}
		return false;
	} 
		 
	public static void main(String[] args) {
	    	
		    Graph graph = simulation.Step0();
		    Graph Algo1 = new SingleGraph("Algo1");
			try {
				Algo1.read("primary_graph.dgs");
			} catch (ElementNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (GraphParseException e) {
				e.printStackTrace();
			}
			simulation.preReadGraph(graph, Algo1);
	    	
			/*try {
				simulation.writeFile("./result/baseone_test.txt");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}                  
			*/
			ArrayList<Double> cap = parameters_generator.cloudletCapacity(parameters_generator.CLOUDLET_NUM, 7000, 9000);
			ArrayList<Integer> typeK = parameters_generator.typeK(parameters_generator.CLOUDLET_NUM, 2, 4);
	    	base_one test = new base_one();
	    	test.randomPlace(Algo1, cap);
	    	test.randomConfig(Algo1, typeK);
	    	test.optimalDispatch(Algo1);
	    	
	    	//System.out.println("done!");
	    	
	    	
			
		}
	
	
}
