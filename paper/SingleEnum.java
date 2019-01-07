package paper;

import appro.*;

import java.util.ArrayList;
import java.util.Vector;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;

public class SingleEnum {
	private double[][] cost; // cost[n][n], 0~n-1 denote the AP
	//private ArrayList<Double> w; // the demand resource of each type of request
	private ArrayList<Integer> k; // the most configuring types of each server;
	private ArrayList<Double> capacity; // the capacity of each server;
	private int APNum,serverNum,typeNum;
	private double[] networkdelay;
	private Graph graph;
	
	public SingleEnum(double[][] cost,int typeNum/*,ArrayList<Double> w*/,ArrayList<Integer> k,ArrayList<Double> capacity,double[] networkdelay,Graph graph){
		this.cost = cost;
		//this.w = w;
		this.k = k;
		this.capacity = capacity;
		APNum = cost.length;
		serverNum = k.size();
		this.typeNum = typeNum;
		this.graph = graph;
		this.networkdelay = networkdelay;
	}
	
	public double run(){
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
					ConfigurationandCost oneenum = enumconfigure(i,j,new ArrayList<Integer>(),k.get(j),serverconfig);
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
	
	private ConfigurationandCost enumconfigure(int ap,int server
			,ArrayList<Integer> configuration,int serverK
			,Vector<ConfigurationandPoint> haveconfig){
		if(serverK == configuration.size()){
			
			Vector<ArrayList<Integer>> cloudletconfig = new Vector<ArrayList<Integer>>();
			ArrayList<Double> cloudletCap = new ArrayList<Double>();
			ArrayList<Integer> cloudlet = new ArrayList<Integer>();
			for(int i=0;i<haveconfig.size();i++){
				cloudletconfig.add(haveconfig.get(i).getConfig());
				cloudletCap.add(capacity.get(haveconfig.get(i).getServer()));
				cloudlet.add(haveconfig.get(i).getAP());
			}
			cloudletconfig.add(configuration);
			cloudletCap.add(capacity.get(server));
			cloudlet.add(ap);
			
			maxbenefitmaxflow m = new maxbenefitmaxflow();
			double result = m.mbmf(graph, APNum, serverNum, cloudletconfig, cloudletCap, cloudlet, networkdelay, cost);
			
			return new ConfigurationandCost(result,configuration);
		}
		else{
			ConfigurationandCost max = new ConfigurationandCost();
			for(int i=0;i<typeNum;i++){
				ArrayList<Integer> temp = (ArrayList<Integer>)configuration.clone();
				if(temp.contains(i))
					continue;
				temp.add(i);
				ConfigurationandCost result = enumconfigure(ap,server,temp,serverK,haveconfig);
				if(result.getBenefit() > max.getBenefit()){
					max = result;
				}
			}
			return max;
		}
	}
	
	public static void main(String arg[]){
		Graph graph = new SingleGraph("network");
		network_topology_generator.BANetwork(graph);
		network_topology_generator.initApNode(graph);
		network_topology_generator.initApEdge(graph);
    	process_data.readDataFile("G:\\workspace\\infocom\\datatest\\part-00002-of-00500.csv",graph);
	}
}
