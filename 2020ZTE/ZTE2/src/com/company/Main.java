package com.company;

import java.io.*;
import java.util.*;
import java.util.List;
class Solution{
    //最大车道
    public static int maxtrain;
    //最大装载量
    public static double maxload;
    //重货占比
    public static double Wrate;
    public static int maxpathbfs=10;
    public static int maxpathdfs=15;
    public static int maxpaths=15;
    //轨道定位数组
    public static int  [][] roads=null;
    //捡货工余量表
    public static HashMap<Integer,Integer> stationWokerMap;
    static HashMap<itemIndex, Itempag> pagMap;
    static NavigableSet<Items> itemset ;
    public static HashMap<String,String> finalpath  = null;
    public static HashMap<Integer,Graph.Edge> graph;
    public Solution() {
        stationWokerMap = new HashMap<>();
        graph = new HashMap<>();
        finalpath = new HashMap<>();
        itemset =new TreeSet<>();
        pagMap = new HashMap<>();
    }
    static class Item implements Comparable<Item>{
        int name;
        int start;
        int end;
        double weight;
        List<Integer> ms ;
        public Item(int name, int start, int end, double weight, List<Integer> ms) {
            this.name = name;
            this.start = start;
            this.end = end;
            this.weight = weight;
            this.ms = ms;
        }
        @Override
        public String toString() {
            String ms="Z";
            if (this.ms!=null){
                ms+=this.ms.get(0);
                for (int i=1;i<this.ms.size();i++){
                    ms +=",Z"+this.ms.get(i);
                }
            }
            else ms="null";
            return "Item{" +
                    "name=" + name +
                    ", start=" + start +
                    ", end=" + end +
                    ", weight=" + weight +
                    "}\n" + ms+"\n" ;
        }

        @Override
        public int compareTo(Item o) {
            if (this.weight==o.weight){
                return Integer.compare(this.name,o.name);
            }
            return Double.compare(o.weight,this.weight);
        }
    }

    static class Items implements Comparable<Items>{
        int name;
        int start;
        int end;
        double weight;
        List<Item> items;
        int size = 0;
        double value=0;

        public Items(Item item) {
            this.name=item.name;
            this.start = item.start;
            this.end = item.end;
            this.weight = 0;
            items = new LinkedList<>();
            add(item);
            updatevalue();
        }

        private void updatevalue() {
            this.value = size+weight/maxload;
        }

        public boolean addable(double weight){
            return this.weight + weight < maxload;
        }
        public void add(Item item){
            size++;
            this.weight += item.weight;
            items.add(item);
            updatevalue();
        }
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Items items1 = (Items) o;
            return start == items1.start &&
                    end == items1.end &&
                    size == items1.size&&
                    Double.compare(items1.weight, weight) == 0 &&
                    items1.hashCode()==this.hashCode();
        }

