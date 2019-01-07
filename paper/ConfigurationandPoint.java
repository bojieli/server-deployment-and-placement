package paper;

import java.util.ArrayList;

public class ConfigurationandPoint {
	private int ap;
	private int server;
	private ArrayList<Integer> configuration;
	
	public ConfigurationandPoint(int ap,int server,ArrayList<Integer> configuration){
		this.ap = ap;
		this.server = server;
		this.configuration = configuration;
	}
	
	public int getAP(){
		return ap;
	}
	
	public int getServer(){
		return server;
	}
	
	public ArrayList<Integer> getConfig(){
		return configuration;
	}
}
