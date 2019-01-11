package appro;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import org.graphstream.algorithm.Dijkstra;
import org.graphstream.graph.*;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.GraphParseException;

import tpds.simulation;

public class parameters_generator {
	
	public static final int AP_NUM = 100;          
	
	public static final int CLOUDLET_NUM = 5;
	
	public static final int TYPE_SUM = 20;    //the total number of service
	
	public static final int CLOUDLET_CAP = 100;  //if cloudlets hava identical capacity
	
	static final int ATTACH_NET = 3;    //generate network,the attachment rate of the network 
	
	public static final double INTERNET_DELAY = 8;    //Internet delay = 0.8
	
	public static final int CONFIG_NUM = 5;      //the number of service for each server
	
	public static double[] INTERNET;
	
    static final double MAXD = 10000;  
	
	static final int MAXI = 10000;
	
	
	public static void delayinternet(){
		
		INTERNET = new double[AP_NUM];
		//Random ran = new Random();
		//double number =0;
		for(int i = 0 ; i < INTERNET.length; i++){
		/*
			number = ran.nextDouble()*0.8+0.2;
			if(number > 0.8)
				INTERNET[i] = number;
			else
				i--;
		*/
			INTERNET[i] = INTERNET_DELAY;
		}	
	}
	
	/*if cloudlet don't have identical capacity
	 * 	input: ----------------------------------------------------------暂时随机生成，后面有需要再改
	 * 	output: an array of cloudlets' capacity, capacity increasingly
	 * */
	public static ArrayList<Double> cloudletCapacity(int cloudletNum, double floor, double ceil){
		
		ArrayList<Double> capacity = new ArrayList<>();
		Random ran = new Random();
		double number = 0;
		for (int i = 0 ; i < cloudletNum ; i++){
			number = ran.nextDouble()*floor+ceil-floor;
			if(number > floor)
				capacity.add(number);
			else
				i--;
		}
		return capacity;	
	}
	
	public static ArrayList<Integer> typeK(int cloudletNum, int floor, int ceil){
		
		ArrayList<Integer> typeK = new ArrayList<>();
		Random ran = new Random();
		for (int i = 0 ; i < cloudletNum ; i++){
			typeK.add(floor+ran.nextInt(ceil-floor));   
		}
		return typeK;
	}
	
	//configure number ~ N(5,2)
			public static int configNumber(){
				int num;
				Random ran = new Random();
				do{
				num = (int)(Math.sqrt(2)*ran.nextGaussian()+5);
				}while(num<3 || num >7);
				return num;
			}
			
		//openning cost ~N(100,50)
			public static double openCost(){
				double num;
				Random ran = new Random();
				do{
				num = Math.sqrt(50)*ran.nextGaussian()+100;
				}while(num < 60 || num > 140);
				return num;
			}
			
		//computation capacity ~N(10,5)
			public static double computeCap1(){
				double num;
				Random ran = new Random();
				num = (int)(Math.sqrt(5)*ran.nextGaussian()+10);
				return num;
			}
			
			
	
	//network delay ~ c
	public static double networkDelay(){
		double netdelay;
		Random ran = new Random();
		do{
			netdelay = Math.sqrt(0.05)*ran.nextGaussian()+0.15;
		}while(netdelay < 0.1 || netdelay > 0.2);
		return netdelay;	
	}
		
	//network delay matrix in term of number of hops and network delay,using Dijkstra's algorithm
	//input: graph
	public static double[][] netDelayMatrix( Graph graph ){
		
		double[][] hop = new double[graph.getNodeCount()][graph.getNodeCount()];
		Dijkstra dijkstra = new Dijkstra(Dijkstra.Element.EDGE, "result","weight");
		dijkstra.init(graph);
		Node node;
		for (int i = 0; i < graph.getNodeCount(); i++){
			node = graph.getNode(i);
			dijkstra.setSource(node);
			dijkstra.compute();
			for(int j = 0; j < graph.getNodeCount(); j++){
				hop[i][j] = dijkstra.getPathLength(graph.getNode(j));
			}
		}
		return hop;	
		}   
	
	public static ArrayList<Double> nodeRequestResource(Graph graph){
		
		ArrayList<Double> resource;
		ArrayList<Double> ressum = new ArrayList<>();
		Node node;
		double res =0;
		for(int i =0; i < graph.getNodeCount();i++){
			node = graph.getNode(i);
			resource = node.getAttribute("resource");
			for(int j = 0 ; j < resource.size() ;j++){
				res += resource.get(j);
			}
			ressum.add(res);	
		}
		return ressum;
	}
	
	public static Graph copy(Graph graph, String name){
		    
		    Graph Algo1 = new SingleGraph(name);
			try {
				Algo1.read(parameters_generator.AP_NUM+"AP_graph_special.dgs");
			} catch (ElementNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (GraphParseException e) {
				e.printStackTrace();
			}
			simulation.preReadGraph(graph, Algo1);
			
			return Algo1;
	}

	public static void write(String filename, String s){
		
		FileWriter writer = null;
		try {
			writer = new FileWriter(filename,true);
			writer.write(s);
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			try {
				if(writer != null)
				 writer.close();
			} catch (IOException e) {
					e.printStackTrace();
			}
		}
		//System.out.println(s);
	}

	public static ArrayList<Double> cost(int apNum){
		
		ArrayList<Double> costlist = new ArrayList<>();
		Random ran = new Random();
		for (int i = 0 ; i < apNum ; i++){
			costlist.add(100*ran.nextDouble());   
		}
		return costlist;
	}
	
	public static void main(String arg[]){
		ArrayList<Integer> type = new ArrayList<Integer>();
		type = typeK(5, 7,8);
		for(int i = 0 ; i < type.size(); i++){
			System.out.println(type.get(i));
		}
	}
}
