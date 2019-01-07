package configuration;

import java.util.Comparator;

public class TypeandNum implements Comparable<Object>{
	private int num;
	private int type;
	
	public TypeandNum(){
		num = 0;
		type = 0;
	}
	
	public TypeandNum(int type,int num){
		this.num = num;
		this.type = type;
	}
	
	public int getNum(){
		return num;
	}
	
	public int getType(){
		return type;
	}
	
	public int compareTo(Object o){
		TypeandNum k = (TypeandNum) o;
		return num - k.getNum();
	}
	
	public static Comparator<TypeandNum> getComparator(){
		return new Comparator<TypeandNum>(){
			@Override
			public int compare(TypeandNum n1,TypeandNum n2){
				return n1.compareTo(n2);
			}
		};
	}
	
	public static Comparator<TypeandNum> getDesComparator(){
		return new Comparator<TypeandNum>(){
			@Override
			public int compare(TypeandNum n1,TypeandNum n2){
				return -n1.compareTo(n2);
			}
		};
	}

}
