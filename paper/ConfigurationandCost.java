package paper;

import java.util.ArrayList;

public class ConfigurationandCost {
	private double benefit;
	private ArrayList<Integer> configuration;
	
	public ConfigurationandCost(double benefit,ArrayList<Integer> configuration){
		this.benefit = benefit;
		this.configuration = configuration;
	}
	
	public ConfigurationandCost(){
		benefit = -1.0;
		configuration = new ArrayList<Integer>();
	}
	
	public double getBenefit(){
		return benefit;
	}
	
	public ArrayList<Integer> getConfig(){
		return configuration;
	}
}
