package tpds;

import java.io.IOException;
import java.util.HashMap;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;

import appro.*;
import kmedian.*;

public class Alo2 {
	
	public Result run(Graph graph){
		auxi_graph constructor = new auxi_graph();
		step345 step345 = new step345();
		Graph auxiG = constructor.constructAuxi(graph, step345.minWeightNode(graph));
		/*
		System.out.println("step3: auxigraph node : "+auxiG.getNodeCount());
		System.out.println("step3: graph node : "+graph.getNodeCount());
		*/
		CapacitatedKMedian CKM = new CapacitatedKMedian
				(constructor.auxiWeight(auxiG),auxiG.getNodeCount(),parameters_generator.CLOUDLET_NUM,constructor.cloudletCap(step345.minWeightNode(graph)));
		/*
		System.out.println("step3: auxigraph node : "+auxiG.getNodeCount());
		System.out.println("step3: graph node : "+graph.getNodeCount());
		*/
		HashMap<Integer,Integer> initial_allocate = CKM.solve();
		step345 nextstep = new step345();
		nextstep.placeCloudlet(initial_allocate,graph,auxiG);
		HashMap<Integer,int[]> map = nextstep.assignStep5(nextstep.assignStep4(initial_allocate,graph,auxiG), graph, step345.minWeightNode(graph));
		int[] placement = network_topology_generator.findAllCloudletK(graph);
		return new Result(map,placement);
	}
	
	public static void main(String arg[]){
		Graph graph = new SingleGraph("network");
		network_topology_generator.BANetwork(graph);
		network_topology_generator.initApNode(graph);
		network_topology_generator.initApEdge(graph);
    	process_data.readDataFile("G:\\workspace\\infocom\\datatest\\part-00002-of-00500.csv",graph);

    	Alo2 a = new Alo2();
    	Result r = a.run(graph);
    	r.print();
	}

}
