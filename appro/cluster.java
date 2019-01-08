package appro;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;

import org.graphstream.graph.Element;
import org.graphstream.graph.ElementNotFoundException;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.GraphParseException;

import com.alibaba.fastjson.JSONArray;

//import appro.parameters_generator;

public class cluster{
	
	private double total_delay = 0;
	private int itertor_num = 0;
	
 	public void kmeans(Graph graph){
		
		//kmeans_alg k_mean = new kmeans_alg();
		Node ap_node, cloudlet_node;
		int cloudlet_index,ap_index,cloudlet_sub;
		String classify = "cloudlet_node";
		
		ArrayList<ArrayList<Integer>> arrys = new ArrayList<>();
		for(int i = 0 ; i < parameters_generator.CLOUDLET_NUM ; i++){
			ArrayList<Integer> arry = new ArrayList<>();
			arrys.add(arry);
		}
		ArrayList<ArrayList<Integer>> cur_arrys = new ArrayList<>();
		for(int i = 0 ; i < parameters_generator.CLOUDLET_NUM ; i++){
			ArrayList<Integer> arry = new ArrayList<>();
			cur_arrys.add(arry);
		}
		
		double[][] shortestpath = new double[graph.getNodeCount()][graph.getNodeCount()];
		shortestpath = parameters_generator.netDelayMatrix(graph);      
		
		int[] cur_cloudlet, new_cloudlet;
		cur_cloudlet = new_cloudlet = new int[parameters_generator.CLOUDLET_NUM];
		
		this.randomSelectKAp(graph);
		
		cur_cloudlet = this.findAllCloudlet(graph);
		
		this.initConfiguration(graph, cur_cloudlet);
			
		for(int iteration = 1; iteration < parameters_generator.MAXI ; iteration ++){
			
			cur_cloudlet = this.findAllCloudlet(graph);
			
			total_delay = 0;
			
			for(int i = 0 ; i < parameters_generator.CLOUDLET_NUM ; i++){
				cur_arrys.get(i).clear();
				this.copyArrayList(arrys.get(i), cur_arrys.get(i));
			}	
			
			//clean the arraylist
			for(int i = 0 ; i < parameters_generator.CLOUDLET_NUM ; i++){
				arrys.get(i).clear();
			}
			
			//assign each ap to the closest cloudlet
			for(int i = 0 ; i < graph.getNodeCount() ; i++){
				ap_node = graph.getNode(i);
				ap_index = ap_node.getIndex();
				//if(classify.equals(ap_node.getAttribute("classify")))
					//cloudlet_index = this.assignAp(graph, ap_node, cur_cloudlet, shortestpath);
					//cloudlet_index = ap_index;
				//else
				cloudlet_index = this.assignAp(graph, ap_node, cur_cloudlet, shortestpath);
				
				//maintain a new cluster
				cloudlet_sub = cluster.findCloudletIndex(cur_cloudlet, cloudlet_index);
				arrys.get(cloudlet_sub).add(ap_index);	
			}
			
			//compute the new cluster median and configure this new median
			for(int k = 0 ; k < parameters_generator.CLOUDLET_NUM ; k ++){
				this.findNewCluster(graph, arrys.get(k), shortestpath);
			}
			
			
			
			//如果聚类中心没有变化则退出
			new_cloudlet = this.findAllCloudlet(graph);
			double cost=0, total = 0;
			if(this.isEqual(cur_cloudlet, new_cloudlet))
				if(this.isAllEqualArrayList(arrys, cur_arrys, new_cloudlet, cur_cloudlet)){
					for(int i=0;i<new_cloudlet.length;i++){
						cloudlet_node = graph.getNode(new_cloudlet[i]);
						cost = cloudlet_node.getAttribute("opencost");
						total = total +cost;
					}
					System.out.println("total delay : " + total_delay);
					System.out.println("total cost : " + total);
					break;	
				}	
			itertor_num ++;
			System.out.println("total delay : " + total_delay);
			System.out.println("total cost : " + total);
		}	
	}
	
