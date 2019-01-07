package kmedian;

import java.util.ArrayList;

public class DynamicForest {
	private ArrayList<TreeNode> forest;
	private double[][] d;
	private int k;
	
	public DynamicForest(ArrayList<TreeNode> forest,double[][] d,int k){
		this.forest = forest;
		this.d = d;
		this.k = k;
	}
	
	public DynamicFunctionResult dynamicRun(){
		if(k <= 0){
			return new DynamicFunctionResult(new ArrayList<TreeNode>(),Double.MAX_VALUE);
		}
		
		TreeNode node = forest.get(0);
		DynamicFunctionResult result = new DynamicTreeGraph(d,1,node).dynamicRun();
		if(forest.size() == 1){
			return result;
		}
		
		ArrayList<TreeNode> temp = new ArrayList<TreeNode>();
		temp.addAll(forest);
		temp.remove(0);
		if(k == 1){
			for(int i=0;i<temp.size();i++){
				DynamicFunctionResult result2 = new DynamicTreeGraph(d,1,temp.remove(0)).dynamicRun();
				if(result.getValue() > result2.getValue())
					result = result2;
			}
			return result;
		}
		
		DynamicFunctionResult subresult = new DynamicForest(temp,d,k-1).dynamicRun();
		for(int i=2;i<=node.length();i++){
			DynamicFunctionResult result2,subresult2;
			result2 = new DynamicTreeGraph(d,i,node).dynamicRun();
			subresult2 = new DynamicForest(temp,d,k-i).dynamicRun();
			if(result.getValue()+subresult2.getValue() > result2.getValue()+subresult2.getValue()){
				result = result2;
				subresult = subresult2;
			}
		}
		
		ArrayList<TreeNode> set = new ArrayList<TreeNode>();
		set.addAll(result.getResult());
		set.addAll(subresult.getResult());
		
		return new DynamicFunctionResult(set,result.getValue()+subresult.getValue());
		
	}
	

}
