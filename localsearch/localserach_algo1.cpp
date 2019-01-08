#include <iostream>
#include <cstdlib>
#include <vector>
#include <map>
#include <algorithm>
#include <random>
#include <chrono>
#include "MCMF.cpp"

using namespace std;
//extern ;

#define avaPoint 10 //图中点的总个数
#define appNum_w 10 //可选的app的总数，即算法中的w
#define appOnEdge_m 3 //每个选中的server配置的app的个数，即算法中的M

vector<int> selectAppOnNewPoint();
double costCalculation(const vector<int>& a, const vector<vector<int>>& b);
int configurationFunction(const vector<int> a,vector<vector<int>>& b);
int addPoint(vector<int>& a, vector<vector<int>>& b);
int dropPoint(vector<int>& a, vector<vector<int>>& b);
void swapPoint(vector<int>& a, vector<vector<int>>& b);
int firstAlgorithm();

struct APAttributes
{
    map<int,double> requests;
    APAttributes() {};
    APAttributes(APAttributes&& a)
    :requests(move(a.requests)) {}
};

// Experiment settings
double alpha = 0.5;
double internet_delay = 0.8;
vector<double> cloudlet_capacity;
vector<double> opencost;
vector<vector<double>> pathcost;
vector<APAttributes> nodeattributes;

int firstAlgorithm() {
    vector<int> placement_z(avaPoint, 0);   //当placement_z[i]为1时代表图中的点i放置了服务器，为0则没有放置
    placement_z[0] = 1; //初始化只选第一个点放置服务器

    //configuration_x矩阵中每一行都代表图中的一个点，而每一行中的每一列代表某个点i是否配置了服务j，configuration_x[i][j] = 1说明，i点放置了服务器，且配置了服务j；
    //configuration_x矩阵中某一行中有1存在，则说明这一行代表的点被放置了服务器
    vector<vector<int>> configuration_x(avaPoint, vector<int>(appNum_w, 0));
    //初始化选第一个点放置服务器后，要选择M应用放到服务器0上，用selectAppOnNewPoint()函数随机选择，然后改变configuration_x矩阵中的值；
    vector<int> tmp = selectAppOnNewPoint();
    for (unsigned i = 0; i < appOnEdge_m; i++) {
        configuration_x[0][tmp[i]] = 1;
    }
    //分别调用增加一个点，减去一个点，交换点的操作
    addPoint(placement_z, configuration_x);
    dropPoint(placement_z, configuration_x);
    swapPoint(placement_z, configuration_x);

    std::cout << "Hello, World!" << std::endl;
    return 0;
}

int addPoint(vector<int>& placement_z, vector<vector<int>>& config_x) {
    //三种点的操作最开始都是根据placement_z数组来计算现在有哪些点已经被选中放了服务器，哪些点没有放服务器；
    vector<int> selectedPoint;
    vector<int> idlePoint;
    for (unsigned int i = 0; i < avaPoint; i++) {
        if (placement_z[i] == 1) {
            selectedPoint.push_back(i);
        } else {
            idlePoint.push_back(i);
        }
    }
    if (idlePoint.size() == 0) {//所有点都被选中，无法再加入点，所以退出
        return 0;
    }
    double oldCost = costCalculation(placement_z, config_x);
    unsigned int p = 0;
    while (p < idlePoint.size()) {//遍历还没有放置服务器的点
        placement_z[idlePoint[p]] = 1;//放置服务器
        vector<int> tmp = selectAppOnNewPoint();//配置M个app
        for (unsigned int j = 0; j < appOnEdge_m; j++) {
            config_x[idlePoint[p]][tmp[j]] = 1;
        }
        configurationFunction(placement_z, config_x);//调用configuration过程，来求最佳的配置（我们调用configuration函数的目的就是为了求比当前cost小的配置）
        double newCost = costCalculation(placement_z, config_x);//求到最佳配置后，再回来求现在的cost
        if (newCost < oldCost) {//如果现在的cost比原来的cost小，也就是加了点之后，cost更小，那就跳出这个while，做其他操作去了；
            break;
        } else {//如果现在的cost没有变小，则还原称以前没加这个点之前的情况，并且要把放置的app清空，然后继续遍历下一个没有放置服务器的点
            p++;
            placement_z[idlePoint[p]] = 0;
            for (unsigned int k = 0; k < appNum_w; k++) {
                config_x[idlePoint[p]][k] = 0;
            }
        }
    }
    //跳出继续做其他操作
    addPoint(placement_z, config_x);
    dropPoint(placement_z, config_x);
    swapPoint(placement_z, config_x);
    return -1;
}

