package kmedian;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.linear.LinearConstraint;
import org.apache.commons.math3.optim.linear.LinearConstraintSet;
import org.apache.commons.math3.optim.linear.LinearObjectiveFunction;
import org.apache.commons.math3.optim.linear.Relationship;
import org.apache.commons.math3.optim.linear.SimplexSolver;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.linear.OpenMapRealVector;
import org.apache.commons.math3.optim.linear.NonNegativeConstraint;

public class CapacitatedKMedian {
	private double c[][];
	private int n,k,m;
	private double totalcost;

	public CapacitatedKMedian(double c[][],int n,int k,int m){
		this.c = c.clone();
		for(int i=0;i<n;i++){
			this.c[i] = c[i].clone();
		}
		this.n = n;
		this.k = k;
		this.m = m;
		totalcost = -1;
	}
	
	public double getCost(){
		return totalcost;
	}
	
	private PointValuePair runInitialOptimizationProblem(){
		// describe the optimization problem: minimize c[i][j]*x[i][j]+0*y[i]
		
		// initialize coefficients 
		int length = n*n+n;
		double [] cof = new double[length];
		int index = 0;
		for(double[] x:c)
			for(double y:x)
				cof[index++] = y;
		for(int i=0;i<n;i++)
			cof[index++]=0;

		LinearObjectiveFunction f = new LinearObjectiveFunction(cof,0);
		
		// describe the constrains
		ArrayList<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
		//(2)
		for(int i=0;i<n;i++){
			OpenMapRealVector SparseVector = new OpenMapRealVector(length);
			for(int j=0;j<n;j++)
				SparseVector.setEntry(i+j*n, 1);
			constraints.add(new LinearConstraint(SparseVector, Relationship.EQ, 1));
		}
		//(3)
		for(int i=0;i<n;i++)
			for(int j=0;j<n;j++){
				OpenMapRealVector SparseVector = new OpenMapRealVector(length);
				SparseVector.setEntry(i*n+j,1);
				SparseVector.setEntry(n*n+i,-1);
				constraints.add(new LinearConstraint(SparseVector, Relationship.LEQ, 0));
			}
		//(4)
		OpenMapRealVector SparseVector4 = new OpenMapRealVector(length);
		for(int i=0;i<n;i++)
			SparseVector4.setEntry(n*n+i, 1);
		constraints.add(new LinearConstraint(SparseVector4, Relationship.LEQ, k));
		//(7)(8)
		/*
		for(int i=0;i<n*n+n;i++){
			OpenMapRealVector SparseVector = new OpenMapRealVector(length);
			SparseVector.setEntry(i,1);
			constraints.add(new LinearConstraint(SparseVector, Relationship.LEQ, 1));
			constraints.add(new LinearConstraint(SparseVector, Relationship.GEQ, 0));
		}
		*/
		// capacity constrain
		for(int i=0;i<n;i++){
			OpenMapRealVector SparseVector = new OpenMapRealVector(length);
			for(int j=0;j<n;j++)
				SparseVector.setEntry(i*n+j, 1);
			SparseVector.setEntry(n*n+i, -m);
			constraints.add(new LinearConstraint(SparseVector, Relationship.LEQ, 0));
		}
		
		
		// create and run solver
		PointValuePair solution = null;
		try{
			solution = new SimplexSolver().optimize(f, new LinearConstraintSet(constraints), GoalType.MINIMIZE,
					new NonNegativeConstraint(true));
		}
		catch(Exception e){
			e.printStackTrace();
		}
        return solution;
	}

