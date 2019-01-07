package kmedian;

import java.util.ArrayList;

public class DynamicTreeGraph {
	private double[][] d;
	private int p;
	private TreeNode node;
	
	public DynamicTreeGraph(double[][] d, int p,TreeNode node){
		this.d = d;
		this.p = p;
		this.node = node;
	}
	
	public DynamicFunctionResult dynamicRun(){
		TreeNode n = node;
		while(n != null){
			n.buildL();
			n = n.getChild();
		}
		return G(node,p,node.L.size());
	}
	
	private DynamicFunctionResult G(TreeNode v,int q,int i){
		if(v.getChild() == null){
			// v is a leaf
			ArrayList<TreeNode> set = new ArrayList<TreeNode>();
			set.add(v);
			return new DynamicFunctionResult(set,0);
		}
		else{
			if(i == 1){
				DynamicFunctionResult f1,f2;
				TreeNode c = v.getChild();
				int location = c.search(v);
				f1 = F(v.getChild(),q,location);
				f2 = F(v.getChild(),q-1,location);
				
				double value;
				ArrayList<TreeNode> set = new ArrayList<TreeNode>();
				set.add(v);
				if(f1.getValue()<f2.getValue()){
					value = f1.getValue();
					set.addAll(f1.getResult());
				}
				else{
					value = f2.getValue();
					set.addAll(f2.getResult());
				}
				return new DynamicFunctionResult(set,value);
			}
			else{ // i != 1
				if(!v.isDescendant(v.L.get(i-1))){
					return G(v,q,i-1);
				}
				else{
					DynamicFunctionResult g = G(v,q,i-1);
					TreeNode vij = v.L.get(i-1);
					double fij = d[v.getRoot()][vij.getRoot()];
					if(g.getValue() <= fij){
						return g;
					}
					else{
						DynamicFunctionResult g1,g2;
						int location = v.getChild().search(vij);
						g1 = G(v.getChild(),q,location);
						g2 = G(v.getChild(),q-1,location);
						if(g1.getValue() < g2.getValue() && g1.getValue()+2*fij < g.getValue()){
							return new DynamicFunctionResult(g1.getResult(),g1.getValue()+2*fij);
						}
						else if(g2.getValue() <= g1.getValue() && g2.getValue()+2*fij < g.getValue()){
							return new DynamicFunctionResult(g2.getResult(),g2.getValue()+2*fij);
						}
						else{
							return g;
						}
					}
				}
			}
		}
	}
	
	private DynamicFunctionResult F(TreeNode v,int q,int i){
		if(v.getChild() == null){
			// v is a leaf
			if(q <= 0){
				return new DynamicFunctionResult(new ArrayList<TreeNode>(),d[v.getRoot()][v.L.get(i-1).getRoot()]);
			}
			else{
				DynamicFunctionResult f,g;
				f = F(v,0,i);
				g = G(v,1,i);
				if(f.getValue()<g.getValue())
					return f;
				else
					return g;
			}
		}
		else{
			DynamicFunctionResult g = G(v,q,i);
			TreeNode vij = v.L.get(i-1);
			double fij = d[v.getRoot()][vij.getRoot()];
			if(g.getValue() <= fij){
				return g;
			}
			else{
				DynamicFunctionResult f1,f2;
				int location = v.getChild().search(vij);
				f1 = F(v.getChild(),q,location);
				f2 = F(v.getChild(),q-1,location);
				if(f1.getValue() < f2.getValue() && f1.getValue()+2*fij < g.getValue()){
					return new DynamicFunctionResult(f1.getResult(),f1.getValue()+2*fij);
				}
				else if(f2.getValue() <= f1.getValue() && f2.getValue()+2*fij < g.getValue()){
					return new DynamicFunctionResult(f2.getResult(),f2.getValue()+2*fij);
				}
				else{
					return g;
				}
			}
		}
	}
	
}
