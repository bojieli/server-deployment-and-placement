#include "localserach_algo1.cpp"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <assert.h>

using namespace std;

/*
long rand_range(long max, long min)
{
    return rand() % (max - min + 1) + min;
}

double float_rand_range(double max, double min)
{
    return rand() * (double)(max - min) / RAND_MAX + min;
}

// generate 0 .. range-1
long zipf_rand(long range)
{
    double log_range = log(range + 1);
    double fractal_rand = rand() * 1.0 / RAND_MAX;
    long sample = (long)(exp(fractal_rand * log_range));
    // sample is 1 .. range
    return (sample * 65537) % range;
}
*/

chrono::time_point<chrono::steady_clock> start_time;

double shortest_len[avaPoint][avaPoint];
int shortest_path[avaPoint][avaPoint];
double latency[avaPoint][avaPoint];

void compute_shortest_path()
{
    for (long i=0; i<avaPoint; i++)
        for (long j=0; j<avaPoint; j++) {
            shortest_len[i][j] = latency[i][j];
            shortest_path[i][j] = j;
        }

    for (long k=0; k<avaPoint; k++)
        for (long i=0; i<avaPoint; i++)
            for (long j=0; j<avaPoint; j++)
                if (shortest_len[i][j] > shortest_len[i][k] + shortest_len[k][j]) {
                    shortest_len[i][j] = shortest_len[i][k] + shortest_len[k][j];
                    shortest_path[i][j] = shortest_path[i][k];
                }
}

void init_graph()
{
    cloudlet_capacity.reserve(avaPoint);
    opencost.reserve(avaPoint);
    node_apps.reserve(avaPoint);
    node_attributes.reserve(avaPoint);
    pathcost.reserve(avaPoint);

    for (long i=0; i<avaPoint; i++) {
        node_attributes.push_back(APAttributes());
        pathcost.push_back(vector<double>(avaPoint));

        for (long j=0; j<avaPoint; j++) {
            if (i == j) {
                latency[i][j] = 0;
                pathcost[i][j] = 0;
            }
            else {
                latency[i][j] = std::numeric_limits<double>::infinity();
                pathcost[i][j] = std::numeric_limits<double>::infinity();
            }
        }
    }
}

void print_shortest_path()
{
    printf("Latency matrix:\n");
    for (long i=0; i<avaPoint; i++) {
        for (long j=0; j<avaPoint; j++) {
            if (shortest_len[i][j] < std::numeric_limits<double>::infinity())
                printf("%10lf ", shortest_len[i][j]);
            else
                printf("********** ");
        }
        printf("\n");
    }
}

/*
// generated tasks should be ordered by release time
void generate_tasks()
{
    for(long i=0; i<NUM_TASK; i++) {
        tasks[i].ap = 0;
        tasks[i].release_time = 0;
        tasks[i].flow_size = (i<NUM_TASK/2) ? 1001 : 1000;
        tasks[i].compute_time[0] = tasks[i].compute_time[1] = tasks[i].flow_size;
        tasks[i].deadline = 1001 * (i+2);
    }
}

// tasks should be ordered by release time
void load_tasks_original()
{
    FILE *fp = fopen("tasks.csv", "r");
    long first_timestamp = 0;
    for (long i=0; i<NUM_TASK; i++) {
        char str[256];
        assert(fgets(str, sizeof(str), fp));
        long timestamp;
        assert(sscanf(str, "%ld", &timestamp) == 1);
        if (i == 0)
            first_timestamp = timestamp;
        char *p = str;
        for (long i=0; i<10; i++) {
            while (*p++ != ',');
        }
        double compute_time;
        assert(sscanf(p, "%lf", &compute_time) == 1);

#ifndef ZIPF_DISTRIBUTION // uniform distribution
        long ap = rand() % avaPoint;
#else // generate Zipf distribution
        long ap = zipf_rand(avaPoint);
#endif
        tasks[i].ap = ap;
        tasks[i].release_time = (timestamp - first_timestamp) / TASK_FREQUENCY;
        tasks[i].flow_size = rand_range(MAX_RANDOM_FLOW_SIZE, MIN_RANDOM_FLOW_SIZE);
        long min_necessary_time = MAX_TIME;
        for (long server=0; server<avaPoint; server++) {
            if (is_server[server]) {
                tasks[i].compute_time[server] = (double)compute_time * 1e6 * float_rand_range(MAX_RANDOM_COMPUTE_FACTOR, MIN_RANDOM_COMPUTE_FACTOR);

                long upload_time = tasks[i].flow_size / shortest_path_bandwidth[ap][server] + shortest_len[ap][server];
                long download_time = shortest_len[server][ap];
                long necessary_time = upload_time + tasks[i].compute_time[server] + download_time;
                if (necessary_time < min_necessary_time)
                    min_necessary_time = necessary_time;
            }
        }
        tasks[i].deadline = tasks[i].release_time + min_necessary_time * float_rand_range(MAX_RANDOM_DEADLINE_FACTOR, MIN_RANDOM_DEADLINE_FACTOR);

//#define PRINT_TASKS
#ifdef PRINT_TASKS
        printf("task %3ld ap %3ld release_time %10ld flow_size %6ld necessary_time %10ld deadline %10ld (ratio %lf)\n",
            i, tasks[i].ap, tasks[i].release_time, tasks[i].flow_size, min_necessary_time, tasks[i].deadline, (tasks[i].deadline - tasks[i].release_time) * 1.0 / min_necessary_time);
#endif
    }
    fclose(fp);
}
*/

