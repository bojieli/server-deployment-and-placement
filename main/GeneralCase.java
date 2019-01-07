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

import appro.*;
import configuration.*;
import greedyheuristic.*;

public class GeneralCase {

	private OutputResult random(Graph graph, ArrayList<Double> capacity, ArrayList<Integer> k, ArrayList<Double> costlist) {
		base_one test = new base_one();
    	test.randomPlace(graph, capacity);
    	test.randomConfig(graph, k);
    	double benefit = test.optimalDispatch(graph);
    	double opencost = 0.0;
    	for(int i = 0; i < test.cloudlet.length; ++i) {
    		opencost = opencost + costlist.get(test.cloudlet[i]);
    	}
    	return new OutputResult(benefit,opencost);
	}

	private OutputResult heuristic(Graph graph, ArrayList<Double> capacity, ArrayList<Integer> k, ArrayList<Double> costlist) {
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
		
		lastopt opt = new lastopt();
		double benefit = opt.lastoptdispatching(graph, placement, capacity, topk.configure().getConfiguration(), cost);
		double opencost = 0.0;
		for(int i : placement) {
    		opencost = opencost + costlist.get(i);
    	}
    	return new OutputResult(benefit,opencost);
	}

	public void run(Graph graph,ArrayList<Double> capacity,ArrayList<Integer> k,ArrayList<Double> costlist,String filename) throws IOException{
		
		/*File f=new File(filename);
        f.createNewFile();
        FileOutputStream fileOutputStream = new FileOutputStream(f,true);
        PrintStream printStream = new PrintStream(fileOutputStream);
        System.setOut(printStream);  */
		
		DateFormat matter =new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");	
		parameters_generator.write(filename,matter.format(new Date())+"\n");
		
        OutputResult a,b;
        parameters_generator.write(filename,"Current Ap Node : " + parameters_generator.AP_NUM + "\tCurrent Cloudlet Node : " +parameters_generator.CLOUDLET_NUM
	       		 +"\tCurrent Service Type : " + parameters_generator.TYPE_SUM+"\n");
		long count = 0;
		for(int i=0;i<graph.getNodeCount();i++){
			 Node node = graph.getNode(i);
			 ArrayList<Integer> list = (ArrayList<Integer>)node.getAttribute("type");
			 count += list.size();
		}

		parameters_generator.delayinternet();
        
        a = random(parameters_generator.copy(graph, "random"),capacity,k,costlist);
        parameters_generator.write(filename,"random benefit:"+a.benefit+"   delay:"+(count*parameters_generator.INTERNET_DELAY-a.benefit)
        	+"	cost:"+a.cost+"\n");
        System.out.println("random finished");
        b = heuristic(parameters_generator.copy(graph, "heuristic"),capacity,k,costlist);
        parameters_generator.write(filename,"heuristic benefit:"+b.benefit+"   delay:"+(count*parameters_generator.INTERNET_DELAY-b.benefit)
        	+"	cost:"+b.cost+"\n");
        System.out.println("heuristic finished");
        
        parameters_generator.write(filename,matter.format(new Date())+"\n\n");
        System.exit(0);
        /*printStream.close();
        fileOutputStream.close();  */
        
	}

	public static void main(String arg[]) { 
		Graph graph = new SingleGraph("network");
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

		GeneralCase gc = new GeneralCase();
		try{
    		ArrayList<Double> capacity = new ArrayList<Double>();
    		for(int i=0;i<parameters_generator.CLOUDLET_NUM;i++)
    			capacity.add((double)parameters_generator.CLOUDLET_CAP);
    		gc.run(graph
    			,capacity
    			,parameters_generator.typeK(parameters_generator.CLOUDLET_NUM, 30,31)
    			,parameters_generator.cost(parameters_generator.AP_NUM)
    			,"GeneralCase");
    	}
    	catch(IOException x){
    		x.printStackTrace();
    	}
	}
}