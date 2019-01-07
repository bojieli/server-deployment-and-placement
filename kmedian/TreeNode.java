package kmedian;

import java.util.ArrayList;

public class TreeNode {
	private int root;
	private TreeNode child;
	private TreeNode parent;
	public ArrayList<TreeNode> L;
	public double pr,rc;
	
	public TreeNode(int root, TreeNode parent, double pr){
		this.root = root;
		child = null;
		this.parent = parent;
		L = null;
		this.pr = pr;
		rc = Double.MAX_VALUE;
	}
	
	
	public boolean addChild(TreeNode c,  double rc){
		if(child == null){
			child = c;
			this.rc = rc;
			return true;
		}
		else
			return false;
	}
	
	public TreeNode getParent(){
		return parent;
	}
	
	public int getRoot(){
		return root;
	}
	
	public void removeChild(){
		child = null;
		rc = Double.MAX_VALUE;
	}
	
	public TreeNode getChild(){
		return child;
	}
	
	public void buildL(){
		if(L != null)
			return;
		TreeNode up = parent;
		TreeNode down = child;
		double upc,doc;
		
		L = new ArrayList<TreeNode>();
		L.add(this);
		while(up != null && down != null){
			upc = up.rc;
			doc = down.pr;
			if(upc < doc){
				L.add(up);
				up = up.getParent();
			}
			else{
				L.add(down);
				down = down.getChild();
			}
		}
		if(up != null){
			L.add(up);
			up = up.getParent();
		}
		if(down != null){
			L.add(down);
			down = down.getChild();
		}		
	}
	
	public int search(TreeNode v){
		if(L != null){
			int i;
			for(i=0;i<L.size();i++){
				if(L.get(i).getRoot() == v.getRoot())
					break;
			}
			return i + 1;
		}
		return 0;
	}
	
	public boolean isDescendant(TreeNode v){
		TreeNode c = child;
		while(c != null){
			if(c.getRoot() == v.getRoot())
				return true;
			c = c.getChild();
		}
		return false;
	}
	
	public int length(){
		int len = 0;
		TreeNode no = this;
		while(no != null){
			no = no.getChild();
			len++;
		}
		return len;
	}
	
}

