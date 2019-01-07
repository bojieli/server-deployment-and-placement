package paper;

import appro.*;
import tpds.simulation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.graphstream.graph.ElementNotFoundException;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.GraphParseException;

public class DoubleEnum {
	private double[][] cost; // cost[n][n], 0~n-1 denote the AP
	//private ArrayList<Double> w; // the demand resource of each type of request
	private ArrayList<Integer> k; // the most configuring types of each server;
	private ArrayList<Double> capacity; // the capacity of each server;
	private int APNum,serverNum,typeNum;
	private double[] networkdelay;
	private Graph graph;
	
	public DoubleEnum(double[][] cost,int typeNum/*,ArrayList<Double> w*/,ArrayList<Integer> k,ArrayList<Double> capacity,double[] networkdelay,Graph graph){
		this.cost = cost;
		//this.w = w;
		this.k = k;
		this.capacity = capacity;
		APNum = cost.length;
		serverNum = k.size();
		this.typeNum = typeNum;
		this.networkdelay = networkdelay;
		this.graph = graph;
	}
	
	public double run(int ThreadsNum){
		boolean[] serverisconfig = new boolean[serverNum];
		for(int i=0;i<serverNum;i++)
			serverisconfig[i] = false;
		boolean[] APisconfig = new boolean[APNum];
		for(int i=0;i<APNum;i++)
			APisconfig[i] = false;
		
		Vector<ConfigurationandPoint> serverconfig = new Vector<ConfigurationandPoint>();
		double benefit = 0.0;
		for(int round=0;round<serverNum;round++){
			ConfigurationandCost max = new ConfigurationandCost();
			int APindex = -1;
			int serverindex = -1;
			for(int i=0;i<APNum;i++){
				if(APisconfig[i])
					continue;
				for(int j=0;j<serverNum;j++){
					if(serverisconfig[j])
						continue;
					ConfigurationandCost oneenum = enumconfigure(i,j,k.get(j),serverconfig,ThreadsNum);
					if(oneenum.getBenefit() >= max.getBenefit()){
						APindex = i;
						serverindex = j;
						max = oneenum;
					}
				}
			}
			serverconfig.add(new ConfigurationandPoint(APindex,serverindex,max.getConfig()));
			APisconfig[APindex] = true;
			serverisconfig[serverindex] = true;
			benefit = max.getBenefit();
		}
		
		return benefit;
	}
	
	private ConfigurationandCost enumconfigure(int ap,int server,int serverK,Vector<ConfigurationandPoint> haveconfig,int ThreadsNum){
		ArrayList<Integer> config = new ArrayList<Integer>();
		Vector<ArrayList<Integer>> cloudletconfig = new Vector<ArrayList<Integer>>();
		ArrayList<Double> cloudletCap = new ArrayList<Double>();
		ArrayList<Integer> cloudlet = new ArrayList<Integer>();
		for(int i=0;i<haveconfig.size();i++){
			cloudletconfig.add(haveconfig.get(i).getConfig());
			cloudletCap.add(capacity.get(haveconfig.get(i).getServer()));
			cloudlet.add(haveconfig.get(i).getAP());
		}	
		cloudletCap.add(capacity.get(server));
		cloudlet.add(ap);
				
		
        
		double result = 0;
		//System.out.println("Number of active threads from the given thread: " + Thread.activeCount());
		for(int i=0;i<serverK;i++){
			double max = -1;
			int index = -1;
			int threads = 0;
			ExecutorService threadPool = Executors.newFixedThreadPool(ThreadsNum);
	        CompletionService<ConfigurationandCost> cs = new ExecutorCompletionService<ConfigurationandCost>(threadPool);
			for(int j=0;j<typeNum;j++){
				if(config.contains(j))
					continue;
				ArrayList<Integer> tmpconfig = (ArrayList<Integer>) config.clone();
				tmpconfig.add(j);
				Vector<ArrayList<Integer>> tmpcloudletconfig = (Vector<ArrayList<Integer>>) cloudletconfig.clone();
				tmpcloudletconfig.addElement(tmpconfig);
				Flow f = new Flow(tmpcloudletconfig,cloudletCap,cloudlet,graph,APNum,networkdelay,cost);
				//
				cs.submit(f);
				threads++;
				/*
				double benefit = flow(cloudletconfig,cloudletCap,cloudlet);
				if(benefit > max){
					max = benefit;
					index = j;
				}
				*/
			}
			
			for(int j=0;j<threads;j++){
				try {
					ConfigurationandCost tmp = cs.take().get(); 
					double benefit = tmp.getBenefit();
					if(benefit > max){
						max = benefit;
						config = tmp.getConfig();
					}
				} catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();
				}
			}
			threadPool.shutdown();
			result = max;
		}
		return new ConfigurationandCost(result,config);
	}
/*	
	private double flow(Vector<ArrayList<Integer>> config,ArrayList<Double> cloudletCap,ArrayList<Integer> cloudlet){
		maxbenefitmaxflow m = new maxbenefitmaxflow();
		double benefit = m.mbmf(graph, APNum, config.size(), config, cloudletCap, cloudlet, networkdelay, cost);
		return benefit;
	}
*/
	/*
	public static void main(String arg[]){

		Graph graph = simulation.Step0();
	    Graph Algo2 = new SingleGraph("Algo1");
		try {
			Algo2.read("primary_graph.dgs");
		} catch (ElementNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (GraphParseException e) {
			e.printStackTrace();
		}
		simulation.preReadGraph(graph, Algo2);
    	
		try {
			simulation.writeFile("Algo2.txt");
		} catch (Exception e) {
			e.printStackTrace();
		}
    	
    	double[][] shortestpath = new double[Algo2.getNodeCount()][Algo2.getNodeCount()];
		shortestpath = parameters_generator.netDelayMatrix(Algo2); 
		//System.out.println("node number : "+ graph.getNodeCount());
		
		Graph augraph = new maxbenefitmaxflow().augraph1(Algo2);
		
		DoubleEnum d = new DoubleEnum(shortestpath,parameters_generator.TYPE_SUM
				,parameters_generator.typeK(parameters_generator.CLOUDLET_NUM, 2, 4)
				,parameters_generator.cloudletCapacity(parameters_generator.CLOUDLET_NUM, 7000, 9000)
				,parameters_generator.INTERNET_DELAY,Algo2);
		System.out.println("benefit:"+d.run());
	
	}
	*/
}