	private PointValuePair runSecondOptimizationProblem(int[] center){
		// describe the optimization problem: minimize cost[i][j]*x[i][j]
		
		// modify cost
		int mm = center.length;
		double[][] cost = new double[mm][n];
		for(int i=0;i<mm;i++)
			for(int j=0;j<n;j++)
				cost[i][j] = c[center[i]][j];
		
	    // initialize coefficients 
	    int length = mm*n;
	    double [] cof = new double[length];
	    int index = 0;
	    for(double[] x:cost)
	    	for(double y:x)
	    		cof[index++] = y;
        
	    LinearObjectiveFunction f = new LinearObjectiveFunction(cof,0);
	    
	    // describe the constrains
	    ArrayList<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
	    //(2)
	    for(int i=0;i<n;i++){
	    	OpenMapRealVector SparseVector = new OpenMapRealVector(length);
	    	for(int j=0;j<mm;j++)
	    		SparseVector.setEntry(i+j*n, 1);
	    	constraints.add(new LinearConstraint(SparseVector, Relationship.EQ, 1));
	    }
	    // capacity constrain
	    for(int i=0;i<mm;i++){
	    	OpenMapRealVector SparseVector = new OpenMapRealVector(length);
	    	for(int j=0;j<n;j++)
	    		SparseVector.setEntry(i*n+j, 1);
	    	constraints.add(new LinearConstraint(SparseVector, Relationship.LEQ, m));
	    }
	    //(7)(8)
	    /*
	    for(int i=0;i<n*n+n;i++){
	    	OpenMapRealVector SparseVector = new OpenMapRealVector(length);
	    	SparseVector.setEntry(i,1);
	    	constraints.add(new LinearConstraint(SparseVector, Relationship.GEQ, 0));
	    }
	    */
	    
	    // create and run solver
	    PointValuePair solution = null;
	    try{
	    	solution = new SimplexSolver().optimize(f, new LinearConstraintSet(constraints), GoalType.MINIMIZE,
	    			new NonNegativeConstraint(true));
	    }
	    catch(Exception e){
	    	e.printStackTrace();
	    }
	        return solution;
	}
	
	private Step1Result step1(PointValuePair value){
		// virtual step1
		double[] location_c = new double[n];
		for(int j=0;j<n;j++)
			for(int i=0;i<n;i++)
				location_c[j] += c[i][j]*value.getPoint()[i*n+j]; 
		
		double[] d = new double[n];
		for(int i=0;i<n;i++)
			d[i] = 1;
		for(int j=0;j<n;j++)
			for(int i=0;i<j;i++)
				if(d[i]>0&&c[i][j]<=4*location_c[j]){
					d[i] = d[i] + d[j];
					d[j] = 0;
					break;
				}
		ArrayList<Integer> N = new ArrayList<Integer>();
		for(int j=0;j<n;j++)
			if(d[j]>0)
				N.add(j);
		
		return new Step1Result(d,N);
	}
	
	private int[] step2(Step1Result s1r){
		int[] N = new int[s1r.N.size()];
		double[] dc = new double[s1r.N.size()];
		for(int i=0;i<s1r.N.size();i++){
			int index = s1r.N.get(i);
			dc[i] = s1r.d[index]*c[closest(index,s1r.N)][index];
		}
		for(int i=0;i<s1r.N.size();i++)
			N[i] = s1r.N.get(i);
		for(int i=0;i<s1r.N.size()/2;i++){
			double max = -Double.MAX_VALUE;
			int maxindex = 0;
			for(int j=s1r.N.size()-1;j>=i;j--)
				if(dc[j]>max){
					max = dc[j];
					maxindex = j;
				}
			double temp = dc[i];
			dc[i] = dc[maxindex];
			dc[maxindex] = temp;
			
			int tempindex = N[i];
			N[i] = N[maxindex];
			N[maxindex] = tempindex;
		}
		
		return N;
	}
	
	private int[] step3(int [] candidateCenter){
		int[] center = new int[k];
		
		// selected center
		for(int i=0;i<candidateCenter.length/2;i++)
			center[i] = candidateCenter[i];
		
		// construct the collection of candidate center
		ArrayList<Integer> N = new ArrayList<Integer>();
		for(int i=0;i<n;i++){
			N.add(i);
		}
		
		// construct forest
		HashMap<Integer,TreeNode> hp = new HashMap<Integer,TreeNode>();
		ArrayList<TreeNode> forest = new ArrayList<TreeNode>();
		for(int i=candidateCenter.length/2;i<candidateCenter.length;i++){
			int index = candidateCenter[i];
			int closestindex = closest(index,N);
		
			TreeNode node;
			if(hp.containsKey(index)){
				node = hp.get(index);
			}
			else{
				node = new TreeNode(index,null,0);
			}
			TreeNode parent = node.getParent();
			if(parent==null || parent.getRoot()!=closestindex){
				TreeNode child = new TreeNode(closestindex,node,c[index][closestindex]);
				node.addChild(child,c[closestindex][index]);
				hp.put(closestindex, child);
				if(parent == null){
					hp.put(index, node);
					forest.add(node);
				}
			}
		}
		
		// dynamic handle forest
		DynamicFunctionResult result = new DynamicForest(forest,c,k-candidateCenter.length/2).dynamicRun();
		ArrayList<TreeNode> list = result.getResult();
		int index = candidateCenter.length/2;
		for(TreeNode x:list){
			if(index >= center.length)
				break;
			center[index] = x.getRoot();
			index++;
		}
		
		return center;
	}
	
