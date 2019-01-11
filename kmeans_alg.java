package paper2;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.Random;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.GraphParseException;

import com.alibaba.fastjson.JSONArray;

//import appro.parameters_generator;

public class kmeans_alg {
	
	private double total_delay = 0;
	
 	public void kmeans(Graph graph, int cloudletNum, int iteraNum, int serviceNum, double internetDelay ,double alpha){
		
		//kmeans_alg k_mean = new kmeans_alg();
		Node ap_node, cloudlet_node;
		int cloudlet_index,ap_index,cloudlet_sub;
		//String classify = "cloudlet_node";
		int flag=0;
		
		ArrayList<ArrayList<Integer>> arrys = new ArrayList<>();
		for(int i = 0 ; i < cloudletNum ; i++){
			ArrayList<Integer> arry = new ArrayList<>();
			arrys.add(arry);
		}
		ArrayList<ArrayList<Integer>> cur_arrys = new ArrayList<>();
		for(int i = 0 ; i < cloudletNum ; i++){
			ArrayList<Integer> arry = new ArrayList<>();
			cur_arrys.add(arry);
		}
		
		for(int i=0;i<graph.getNodeCount();i++){
			ap_node =graph.getNode(i);
			ap_node.setAttribute("classify", "ap_node");
			double a = ap_node.getAttribute("capacity");
			ap_node.setAttribute("restcap", a);
		}
		
		double[][] shortestpath = new double[graph.getNodeCount()][graph.getNodeCount()];
		shortestpath = parameters_generator.netDelayMatrix(graph);      
		
		int[] cur_cloudlet, new_cloudlet;
		cur_cloudlet = new_cloudlet = new int[cloudletNum];
		
		this.randomSelectKAp(graph, cloudletNum);
		
		cur_cloudlet = this.findAllCloudlet(graph, cloudletNum);
		
		this.initConfiguration(graph, cur_cloudlet, serviceNum);
			
		for(int iteration = 1; iteration < iteraNum ; iteration ++){
			
			cur_cloudlet = this.findAllCloudlet(graph, cloudletNum);
			
			total_delay = 0; 
			
			for(int i=0;i<graph.getNodeCount();i++){
				ap_node =graph.getNode(i);
				ap_node.setAttribute("classify", "ap_node");
				double a = ap_node.getAttribute("capacity");
				ap_node.setAttribute("restcap", a);
			}
			
			for(int i = 0 ; i < cloudletNum ; i++){
				cur_arrys.get(i).clear();
				this.copyArrayList(arrys.get(i), cur_arrys.get(i));
			}	
			
			//clean the arraylist
			for(int i = 0 ; i < cloudletNum ; i++){
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
				cloudlet_index = this.assignAp(graph, ap_node, cur_cloudlet, shortestpath, cloudletNum, internetDelay, serviceNum);
				
				//maintain a new cluster
				cloudlet_sub = kmeans_alg.findCloudletIndex(cur_cloudlet, cloudlet_index);
				arrys.get(cloudlet_sub).add(ap_index);	
			}
			
			//compute the new cluster median and configure this new median
			for(int k = 0 ; k < cloudletNum ; k ++){
				this.findNewCluster(graph, arrys.get(k), shortestpath, serviceNum);
			}
			
			//如果聚类中心没有变化则退出
			new_cloudlet = this.findAllCloudlet(graph,cloudletNum);
			double cost=0, total = 0;
			if(this.isEqual(cur_cloudlet, new_cloudlet)){
				if(isConfigEqual(graph,cur_cloudlet, new_cloudlet)){
					for(int i=0;i<new_cloudlet.length;i++){
						cloudlet_node = graph.getNode(new_cloudlet[i]);
						cost = cloudlet_node.getAttribute("opencost");
						//System.out.println(cost);
						total = total +cost;
					}
					System.out.println("total delay! : " + total_delay);
					System.out.println("total cost! : " + total);
					System.out.println("total! : " + (total_delay*alpha+(1-alpha)*total));
					flag =1;
					break;	
				}
			}
		}
		if(flag ==0){
			new_cloudlet = this.findAllCloudlet(graph,cloudletNum);
			double cost=0, total = 0;
			for(int i=0;i<new_cloudlet.length;i++){
				cloudlet_node = graph.getNode(new_cloudlet[i]);
				cost = cloudlet_node.getAttribute("opencost");
//				System.out.println(cost);
				total = total +cost;
			}
			System.out.println("total delay : " + total_delay);
			System.out.println("total cost : " + total);
		}
	}
 	