int dropPoint(vector<int>& placement_z, vector<vector<int>>& config_x) {
    vector<int> selectedPoint;
    vector<int> idlePoint;
    for (unsigned int i = 0; i < avaPoint; i++) {
        if (placement_z[i] == 1) {
            selectedPoint.push_back(i);
        } else {
            idlePoint.push_back(i);
        }
    }
    if (selectedPoint.size() == 0) {//没有点被选中，则不能再做减法
        return 0;
    }
    double oldCost = costCalculation(placement_z, config_x);
    unsigned int p = 0;
    while (p < selectedPoint.size()) {//从选中的点里面开始删减
        placement_z[selectedPoint[p]] = 0;
        double newCost = costCalculation(placement_z, config_x);//删减点的话只要将选中的服务器的选中情况置位0之后，计算新的cost
        if (newCost < oldCost) {//如果删减后cost减小，就跳出，继续做其他操作
            for (unsigned int k = 0; k < appNum_w; k++) {
                config_x[selectedPoint[p]][k] = 0;
            }
            break;
        } else {//如果删减后cost没有减小，就还原，继续删减
            p++;
            placement_z[selectedPoint[p]] = 1;
        }
    }
    addPoint(placement_z, config_x);
    dropPoint(placement_z, config_x);
    swapPoint(placement_z, config_x);
    return -1;
}

