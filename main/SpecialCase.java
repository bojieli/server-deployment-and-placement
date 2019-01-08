package main;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Vector;

//import javax.swing.text.html.HTMLDocument.Iterator;

import org.graphstream.graph.ElementNotFoundException;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.GraphParseException;

import com.alibaba.fastjson.JSONArray;

import appro.*;
import configuration.*;
import extension.*;
import tpds.*;
import greedyheuristic.*;

public class SpecialCase {

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

	private OutputResult approextension(Graph graph, ArrayList<Double> capacity, ArrayList<Integer> k, ArrayList<Double> costlist){
		double[][] cost = parameters_generator.netDelayMatrix(graph);

		// ReadMe: If there are errors, use Alo2() instead.
		//		   Alo2() is appro, instead of extension version.
		//Alo3 a = new Alo3();
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
		double benefit = opt.benefit(topk.configure().getDistribution(), cost, placement, graph);
		double opencost = 0.0;
		for(int i = 0; i < r.getPlacement().length; ++i) {
    		opencost = opencost + costlist.get(r.getPlacement()[i]);
    	}
    	return new OutputResult(benefit,opencost);
	}
    
	//改输入：graph, 第二个是computation capacity, configuration number, openning cost
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
			 ArrayList<Integer> list = str1arr(node.getAttribute("type"));
			// list = node.getAttribute("type");
			 count += list.size();
		}

		parameters_generator.delayinternet();
        
        a = random(parameters_generator.copy(graph, "random"),capacity,k,costlist);
        parameters_generator.write(filename,"random benefit:"+a.benefit+"   delay:"+(count*parameters_generator.INTERNET_DELAY-a.benefit)
        	+"	cost:"+a.cost+"\n");
        System.out.println("random finished");
        b = approextension(parameters_generator.copy(graph, "approextension"),capacity,k,costlist);
        parameters_generator.write(filename,"approextension benefit:"+b.benefit+"   delay:"+(count*parameters_generator.INTERNET_DELAY-b.benefit)
        	+"	cost:"+b.cost+"\n");
        System.out.println("approextension finished");
        
        parameters_generator.write(filename,matter.format(new Date())+"\n\n");
        System.exit(0);
        /*printStream.close();
        fileOutputStream.close();  */
        
	}
	
	public ArrayList<Integer> str1arr(Object input) {
	    JSONArray jsonArray = JSONArray.parseArray(String.valueOf(input));
	    ArrayList<Integer> arrayList = new ArrayList<>();
	    for (Iterator<Object> it = jsonArray.iterator(); it.hasNext(); ) {
	        Integer integer = (Integer) it.next();
	        arrayList.add(integer);
	    }
	    return arrayList;
	}


	public static void main(String arg[]) { 
		Graph graph = new SingleGraph("network");
		try {
			graph.read(parameters_generator.AP_NUM+"AP_graph.dgs");
		} catch (ElementNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (GraphParseException e) {
			e.printStackTrace();
		}
		//process_data.readDataFile("part-00002-of-00500.csv",graph);

		SpecialCase sc = new SpecialCase();
		Node node;
		double cap;
		int config;
		try{
    		ArrayList<Double> capacity = new ArrayList<Double>();
    		ArrayList<Integer> k = new ArrayList<Integer>();
    		for(int i=0;i<parameters_generator.AP_NUM;i++){
    			node = graph.getNode(i);
    			cap = node.getAttribute("capacity");
    			config = node.getAttribute("configNum");
    			capacity.add(cap);
    			k.add(config);
    		}
    		
    		sc.run(graph
    			,capacity
    			,k
    			,parameters_generator.cost(parameters_generator.AP_NUM)
    			,"SpecialCase");
    	}
    	catch(IOException x){
    		x.printStackTrace();
    	}
	}
}