 	public boolean isConfigEqual(Graph graph, int[] cur, int[] ne){
 		ArrayList<Integer> array1 = new ArrayList<Integer>();
 		ArrayList<Integer> array2 = new ArrayList<Integer>();
 		Node node;
 		Arrays.sort(cur);
 		Arrays.sort(ne);
 		for(int i=0; i<cur.length; i++){
 			node = graph.getNode(cur[i]);
 			array1 = node.getAttribute("configuration");
 			node = graph.getNode(ne[i]);
 			array2 = node.getAttribute("configuration");
 			if(isarrayListEqual(array1,array2)==false)
 				return false;
 		}
 		return true;
 	}
 	
 	
 	public boolean isarrayListEqual(ArrayList<Integer> array1,ArrayList<Integer> array2){
 		Collections.sort(array1);
 		Collections.sort(array2);
 		if(array1.equals(array2)){
 			return true;
 		}else
 			return false;
 		
 	}
	
	public void randomSelectKAp (Graph graph, int cloudletNum){
		
		Random ran = new Random();
		int ap_index;
		Node ap_node;
		String classify = "ap_node";
		for(int i = 0; i < cloudletNum ; i++){
			ap_index = ran.nextInt(graph.getNodeCount());
			ap_node = graph.getNode(ap_index);
			if(classify.equals(ap_node.getAttribute("classify"))){
				ap_node.setAttribute("classify", "cloudlet_node");
			}
			else
				i--;
		}
	}
	
	public int[] findAllCloudlet(Graph graph, int cloudletNum){
		
		int k = 0;
		String classify = "cloudlet_node";
		int[] cloudlet = new int[cloudletNum];    //store cloudlet index
		for( Node node : graph ){
			 if(classify.equals(node.getAttribute("classify"))){
				 cloudlet[k] = node.getIndex();
				 k++;
			 }	
		 }
		 return cloudlet;	
	}
		
