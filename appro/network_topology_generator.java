package appro;

import org.graphstream.graph.*;
import org.graphstream.algorithm.generator.*;
import java.util.ArrayList;

public class network_topology_generator {
	
	/*generator network topology
	 * parameters:
	 * 		1) the attachment rate of the network: parameters_generator.ATTACH_NET
	 *      2) the number of nodes: parameters_generator.AP_NUM
	 */
	public static void BANetwork(Graph graph){
		
		Generator gen = new BarabasiAlbertGenerator(parameters_generator.ATTACH_NET);
	    //generate nodes
		gen.addSink(graph);  //add a sink for all graph events
		gen.begin();    //start the generator
	    for(int i = 0 ; i < parameters_generator.AP_NUM - 2 ; i++){
	    	gen.nextEvents();    //step of the generator
		}
		gen.end();    //clean degrees
	}
	
	/* add informations to ap nodes
	 * ap_node:
	 * 		Id : Index 
	 * 		classify : ap_node / cloudlet_node
	 * 		type :
	 *      resource :
     *      configuration : service configuration
     *      cap : the capacity of this node, 0.0
     */	
	public static void initApNode(Graph graph){
		
		for( Node ap_node : graph ){
			ArrayList<Integer> type = new ArrayList<>();
			ArrayList<Double> resource = new ArrayList<>();
			ArrayList<Integer> config = new ArrayList<>();
			ap_node.setAttribute("Id", ap_node.getIndex());     //assign index to each ap_node as Id
			ap_node.setAttribute("classify", "ap_node");
			ap_node.setAttribute("type", type);     //
			ap_node.setAttribute("resource", resource);
			ap_node.setAttribute("configuration", config);  
			ap_node.setAttribute("capacity", 0.0);
		}
	}
	
	/* add informations to links
	 * ap_edge:
	 * 		Id : Index
	 * 		classify : ap_edge
	 * 		weight : network delay
     */ 
	public static void initApEdge(Graph graph){

		for( Edge ap_edge : graph.getEachEdge() ){
			ap_edge.setAttribute("Id", ap_edge.getIndex());     //assign index to each ap_edge as Id
			ap_edge.setAttribute("classify", "ap_edge");
			ap_edge.setAttribute("weight", parameters_generator.networkDelay());     //assign index to each ap_node as Id----------------------
		}	
	}
	
	/* satisfy the triangle inequality
	 * */
    public static void setWeightEdge(Graph graph){
		
		Node node0, node1;
		Edge edge0,edge1;
		Number weight0;
		Number weight1;
		double weight2,weight;
		for( Edge ap_edge : graph.getEachEdge()){
			node0 = ap_edge.getNode0();
			node1 = ap_edge.getNode1();
			for(Node node : graph){
				if(node0.hasEdgeBetween(node) && node1.hasEdgeBetween(node)){
					edge0 = node0.getEdgeBetween(node);
					edge1 = node1.getEdgeBetween(node);
					weight0 = edge0.getAttribute("weight");
					weight1 = edge1.getAttribute("weight");
					if( weight0.floatValue()== 0.0 ||weight1.floatValue() == 0.0)
						ap_edge.setAttribute("weight", parameters_generator.networkDelay());
					else{
						weight = weight0.floatValue() + weight1.floatValue();
						weight2 = parameters_generator.networkDelay();
						while(weight2 >= weight){
							weight2 = parameters_generator.networkDelay();	
						}
						ap_edge.setAttribute("weight", weight2);		
					}	
				}else
					ap_edge.setAttribute("weight", parameters_generator.networkDelay());	
			}			
		}
		for( Edge ap_edge : graph.getEachEdge() ){
			ap_edge.addAttribute("ui.label", (Object)ap_edge.getAttribute("weight"));
		}
	}
    
    public static int[] findAllCloudletK (Graph graph){                //find all cloudlet, return cloudlet index array
		 int k = 0;
		 String classify = "cloudlet_node";
		 int[] cloudlet = new int[parameters_generator.CLOUDLET_NUM];    //store cloudlet index
		 for( Node node : graph ){
			 if(classify.equals(node.getAttribute("classify"))){
				 cloudlet[k] = node.getIndex();
				 k++;
			 }	
		 }
		 return cloudlet;
	}

}
