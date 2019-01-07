package appro;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;

public class process_data {
	
	/* read a .csv file according to the line
	 * input : file path + file name
	 * */
	public static void readDataFile(String filename, Graph graph){
		
		int count = 0;
		String[] item = new String[13];
		for(int i = 0 ;i < item.length ; i++)
			item[i] = "";
		try{ 
			BufferedReader reader = new BufferedReader(new FileReader(filename));
            String line = null; 
            while((line = reader.readLine())!=null){	
                String[] item1 = line.split(",");
                for(int i = 0 ;i < item1.length ; i++)
        			item[i] = item1[i];
                process_data.setWeightNode(graph, item);   //
                count++;
            } 
            //System.out.println(count);
            reader.close();
           // System.out.println(Arrays.toString(typeradio));
        } catch (Exception e){e.printStackTrace();}  
	}
	
	/* read a batch file, 
	 * input : the path of the directory
	 * */
	public static void readDataSet(String filepath, Graph graph){
		
		 File file = new File(filepath);
		 if(!file.isDirectory()){
			 if(file.isFile())
				 process_data.readDataFile(file.getName(), graph);
			 else
		         System.out.println("Wrong!");
		 }
		 else if(file.isDirectory()) {
			 String[] filelist = file.list();
			 String readfile;
		     for(int i = 0; i < filelist.length; i++){
		    	 readfile = filepath + "\\" + filelist[i];
		    	 process_data.readDataFile(readfile, graph);
		    	 System.out.println("the " + i + "th file is already read!");
		     }
		 }
		 System.out.println(Arrays.toString(typeradio));
	}
	
	public static int[] typeradio = new int[parameters_generator.TYPE_SUM];
	/* 
	 * */
	public static int setWeightNode(Graph graph, String[] item){
		
		long machine;
		long type;
		double resource;
		Node node;
		ArrayList<Integer> typelist = new ArrayList<>();
		ArrayList<Double> resourcelist = new ArrayList<>();
		
		if((!item[2].equals("")) && (!item[4].equals("")) && (!item[9].equals("")) && (Double.parseDouble(item[9])!= 0)){
			machine = Long.parseLong(item[4]) % parameters_generator.AP_NUM;
			node = graph.getNode((int)machine);
			typelist = node.getAttribute("type",ArrayList.class);     //--------------------
			if(typelist==null)
				typelist = new ArrayList<Integer>();
			type = Long.parseLong(item[2]) % parameters_generator.TYPE_SUM;
			typeradio[(int)type]++;
			typelist.add((int) type);
			node.setAttribute("type", typelist);   //--------------------
			
			resourcelist = node.getAttribute("resource",ArrayList.class);     //--------------------
			if(resourcelist==null)
				resourcelist = new ArrayList<Double>();
			resource = Double.parseDouble(item[9]);
			//System.out.println("m : r   "+(int)machine+":"+resource);
			resourcelist.add(resource);
			node.setAttribute("resource", resourcelist);
			return 1;
		}
		else
			return 0;
	}
	
    public static void main(String[] args) {
    	Graph graph = new SingleGraph("network");
		network_topology_generator.BANetwork(graph);
		network_topology_generator.initApNode(graph);
		network_topology_generator.initApEdge(graph);
    	process_data.readDataFile("G:\\workspace\\infocom\\datatest\\part-00002-of-00500.csv",graph);
		//process_data.readDataSet("G:\\workspace\\infocom\\datatest",graph);
    	
    	/*
    	Node node = graph.getNode(0);
    	ArrayList<Integer> a = new ArrayList<>();
		ArrayList<Double> b = new ArrayList<>();
		a = node.getAttribute("type");
		b = node.getAttribute("resource");
		
		for(int i = 0; i < a.size() ; i++){
			System.out.print(a.get(i)+"\t");
			System.out.println(b.get(i));
		}
		*/
    	
    	for(Node node:graph){
    		ArrayList<Double> a = node.getAttribute("resource");
    		System.out.println(a.size());
    	}

	}
}