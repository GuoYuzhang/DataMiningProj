package Clustering;

import java.io.*;
import java.util.*;

public class kmeans {

    private ArrayList<ArrayList<Double>> dataset = new ArrayList<>();
    private ArrayList<ArrayList<Double>> centers;
    private Map<ArrayList<Double>, Set<Integer>> clusters;
    private String outputName;
    private int k;


    public kmeans(String datasetName, int k, String output) {
        centers = new ArrayList<>(k);
        this.k = k;
        outputName = output;
        try {
            readData(datasetName);
        } catch (IOException e) {
            System.out.println("Cannot read dataset file:");
            e.printStackTrace();
        }
    }

    private void readData(String fileName) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        String line;
        StringTokenizer st;
        while ((line = br.readLine()) != null ) {
            if(line.isEmpty()) continue;
            st = new StringTokenizer(line, ",");
            ArrayList<Double> tuple = new ArrayList<>();
            while (st.hasMoreTokens()) {
                tuple.add(Double.parseDouble(st.nextToken()));
            }

            dataset.add(tuple);
        }
    }

    public void solve() {
        randomlySelectCenters();

        Map<ArrayList<Double>, Set<Integer>> map; //associate each center with the indices of points
                                                        // which has this center as nearest center
        while (true) {
            map = new HashMap<>();
            for(ArrayList<Double> c: centers){
                map.put(c, new HashSet<>());
            }

            for (int i=0; i<dataset.size(); i++) {
                int closestCenterIndex = getClosestCenterIndex(dataset.get(i));
                map.get(centers.get(closestCenterIndex)).add(i);
            }

            boolean unchanged = true;
            for(int i=0; i<k; i++){
                ArrayList<Double> newCenter = average(map.get(centers.get(i)));
                if(!centers.get(i).equals(newCenter)) {
                    unchanged = false;
                    centers.set(i, newCenter);
                }
            }

            if(unchanged) {
                clusters = map;
                break;
            }
        }

    }

    private void randomlySelectCenters() {
        centers.clear();
        Random rand = new Random();
        Set<ArrayList<Double>> cen = new HashSet<>();
        while (cen.size() < k) {
            int index = rand.nextInt(dataset.size());
            ArrayList<Double> center = new ArrayList<>(dataset.get(index));
            if (!cen.contains(center)) {
                centers.add(center);
                cen.add(center);
            }
        }
    }

    private int getClosestCenterIndex(List<Double> point) {
        int index = 0;
        double minDis = euclideanDistance(point, centers.get(0));
        for(int i=1; i<centers.size(); i++){
            double d = euclideanDistance(point, centers.get(i));
            if(d<minDis){
                minDis = d;
                index = i;
            }
        }

        return index;

    }

    private ArrayList<Double> average(Set<Integer> cluster){
        ArrayList<Double> average = new ArrayList<>();
        ArrayList<Double> point;
        for(Integer index: cluster){
            point = dataset.get(index);
            if(average.isEmpty()){
                for(Double x: point) average.add(x);
            }else{
                for(int i=0; i<point.size(); i++){
                    average.set(i, average.get(i)+point.get(i));
                }
            }
        }

        for(int i=0; i< average.size(); i++){
            average.set(i, average.get(i)/cluster.size());
        }

        return average;

    }

    private double euclideanDistance(List<Double> a, List<Double> b){
        double distance = 0.0;
        for(int i=0; i<a.size(); i++){
            distance += (a.get(i) - b.get(i)) * (a.get(i) - b.get(i));
        }

        return Math.sqrt(distance);
    }


    private void writeResult() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(outputName))) {
            StringBuilder s = new StringBuilder();
            s.append("Centroids: \n");
            for(int i=0; i<centers.size(); i++){
                s.append("Label: "+i+" "+centers.get(i).toString()+"\n");
            }

            for(int i=0; i<dataset.size(); i++){
                for(int j=0; j<centers.size(); j++){
                    if(clusters.get(centers.get(j)).contains(i)) {
                        s.append(j+" "+dataset.get(i)+"\n");
                        break;
                    }
                }
            }
            s.append("SSE: "+SSE());
            bw.write(s.toString());
            bw.close();
        } catch (IOException e) {
            System.out.println("Error when writing to output file: ");
            e.printStackTrace();
        }
    }

    public double SSE(){
        double sse = 0.0;
        for(ArrayList<Double> center: clusters.keySet()){
            Set<Integer> indices = clusters.get(center);
            for(Integer index: indices){
                sse += Math.pow(euclideanDistance(dataset.get(index),center),2);
            }
        }
        return sse;
    }


    public static void main(String[] args) {
        kmeans k = new kmeans(args[0], Integer.parseInt(args[1]), args[2]);
        k.solve();
        k.writeResult();
    }

}