	//compute a ap assign to which cloudlet , output : cloudlet index
	public int assignAp(Graph graph, Node ap_node, int[] cloudlet, double[][] shortestpath, int cloudletNum, double internetDelay, int serviceNum){
		ArrayList<Integer> jtype = new ArrayList<>();
		ArrayList<Double> jres = new ArrayList<>();
		ArrayList<Integer> config = new ArrayList<>();
		int[] maxcloudlet;
		int[] jobnum = new int[cloudletNum];
		double curcap;
		boolean flag = false;
		
		
		jtype = str1arr(ap_node.getAttribute("type"));
		jres = str2arr(ap_node.getAttribute("resource"));
		int[] sort = kmeans_alg.sortCloudlet(cloudlet, ap_node, shortestpath, cloudletNum);
		Node cloudlet_node;
		int ap_index = ap_node.getIndex();
		
		for(int i =0;i<jres.size();i++){
			int j=0;
			do{
				cloudlet_node = graph.getNode(sort[j]);
				config = str1arr(cloudlet_node.getAttribute("configuration"));
				curcap=cloudlet_node.getAttribute("restcap");
				if(kmeans_alg.hasElement(jtype.get(i), config)&&(jres.get(i)< curcap)){
					    total_delay = total_delay + jres.get(i)*shortestpath[ap_index][sort[j]];       //?????????????????????????????????????????
						curcap=curcap-jres.get(i);
						cloudlet_node.setAttribute("restcap", curcap);	
						jobnum[j]++;
						flag = true;
				}else{
					j++;
				}	
			}while((flag == false)&&(j<sort.length));
			if(flag == false && jtype.get(i) < serviceNum){
				total_delay = total_delay + jres.get(i)*internetDelay;
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
	
	
	public void findNewCluster( Graph graph, ArrayList<Integer> a , double[][] shortestpath, int serviceNum){
		
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
		double delay, min_delay = Double.MAX_VALUE;
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
		
		kmeans_alg f = new kmeans_alg();
		f.configuration(graph, min_index, cluster, serviceNum);		
	}
	
	//initialize configuration
	public void initConfiguration(Graph graph, int[] cloudlet, int serviceNum){
		
		ArrayList<Integer> config = new ArrayList<>();
		ArrayList<Double> jres = new ArrayList<>();
		ArrayList<Integer> jtype = new ArrayList<>();
		int index = 0, cloudlet_index, num = 0,k=0;
		int confignum;
		double num1 = 0,max=0;
		Node cloudlet_node, ap_node = null;
		double[] ap_service = new double[serviceNum];
		double[] ap_service2 = new double[serviceNum];
		
		
		for(int i = 0; i< graph.getNodeCount(); i++ ){
			ap_node = graph.getNode(i);
			jres = str2arr(ap_node.getAttribute("resource"));
			jtype = str1arr(ap_node.getAttribute("type"));
			for(int j = 0;j < jres.size();j++){
				if(jtype.get(j)<serviceNum){
					ap_service[jtype.get(j)]++;}
			}
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
	public void configuration(Graph graph, int cloudlet_index, int[] ap, int serviceNum){
	
		ArrayList<Integer> config = new ArrayList<>();
		ArrayList<Double> jres = new ArrayList<>();
		ArrayList<Integer> jtype = new ArrayList<>();
		Node ap_node,cloudlet_node;
		double[] ap_service= new double[serviceNum];
		
		for(int i=0;i<ap.length;i++){
			ap_node=graph.getNode(ap[i]);
			jtype = str1arr(ap_node.getAttribute("type"));
			jres = str2arr(ap_node.getAttribute("resource"));
			for(int j=0;j<jtype.size();j++){
				if(jtype.get(j)<serviceNum){
					ap_service[jtype.get(j)]=ap_service[jtype.get(j)]+jres.get(j);}
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
	public static int[] sortCloudlet(int[] cloudlet, Node ap_node, double[][] shortestpath, int cloudletNum){
		
		int ap_index = ap_node.getIndex();
		int[] sort = new int[cloudletNum];
		double[] delay = new double[cloudletNum];
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
	
	public void printCloudlet(Graph graph, int serviceNum){
		
		ArrayList<Integer> config = new ArrayList<>();
		//int[] job = new int[serviceNum];
		String classify = "cloudlet_node";
		int cloudlet_index;
		
		for( Node node_ap : graph ){
			
			if(classify.equals(node_ap.getAttribute("classify"))){
				cloudlet_index = node_ap.getIndex();
                System.out.print("cloudlet index : " + cloudlet_index + "\t configuration : ");
				config = node_ap.getAttribute("configuration"); 
				for(int i=0;i<config.size();i++){
					System.out.print(config.get(i)+" ");
				}
				System.out.println();
			}
		}
//		itertor_num ++;
//		System.out.println("total iterator number : " + itertor_num);
	}
	
	public static void main(String[] args) throws Exception, GraphParseException {
		
//		Graph graph = new SingleGraph("Scale_free_network");
//		graph.read("100AP_graph.dgs");
		
		
		//graph1.write("test.dgs");
		kmeans_alg test = new kmeans_alg();
		
		int[] edgeNum = {2,3,4,6,10};
		int[] appNum = {1,5,10,15,20};
		double[] cap = {0.25,0.5,1,2,4};
		int[] ap = {10,20,30,40,50};
		double[] alpha ={0.1,0.3,0.5,0.7,0.9};
		
		//Graph genGraph(Graph graph, int nodeNum, double capScale, int serviceNum)
		for(int i=0;i<5;i++ ){
			Graph graph = new SingleGraph("Scale_free_network");
			graph.read("100AP_graph.dgs");
			Graph graph1 = new SingleGraph("Scale_free_network");
			graph1= genGraph(graph, 30, 1, appNum[i]);
			graph1.write("test.dgs");
//			System.exit(0);
			//kmeans(Graph graph, int cloudletNum, int iteraNum, int serviceNum, double internetDelay)
			test.kmeans(graph1, edgeNum[i], 100,appNum[i], 8,alpha[2]);
			System.out.println("cloudlet num: "+edgeNum[i]+"; service number: "+appNum[i]+"; Internet delay: "+8);
			//printCloudlet(Graph graph, int serviceNum)
			test.printCloudlet(graph1, appNum[i]);	
			//graph1.write("test.dgs")
		}
	} 
	
	public static Graph genGraph(Graph graph, int nodeNum, double capScale, int serviceNum){
		
		int i=0;
		int num = graph.getNodeCount();
		double cap;
		Node node;
		Graph graph1 = new SingleGraph("network");
		graph1 = graph;
		for(;i<nodeNum; i++){
			node=graph1.getNode(i);
			cap = node.getAttribute("capacity");
			cap = cap*capScale;
			node.setAttribute("capacity", cap);
			node.setAttribute("restcap", cap);
		}
		do{
			node=graph1.getNode(nodeNum);
			graph1.removeNode(node);
		}while(graph1.getNodeCount()>nodeNum);
		
		ArrayList<Integer> type = new ArrayList<>();
		ArrayList<Double> resource = new ArrayList<>();
		for(int j =0; j < graph1.getNodeCount();j++){
			node = graph1.getNode(j);
			type = str1arr(node.getAttribute("type"));
			resource = str2arr(node.getAttribute("resource"));
			for(int k=0;k<type.size();k++){
				if(type.get(k)>= serviceNum){
					type.remove(k);
					resource.remove(k);
					k--;
				}	
			}
			node.setAttribute("type", type);
			node.setAttribute("resource", resource);
		}
		return graph1;
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
