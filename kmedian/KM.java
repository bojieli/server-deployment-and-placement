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

    private boolean dfs(int x) {   //�����������㷨������·��
        sx[x] = true;       //������벿�ֶ���x���������ս����
        for(int y = 0;y < n;y++) {
            if(!sy[y] && lx[x] + ly[y] == value[x][y]) {
                sy[y] = true;   //�����Ұ벿�ֶ���y���������ս����
                if(pre[y] == -1 || dfs(pre[y])) {
                    pre[y] = x;
                    return true;
                }
            }
        }
        return false;
    }
    
    public double getKM() {
        //��ʼ��lx[i]��ly[i]
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
            pre[i] = -1;      //��ʼ���Ұ벿�ֶ���y��ƥ�䶥��Ϊ-1
        
        for(int x = 0;x < m;x++) { //����벿�ֶ��㿪ʼ��Ѱ�Ҷ���ͼ����ƥ��������ͼ����ƥ��
            while(true) {
                for(int i = 0;i < n;i++) {//ÿ��Ѱ��x������·������ʼ��sx[i]��sy[i]��Ϊ������
                    sy[i] = false;
                }
                for(int i = 0;i < m;i++) {
                	sx[i] = false;
                }
                if(dfs(x))  //�ҵ���x����������·��������ѭ����Ѱ����һ��x������·��
                    break;
                //�������û���ҵ�����x������·������lx[i]��ly[i]ֵ�ĵ���
                double min = Double.MAX_VALUE;
                for(int i = 0;i < m;i++) {
                    if(sx[i]) {  //��sx[i]�ѱ�����ʱ
                        for(int j = 0;j < n;j++) {
                            if(!sy[j]) {  //��sy[j]δ������ʱ
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
