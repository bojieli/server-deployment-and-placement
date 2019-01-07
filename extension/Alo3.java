package extension;

import configuration.*;
import tpds.*;

import java.util.ArrayList;
import java.util.Vector;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;

import appro.network_topology_generator;
import appro.parameters_generator;
import appro.process_data;

public class Alo3 {
	public Result run(Graph graph){
		Vector<ArrayList<Double>> vec = new Vector<ArrayList<Double>>();
		for(int i=0;i<graph.getNodeCount();i++){
			ArrayList<Double> list = graph.getNode(i).getAttribute("resource");
			vec.add(list);
		}
		Preliminary pre = new Preliminary(graph.getNodeCount(),vec);
		Graph modifyG = new ModifyGraph().modify(pre.getMin(), graph);
		Result firstR = new Alo2().run(modifyG);
		Reallocation realloc = new Reallocation(graph.getNodeCount(),firstR.getPlacement(),parameters_generator.netDelayMatrix(graph)
				,firstR.getAllocation(),pre.getVRequest());
		return new Result(realloc.realloc(),firstR.getPlacement());
	}
	
/*	public static void main(String arg[]){
		
		Graph graph = new SingleGraph("network");
		network_topology_generator.BANetwork(graph);
		network_topology_generator.initApNode(graph);
		network_topology_generator.initApEdge(graph);
    	process_data.readDataFile("G:\\workspace\\infocom\\datatest\\part-00002-of-00500.csv",graph);

    	Alo3 a = new Alo3();
    	Result r = a.run(graph);
    	r.print();
    	
    	Vector<ArrayList<Integer>> types = new Vector<ArrayList<Integer>>();
    	Vector<ArrayList<Double>> resources = new Vector<ArrayList<Double>>();
    	
    	for(Node node:graph){
    		ArrayList<Integer> type;
    		ArrayList<Double> resource;
    		
    		type = node.getAttribute("type");
    		resource = node.getAttribute("resource");
    		
    		types.add(type);
    		resources.add(resource);
    	}
    	
    	TopK top = new TopK(types,resources,r.getAllocation(),
    			parameters_generator.CONFIG_NUM,parameters_generator.CLOUDLET_NUM,
    			parameters_generator.TYPE_SUM,parameters_generator.CLOUDLET_CAP);
    	ConfigandDisResult config = top.configure();
    	config.print();
	}            
*/	
}