        @Override
        public int hashCode() {
            int res=0;
            for (Item item:items){
                res += Integer.hashCode(item.name);
            }
            return res;
        }
        @Override
        public int compareTo(Items o) {
            if (this.value==o.value){
                return Integer.compare(this.name,o.name);
            }
            return Double.compare(o.value,this.value);
        }
        @Override
        public String toString() {

            return "Items{" +
                    "start=" + start +
                    ", end=" + end +
                    ", weight=" + weight +
                    ", size=" + size +
                    ", value=" + value +
                    '}'+"\n";
        }
    }

    static  class Itempag implements Comparable{
        int start;
        int end;
        int size;
        double weight;
        List<Integer> ms;
        List<Items> subpag;
        double value = 0.0;

        public Itempag(Item item) {
            this.size = 1;
            this.start = item.start;
            this.end = item.end;
            this.weight = item.weight;
            this.ms = item.ms;
            this.subpag = new LinkedList<>();
            this.subpag.add(new Items(item));
        }
        public void add(Item item){
            for (Items items: subpag){
                if (items.addable(item.weight)){
                    update(item.weight);
                    items.add(item);
                    return;
                }
            }
            subpag.add(new Items(item));
            update(item.weight);
        }
        public void update(double weight){
            this.size++;
            this.weight+=weight;
            this.value=(this.size+this.weight/100)/this.subpag.size();

        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Itempag that = (Itempag) o;
            return start == that.start &&
                    end == that.end &&
                    Double.compare(that.weight, weight) == 0 &&
                    Double.compare(that.value, value) == 0 &&
                    subpag.get(0).items.get(0)==that.subpag.get(0).items.get(0);
        }
        @Override
        public int compareTo(Object o) {
            Itempag that=(Itempag)o;
            return  this.value==that.value?that.weight==this.weight?Integer.compare(that.subpag.get(0).name,
                    subpag.get(0).name):
                    Double.compare(that.weight,weight):
                    Double .compare(that.value,this.value);
        }

        @Override
        public String toString() {
            return "itempag{" +
                    "start=" + start +
                    ", end=" + end +
                    ", weight=" + weight +
                    ", size=" + size+
                    ", value=" + value +
                    '}';
        }
    }
    static class itemIndex{
        int start;
        int end;
        String ms;

        public itemIndex(int start, int end, String ms) {
            this.start = start;
            this.end = end;
            this.ms = ms;
        }


        @Override
        public boolean equals(Object o) {
            itemIndex itemIndex = (itemIndex) o;
            return start == itemIndex.start &&
                    end == itemIndex.end &&
                    this.ms.equals(itemIndex.ms);
        }

        @Override
        public int hashCode() {
            return Integer.hashCode(start)+
                    Integer.hashCode(end)+
                    this.ms.hashCode();
        }
    }

    static class Graph {
        private final Map<Integer, Vertex> graph; // mapping of vertex names to Vertex objects, built from a set of Edges

        /** One edge of the graph (only used by Graph constructor) */
        static class Edge {
            //起止点
            public final int v1, v2;
            //代价
            public double dist;
            //各车辆占用表
            public BitSet trains;
            //各车辆运载量
            public double []loadMap;
            //当前总运载量
            public double hasweight;
            //拣货工位图
            public HashMap<Integer,BitSet> selectworkermap;
            //可用列车位图
            public BitSet usefulTrainmap;
            //下一跳信息<statinname，trainnum,nexttrainnum,nextstationname>>
            /**/
            public HashMap<Integer,BitSet> isStartEndmap;
            public HashSet<Integer>[][][] next;

            public Edge(int v1, int v2, int dist) {
                this.v1 = v1;
                this.v2 = v2;
                this.dist = dist;
                init();
            }
            private void init(){
                trains=new BitSet(Solution.maxtrain+1);
                trains.set(1, Solution.maxtrain+1);
                usefulTrainmap=new BitSet(Solution.maxtrain+1);
                loadMap=new double[Solution.maxtrain+1];
                Arrays.fill(loadMap,maxload);
                loadMap[0]=0;
                selectworkermap=new HashMap<>();
                selectworkermap.put(v1,new BitSet(maxtrain+1));
                selectworkermap.put(v2,new BitSet(maxtrain+1));
                isStartEndmap = new HashMap<>();
                isStartEndmap.put(v1,new BitSet(maxtrain+1));
                isStartEndmap.put(v2,new BitSet(maxtrain+1));

                next = new HashSet[2][maxtrain+1][maxtrain+1];
            }
            public void update(){
                this.dist =80+hasweight/maxload*maxtrain;
            }
            public void update(double weight){
                this.dist += 1+weight/maxload*maxtrain;
            }
            public void setSelectworker(int stationname,int trainnumber){
                if (!isSelectworker(stationname,trainnumber)){
                    selectworkermap.get(stationname).set(trainnumber);
                    Solution.stationWokerMap.put(stationname,Solution.stationWokerMap.get(stationname)-1);
                }
            }
            public void setUsefulTrainmap(double weight){
                usefulTrainmap.clear();
                for (int i=maxtrain;i>0;i--){
                    if (loadMap[i]>=weight){
                        usefulTrainmap.set(i);
                    }
                }
            }
            public void setStartEndmap(int stationname,int trainnum){

                isStartEndmap.get(stationname).set(trainnum);
            }
            public void setNext(int trainnum, int stationname,int nextstationname,int nexttrainnum){
                int intdex=stationname==v2?1:0;
                if(next[intdex][trainnum][nexttrainnum]==null){
                    next[intdex][trainnum][nexttrainnum]=new HashSet<Integer>();
                    next[intdex][trainnum][nexttrainnum].add(nextstationname);
                }
                else {
                    next[intdex][trainnum][nexttrainnum].add(nextstationname);
                }
            }
            public void setTrian(int triannumber,double weight){
                loadMap[triannumber] -=weight;
                hasweight +=weight;
                /*update(weight);*/
            }
            public boolean isSelectworker(int staionname,int trainnumber){
                return selectworkermap.get(staionname).get(trainnumber);
            }

            public boolean isStartEnd(int stationname,int trainnum){
                return isStartEndmap.get(stationname).get(trainnum);
            }
            public int getUsefulTrain(double weight){

                for (int i=0;i<=maxtrain;i++){
                    if (this.loadMap[i]>=weight){
                        if (isSelectworker(v1,i)||isSelectworker(v2,i))
                            return i;
                    }
                }
                setUsefulTrainmap(weight);
                return usefulTrainmap.nextSetBit(0);
            }

            /*返回当前列车是否有下一跳*/
            public boolean hasnext(int trainnum, int stationname){
                int intdex=stationname==v2?1:0;
                /* System.out.println(maxtrain);*/
                for (int i=1;i<=maxtrain;i++){
                    if (next[intdex][trainnum][i]!=null){
                        return true;
                    }
                }
                return false;
            }
            public int getintdex(int stationname){
                return  stationname==v2?1:0;
            }

        }

        static class Vertex implements Comparable<Vertex>{
            public final int name;
            public double dist = Double.MAX_VALUE; // MAX_VALUE assumed to be infinity
            public Vertex previous = null;
            public boolean isvisited ;
            public final Map<Vertex, Double> neighbours = new HashMap<>();
            public Vertex(int name)
            {
                this.name = name;
                this.isvisited=false;
            }

            private void getPath(List<Integer> list)
            {
                if (this == this.previous)
                {
                    list.add(this.name);
                }
                else if (this.previous == null)
                {
                    list = null;
                }
                else
                {
                    this.previous.getPath(list);
                    list.add(this.name);
                }
            }

            public int compareTo(Vertex other)
            {
                if (dist == other.dist)
                    return Integer.compare(this.name, other.name);
                return Double.compare(dist, other.dist);
            }

            @Override public String toString()
            {
                return "(" + name + ", " + dist + ")";
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                Vertex vertex = (Vertex) o;
                return name == vertex.name;
            }

            @Override
            public int hashCode() {
                return Objects.hash(name);
            }
        }


        public Graph(HashMap<Integer,Edge> edges) {
            /*<String vextex>*/
            graph = new HashMap<>(edges.size());

            //one pass to find all vertices
            for (Map.Entry<Integer,Edge> e :edges.entrySet()) {
                if (!graph.containsKey(e.getValue().v1)) graph.put(e.getValue().v1, new Vertex(e.getValue().v1));
                if (!graph.containsKey(e.getValue().v2)) graph.put(e.getValue().v2, new Vertex(e.getValue().v2));
            }

            //another pass to set neighbouring vertices
            for (Map.Entry<Integer,Edge> e :edges.entrySet()) {
                graph.get(e.getValue().v1).neighbours.put(graph.get(e.getValue().v2), e.getValue().dist);
                graph.get(e.getValue().v2).neighbours.put(graph.get(e.getValue().v1), e.getValue().dist); // also do this for an undirected graph
            }
        }

        public List<Integer> BFS(int startName,List<Integer> ms){
            if (!graph.containsKey(startName)) {
                return null;
            }
            Vertex source = graph.get(startName);
            Vertex destination = graph.get(ms.get(0));

            List<Integer> path= new LinkedList<>();
            if (BFS(source,destination,path)){
                path.remove(path.size()-1);
                Init();
                for (int k=0;k<path.size();k++){
                    graph.get(path.get(k)).isvisited=true;
                }
                for (int i=1;i<ms.size();i++){
                    source=destination;
                    destination=graph.get(ms.get(i));
                    if (BFS(source,destination,path)){
                        path.remove(path.size()-1);
                        Init();
                        for (int k=0;k<path.size();k++){
                            graph.get(path.get(k)).isvisited=true;
                        }
                    }
                    else{
                        return null;
                    }
                }
            }
            path.add(ms.get(ms.size()-1));
            return path;
        }
        public void Init(){
            for (Map.Entry<Integer, Vertex> v : graph.entrySet()){
                v.getValue().isvisited=false;
            }
        }
        public boolean BFS(Vertex source,Vertex destination,List<Integer> path){
            source.isvisited=true;
            source.previous=source;
            Queue<Vertex> queue = new LinkedList<>();
            queue.add(source);
            while (!queue.isEmpty()){
                Vertex v = queue.poll();
                v.isvisited=true;
                for (Map.Entry<Vertex,Double>  entry: v.neighbours.entrySet()){
                    Vertex u=entry.getKey();
                    if (!u.isvisited){
                        u.isvisited=true;
                        u.previous=v;
                        if (u==destination){
                            u.getPath(path);
                            return true;
                        }
                        queue.add(u);
                    }
                }
            }
            return false;
        }

        public List<Integer> dijkstra(int startName,int endName) {
            if (!graph.containsKey(startName)) {
                return null;
            }
            final Vertex source = graph.get(startName);
            NavigableSet<Vertex> q = new TreeSet<>();
            // set-up vertices
            for (Vertex v : graph.values()) {
                v.previous = v == source ? source : null;
                v.dist = v == source ? 0 : Integer.MAX_VALUE;
                q.add(v);
            }
            return  dijkstra(q,endName);
        }


        private List<Integer> dijkstra(final NavigableSet<Vertex> q,int endname) {
            Vertex u, v;
            while (!q.isEmpty()) {
                u = q.pollFirst(); // vertex with shortest distance (first iteration will return source)
                if (u.dist == Integer.MAX_VALUE) {
                    return null;
                }
                if (u.name==endname) {
                    List<Integer> path= new ArrayList<Integer>();
                    u.getPath(path);
                    return path;
                }

                // we can ignore u (and any other remaining vertices) since they are unreachable

                //look at distances to each neighbour
                for (Map.Entry<Vertex, Double> a : u.neighbours.entrySet()) {
                    v = a.getKey(); //the neighbour in this iteration

                    final double alternateDist = u.dist + a.getValue();
                    if (alternateDist < v.dist) { // shorter path to neighbour found
                        q.remove(v);
                        v.dist = alternateDist;
                        v.previous = u;
                        q.add(v);
                    }
                }
            }
            return null;
        }

        public  NavigableSet<List<Integer>> DFS(Integer startName, Integer endName){
            if (!graph.containsKey(startName)) {
                System.err.printf("Graph doesn't contain start vertex \"%s\"\n", startName);
                return null;
            }
            final Vertex source = graph.get(startName);
            final Vertex destination = graph.get(endName);
            class Listcomperator implements Comparator<List<Integer>>{
                @Override
                public int compare(List<Integer> o1, List<Integer> o2) {
                    return o1.size()-o2.size();
                }
            }
            Listcomperator comp=new Listcomperator();
            /**  arrange paths by size()*/
            NavigableSet<List<Integer>> paths= new TreeSet<List<Integer>>(comp);
            List<Integer > path = new LinkedList<>();
            DFS(source, destination, paths,path);
            return paths;
        }

        public void DFS(Vertex source, Vertex destination, NavigableSet<List<Integer>> paths, List<Integer> path){
            /*最多可行路径*/
            if (paths.size()>maxpathdfs||path.size()>maxpaths){
                return;
            }
            /*********************DEBUG***********************/
//            System.out.println(source.name);
            source.isvisited=true;
            path.add(source.name);
            if (source==destination){
                List<Integer> list=new LinkedList<>(path);
                paths.add(list);
            }
            else {
                for (Map.Entry<Vertex,Double> entry:source.neighbours.entrySet()){
                    if (!entry.getKey().isvisited){
                        DFS(entry.getKey(),destination,paths,path);
                    }
                }
            }
            /* 回溯 */
            source.isvisited=false;
            path.remove(path.size()-1);
        }

        public  NavigableSet<List<Integer>> DFSnr(Integer startName, Integer endName){
            if (!graph.containsKey(startName)) {
                System.err.printf("Graph doesn't contain start vertex \"%s\"\n", startName);
                return null;
            }
            final Vertex source = graph.get(startName);
            final Vertex destination = graph.get(endName);
            class Listcomperator implements Comparator<List<Integer>>{
                @Override
                public int compare(List<Integer> o1, List<Integer> o2) {
                    return o1.size()-o2.size();
                }
            }
            Listcomperator comp=new Listcomperator();
            /**  arrange paths by size()*/
            NavigableSet<List<Integer>> paths= new TreeSet<List<Integer>>(comp);
            List<Integer > path = new LinkedList<>();
            DFS(source, destination, paths);
            return paths;

        }

        public void DFS(Vertex source, Vertex destination, NavigableSet<List<Integer>> paths){
            Stack<Vertex> stack = new Stack<>();
            stack.push(source);
            LinkedList<Integer> path=new LinkedList<Integer>();
            while (!stack.isEmpty()){
                if (paths.size()>maxpathdfs){
                    while (!stack.isEmpty()){
                        stack.pop().isvisited=false;
                    }
                    break;
                }
                if (path.size()>maxpaths){
                    Vertex u=stack.pop();
                    if (u.isvisited){
                        u.isvisited=false;
                        path.removeLast();
                    }
                    while (!stack.isEmpty()&&!u.isvisited){
                        u=stack.pop();
                    }
                    stack.push(u);
                    continue;
                }
                Vertex v = stack.pop();
                if (v.isvisited){
                    v.isvisited=false;
                    path.remove(path.size()-1);
                    continue;
                }
                v.isvisited=true;
                path.add(v.name);
                if (v==destination){
                    List<Integer> list=new LinkedList(path);
                    paths.add(list);
                    v.isvisited=false;
                    path.remove(path.size()-1);
                }
                else{
                    stack.push(v);
                    for (Map.Entry<Vertex,Double> entry:v.neighbours.entrySet()){
                        if (!entry.getKey().isvisited){
                            stack.push(entry.getKey());
                        }
                    }
                }
            }
        }

        public  NavigableSet<List<Integer>> BFS(Integer startName,Integer endName){
            if (!graph.containsKey(startName)) {
                return null;
            }
            final Vertex source = graph.get(startName);
            final Vertex destination = graph.get(endName);


            class Listcomperator implements Comparator<List<Integer>>{
                @Override
                public int compare(List<Integer> o1, List<Integer> o2) {
                    return o1.size()-o2.size();
                }
            }
            Listcomperator comp=new Listcomperator();
            /**  arrange paths by size()*/
            NavigableSet<List<Integer>> paths= new TreeSet<List<Integer>>(comp);
            List<String > path = new LinkedList<>();
            BFS(source, destination, paths);
            return paths;
        }

        private void BFS(Vertex source, Vertex destination, NavigableSet<List<Integer>> paths) {
            source.previous=source;
            Queue<Vertex> queue=new LinkedList<>();
            queue.add(source);
            while (!queue.isEmpty()){
                if (paths.size()>=maxpathbfs){
                    break;
                }
                Vertex v = queue.poll();
                v.isvisited=true;
                for (Map.Entry<Vertex,Double> entry:v.neighbours.entrySet()){
                    Vertex u=entry.getKey();
                    if (!u.isvisited){
                        u.previous=v;
                        u.isvisited = true;
                        if ( u == destination ){
                            List<Integer> path = new ArrayList<Integer>();
                            u.getPath(path);
                            paths.add(path);
                            u.isvisited=false;
                            break;
                        }
                        queue.add(u);
                    }
                }
            }
        }

    }
    /*检查起点终点*/
    public boolean checkStartEnd(int station,Graph.Edge edge,int trainnum){
        int needworker=0;
        needworker += edge.isSelectworker(station,trainnum)?0:1;
        int index = edge.getintdex(station);
        if (edge.hasnext(trainnum,station)){
            for (int k=1;k<=maxtrain;k++){
                if (edge.next[index][trainnum][k]!=null){
                    for (Integer next:edge.next[index][trainnum][k]){
                        needworker += graph.get(roads[station][next]).isSelectworker(station,k)?0:1;
                    }
                }
            }
        }
        if (stationWokerMap.get(station)<needworker){
            return false;
        }
        return true;
    }

    public void arrangeStartEnd(int station,Graph.Edge edge,int trainnum){
        edge.setSelectworker(station,trainnum);
        int index = edge.getintdex(station);
        if (edge.hasnext(trainnum,station)){
            for (int k=1;k<=maxtrain;k++){
                if (edge.next[index][trainnum][k]!=null){
                    for (Integer next:edge.next[index][trainnum][k]){
                        graph.get(roads[station][next]).setSelectworker(station,k);
                    }
                }
            }
        }
    }
    /*检查所有中间节点*/
    public boolean checkMid(int []road,List<Integer> path,int [] trainnums){
        int needworker=0;
        int maxworker;
        int laststation;
        int station;
        int nextstation;
        int trainnum;
        int trainnum1;
        Graph.Edge edge;
        Graph.Edge edge1;
        for (int i=1;i<road.length;i++){
            laststation=path.get(i-1);
            station=path.get(i);
            nextstation=path.get(i+1);
            edge=graph.get(road[i-1]);
            edge1=graph.get(road[i]);
            trainnum=trainnums[i-1];
            trainnum1=trainnums[i];
            needworker=0;
            maxworker=stationWokerMap.get(station);
            /*  检查next  */
            int index=edge.getintdex(station);
            boolean flag=true;
            if (edge.hasnext(trainnum,station)){
                for (int k=1;k<=maxtrain;k++){
                    if (edge.next[index][trainnum][k]!=null){
                        for (Object next:edge.next[index][trainnum][k]){
                            if (!next.equals(nextstation)||k!=trainnum1){
                                needworker+=graph.get(roads[station][(int)next]).
                                        isSelectworker(station,k)?0:1;
                            }
                            else flag=false;
                        }
                    }
                }
            }
            else flag=false;

            int index1=edge1.getintdex(station);
            boolean flag1=true;
            if (edge1.hasnext(trainnum1,station)){
                for (int k=1;k<=maxtrain;k++){
                    if (edge1.next[index1][trainnum1][k]!=null){
                        for (Integer next:edge1.next[index1][trainnum1][k]){
                            if (!(next.equals(laststation)&&k==trainnum)){
                                needworker+= graph.get(roads[station][next]).
                                        isSelectworker(station,k)?0:1;
                            }
                            else flag1=false;
                        }
                    }
                }
            }
            else  flag1=false;
            if ((flag||flag1)|| (edge.isStartEnd(station,trainnum)||
                    edge1.isStartEnd(station,trainnum1))||trainnum!=trainnum1){
                needworker += edge.isSelectworker(station,trainnum)?0:1;
                needworker += edge1.isSelectworker(station,trainnum1)?0:1;

            }
            /*检查路径上是否有足够拣货员*/
            /*System.out.println(maxworker+"       "+needworker);*/
            if (maxworker<needworker){
                return false;
            }
        }
        return true;
    }
    /*扣除资源*/
    public String arrange(int[] road,List<Integer> path,double weight,int []trainnums){

        int laststation;
        int station;
        int nextstation;
        Graph.Edge edge;
        Graph.Edge edge1;
        StringBuilder outtrain=new StringBuilder();
        int trainnum=trainnums[0];
        int trainnum1;
        outtrain.append(trainnum);
        graph.get(road[0]).setTrian(trainnum,weight);
        graph.get(road[0]).trains.clear(trainnum);
        for (int i=1;i<road.length;i++){
            edge=graph.get(road[i-1]);
            edge1=graph.get(road[i]);
            trainnum=trainnums[i-1];
            trainnum1=trainnums[i];
            laststation=path.get(i-1);
            station=path.get(i);
            nextstation=path.get(i+1);
            /*设置中间站点 拣货员*/
            int index=edge.getintdex(station);
            boolean flag=true;
            if (edge.hasnext(trainnum,station)){
                for (int k=1;k<=maxtrain;k++){
                    if (edge.next[index][trainnum][k]!=null){
                        for (Integer next:edge.next[index][trainnum][k]){
                            if (next!=nextstation||k!=trainnum1){
                                graph.get(roads[station][(int)next]).
                                        setSelectworker(station,k);
                            }
                            else {
                                flag=false;
                            }
                        }
                    }
                }
            } else flag=false;

            boolean flag1=true;
            int index1=edge1.getintdex(station);
            if (edge1.hasnext(trainnum1,station)){
                for (int k=1;k<=maxtrain;k++){
                    if (edge1.next[index1][trainnum1][k]!=null)
                        for (Integer next:edge1.next[index1][trainnum1][k]){
                            if (next!=laststation||k!=trainnum){
                                graph.get(roads[station][(int)next]).
                                        setSelectworker(station,k);
                            }else {
                                flag1=false;
                            }
                        }
                }
            } else flag1=false;

            //设置station
            if (flag||flag1|| edge.isStartEnd(station,trainnum)
                    ||edge1.isStartEnd(station,trainnum1)||trainnum!=trainnum1){
                edge.setSelectworker(station,trainnum);
                edge1.setSelectworker(station,trainnum1);
            }
            outtrain.append(",").append(trainnum1);

            edge1.setTrian(trainnum1,weight);

            edge1.trains.clear(trainnum1);

            edge.setNext(trainnum,station,nextstation,trainnum1);

            edge1.setNext(trainnum1,station,laststation,trainnum);

        }
        /*起点终点 扣除拣货员 标识起止点*/
        edge=graph.get(road[0]);
        edge1=graph.get(road[road.length-1]);
        arrangeStartEnd(path.get(0),edge,trainnums[0]);
        arrangeStartEnd(path.get(path.size()-1),edge1,trainnums[road.length-1]);
        edge.setStartEndmap(path.get(0),trainnums[0]);
        edge1.setStartEndmap(path.get(path.size()-1),trainnums[road.length-1]);
        return outtrain.toString();
    }

    /*读取数据*/
    public  void  read(String path){
        try {
            BufferedReader in =!path.equals("")?new BufferedReader(new FileReader(path)):
                    new BufferedReader(new InputStreamReader(System.in));
            String str = in.readLine();
            String [] strs=str.split(",");
            int stasionNum = Integer.parseInt(strs[0]);
            int edgeNum = Integer.parseInt(strs[1]);
            maxtrain=Integer.parseInt(strs[2]);
            maxload=Double.parseDouble(strs[3]);

            for (int i=0;i<stasionNum;i++){
                str=in.readLine();
                strs=str.split(",");
                int name=Integer.parseInt(strs[0].substring(1));
                stationWokerMap.put(name,Integer.parseInt(strs[1]));
            }

            roads=new int [stasionNum][stasionNum];
            graph=new HashMap<>();
            for(int i=1;i<edgeNum+1;i++){
                str=in.readLine();
                strs=str.split(",");
                int name=Integer.parseInt(strs[0].substring(1));
                int v1=Integer.parseInt(strs[1].substring(1));
                int v2=Integer.parseInt(strs[2].substring(1));
                roads[v1][v2]=name;
                roads[v2][v1]=name;
                graph.put(name,new Graph.Edge(v1,v2,1));
            }

            str = in.readLine();
            int itemNum=Integer.parseInt(str);
            String ms;
            int count=0;
            for (int i=1;i<itemNum+1;i++){
                str=in.readLine();
                strs=str.split(",");
                int name=Integer.parseInt(strs[0].substring(1));
                int v1=Integer.parseInt(strs[1].substring(1));
                int v2=Integer.parseInt(strs[2].substring(1));
                int start = Math.min(v1,v2);
                int end = start==v1?v2:v1;
                double weight=Double.parseDouble(strs[3]);
                if (weight>=80){
                    count++;
                }
                LinkedList<Integer> list = null;
                if (!strs[4].equals("null")){
                    ms = "";
                    list=new LinkedList<>();
                    for (int j = 4;j<strs.length;j++){
                        ms += strs[j];
                        list.add(Integer.parseInt(strs[j].substring(1)));
                    }
                    list.add(end);
                }
                else ms="null";
                Item item=new Item(name,start,end,weight,list);
                itemIndex index=new itemIndex(start,end,ms);
                if (pagMap.containsKey(index)){
                    pagMap.get(index).add(item);
                }
                else {
                    pagMap.put(index,new Itempag(item));
                }
            }
            Wrate=(double)count/itemNum;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*考虑拼车*/
    private String findPathwithchange(int []road, double weight, List<Integer> path) {
//        return  "null";
        String res="null";
        Graph.Edge edge;
        Graph.Edge edge1;
        int []trainnums=new int [road.length];
        BitSet set = new BitSet(maxtrain+1);
        set.set(1,maxtrain);
        for (int value : road) {
            edge = graph.get(value);
            edge.setUsefulTrainmap(weight);
            set.and(edge.usefulTrainmap);
        }
        if (set.cardinality()!=0){
            int trainnum=set.nextSetBit(0);

            /*首尾站点检查*/
            while(trainnum!=-1){
                Arrays.fill(trainnums, trainnum);
                if (!checkStartEnd(path.get(0),graph.get(road[0]),trainnum)
                        ||!checkStartEnd(path.get(path.size()-1),graph.get(road[road.length-1]),trainnum)){
                    trainnum = set.nextSetBit(trainnum+1);
                    continue;
                }
                /*分叉  汇聚双向检查*/
                if (!checkMid(road,path,trainnums)){
                    trainnum = set.nextSetBit(trainnum+1);
                }else {
                    /*扣除拣货员  构建列车图*/
                    res = arrange(road,path,weight,trainnums);
                    break;
                }
            }
        }
        else {
            for (int i=0;i<road.length;i++){
                trainnums[i] = graph.get(road[i]).getUsefulTrain(weight);
                if(trainnums[i]==-1){
                    return "null";
                }
            }
//            首尾站点检查
            if (!checkStartEnd(path.get(0),graph.get(road[0]),trainnums[0])
                    ||!checkStartEnd(path.get(path.size()-1),
                    graph.get(road[road.length-1]),trainnums[trainnums.length-1])){
                return  "null";
            }
//            分叉  汇聚双向检查
            if (!checkMid(road,path,trainnums)){
                return "null";
            }else {
//                扣除拣货员  构建列车图
                res=arrange(road,path,weight,trainnums);
            }
        }
        return res;
    }
    /*独占 直达*/
    public String findpathsolo(int []road,List<Integer> path,double weight){
        int start=path.get(0);
        int end=path.get(path.size()-1);
        String outtrain ="";
        BitSet set = new BitSet(maxtrain);
        set.set(0, maxtrain+1);
        set.and(graph.get(road[0]).trains);
        for (int i = 1; i < path.size() - 1; i++) {
            set.and(graph.get(road[i]).trains);
        }
        //规划车辆号码
        if (set.cardinality()> 0) {
//                       不需要拣货员
//                       选择列车
            int trainnum = set.nextSetBit(0) ;
            /*首尾检查*/
            if (!checkStartEnd(start,graph.get(road[0]),trainnum)||
                    !checkStartEnd(end,graph.get(road[road.length-1]),trainnum)){
                return "null";
            }
            /*占用车道*/
            graph.get(road[0]).trains.clear(trainnum);
            /*扣除装载量*/
            graph.get(road[0]).setTrian(trainnum,weight);
            /*setnext*/

            outtrain += trainnum;
            for (int i = 1; i < road.length; i++) {
                //设置下一跳信息
                /*占用车道*/
                graph.get(road[i]).trains.clear(trainnum);
                /*扣除装载量*/
                graph.get(road[i]).setTrian(trainnum,weight);
                /*setnext last*/
                graph.get(road[i-1]).setNext(trainnum,path.get(i),path.get(i+1),trainnum);
                /*setlast last*/
                graph.get(road[i]).setNext(trainnum,path.get(i),path.get(i-1),trainnum);
                outtrain += "," + trainnum;
            }
            //安排装卸工
            arrangeStartEnd(start,graph.get(road[0]),trainnum);
            arrangeStartEnd(end,graph.get(road[road.length-1]),trainnum);
            graph.get(road[0]).setStartEndmap(start,trainnum);
            graph.get(road[road.length-1]).setStartEndmap(end,trainnum);
        }
        else {
            outtrain= "null";
        }
        return outtrain;
    }
    public List<Integer> subfindms(int start,List<Integer> list){
        List<Integer>[] path;
        path=new List[list.size()];
        for (int i=0;i<list.size();i++){
            Graph g=new Graph(graph);
            path[i]=g.dijkstra(start,list.get(i));
            start=list.get(i);
        }
        List<Integer> res=path[0];
        for (int i=1;i<path.length;i++){
            if (path[i]!=null){
                for (int k=1;k<path[i].size();k++){
                    res.add(path[i].get(k));
                }
            }
        }
        return checkpath(res)?res:null;
    }

    public boolean checkpath(List<Integer> res){
        TreeSet<Integer> set=new TreeSet<>();
        for (Integer i:res){
            if (set.contains(i)){
                return false;
            }
            else {
                set.add(i);
            }
        }
        return true;
    }

    /**
     * get right trainnumber in path
     * */

    public String getTrain(List<Integer> path,double weight,int[] road,double value,boolean islimit){
        String outtrain="null";
        if (!islimit||value>(Wrate>0.2?2.0:Wrate>0.1?2.5:Wrate<0.09?1.4:0)){
            outtrain=findpathsolo(road,path,weight);
        }
        if (outtrain.equals("null")) {
            //需要拼车
            outtrain=findPathwithchange(road,weight,path);
        }
        return outtrain;
    }


    /**
     * print rsult
     * */
    public  void print(String head) {
        System.out.print(head);
        for (Map.Entry<String,String> e:finalpath.entrySet()){
            System.out.print(e.getValue());
        }
    }



    public void writePath(Items cur,String path,String train){
        for (Item item:cur.items){
            String name = "G"+item.name;
            finalpath.put(name,name+"\n"+path+"\n"+train+"\n");
        }
    }

    public void writePath(Item cur,String path,String train){
        String name = "G"+cur.name;
        finalpath.put(name,name+"\n"+path+"\n"+train+"\n");
    }

    /**
     * get road form List path
     * */

    public int [] getRoad(List<Integer> path){
        int [] road = new int [path.size()-1];
        for (int i=0 ; i<road.length; i++){
            road[i]=roads[path.get(i)][path.get(i+1)];
        }
        return road;
    }

    /***
     * get path  from road
     * */

    public  String getpath(int []road){
        String res = "";
        res += "R"+road[0];
        for (int i=1; i<road.length; i++){
            res += ",R"+ road[i];
        }
        return res;
    }


    /**
     *first time
     */

    public void findpath(TreeSet<Items> falseItem){
        String name;
        String outpath= "";
        String outtrian= "null";
        for (Items cur:itemset){
            outpath="null";
            outtrian="null";
            List<Integer> path = new LinkedList<>();
            int [] road=null;
            Graph g=new Graph(graph);
            if (cur.items.get(0).ms==null){
                path=g.dijkstra(cur.start,cur.end);
            }else {
                path=g.BFS(cur.start,cur.items.get(0).ms);
            }
            if (path!=null&&!path.isEmpty()){
                road = getRoad(path);
                outtrian = getTrain(path,cur.weight,road,cur.value,true);
            }
            if (outtrian.equals("null")){
                falseItem.add(cur);

            }else {
                outpath= getpath(road);
            }
            writePath(cur,outpath,outtrian);
        }
    }
    /**
     *second time
     * @param
     */
    public void findpath1(TreeSet<Items> falseItems) {
        String name;
        String outpath = "";
        String outtrain ="";
        Iterator<Items> iterator = falseItems.iterator();
        while (iterator.hasNext()){
            Items cur= iterator.next();
            outpath = "";
            outtrain = "null";
            List<Integer> path;

            int []road =null;
            if (cur.items.get(0).ms==null){
                Graph g=new Graph(graph);
                NavigableSet<List<Integer>>
                        paths =  g.BFS(cur.start,cur.end);
                while (!paths.isEmpty()){
                    outpath = "";
                    path=paths.pollFirst();
                    road= getRoad(path);
                    outtrain=findpathsolo(road,path,cur.weight);
                    if (outtrain.equals("null")){
                        outtrain=findPathwithchange(road,cur.weight,path);
                    }
                    if(!outtrain.equals("null")){
                        break;
                    }
                }
            }
            else {
                List<List<Integer>> paths=subfindmsOneStep(cur.start,cur.end,cur.end,cur.items.get(0).ms);
                if (paths!=null){
//                    System.out.println(paths.size());
                    for (List<Integer> integers : paths) {
                        path = integers;
                        road = getRoad(path);
                        outtrain = getTrain(path, cur.weight,road,cur.value,false);
                        if (!outtrain.equals("null")) {
                            break;
                        }
                    }
                }
            }
            if (!outtrain.equals("null")){
                outpath=getpath(road);
                iterator.remove();
                writePath(cur,outpath,outtrain);
            }
        }
    }

    public void findpath2(TreeSet<Item> falseItem) {

        for (Map.Entry<Integer,Graph.Edge> e:graph.entrySet()){
            e.getValue().update();
        }

        String name;
        String outpath = "";
        String outtrain ="";
        Iterator<Item> iterator = falseItem.iterator();
        while (iterator.hasNext()){
            Item cur= iterator.next();
            name = "G"+cur.name;
            outpath = "";
            outtrain = "null";
            List<Integer> path;

            int []road =null;
            if (cur.ms==null){
                Graph g=new Graph(graph);
                NavigableSet<List<Integer>>
                        paths =  g.DFSnr(cur.start,cur.end);
                while (!paths.isEmpty()){
                    outpath = "";
                    path=paths.pollFirst();
                    road= getRoad(path);
                    outtrain=findpathsolo(road,path,cur.weight);
                    if (outtrain.equals("null")){
                        outtrain=findPathwithchange(road,cur.weight,path);
                    }
                    if(!outtrain.equals("null")){
                        break;
                    }
                }
            }
            else {
                List<List<Integer>> paths=subfindmsOneStep(cur.start,cur.end,cur.weight,cur.ms);
                if (paths!=null){
//                    System.out.println(paths.size());
                    for (List<Integer> integers : paths) {
                        path = integers;
                        road = getRoad(path);
                        outtrain = getTrain(path, cur.weight,road,1,false);
                        if (!outtrain.equals("null")) {
                            break;
                        }
                    }
                }
            }
            if (!outtrain.equals("null")){
                outpath=getpath(road);
                iterator.remove();
                writePath(cur,outpath,outtrain);
            }
        }
    }
    /**
     * use dfs find path with ms
     *@param
     *@return
     */
    public List<List<Integer>> subfindmsOneStep(int start,int end,double weight,List<Integer> ms){
        List<Integer> list = ms;
        NavigableSet<List<Integer>> paths=null;
        HashSet<Integer> set=new HashSet<>();
        List<List<Integer>> res = new ArrayList<>();
        boolean isok = false;
        for (Integer i:list){
            set.add(i);
        }
        Graph g=new Graph(graph);
        paths=g.DFSnr(start,end);
        if (paths.isEmpty()){
            return null;
        }
        while (!paths.isEmpty()){
            List<Integer> path = paths.pollFirst();
            if (checkpath(path,set)){
                res.add(path);
            }
        }
        return res;
    }

    /**
     * in order check
     *@param path and points wich must be passed
     *@return isiligal
     */

    public boolean checkpath(List<Integer> path,HashSet<Integer> set){
        int count=0;
        for ( Integer i:path){
            if (set.contains(i)){
                count++;
            }
        }
        return count==set.size();
    }

    /*拆包*/
    public void classify(){
        for (Map.Entry<itemIndex, Itempag> e : pagMap.entrySet() ){
            for (Items i:e.getValue().subpag){
                itemset.add(i);
            }
        }
    }
    /*拆包为单个货物*/
    public void unPag(TreeSet<Items> falseItems, TreeSet<Item> falseItem){
        for (Items items:falseItems){
            for (Item item:items.items){
                falseItem.add(item);
            }
        }
    }

    /********************************** test ***************************************/

    public void testdfs(){
        for (Items items:itemset){
            Graph g= new Graph(graph);
            System.out.println(  g.DFSnr(items.start,items.end).size());
        }
    }
    public void testbfs(){
        List<Integer> path=  new LinkedList<>();
        int count1=0;
        int count2=0;
        for (Items items:itemset){
            if (items.items.get(0).ms!=null){
                count2++;
//                System.out.print(items.items.get(0));
                Graph g=new Graph(graph);
                path=g.BFS(items.start ,items.items.get(0).ms) ;
                if (path!=null){
                    count1++;
//                    System.out.print("Z" + path.get(0));
                    for (int i = 1;i<path.size();i++){
//                        System.out.print("->Z"+path.get(i));
                    }
                }else {
//                    System.out.print("null");
                }
//                System.out.println();
            }
        }
        System.out.println(count1+"/"+count2);
    }

    public void testdfsms(){
        List<Integer> path=  new LinkedList<>();
        int count1=0;
        int count2=0;
        for (Items items:itemset){
            if (items.items.get(0).ms!=null){
                count2++;
                /*  System.out.print(items.items.get(0));*/
                Graph g=new Graph(graph);
                path=subfindms(items.start,items.items.get(0).ms);
                if (path!=null){
                    count1++;
                   /* System.out.print("Z" + path.get(0));
                    for (int i = 1;i<path.size();i++){
                        System.out.print("->Z"+path.get(i));
                    }*/
                }else {
                    /* System.out.print("null");*/
                }
//                System.out.println();
            }
        }
        System.out.println(count1+"/"+count2);
    }
    public void printfalse(TreeSet<Items> falseItems){
        for (Items items: falseItems){
            System.out.println(items);
            if (items.items.get(0).ms!=null){
                for (Integer integer: items.items.get(0).ms){
                    System.out.print(integer+" ");
                }
                System.out.println();
            }else {
                System.out.println("null");
            }
        }
    }


}
public class Main{
    public static void main(String []args){
        Solution solution = new Solution();
        TreeSet<Solution.Items> falseItems = new TreeSet<>();
        TreeSet<Solution.Item> falseItem = new TreeSet<>();
        solution.read("");
        /*拆包*/
        solution.classify();
        /*单路径规划*/
        solution.findpath(falseItems);
        /*多路径规划*/
        solution.findpath1(falseItems);
        /*再次拆包*/
        solution.unPag(falseItems,falseItem);
        /*多路径规划*/
        solution.findpath2(falseItem);
        /*统计输出结果*/
        String head = getlost(falseItem);
        solution.print(head);
    }

    public  static  String getlosts(TreeSet<Solution.Items> list){
        double weight=0.0;
        int count=0;
        if (!list.isEmpty()){
            for (Solution.Items items:list){
                for (Solution.Item item:items.items){
                    count++;
                    weight +=item.weight;
                }
            }
        }
        String res=count+","+weight+"\n";
        return res;
    }

    public  static  String getlost(TreeSet<Solution.Item> list){
        double weight=0.0;
        int count=0;
        if (!list.isEmpty()){
            for (Solution.Item item:list){
                count++;
                weight +=item.weight;
            }
        }
        String res=count+","+weight+"\n";
        return res;
    }
/*************************for test**************************/
    public static void debug(){
        long start,end;
        for (int i=0;i<9;i++){
            start = System.currentTimeMillis();
            String path="test"+i+".txt";
            test(path,i);
            end = System.currentTimeMillis();
            System.out.println("Using time: "+(end-start)/1000.0+"Sec");

        }
    }

    public static void test(String path,int i){
        Solution solution = new Solution();
        TreeSet<Solution.Items> falseItems = new TreeSet<>();
        TreeSet<Solution.Item> falseItem = new TreeSet<>();
        solution.read(path);
        solution.classify();
        solution.findpath(falseItems);
        String head = getlosts(falseItems);
        System.out.print(head);
        solution.findpath1(falseItems);
        head = getlosts(falseItems);
        System.out.print(head);
        solution.unPag(falseItems,falseItem);
        solution.findpath2(falseItem);
        head = getlost(falseItem);
        System.out.print(head);
        print(head,solution.finalpath,i);
        print(falseItem,"",i);
        print(Solution.stationWokerMap,i);
        System.out.println("rate:"+solution.Wrate);
    }

    private static void print(HashMap<Integer, Integer> stationWokerMap, int i) {
        FileWriter writer=null;
        try {
            writer=new FileWriter("stationmap"+i+".txt");
            /*System.out.println(head);*/
            for (Map.Entry<Integer ,Integer> e:stationWokerMap.entrySet()){
                writer.write("Z"+e.getKey()+": "+e.getValue()+"\n");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void print(String head,HashMap<String,String> map,int i){
        FileWriter writer=null;
        try {
            writer=new FileWriter("result"+i+".txt");
            /*System.out.println(head);*/
            writer.write(head);
            for (Map.Entry<String ,String > e:map.entrySet()){
                writer.write(e.getValue());
            }
            System.out.println("write result "+i+" succcess!");
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void print(TreeSet<Solution.Item> set,String type,int i){
        String filename="debug"+type+i+".txt";
        FileWriter writer=null;

        try {
            writer=new FileWriter(filename);
            /*System.out.println(head);*/
            for (Solution.Item cur : set){
                writer.write(cur.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                assert writer != null;
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
