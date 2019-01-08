package tpds;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;

import org.graphstream.graph.ElementNotFoundException;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.GraphParseException;

import com.alibaba.fastjson.JSONArray;

import appro.network_topology_generator;
import appro.parameters_generator;
import appro.process_data;

public class simulation {

	public static Graph Step0() {
			
		Graph graph = new SingleGraph("network");
		network_topology_generator.BANetwork(graph);
		network_topology_generator.initApNode(graph);
		network_topology_generator.initApEdge(graph);
    	process_data.readDataFile("part-00002-of-00500.csv",graph);
    	//process_data.readDataSet("G:\\workspace\\infocom\\datatest",graph);
		try {
			graph.write("primary_graph.dgs");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return graph;	
    }
    
    public static void preReadGraph(Graph graph, Graph new_graph){
    	Node node;
    	for(int i = 0; i < graph.getNodeCount(); i++){
    		readNode(i, graph, new_graph);	
    	}
    }

    private static void readNode(int nodeindex, Graph graph, Graph new_graph){
	
    	ArrayList<Double> resource1 = new ArrayList<>();
    	ArrayList<Double> resource2 = new ArrayList<>();
    	ArrayList<Integer> type1 = new ArrayList<>();
    	ArrayList<Integer> type2 = new ArrayList<>();
    	ArrayList<Integer> config1 = new ArrayList<>();
    	ArrayList<Integer> config2 = new ArrayList<>();
    	Node node1,node2;
    	node1 = graph.getNode(nodeindex);
    	resource1 = str2arr(node1.getAttribute("resource"));
    	if(resource1==null)
    		resource1 = new ArrayList<Double>();
    	resource2 =(ArrayList<Double>)resource1.clone();
    	type1 = str1arr(node1.getAttribute("type"));
    	if(type1 == null)
    		type1 = new ArrayList<Integer>();
    	type2 = (ArrayList<Integer>)type1.clone();
    	config1 = str1arr(node1.getAttribute("configuration"));
    	if(config1 == null)
    		config1 = new ArrayList<Integer>();
    	if(config1 == null)
    		config2 = null;
    	else
    		config2 = (ArrayList<Integer>)config1.clone();
	
    	node2 = new_graph.getNode(nodeindex);
    	node2.setAttribute("resource", resource2);
    	node2.setAttribute("type", type2); 
    	node2.setAttribute("configuration", config2);  
	
    }
    
    public static ArrayList<Integer> str1arr(Object input) {
	    JSONArray jsonArray = JSONArray.parseArray(String.valueOf(input));
	    ArrayList<Integer> arrayList = new ArrayList<>();
	    for (Iterator<Object> it = jsonArray.iterator(); it.hasNext(); ) {
	        Integer integer = (Integer) it.next();
	        arrayList.add(integer);
	    }
	    return arrayList;
	}
    
    public static ArrayList<Double> str2arr(Object input) {
    	   JSONArray jsonArray = JSONArray.parseArray(String.valueOf(input));
    	   ArrayList<Double> arrayList = new ArrayList<>();
    	   for (Iterator<Object> it = jsonArray.iterator(); it.hasNext(); ) {
    	      Double aDouble =  ((BigDecimal)it.next()).doubleValue();
    	      arrayList.add(aDouble);
    	   }
    	   return arrayList;
    	}

    
     public static void writeFile(String filename) throws IOException {
    	 
         File f=new File(filename);
         f.createNewFile();
         FileOutputStream fileOutputStream = new FileOutputStream(f,true);
         PrintStream printStream = new PrintStream(fileOutputStream);
         System.setOut(printStream);
         
         System.out.println("Current Ap Node : " + parameters_generator.AP_NUM + "\tCurrent Cloudlet Node : " +parameters_generator.CLOUDLET_NUM
        		 +"\tCurrent Service Type : " + parameters_generator.TYPE_SUM);
     }
    

}
