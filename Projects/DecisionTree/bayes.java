package DecisionTree;

import java.io.*;
import java.util.*;

public class bayes {

    private ArrayList<ArrayList<String>> dataSet = new ArrayList<>();
    private Map<String, Integer> categories = new HashMap<>();
    private Map<String, Double> categProb = new HashMap<>();
    private List<Map<String,Map<String,Integer>>> attrsCounts = new ArrayList<>();
    private String trainFileName;
    private String  testFileName;
    private String outputFileName;

    public bayes(String train, String test, String output){
        trainFileName = train;
        testFileName = test;
        outputFileName = output;
        try {
            readTrainingData();
        }catch (IOException e){
            System.out.println("Cannot read training data file:");
            e.printStackTrace();
        }
    }

    private void readTrainingData() throws IOException{
        BufferedReader br = new BufferedReader(new FileReader(trainFileName));
        String line;
        while ((line = br.readLine()) != null) {
            StringTokenizer stringTokenizer = new StringTokenizer(line, "\t");
            ArrayList<String> tuple = new ArrayList<>(22);
            while (stringTokenizer.hasMoreTokens()) {
                String attr = stringTokenizer.nextToken();
                tuple.add(attr);
            }
            categories.put(tuple.get(0),categories.getOrDefault(tuple.get(0),0)+1);
            dataSet.add(tuple);
        }
    }

    public void train(){
        calculateClassProb();
        for(int i=1; i < dataSet.get(0).size(); i++){
            Map<String,Map<String,Integer>> attrClassCount = new HashMap<>();
            for(String c: categories.keySet()){
                attrClassCount.put(c, new HashMap<>());
            }
            for(ArrayList<String> tuple: dataSet){
                Map<String,Integer> count = attrClassCount.get(tuple.get(0));
                count.put(tuple.get(i), count.getOrDefault(tuple.get(i),0)+1);
            }
            attrsCounts.add(attrClassCount);
        }

    }

    private void calculateClassProb(){
        double total = 0.0;
        for(Integer value: categories.values()){
            total+=value;
        }
        for(String category: categories.keySet()){
            double prob = categories.get(category)/total;
            categProb.put(category,prob);
        }
    }

    private List<List<String>> readTestData() throws IOException{
        List<List<String>> testData = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(testFileName));
        String line;
        while ((line = br.readLine()) != null) {
            StringTokenizer stringTokenizer = new StringTokenizer(line, "\t");
            ArrayList<String> tuple = new ArrayList<>(22);
            while (stringTokenizer.hasMoreTokens()) {
                String attr = stringTokenizer.nextToken();
                tuple.add(attr);
            }
            testData.add(tuple);
        }

        return testData;
    }

    public void test(){
        try {
            List<List<String>> testData = readTestData();
            double num = 0;
            StringBuilder s = new StringBuilder();
            for(List<String> tuple: testData) {
                String l = getTupleClass(tuple);
                String actualL = tuple.get(0);
                s.append("Label: "+ l+ "  Actual label: "+actualL+"\n");
                if(l.equals(actualL)) num+=1;
            }
            s.append("Accuracy Percentage: "+num/testData.size()+"\n");
            writeToFile(s);
        } catch (IOException e){
            System.out.println("Cannot read test data file");
            e.printStackTrace();
        }

    }

    private void writeToFile(StringBuilder s){
        try(BufferedWriter bw = new BufferedWriter(new FileWriter(outputFileName, true))){
            bw.write(s.toString());
            bw.close();
        }catch (IOException e){
            System.out.println("Error when writing to output file: ");
            e.printStackTrace();
        }

    }

    private String getTupleClass(List<String> tuple){
        double maxProb = 0.0;
        String category = null;
        for(String c: categories.keySet()){
            double prob = 1.0;
            double cCount = categories.get(c);
            for(int i=1; i<tuple.size(); i++){
                double count = attrsCounts.get(i-1).get(c).getOrDefault(tuple.get(i),0)+1;
                prob *= (count*100)/cCount;
            }
            prob *= categProb.get(c);
            if(prob>maxProb){
                maxProb = prob;
                category = c;
            }
        }
        return category;
    }


    public static void main(String[] args) {
        bayes a = new bayes(args[0],args[1],args[2]);
        a.train();
        a.test();
    }
}
