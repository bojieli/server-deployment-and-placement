package paper;

import java.util.ArrayList;
import java.util.Vector;
import java.util.concurrent.Callable;

import org.graphstream.graph.Graph;

import appro.maxbenefitmaxflow;

public class Flow implements Callable<ConfigurationandCost>{
	private Vector<ArrayList<Integer>> config;
	private ArrayList<Double> cloudletCap;
	private ArrayList<Integer> cloudlet;
	private Graph graph;
	private int APNum;
	private double[] networkdelay;
	private double[][] cost;
	public Flow(Vector<ArrayList<Integer>> config,ArrayList<Double> cloudletCap,ArrayList<Integer> cloudlet
			,Graph graph,int APNum,double[] networkdelay,double[][] cost){
		this.config = config;
		this.cloudlet = cloudlet;
		this.cloudletCap = cloudletCap;
		this.graph = graph;
		this.APNum = APNum;
		this.networkdelay = networkdelay;
		this.cost = cost;
	}
	@Override
	public ConfigurationandCost call(){
		maxbenefitmaxflow m = new maxbenefitmaxflow();
		double benefit = m.mbmf(graph, APNum, config.size(), config, cloudletCap, cloudlet, networkdelay, cost);
		return new ConfigurationandCost(benefit,config.get(config.size()-1));
	}
}