	public void randomSelectKAp (Graph graph){
		
		Random ran = new Random();
		int ap_index;
		Node ap_node;
		String classify = "ap_node";
		for(int i = 0; i < parameters_generator.CLOUDLET_NUM ; i++){
			ap_index = ran.nextInt(parameters_generator.AP_NUM);
			ap_node = graph.getNode(ap_index);
			if(classify.equals(ap_node.getAttribute("classify"))){
				ap_node.setAttribute("classify", "cloudlet_node");
			}
			else
				i--;
		}
	}
	
	public int[] findAllCloudlet(Graph graph){
		
		int k = 0;
		String classify = "cloudlet_node";
		int[] cloudlet = new int[parameters_generator.CLOUDLET_NUM];    //store cloudlet index
		for( Node node : graph ){
			 if(classify.equals(node.getAttribute("classify"))){
				 cloudlet[k] = node.getIndex();
				 k++;
			 }	
		 }
		 return cloudlet;	
	}
	
	
	//compute a ap assign to which cloudlet , output : cloudlet index
	
	public int assignAp(Graph graph, Node ap_node, int[] cloudlet, double[][] shortestpath){
		ArrayList<Integer> jtype = new ArrayList<>();
		ArrayList<Double> jres = new ArrayList<>();
		ArrayList<Integer> config = new ArrayList<>();
		int[] maxcloudlet;
		int[] jobnum = new int[parameters_generator.CLOUDLET_CAP];
		double curcap;
		boolean flag = false;
		
		
		jtype = str1arr(ap_node.getAttribute("type"));
		jres = str2arr(ap_node.getAttribute("resource"));
		int[] sort = cluster.sortCloudlet(cloudlet, ap_node, shortestpath);
		Node cloudlet_node;
		int ap_index = ap_node.getIndex();
		
		for(int i =0;i<jres.size();i++){
			int j=0;
			do{
				cloudlet_node = graph.getNode(sort[j]);
				config = str1arr(cloudlet_node.getAttribute("configuration"));
				curcap=cloudlet_node.getAttribute("restcap");
				if(cluster.hasElement(jtype.get(i), config)&&(jres.get(i)< curcap)){
					    total_delay = total_delay + shortestpath[ap_index][sort[j]];       //?????????????????????????????????????????
						curcap=curcap-jres.get(i);
						ap_node.setAttribute("restcap", curcap);	
						jobnum[j]++;
						flag = true;
				}else{
					j++;
				}	
			}while((flag == false)&&(j<sort.length));
			if(flag == false){
				total_delay = total_delay + parameters_generator.INTERNET_DELAY;
			}	
		}
		
		int max = jobnum[0];
		int index =0;
		for(int i=0;i<jobnum.length;i++){
			if(jobnum[i] >max){
				max = jobnum[i];
				index =i;
			}	
		}
		
		return sort[index];
	}
	
//	public int assignAp(Graph graph, Node ap_node, int[] cloudlet, double[][] shortestpath){
//		
//		double delay = 0, min_delay = parameters_generator.MAXD, process_delay = 0;
//		int cloudlet_index = 0, ap_index;
//		int[] config = new int[parameters_generator.SERVICE_NUM];
//		int[] config1 = new int[parameters_generator.SERVICE_NUM];
//		ArrayList<Integer> type = new ArrayList<>();
//		ArrayList<Double> resource = new ArrayList<>();
//		//int[] job = new int[parameters_generator.SERVICE_SUM];
//		int[] sort = new int[parameters_generator.CLOUDLET_NUM];
//		Node cloudlet_node;
//		double computecap, curcap;
//		int flag = 0;
//		
//		type = ap_node.getAttribute("type");
//		resource = ap_node.getAttribute("resource");
//		
//		//for(int i = 0;i < job_object.length;i++)
//		//	job[i] = Integer.parseInt(String.valueOf(job_object[i]));
//		ap_index = ap_node.getIndex();
//		sort = kmeans_alg.sortCloudlet(cloudlet, ap_node, shortestpath);
//		
//		for(int i = 0 ; i < cloudlet.length ; i++){
//			
//			delay = 0;
//			flag = 0;
//			cloudlet_node = graph.getNode(cloudlet[i]);
//			config = cloudlet_node.getAttribute("configuration"); 
//			totalcap = cloudlet_node.getAttribute("capacity"); 
//			cur
//			
//			
//			
//			for(int j = 0 ; j < resource.size() ; j++){
//				int typ = type.get(j);
//				double res = resource.get(j);
//				if(kmeans_alg.hasElement(typ, config)){
//						delay = delay + shortestpath[ap_index][cloudlet[i]];
//				}
//				else{
//						for(int k = 0 ; k < sort.length ; k++){
//							cloudlet_node = graph.getNode(sort[k]);
//							config1 = cloudlet_node.getAttribute("configuration"); 
//							if( kmeans_alg.hasElement(j, config1)){
//								if(shortestpath[sort[k]][cloudlet[i]] < parameters_generator.INTERNET_DELAY){
//									//process_delay = (double)job[j] / kmeans_alg.sunElement(j, config1);
//									delay = delay + shortestpath[ap_index][cloudlet[i]]+shortestpath[cloudlet[i]][sort[k]] + process_delay + shortestpath[ap_index][sort[k]];
//									flag = 1;
//								}
//								else{
//									delay = delay + shortestpath[ap_index][cloudlet[i]] + parameters_generator.INTERNET_DELAY*2;
//									flag = 1;	
//								}
//						    }	
//					    }
//						if(flag == 0)
//							delay = delay + shortestpath[ap_index][cloudlet[i]] + parameters_generator.INTERNET_DELAY*2;		
//					}
//				}	
//			}
//			if(delay < min_delay){
//				cloudlet_index = cloudlet[i];
//				min_delay = delay;
//			}
//		}
//	    
//		total_delay = total_delay + min_delay;
//		return cloudlet_index;
//	}
	
