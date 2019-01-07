package extension;

import java.util.ArrayList;
import java.util.Vector;

public class Preliminary {
	private Vector<ArrayList<Integer>> virtual_request;
	private double min;
	
	/*
	 *  n is AP_NUM
	 */
	public Preliminary(int n,Vector<ArrayList<Double>> resources){
		min = Double.MAX_VALUE;
		virtual_request = new Vector<ArrayList<Integer>>();
		for(ArrayList<Double> list:resources){
			for(double request:list){
				if(request < min)
					min = request;
			}
		}
		for(int index = 0;index < n;index++){
			ArrayList<Double> resource = resources.get(index);
			virtual_request.add(new ArrayList<Integer>());
			for(double request:resource)
				virtual_request.get(index).add((int)Math.ceil(request/min));
		}
		
	}
	
	public Vector<ArrayList<Integer>> getVRequest(){
		return virtual_request;
	}
	
	public double getMin(){
		return min;
	}
	
}
