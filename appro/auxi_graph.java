package appro;

import java.util.ArrayList;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;

public class auxi_graph {
	
	public Graph constructAuxi(Graph graph, int N){
		
		Graph auxigraph = new SingleGraph("auxiliary graph");
		Node node;
		for(int i = 0 ; i < graph.getNodeCount() ;i++){
			node = graph.getNode(i);
			auxi_graph.splitnode(node, auxigraph, N);	
		}
		auxi_graph.addSplitEdge(graph, auxigraph, N);
		//System.out.println("constructAuxi: auxigraph node : "+auxigraph.getNodeCount());
		//System.out.println("constructAuxi: graph node : "+graph.getNodeCount());
		
		return auxigraph;
	}
	
	private static void splitnode(Node node, Graph auxigraph, int N){
		
		ArrayList<Double> preresourcelist = new ArrayList<>();  
		preresourcelist = node.getAttribute("resource");
		/*ArrayList<Double> resourcelist = new ArrayList<>();
		resourcelist = (ArrayList<Double>)preresourcelist.clone();
		
		double resource = 0;
		for(int i = 0; i < resourcelist.size(); i++){
			resource = resource + resourcelist.get(i);	
		}
		*/
		int n = (int)Math.floor(preresourcelist.size() / N);
		Node splitnode;
		for(int i = 0 ; i < n; i++){
			auxigraph.addNode(node.getIndex()+" "+i);
			splitnode = auxigraph.getNode(node.getIndex()+" "+i);
			splitnode.setAttribute("parent", node.getIndex());
			splitnode.setAttribute("classify", "split_node");
		}	
	}
	
	private static void addSplitEdge(Graph graph, Graph auxigraph, int N){
		
		//compute shortest path
		double[][] shortestpath = new double[graph.getNodeCount()][graph.getNodeCount()];
		shortestpath = parameters_generator.netDelayMatrix(graph);
		
		int par1,par2;
		Node node1, node2;
		Edge edge;
		double weight = 0;
		for(int i = 0; i < auxigraph.getNodeCount(); i++){
			node1 = auxigraph.getNode(i);
			par1 = node1.getAttribute("parent");
			for(int j = i+1; j < auxigraph.getNodeCount(); j++){
				node2 = auxigraph.getNode(j);
				par2 = node2.getAttribute("parent");
				weight = shortestpath[par1][par2]*N;
				
				auxigraph.addEdge(i+" "+j, i, j);
				edge = auxigraph.getEdge(i+" "+j);
				//System.out.println(edge.getIndex() + "   "+edge.getId());
				edge.setAttribute("weight", weight);
				edge.setAttribute("classify", "split_edge");
			}
		}
	}

	public double[][] auxiWeight(Graph auxigraph){
		double[][] shortestpath = new double[auxigraph.getNodeCount()][auxigraph.getNodeCount()];
		shortestpath = parameters_generator.netDelayMatrix(auxigraph);
		return shortestpath;	
	}
	
	public int totalNode(Graph auxigraph){
		return auxigraph.getNodeCount();
	} 
	
	public int cloudletCap(int N){
		return (int)Math.ceil((double)parameters_generator.CLOUDLET_CAP/N);
	}
	
	public static void main(String[] args) {
		Graph graph = new SingleGraph("network");
		network_topology_generator.BANetwork(graph);
		network_topology_generator.initApNode(graph);
		network_topology_generator.initApEdge(graph);
    	process_data.readDataFile("G:\\workspace\\infocom\\datatest\\part-00002-of-00500.csv",graph);
    	
    	auxi_graph test = new auxi_graph();
    	Graph auxigraph = new SingleGraph("auxigraph");
    	step345 s = new step345();
    	auxigraph = test.constructAuxi(graph, (int)s.minWeightNode(graph));
    	//System.out.println("cap :");
    	//double[][] shortestpath = test.auxiWeight(auxigraph);
    	//System.out.println("totalNode :");
    	System.out.println("auxi_graph: auxigraph node : "+auxigraph.getNodeCount());
		System.out.println("auxi_graph: graph node : "+graph.getNodeCount());
    	int totalNode = test.totalNode(auxigraph);
    	System.out.println("totalNode :"+totalNode);
    	int cap = test.cloudletCap(200);
    	System.out.println("cap :"+cap);

	}

}
