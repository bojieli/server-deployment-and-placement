package main;

import greedyheuristic.Heuristic;
import greedyheuristic.lastopt;
import greedyheuristic.optdispaching;
import paper.DoubleEnum;
import tpds.simulation;
import appro.base_one;
import appro.maxbenefitmaxflow;
import appro.network_topology_generator;
import appro.parameters_generator;
import appro.process_data;
import configuration.TopK;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;

import org.graphstream.graph.ElementNotFoundException;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.GraphParseException;
import java.text.*;

public class Exp1 {
	
	private double caseA(Graph graph,ArrayList<Double> capacity,ArrayList<Integer> k){
		base_one test = new base_one();
    	test.randomPlace(graph, capacity);
    	test.randomConfig(graph, k);
    	return test.optimalDispatch(graph);
	}
	
	private double caseB(Graph graph,ArrayList<Double> capacity,ArrayList<Integer> k){
		double[][] cost = parameters_generator.netDelayMatrix(graph);
		Heuristic heu = new Heuristic(cost,capacity,parameters_generator.nodeRequestResource(graph),graph.getNodeCount(),capacity.size());
		Vector<Integer> placement = heu.run();
		optdispaching optdis = new optdispaching();
		HashMap<Integer,int[]> distribution = optdis.heuristicdispaching(graph, placement, capacity, cost);
		
		Vector<ArrayList<Integer>> types = new Vector<ArrayList<Integer>>();
		Vector<ArrayList<Double>> resources = new Vector<ArrayList<Double>>();
		for(Node node:graph){
			types.add((ArrayList<Integer>)node.getAttribute("type"));
			resources.add((ArrayList<Double>)node.getAttribute("resource"));
		}
		TopK topk = new TopK(types,resources,distribution,k,parameters_generator.CLOUDLET_NUM,parameters_generator.TYPE_SUM,capacity);
		return optdis.benefit(topk.configure().getDistribution(), cost, placement, graph);
	}
	
	private double caseC(Graph graph,ArrayList<Double> capacity,ArrayList<Integer> k){
		
		double[][] cost = parameters_generator.netDelayMatrix(graph);
		Heuristic heu = new Heuristic(cost,capacity,parameters_generator.nodeRequestResource(graph),graph.getNodeCount(),capacity.size());
		Vector<Integer> placement = heu.run();
		optdispaching optdis = new optdispaching();
		HashMap<Integer,int[]> distribution = optdis.heuristicdispaching(graph, placement, capacity, cost);
		
		Vector<ArrayList<Integer>> types = new Vector<ArrayList<Integer>>();
		Vector<ArrayList<Double>> resources = new Vector<ArrayList<Double>>();
		for(Node node:graph){
			types.add((ArrayList<Integer>)node.getAttribute("type"));
			resources.add((ArrayList<Double>)node.getAttribute("resource"));
		}
		TopK topk = new TopK(types,resources,distribution,k,parameters_generator.CLOUDLET_NUM,parameters_generator.TYPE_SUM,capacity);
		
		return new lastopt().lastoptdispatching(graph, placement, capacity, topk.configure().getConfiguration(), cost);
	}
	
	private double caseD(Graph graph,ArrayList<Double> capacity,ArrayList<Integer> k, int ThreadsNum){
		double[][] cost = parameters_generator.netDelayMatrix(graph);
		Graph augraph = new maxbenefitmaxflow().augraph1(graph);
		DoubleEnum bigK = new DoubleEnum(cost
				,parameters_generator.TYPE_SUM
				,k,capacity,parameters_generator.INTERNET,graph);
		return bigK.run(ThreadsNum);
	}

	public void run(Graph graph,ArrayList<Double> capacity,ArrayList<Integer> k,String filename, int ThreadsNum) throws IOException{
		/*
		 File f=new File(filename);
         f.createNewFile();
         FileOutputStream fileOutputStream = new FileOutputStream(f,true);
         PrintStream printStream = new PrintStream(fileOutputStream);
         System.setOut(printStream);
		*/
		DateFormat matter =new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");	
		parameters_generator.write(filename,matter.format(new Date())+"\n");
		
		parameters_generator.write(filename, "Current Ap Node : " + parameters_generator.AP_NUM + "\tCurrent Cloudlet Node : " +parameters_generator.CLOUDLET_NUM
	       		 +"\tCurrent Service Type : " + parameters_generator.TYPE_SUM +"\n");
		 
		 long count = 0;
		 for(int i=0;i<graph.getNodeCount();i++){
			 Node node = graph.getNode(i);
			 ArrayList<Integer> list = node.getAttribute("type",ArrayList.class);
			 if(list == null)
				 list = new ArrayList<Integer>();
			 count += list.size();
		 }
		 
		double a=0, b=0, c=0, d=0;
         try{
	         System.out.println("Start all cases");
	         a = caseA(parameters_generator.copy(graph, "caseA"),capacity,k);
	         parameters_generator.write(filename,"case A benefit:"+a+"   delay:"+(count*parameters_generator.INTERNET_DELAY-a)+"\n");
	         System.out.println("Case A finished");
	         b = caseB(parameters_generator.copy(graph, "caseB"),capacity,k);
	         parameters_generator.write(filename,"case B benefit:"+b+"   delay:"+(count*parameters_generator.INTERNET_DELAY-b)+"\n");
	         System.out.println("Case B finished");
	         c = caseC(parameters_generator.copy(graph, "caseC"),capacity,k);
	         parameters_generator.write(filename,"case C benefit:"+c+"   delay:"+(count*parameters_generator.INTERNET_DELAY-c)+"\n");
	         System.out.println("Case C finished");
	         d = caseD(parameters_generator.copy(graph, "caseD"),capacity,k,ThreadsNum);
	         parameters_generator.write(filename,"case D benefit:"+d+"   delay:"+(count*parameters_generator.INTERNET_DELAY-d)+"\n"); 
	         System.out.println("Case D finished");
	         
	         parameters_generator.write(filename,matter.format(new Date())+"\n\n");
	         System.exit(0);
         }catch(Exception e){
        	 e.printStackTrace();
         }
         
         
         /*
         printStream.close();
         fileOutputStream.close();*/
	}
	
	public static void main(String arg[]){
		
		Graph graph = new SingleGraph("ordinary_graph");
/*		network_topology_generator.BANetwork(graph);
		network_topology_generator.initApNode(graph);
		network_topology_generator.initApEdge(graph);
    	process_data.readDataFile("part-00002-of-00500.csv",graph);
    	try {
			graph.write("ordinary_graph.dgs");
		} catch (IOException e) {
			e.printStackTrace();
		} 
*/	
		try {
			graph.read("ordinary_graph.dgs");
		} catch (ElementNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (GraphParseException e) {
			e.printStackTrace();
		}
		process_data.readDataFile("part-00002-of-00500.csv",graph);
		
		parameters_generator.delayinternet();
		//System.out.println(Arrays.toString(parameters_generator.INTERNET));
		//System.exit(0);
		
    	Exp1 e = new Exp1();
    	try{
    	e.run(graph
    			,parameters_generator.cloudletCapacity(parameters_generator.CLOUDLET_NUM, 7000, 10000)
    			,parameters_generator.typeK(parameters_generator.CLOUDLET_NUM, 4, 15),"k",40);
    	}
    	catch(IOException x){
    		x.printStackTrace();
    	}       
	}       

}
