package configuration;

import java.util.HashMap;
import java.util.Vector;

public class ConfigandDisResult {
	private HashMap<Integer,int[]> distribution;
	private Vector<int[]> configuration;
	
	public ConfigandDisResult(HashMap<Integer,int[]> distribution,Vector<int[]> configuration){
		this.distribution = distribution;
		this.configuration = configuration;
	}
	
	public HashMap<Integer,int[]> getDistribution(){
		return distribution;
	}
	
	public Vector<int[]> getConfiguration(){
		return configuration;
	}
}
