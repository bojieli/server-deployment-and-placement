package main;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Vector;

import org.graphstream.graph.ElementNotFoundException;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.GraphParseException;

import appro.maxbenefitmaxflow;
import appro.network_topology_generator;
import appro.parameters_generator;
import appro.process_data;
import configuration.TopK;
import extension.Alo3;
import greedyheuristic.optdispaching;
import paper.DoubleEnum;
import tpds.Alo2;
import tpds.Result;

public class Exp2 {
	private double caseA(Graph graph,ArrayList<Double> capacity,ArrayList<Integer> k){
		double[][] cost = parameters_generator.netDelayMatrix(graph);
		
		Alo2 a = new Alo2();
		Result r = a.run(graph);
		
		Vector<ArrayList<Integer>> types = new Vector<ArrayList<Integer>>();
		Vector<ArrayList<Double>> resources = new Vector<ArrayList<Double>>();
		for(Node node:graph){
			types.add((ArrayList<Integer>)node.getAttribute("type"));
			resources.add((ArrayList<Double>)node.getAttribute("resource"));
		}
		
		Vector<Integer> placement = new Vector<Integer>();
		for(int i:r.getPlacement()){
			placement.add(i);
		}
		
		optdispaching opt = new optdispaching();
				
		TopK topk = new TopK(types,resources,opt.translation(r.getAllocation(), graph),k,parameters_generator.CLOUDLET_NUM,parameters_generator.TYPE_SUM,capacity);
		return opt.benefit(topk.configure().getDistribution(), cost, placement, graph);
	}
	
	private double caseB(Graph graph,ArrayList<Double> capacity,ArrayList<Integer> k,int ThreadsNum){
		double[][] cost = parameters_generator.netDelayMatrix(graph);
		Graph augraph = new maxbenefitmaxflow().augraph1(graph);
		DoubleEnum bigK = new DoubleEnum(cost
				,parameters_generator.TYPE_SUM
				,k,capacity,parameters_generator.INTERNET,graph);
		return bigK.run(ThreadsNum);
	}
	
	public void run(Graph graph,ArrayList<Double> capacity,ArrayList<Integer> k,String filename,int ThreadsNum) throws IOException{
		
		/*File f=new File(filename);
        f.createNewFile();
        FileOutputStream fileOutputStream = new FileOutputStream(f,true);
        PrintStream printStream = new PrintStream(fileOutputStream);
        System.setOut(printStream);  */
		
		DateFormat matter =new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");	
		parameters_generator.write(filename,matter.format(new Date())+"\n");
		
        double a,b;
        parameters_generator.write(filename,"Current Ap Node : " + parameters_generator.AP_NUM + "\tCurrent Cloudlet Node : " +parameters_generator.CLOUDLET_NUM
	       		 +"\tCurrent Service Type : " + parameters_generator.TYPE_SUM+"\n");
		long count = 0;
		for(int i=0;i<graph.getNodeCount();i++){
			 Node node = graph.getNode(i);
			 ArrayList<Integer> list = (ArrayList<Integer>)node.getAttribute("type");
			 count += list.size();
		}
        
        a = caseA(parameters_generator.copy(graph, "caseA"),capacity,k);
        parameters_generator.write(filename,"case A benefit:"+a+"   delay:"+(count*parameters_generator.INTERNET_DELAY-a)+"\n");
        System.out.println("Case A finished");
        b = caseB(parameters_generator.copy(graph, "caseB"),capacity,k,ThreadsNum);
        parameters_generator.write(filename,"case B benefit:"+b+"   delay:"+(count*parameters_generator.INTERNET_DELAY-b)+"\n");
        System.out.println("Case B finished");
        
        parameters_generator.write(filename,matter.format(new Date())+"\n\n");
        System.exit(0);
        /*printStream.close();
        fileOutputStream.close();  */
        
	}
	
	public static void main(String arg[]){
		Graph graph = new SingleGraph("network");
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
   	
   	
    	Exp2 e = new Exp2();
    	try{
    	ArrayList<Double> capacity = new ArrayList<Double>();
    	for(int i=0;i<parameters_generator.CLOUDLET_NUM;i++)
    		capacity.add((double)parameters_generator.CLOUDLET_CAP);
    	e.run(graph
    			,capacity
    			,parameters_generator.typeK(parameters_generator.CLOUDLET_NUM, 30,31),"K30",40);
    	}
    	catch(IOException x){
    		x.printStackTrace();
    	}
	}
}