void load_graph(char *filename)
{
    init_graph();

    FILE *fp = fopen(filename, "r");
    if (fp == NULL) {
        printf("Error opening file %s\n", filename);
        exit(1);
    }

    const size_t max_str_length = 100 * 1024;
    char *str = (char *) malloc(max_str_length);
    assert (str != NULL);

    vector<int> types[avaPoint];
    vector<double> resources[avaPoint];

    while (fgets(str, max_str_length, fp)) {
        assert (strlen(str) < max_str_length - 1);

        if (strncmp(str, "cn", 2) == 0) {
            long node;
            if (sscanf(str, "cn \"%ld\"", &node) == 1) {
                if (node >= avaPoint) // ignore
                    continue;

                char *s = strstr(str, "restcap");
                if (s != NULL) {
                    s += 9;
                    double capacity;
                    if (sscanf(s, "%lf", &capacity) == 1) {
                        cloudlet_capacity[node] = capacity * appNum_w;
                    }
                }

                s = strstr(str, "opencost");
                if (s != NULL) {
                    s += 10;
                    double cost;
                    if (sscanf(s, "%lf", &cost) == 1) {
                        opencost[node] = cost;
                    }
                }

                s = strstr(str, "configNum");
                if (s != NULL) {
                    s += 11;
                    int configNum;
                    if (sscanf(s, "%d", &configNum) == 1) {
                        node_apps[node] = (configNum > appNum_w ? appNum_w : configNum);
                    }
                }

                s = strstr(str, "type");
                if (s != NULL) {
                    s += 8;
                    char *type_str = strtok(s, ",");
                    while (type_str != NULL) {
                        int type;
                        if (sscanf(type_str, "%d", &type) == 1) {
                            types[node].push_back(type);
                        }
                        type_str = strtok(NULL, ",");
                    }
                }

                s = strstr(str, "resource");
                if (s != NULL) {
                    s += 12;
                    char *resource_str = strtok(s, ",");
                    while (resource_str != NULL) {
                        double resource;
                        if (sscanf(resource_str, "%lf", &resource) == 1) {
                            resources[node].push_back(resource);
                        }
                        resource_str = strtok(NULL, ",");
                    }
                }
            }
        }
        if (strncmp(str, "ce", 2) == 0) {
            long node1, node2;
            double weight;
            if (sscanf(str, "ce \"%ld_%ld\"  \"weight\":%lf", &node1, &node2, &weight) == 3) {
                assert(node1 != node2);
                if (node1 < avaPoint && node2 < avaPoint) {
                    latency[node1][node2] = latency[node2][node1] = weight;
                }
            }
        }
    }
    fclose(fp);

    for (unsigned node = 0; node < avaPoint; node++) {
        if (types[node].size() != resources[node].size()) {
            printf("Error! size mismatch of node %d: types size %ld, resources size %ld\n", node, types[node].size(), resources[node].size());
            continue;
        }

        map<int,double> & reqmap = node_attributes[node].requests;
        for (unsigned i = 0; i < types[node].size(); i++) {
            if (reqmap.count(types[node][i]) == 0) {
                if (types[node][i] < appNum_w)
                    reqmap.insert(make_pair(types[node][i], resources[node][i]));;
            }
            else {
                reqmap[types[node][i]] += resources[node][i];
            }
        }

    }

    compute_shortest_path();

    for (unsigned i = 0; i < avaPoint; i ++)
        for (unsigned j = 0; j < avaPoint; j ++)
            pathcost[i][j] = shortest_len[i][j];
}

void print_graph()
{
    for (unsigned node = 0; node < avaPoint; node ++) {
        printf("node %3d: apps %2d opencost %10lf capacity %10lf requestmap ", node, node_apps[node], opencost[node], cloudlet_capacity[node]);
        map<int,double> & reqmap = node_attributes[node].requests;
        for (auto it = reqmap.begin(); it != reqmap.end(); it++)
            printf("(%2d:%10lf) ", it->first, it->second);
        printf("\n");
    }
    print_shortest_path();
}

void print_placement_and_config(const vector<int>& placement_z, const vector<vector<int>>& configuration_x, double minCost)
{
    printf ("\n");
    chrono::duration<double> diff = (chrono::steady_clock::now() - start_time);
    printf ("trial %ld   time %f s   speed %f ms/trial\n", total_trials, diff.count(), diff.count() * 1e3 / total_trials);
    printf ("cost %lf\n", minCost);
    for (unsigned node = 0; node < avaPoint; node ++)
        if (placement_z[node]) {
            printf ("%3d: ", node);
            for (unsigned app = 0; app < appNum_w; app ++)
                if (configuration_x[node][app])
                    printf ("%2d ", app);
            printf ("\n");
        }
    printf ("\n");
}

int main(int argc, char **argv)
{
    if (argc != 2) {
        printf("Usage: %s <dgs file>\n", argv[0]);
        return 1;
    }
    load_graph(argv[1]);
    print_graph();

    vector<int> placement_z(avaPoint, 0);   //当placement_z[i]为1时代表图中的点i放置了服务器，为0则没有放置
    //configuration_x矩阵中每一行都代表图中的一个点，而每一行中的每一列代表某个点i是否配置了服务j，configuration_x[i][j] = 1说明，i点放置了服务器，且配置了服务j；
    //configuration_x矩阵中某一行中有1存在，则说明这一行代表的点被放置了服务器
    vector<vector<int>> configuration_x(avaPoint, vector<int>(appNum_w, 0));

    start_time = chrono::steady_clock::now();

    double minCost = placementFunction(placement_z, configuration_x);
    print_placement_and_config(placement_z, configuration_x, minCost);
    return 0;
}