void swapPoint(vector<int>& placement_z, vector<vector<int>>& config_x) {
    vector<int> selectedPoint;
    vector<int> idlePoint;
    double oldCost = costCalculation(placement_z, config_x);
    for (unsigned int i = 0; i < avaPoint; i++) {
        if (placement_z[i] == 1) {
            selectedPoint.push_back(i);
        } else {
            idlePoint.push_back(i);
        }
    }
    unsigned int p = 0, q = 0;
    while (p < selectedPoint.size()) {
        int breakFlag = 0;
        while (q < idlePoint.size()) {
            placement_z[selectedPoint[p]] = 0;
            placement_z[idlePoint[q]] = 1;
            vector<int> tmp = selectAppOnNewPoint();
            for (unsigned int j = 0; j < appOnEdge_m; j++) {
                config_x[idlePoint[q]][tmp[j]] = 1;
            }
            configurationFunction(placement_z, config_x);//调用configuration过程
            double newCost = costCalculation(placement_z, config_x);
            if (newCost < oldCost) {
                for (unsigned int k = 0; k < appNum_w; k++) {
                    config_x[selectedPoint[p]][k] = 0;
                }
                p++;
                breakFlag = 1;
                break;
            } else {
                placement_z[selectedPoint[p]] = 1;
                placement_z[idlePoint[q]] = 0;
                for (unsigned int k = 0; k < appNum_w; k++) {
                    config_x[idlePoint[q]][k] = 0;
                }
                q++;
            }
        }
        if (breakFlag == 0) {
            p++;
        }
    }
    addPoint(placement_z, config_x);
    dropPoint(placement_z, config_x);
    swapPoint(placement_z, config_x);
}
//随机选择app放在server上
vector<int> selectAppOnNewPoint() {
    //这个函数并不是产生M个随机数来选择app，而是用shuffle的方法，举个例子，我们要从0-10中选3个随机数，只要将0-10这11个数随机打乱（shuffle），取前三个即可
    vector<int> tmp;
    for (unsigned int i = 0; i < appNum_w; i++) {
        tmp.push_back(i);
    }
    unsigned seed = chrono::system_clock::now().time_since_epoch().count();
    shuffle(tmp.begin(), tmp.end(), default_random_engine(seed));
    return tmp;
}
//计算cost的函数
double costCalculation(const vector<int>& placement_z, const vector<vector<int>>& config_x) {
    // Find the selected cloudlets.
    vector<int> selectedPoint;
    double deploy_cost = 0.0;
    for (unsigned int i = 0; i < avaPoint; i++) {
        if (placement_z[i] == 1) {
            selectedPoint.push_back(i);
            deploy_cost += opencost[i];
        }
    }

    // Build the flow graph
    MCMF flowgraph;
    /**
     * Total vertices = the sum of the map size in each AP 
     *                  + number of current cloudlets
     *                  + remote DC + 2 virtual points (src and dst)
     *
     * The index in the graph is assigned as follow:
     *  1 -- SUM : the requests of a type in an ap
     *  SUM+1 -- SUM+NUM_CLOUDLET : cloudlet
     *  SUM+NUM_CLOUDLET+1 : DC
     *  SUM+NUM_CLOUDLET+2 : src
     *  SUM+NUM_CLOUDLET+3 : dst
     */
    // requests_sum are caculated based on the request type, 
    // instead of single request in real case.
    int requests_sum = 0;
    for(const auto& attribute: nodeattributes) {
        requests_sum += attribute.requests.size();
    }
    flowgraph.init(requests_sum + selectedPoint.size() + 1 + 2);

    // Each src have an edge point to requests
    unsigned int request_index = 1;
    unsigned int current_ap = 0;
    for(const auto& attribute: nodeattributes) {
        for(const auto& type_request_pair: attribute.requests) {
            double resource = type_request_pair.second;
            flowgraph.AddEdge(requests_sum + selectedPoint.size() + 2,
                              request_index,
                              resource,
                              0);
            // Each request have an edge point to a cloudlet if 
            // it configures corresponding type.
            for(unsigned int i = 0; i < selectedPoint.size(); ++i) {
                if(config_x[selectedPoint[i]][type_request_pair.first]) {
                    flowgraph.AddEdge(request_index,
                                      requests_sum + i + 1,
                                      MCMF::inf,
                                      pathcost[current_ap][selectedPoint[i]]);
                }
            }
            // Each request have an edge point to the DC
            flowgraph.AddEdge(request_index,
                              requests_sum + selectedPoint.size() + 1,
                              MCMF::inf,
                              internet_delay);
            ++request_index;
        }
        ++current_ap;
    }

    // Each cloudlet and DC have an edge point to the dst
    for(unsigned int i = 0; i < selectedPoint.size(); ++i) {
        flowgraph.AddEdge(requests_sum + i + 1,
                          requests_sum + selectedPoint.size() + 3,
                          cloudlet_capacity[i],
                          0);
    }
    flowgraph.AddEdge(requests_sum + selectedPoint.size() + 1,
                      requests_sum + selectedPoint.size() + 3,
                      MCMF::inf,
                      0);

    double delay_cost = flowgraph.MincostMaxflow(requests_sum + selectedPoint.size() + 2,
                                                 requests_sum + selectedPoint.size() + 3);
    return alpha * delay_cost + (1 - alpha) * deploy_cost;
}
//配置函数
int configurationFunction(const vector<int>& placement_z, vector<vector<int>>& config_x) {
    double minCost = costCalculation(placement_z, config_x);
    for (unsigned i = 0; i < avaPoint; i++) {
        //对所有已经放置了服务器的点，我们都要求更小cost的app配置
        if (placement_z[i] == 1) {
            //首先找到该点已经配置的app，和没有配置的app
            vector<int> alreadyDeployed;
            vector<int> waitForSwap;
            for (unsigned int j = 0; j < appNum_w; j++) {
                if (config_x[i][j] == 1) {
                    alreadyDeployed.push_back(j);
                } else {
                    waitForSwap.push_back(j);
                }
            }
            unsigned int p = 0, q = 0;
            while (p < alreadyDeployed.size()) {
                int breakFlag = 0;
                while (q < waitForSwap.size()) {
                    //用两个while循环依次交换配置的服务
                    config_x[i][alreadyDeployed[p]] = 0;
                    config_x[i][waitForSwap[q]] = 1;
                    double newCost = costCalculation(placement_z, config_x);
                    if (newCost < minCost) {//如果替换后cost更小，那我们就跳出，替换下一个
//                        newCost = minCost;
                        breakFlag = 1;//用一个flag来记录是因为找到更小的cost跳出，还是因为可交换的app用完了才跳出
                        p++;
                        break;
                    } else {//如果替换后cost没有更小，那就还原，继续交换下一个试试看
                        config_x[i][alreadyDeployed[p]] = 1;
                        config_x[i][waitForSwap[q]] = 0;
                        q++;
                    }
                }
                if (breakFlag == 0) {//如果是因为可交换的app用完了才跳出，那说明这次循环没有找到可替换的点，我们继续替换下一个
                    p++;
                }
            }
        }
    }
    return 0;
}