	public void findNewCluster( Graph graph, ArrayList<Integer> a , double[][] shortestpath){
		
		int size = a.size();
		int[] cluster = new int[size];
		for(int i = 0 ; i < size ; i++)
			cluster[i] = a.get(i);
		
		//initialize all nodes of this cluster
		Node ap_node, cloudlet_node;
		ArrayList<Integer> config = new ArrayList<>();
		for(int i = 0 ; i < cluster.length ; i++){
			ap_node = graph.getNode(cluster[i]);
			ap_node.setAttribute("classify", "ap_node");
			ap_node.setAttribute("configuration", config);	
		}
		
		//compute new cluster
		double delay, min_delay = parameters_generator.MAXD;
		int min_index = 0;
		for(int i = 0 ; i < cluster.length ; i++){
			delay = 0;
			for(int j = 0 ; j < cluster.length ; j++){
				delay = delay + shortestpath[cluster[i]][cluster[j]];
			}
			if(delay < min_delay){
				min_index = cluster[i];
				min_delay = delay;
			}
		}
		
		cloudlet_node = graph.getNode(min_index);
		cloudlet_node.setAttribute("classify", "cloudlet_node");
		
		cluster f = new cluster();
		f.configuration(graph, min_index, cluster);		
	}
	
	//initialize configuration
	public void initConfiguration(Graph graph, int[] cloudlet){
		
		ArrayList<Integer> config = new ArrayList<>();
		ArrayList<Double> jres = new ArrayList<>();
		ArrayList<Integer> jtype = new ArrayList<>();
		int index = 0, cloudlet_index, num = 0,k=0;
		int confignum;
		double num1 = 0,max=0;
		Node cloudlet_node, ap_node = null;
		double[] ap_service = new double[parameters_generator.TYPE_SUM];
		double[] ap_service2 = new double[parameters_generator.TYPE_SUM];
		
		
		for(int i = 0; i< parameters_generator.AP_NUM; i++ )
			ap_node = graph.getNode(i);
			jres = str2arr(ap_node.getAttribute("resource"));
			jtype = str1arr(ap_node.getAttribute("type"));
			for(int j = 0;j < jres.size();j++){
				ap_service[jtype.get(j)]++;
			}
			//System.out.println(ap_service[1]);
        //System.out.println("service: "+Arrays.toString(service));
		for(int i = 0 ; i < cloudlet.length ; i++){
			k=0;
			cloudlet_index = cloudlet[i];
			cloudlet_node = graph.getNode(cloudlet_index);
			confignum= cloudlet_node.getAttribute("configNum");
			for(int j=0;j<ap_service.length;j++){
				ap_service2[j]=ap_service[j];
			}
			do{
				max = 0;
	        	for(int j = 0 ; j < ap_service.length ; j++){
					if(ap_service2[j] > max){
						max = ap_service2[j];
						index = j;
					}	
				}
				ap_service2[index] = 0;	
				config.add(index);
				k++;
			}while(k < confignum);
			cloudlet_node.setAttribute("configuration", config);
		}	
		
		//System.out.println("config" + Arrays.toString(config));
		
	}
	
