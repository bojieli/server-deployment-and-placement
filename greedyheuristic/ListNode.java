package greedyheuristic;

import java.util.Comparator;

public class ListNode implements Comparable<Object>{
	private double value;
	private int key;
	
	public ListNode(){
		value = 0.0;
		key = 0;
	}
	
	public ListNode(double v,int k){
		value = v;
		key = k;
	}
	
	public double getValue(){
		return value;
	}
	
	public int getKey(){
		return key;
	}
	
	public int compareTo(Object o){
		ListNode k = (ListNode) o;
		if(value > k.getValue())
			return 1;
		else if(value < k.getValue())
			return -1;
		else
			return 0;
	}
	
	public static Comparator<ListNode> getComparator(){
		return new Comparator<ListNode>(){
			@Override
			public int compare(ListNode n1,ListNode n2){
				return n1.compareTo(n2);
			}
		};
	}
	
	public static Comparator<ListNode> getDesComparator(){
		return new Comparator<ListNode>(){
			@Override
			public int compare(ListNode n1,ListNode n2){
				return -n1.compareTo(n2);
			}
		};
	}
}