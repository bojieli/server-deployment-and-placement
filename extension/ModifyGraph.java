package extension;

import java.io.IOException;
import java.util.ArrayList;

import org.graphstream.graph.ElementNotFoundException;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.GraphParseException;

public class ModifyGraph {
	
	/*
	 *  input :
	 *  
	 *  double size is the demand resource of each request
	 *  Each element in the list denote the number of request in the corresponding node.
	 *  Graph initial_graph is the initial input graph.
	 *  
	 *  output:
	 *  
	 *  The modified graph of the initial_graph whose node's requestNum and demand resource is the same as providing data.
	 *  
	 */
	public Graph modify(double size,Graph initial_graph){
		
		Graph graph = new SingleGraph("Alo3 graph");
		try {
			initial_graph.write("initial_graph.dgs");
			graph.read("initial_graph.dgs");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ElementNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GraphParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Node node;
		for(int i = 0 ; i < graph.getNodeCount() ;i++){
			resetResource(i,initial_graph,graph);
		}
		
		for(int i = 0 ; i < graph.getNodeCount() ;i++){
			node = graph.getNode(i);
			modifynode(node, graph, size);	
		}
		return graph;
	}
	
	private void resetResource(int nodeindex, Graph initial_graph, Graph graph){
		
		ArrayList<Double> resource1 = new ArrayList<>();
		ArrayList<Double> resource2 = new ArrayList<>();
		ArrayList<Integer> type1 = new ArrayList<>();
		ArrayList<Integer> type2 = new ArrayList<>();
		Node node1,node2;
		node1 = initial_graph.getNode(nodeindex);
		resource1 = node1.getAttribute("resource");
		resource2 =(ArrayList<Double>)resource1.clone();
		type1 = node1.getAttribute("type");
		type2 = (ArrayList<Integer>)type1.clone();
		
		node2 = graph.getNode(nodeindex);
		node2.setAttribute("resource", resource2);
		node2.setAttribute("type", type2); 
		
	}
	
	private void modifynode(Node node, Graph graph, double size){
		
		ArrayList<Double> resourcelist = new ArrayList<>();
		resourcelist = node.getAttribute("resource");
		
		ArrayList<Double> newlist = new ArrayList<>();
		double resource;
		int n;
		for(int i = 0 ; i < resourcelist.size() ; i++){
			resource = resourcelist.get(i);
			n = (int)Math.ceil(resource / size);   
			for(int j = 0 ; j < n; j ++)
				newlist.add(size);
		}
		node.setAttribute("resource", newlist);   
	}

}