	//reconfigure a cloudlet of a cluster
	public void configuration(Graph graph, int cloudlet_index, int[] ap){
	
		ArrayList<Integer> config = new ArrayList<>();
		ArrayList<Double> jres = new ArrayList<>();
		ArrayList<Integer> jtype = new ArrayList<>();
		Node ap_node,cloudlet_node;
		double[] ap_service= new double[parameters_generator.TYPE_SUM];
		
		for(int i=0;i<ap.length;i++){
			ap_node=graph.getNode(ap[i]);
			jtype = str1arr(ap_node.getAttribute("type"));
			jres = str2arr(ap_node.getAttribute("resource"));
			for(int j=0;j<jtype.size();j++){
				ap_service[jtype.get(j)]=ap_service[jtype.get(j)]+jres.get(j);
			}
		}
			
		
		int k=0,index=0;
		cloudlet_node = graph.getNode(cloudlet_index);
		int confignum= cloudlet_node.getAttribute("configNum");
		do{
			double max = 0;
        	for(int j = 0 ; j < ap_service.length ; j++){
				if(ap_service[j] > max){
					max = ap_service[j];
					index = j;
				}	
			}
			ap_service[index] = 0;	
			config.add(index);
			k++;
		}while(k < confignum);
		cloudlet_node.setAttribute("configuration", config);
		
	}
	
	//if this array has given element
	public static boolean hasElement ( int element, ArrayList<Integer> array ) {
		
		for(int i = 0 ; i < array.size() ; i++){
			if(element == array.get(i))
				return true;
		}
		return false;
	}
	
	//compute the number of element in a array
	public static int sunElement(int element, int[] array){
		
		int total = 0;
		for(int i = 0 ; i < array.length ; i++){
			if(array[i] == element)
				total++;
		}
		return total;	
	}
	
	//sort cloudlet by delay
	public static int[] sortCloudlet(int[] cloudlet, Node ap_node, double[][] shortestpath){
		
		int ap_index = ap_node.getIndex();
		int[] sort = new int[parameters_generator.CLOUDLET_NUM];
		double[] delay = new double[parameters_generator.CLOUDLET_NUM];
		double temp = 0;
		int temp1 = 0;
		
		for(int i = 0 ; i < delay.length ; i++){
			delay[i] = shortestpath[ap_index][cloudlet[i]];
			sort[i] = cloudlet[i];
		}
		
		for(int i = 0 ; i < delay.length-1 ; i++)
			for(int j = i+1 ; j < delay.length ; j++){
				if(delay[i] > delay [j]){
					temp = delay[i];
					delay[i] = delay[j];
					delay[j] = temp;
					temp1 = sort[i];
					sort[i] = sort[j];
					sort[j] = temp1;	
				}
			}
		
		return sort;	
	}
    
