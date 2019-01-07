package tpds;

import java.util.Arrays;
import java.util.HashMap;

import appro.parameters_generator;

public class Result {
	private HashMap<Integer,int[]> dis;
	private int[] placement;
	
	public Result(HashMap<Integer,int[]> dis,int[] placement){
		this.dis = dis;
		this.placement = placement;
	}
	
	public HashMap<Integer,int[]> getAllocation(){
		return dis;
	}
	
	public int[] getPlacement(){
		return placement;
	}
	
	public void print(){
		System.out.println("cloudlet place:");
		System.out.println(Arrays.toString(placement));
		System.out.println("allocation:");
		for(int i=0;i<parameters_generator.AP_NUM;i++){
			System.out.print(i + ":");
			System.out.println(Arrays.toString(dis.get(i)));
		}
	}

}
