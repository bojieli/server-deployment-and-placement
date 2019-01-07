package kmedian;

public class KM {
	private int m;
	private int n;
    private double[][] value; 
    private double[] lx;
    private double[] ly;
    private boolean[] sx; 
    private boolean[] sy; 
    public int[] pre;
    
    public KM(int m,int n,double[][] value){
    	this.m = m;
    	this.n = n;
    	this.value = value;
    	lx = new double[m];
    	ly = new double[n];
    	sx = new boolean[m];
    	sy = new boolean[n];
    	pre = new int[n];
    }

    private boolean dfs(int x) {   //采用匈牙利算法找增广路径
        sx[x] = true;       //代表左半部分顶点x包含在最终结果中
        for(int y = 0;y < n;y++) {
            if(!sy[y] && lx[x] + ly[y] == value[x][y]) {
                sy[y] = true;   //代表右半部分顶点y包含在最终结果中
                if(pre[y] == -1 || dfs(pre[y])) {
                    pre[y] = x;
                    return true;
                }
            }
        }
        return false;
    }
    
    public double getKM() {
        //初始化lx[i]和ly[i]
        for(int i = 0;i < n;i++) {
            ly[i] = 0;
        }
        for(int i=0;i < m;i++){
            lx[i] = -Double.MAX_VALUE;
            for(int j = 0;j < n;j++) {
                if(value[i][j] > lx[i])
                    lx[i] = value[i][j];
            }
        }
    
        for(int i = 0;i < n;i++)
            pre[i] = -1;      //初始化右半部分顶点y的匹配顶点为-1
        
        for(int x = 0;x < m;x++) { //从左半部分顶点开始，寻找二分图完美匹配的相等子图完美匹配
            while(true) {
                for(int i = 0;i < n;i++) {//每次寻找x的增广路径，初始化sx[i]和sy[i]均为被遍历
                    sy[i] = false;
                }
                for(int i = 0;i < m;i++) {
                	sx[i] = false;
                }
                if(dfs(x))  //找到从x出发的增广路径，结束循环，寻找下一个x的增广路径
                    break;
                //下面对于没有找到顶点x的增广路径进行lx[i]和ly[i]值的调整
                double min = Double.MAX_VALUE;
                for(int i = 0;i < m;i++) {
                    if(sx[i]) {  //当sx[i]已被遍历时
                        for(int j = 0;j < n;j++) {
                            if(!sy[j]) {  //当sy[j]未被遍历时
                                if(lx[i] + ly[j] - value[i][j] < min)
                                    min = lx[i] + ly[j] - value[i][j];
                            }
                        }
                    }
                }
                if(min == 0)
                    return -1;
                for(int i = 0;i < n;i++) {
                    if(sy[i])
                        ly[i] = ly[i] + min;
                }
                for(int i = 0;i < m;i++) {
                    if(sx[i])
                        lx[i] = lx[i] - min;
                }
            }
        }
        
        double sum = 0;
        for(int y = 0;y < n;y++) {
            if(pre[y] != -1)
                sum = sum + value[pre[y]][y];
        }

        return sum;
    }


}