	public static int findCloudletIndex(int[] cloudlet, int cloud){
		
		int index = 0;
		for(int i = 0 ; i < cloudlet.length ; i++){
			if(cloudlet[i] == cloud){
				index = i;
				break;
			}
		}
		return index;
	} 
    
	public boolean isEqual(int[] cur, int[] ne){
		
		Arrays.sort(cur);  
        Arrays.sort(ne);  
        if (Arrays.equals(cur, ne)) 
        	return true;  
        else  
            return false;  
	}
	
	public void printCloudlet(Graph graph){
		
		ArrayList<Integer> config = new ArrayList<>();
		int[] job = new int[parameters_generator.TYPE_SUM];
		String classify = "cloudlet_node";
		int cloudlet_index;
		
		for( Node node_ap : graph ){
			
			if(classify.equals(node_ap.getAttribute("classify"))){
				cloudlet_index = node_ap.getIndex();
                System.out.print("cloudlet index : " + cloudlet_index + "\t configuration : ");
				config = node_ap.getAttribute("configuration"); 
				//System.out.println(Arrays.toString(config));
			}
		}
		itertor_num ++;
		System.out.println("total iterator number : " + itertor_num);
	}
	
	public static void main(String[] args) throws Exception, GraphParseException {
		
		Graph graph = new SingleGraph("Scale_free_network");
//		graph_generator.scaleFreeNetwork(graph);
//		graph_generator.initApNode(graph);
//		graph_generator.initApEdge(graph);
//		graph_generator.assignJobAp(graph);
		graph.read(parameters_generator.AP_NUM+"AP_graph.dgs");
		
		cluster test = new cluster();
		test.kmeans(graph);
		test.printCloudlet(graph);;	
		
	} 

    public void copyArrayList( ArrayList<Integer> sour, ArrayList<Integer> des){
    	int size = sour.size();
    	int num;
    	for(int i = 0 ; i < size ; i ++){
    		num = sour.get(i);
    		des.add(num);
    	}
    }
    
    public boolean isEqualArrayList(ArrayList<Integer> a, ArrayList<Integer> b){
    	
    	a.sort(null);
    	b.sort(null);
    	int sizea = a.size(),sizeb = b.size();
    	if(sizea != sizeb)
    		return false;
    	for(int i = 0 ; i < sizea ; i++)
    		if(a.get(i) != b.get(i))
    			return false;
    	return true;	
    }
    
    public boolean isAllEqualArrayList(ArrayList<ArrayList<Integer>> a, ArrayList<ArrayList<Integer>> b, int[] a1, int[] b1){
    	
    	int index = 0;
    	for(int i = 0 ; i < a1.length ; i++){
    		for(int j = 0 ; j < b1.length ;j++){
    			if(a1[i] == b1[j])
    				index = j;
    		}
    		if(this.isEqualArrayList(a.get(i), b.get(index)) == false)
    			return false;
    	}
    	return true;	
    }
    
    public static ArrayList<Integer> str1arr(Object input) {
	    JSONArray jsonArray = JSONArray.parseArray(String.valueOf(input));
	    ArrayList<Integer> arrayList = new ArrayList<>();
	    for (Iterator<Object> it = jsonArray.iterator(); it.hasNext(); ) {
	        Integer integer = (Integer) it.next();
	        arrayList.add(integer);
	    }
	    return arrayList;
	}
    
    public static ArrayList<Double> str2arr(Object input) {
    	   JSONArray jsonArray = JSONArray.parseArray(String.valueOf(input));
    	   ArrayList<Double> arrayList = new ArrayList<>();
    	   for (Iterator<Object> it = jsonArray.iterator(); it.hasNext(); ) {
    	      Double aDouble =  ((BigDecimal)it.next()).doubleValue();
    	      arrayList.add(aDouble);
    	   }
    	   return arrayList;
    	}


}
