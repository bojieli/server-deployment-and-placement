package kmedian;

import java.util.ArrayList;

public class DynamicFunctionResult {
	private ArrayList<TreeNode> set;
	private double value;
	
	public DynamicFunctionResult(ArrayList<TreeNode> set, double value){
		this.set = set;
		this.value = value;
	}
	
	public ArrayList<TreeNode> getResult(){
		return set;
	}
	
	public double getValue(){
		return value;
	}
}