	private int closest(int j,ArrayList<Integer> N){
		int closestindex = j;
		double min = Double.MAX_VALUE;
		for(int i=0;i<N.size();i++)
			if(N.get(i)!=j && c[j][N.get(i)]<min){
				min = c[j][N.get(i)];
				closestindex=N.get(i);
			}
		return closestindex;
	}
	
	private HashMap<Integer,Integer> minMatch(double[] initial_distribute,int[] center){
		int mm = center.length;
		/*
		for(int i=0;i<mm;i++){
			for(int j=0;j<n;j++)
				System.out.print(initial_distribute[i*n+j]+"\t");
			System.out.println();
		}
		*/
		double[][] cost = new double[mm][n];
		for(int i=0;i<mm;i++)
			for(int j=0;j<n;j++)
				cost[i][j] = -c[center[i]][j];
		
		int[] centerlen = new int[mm];
		int sum = 0;
		for(int i=0;i<mm;i++){
			double total = 0;
			for(int j=0;j<n;j++){
				total += initial_distribute[i*n+j];
			}
			centerlen[i] = (int)Math.ceil(total);
			sum += centerlen[i];
		}
		
		double[][] value = new double[sum][n];
		for(int i=0;i<sum;i++)
			for(int j=0;j<n;j++)
				value[i][j] = -Double.MAX_VALUE;
		int index = 0;
		//System.out.println(sum +" "+ n +" "+ mm);
		for(int i=0;i<mm;i++){
			boolean indexchange = false;
			double part = 0;
			for(int j=0;j<n;j++){
				if(initial_distribute[i*n+j]==0)
					continue;
				part += initial_distribute[i*n+j];
				//System.out.println(index + " " + i + " " + j);
				value[index][j] = cost[i][j];
				indexchange = false;
				if(part >= 1.0){
					part -= 1;
					index++;
					indexchange = true;
					if(part != 0){
						value[index][j] = cost[i][j];
					}
				}
			}
			if(!indexchange)
				index++;
		}
		/*
		for(int i = 0;i<sum;i++){
			System.out.println(Arrays.toString(value[i]));
		}
		*/
		KM km = new KM(sum,n,value);
		totalcost = -km.getKM();
		HashMap<Integer,Integer> dis = new HashMap<Integer,Integer>();
		for(int i=0;i<n;i++){
			int send = km.pre[i]+1;
			int j;
			for(j=0;send > centerlen[j];j++){
				send -= centerlen[j];
			}
			dis.put(i,center[j]);
		}
		return dis;
	}
	
	public HashMap<Integer,Integer> solve(){
		PointValuePair InitialOpt = runInitialOptimizationProblem();
		int[] N = step2(step1(InitialOpt));
		int[] center = step3(N);

		PointValuePair SecondOpt = runSecondOptimizationProblem(center);
		return minMatch(SecondOpt.getPoint(),center);
	}
	
	public static void main(String args[]) { 
		
		//double[][] c = new double[3][3];
		/*
		CapacitatedKMedian m = new CapacitatedKMedian(c,3,2,2);
		m.solve();
		/*
		if (so != null) {
            //get solution
            double min = so.getValue();
            System.out.println("Opt: " + min);

            //print decision variables
            for (int i = 0; i < 3*3+3; i++) {
                System.out.print(so.getPoint()[i] + "\t");
            }
        }
        */
	} 
}
