package appro;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

public class maxbenefitmaxflow {
	
	private int number = 0;
	
	/*input:
	 *  1) Graph augraph1 : the auxiliary graph of oridinary graph
	 * 	2) int apNum : the number of AP ，ap的数量
	 *  3) cloudletNum : the number of cloudlet ， cloudlet数量
	 *  4) ArrayList<Integer>[] cloudletConfig : the configuration of cloudlet  ，每个cloudlet的配置
	 *  5) double[] cloudletCap : the capacity of cloudlet ，每个cloudlet的能力
	 *  6) int[] cloudlet : the array of cloudlet index
	 *  7) double netDelay : the delay from ap to cloud
	 *  8) double[][] shortestpath : the shortest path between aps
	 *  
	 * output:
	 *	the flow matrix
	 * */
	
	public double mbmf(Graph augraph1, int apNum, int cloudletNum, Vector<ArrayList<Integer>> cloudletConfig, ArrayList<Double> cloudletCap, ArrayList<Integer> cloudlet, 
			        double[] netDelay, double[][] shortestpath){
		
		MCMF augraph = new MCMF();
		int num_augraph = cloudletNum + augraph1.getNodeCount() - apNum +1;
		augraph.init(num_augraph + 2);  //cloud, a, b
		
		double capacity = MCMF.inf;
		double cost = 0;
		Node node;
		int type;
		int parent;
		int index = augraph1.getNodeCount() - parameters_generator.AP_NUM;    //split ap node number
		
		for(int i = parameters_generator.AP_NUM; i < augraph1.getNodeCount(); i++){
			node = augraph1.getNode(i);
			type = node.getAttribute("type");
			parent = node.getAttribute("parent");
			ArrayList<Integer> config;
			for(int j = 0; j < cloudletNum; j++){
				config = (ArrayList<Integer>)cloudletConfig.get(j).clone();  
				if(config.contains(type)){
					capacity = MCMF.inf;
					cost = shortestpath[parent][cloudlet.get(j)]; 
					augraph.AddEdge(i+1-parameters_generator.AP_NUM , index+j+1, capacity, cost);
				}
			}
		}
		
		for(int i = parameters_generator.AP_NUM; i < augraph1.getNodeCount(); i++){
			node = augraph1.getNode(i);
			parent = node.getAttribute("parent");
			cost = netDelay[parent]; 
			capacity = MCMF.inf;
			augraph.AddEdge(i+1-parameters_generator.AP_NUM , num_augraph, capacity, cost);		
		}
		
		for(int i = parameters_generator.AP_NUM; i < augraph1.getNodeCount(); i++){
			cost = 0; 
			capacity = augraph1.getNode(i).getAttribute("resource");  
			//System.out.println("resource :"+capacity );
			augraph.AddEdge(num_augraph+1 , i+1-parameters_generator.AP_NUM, capacity, cost);
		}
		
		for(int j = 0; j < cloudletNum; j++){
			cost = 0;
			capacity = cloudletCap.get(j);
			augraph.AddEdge(index+j+1 ,num_augraph+2, capacity, cost);	
		}
		
		augraph.AddEdge(num_augraph ,num_augraph+2, MCMF.inf, 0);
		
		augraph.calMCMF(num_augraph+1,num_augraph+2); 
		
		List<Integer> from = augraph.from;
		List<Integer> to = augraph.to;
		List<Double> flow = augraph.flow;
		
		double benefit = totalBenefit(from, to, flow,augraph1, apNum, shortestpath,  netDelay,  cloudlet);
		return benefit;		
	}
	
	private double totalBenefit(List<Integer> from, List<Integer> to, List<Double> flow, Graph augraph1, int apNum, double[][] shortestpath, double[] netDelay, ArrayList<Integer> cloudlet){
		
		double benefit = 0;
		Node node;
		int parent;
		//System.out.println("augraph1 node : " +augraph1.getNodeCount());
		//for(int i = 0 ; i < flowmatrix.length; i++)
		//	System.out.println(Arrays.toString(flowmatrix[i]));
		
		for(int i = 0; i < from.size(); i++){
			if(!((from.get(i) < to.get(i)) &&( to.get(i) <= (augraph1.getNodeCount()-apNum + cloudlet.size() +1))))
				continue;
			if(to.get(i) == (augraph1.getNodeCount() - apNum + cloudlet.size() +1)){
				benefit = benefit+0;
				if(cloudlet.size() == parameters_generator.CLOUDLET_NUM){
					node = augraph1.getNode(from.get(i)+apNum-1);
					parent = node.getAttribute("parent");
					parameters_generator.write("caseD_delay", netDelay[parent]+","+flow.get(i)+"\n");
					number+=flow.get(i);
				}
			}
			else{
				node = augraph1.getNode(from.get(i)+apNum-1);
				parent = node.getAttribute("parent");
				benefit = benefit + flow.get(i)*(netDelay[parent] - shortestpath[parent][cloudlet.get(to.get(i)-(augraph1.getNodeCount()-apNum+1))]);
				if(cloudlet.size() == parameters_generator.CLOUDLET_NUM){
					parameters_generator.write("caseD_delay",shortestpath[parent][cloudlet.get(to.get(i)-(augraph1.getNodeCount()-apNum+1))]+","+flow.get(i)+"\n");
					number+=flow.get(i);
				}
			}
		}
		if(cloudlet.size() == parameters_generator.CLOUDLET_NUM)
			System.out.println("number : "+number);
		return benefit;	
	}
	
	//在调用该算法最开始调用一次，构造该辅助图
	public Graph augraph1(Graph graph){
		Node node;
		for(int i = 0 ; i < parameters_generator.AP_NUM; i++){
			node = graph.getNode(i);
			splitApNode(node, graph);		
		}
		return graph;	
	}
	
    private int splitApNode(Node node, Graph graph){
		
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
			//System.out.println("type :"+typelist.size());
			for(int j = 0; j < typelist.size(); j++){
				if(type == typelist.get(j)){
					resource = resource+1;
				}
			}
			splitnode.setAttribute("resource", resource);
			resource = 0;
			//System.out.println(splitnode.getIndex()+" : "+splitnode.getAttribute("resource"));
		}	
		return splitnum;
	}
	
}
