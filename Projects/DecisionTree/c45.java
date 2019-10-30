package DecisionTree;


import java.io.*;
import java.util.*;

public class c45 {

    private ArrayList<ArrayList<String>> dataSet = new ArrayList<>();
    private HashMap<String,Set<Integer>> categories = new HashMap<>();
    private ArrayList<Set<String>> attrsValues = new ArrayList<>();
    private DecisionNode root;
    private String trainFileName;
    private String  testFileName;
    private String outputFileName;

    public c45(String train, String test, String output){
        trainFileName = train;
        testFileName = test;
        outputFileName = output;
        try {
            readTrainingData();
        }catch (IOException e){
            System.out.println("Cannot read input file:");
            e.printStackTrace();
        }
    }

    private void readTrainingData() throws IOException{
        BufferedReader br = new BufferedReader(new FileReader(trainFileName));
        String line;
        int i = 0;
        while ((line = br.readLine()) != null) {
            StringTokenizer stringTokenizer = new StringTokenizer(line, "\t");
            ArrayList<String> tuple = new ArrayList<>(22);
            while (stringTokenizer.hasMoreTokens()) {
                String attr = stringTokenizer.nextToken();
                tuple.add(attr);
            }
            dataSet.add(tuple);
            if(i==0){
                for(int j = 1; j<tuple.size(); j++){
                    attrsValues.add(new HashSet<>());
                }
            }
            for(int k=1; k<tuple.size(); k++){
                attrsValues.get(k-1).add(tuple.get(k));
            }
            Set<Integer> indices = categories.getOrDefault(tuple.get(0),new HashSet<>());
            indices.add(i);
            categories.put(tuple.get(0), indices);
            i++;
        }


    }

    public void train(){
        double totalEntropy = infoEntropy1(categories);

        Set<Integer> range = new HashSet<>();
        for(int j=0; j<dataSet.size(); j++){
            range.add(j);
        }

        Set<Integer> unExploredAttr = new HashSet<>();
        for(int k = 1; k<dataSet.get(0).size(); k++){
            unExploredAttr.add(k);
        }

        root = decisionTreeBuilder(range, totalEntropy, unExploredAttr);

    }

    private DecisionNode decisionTreeBuilder(Set<Integer> range, double entropy, Set<Integer> unExploredAttr){
        for(String key: categories.keySet()){
            if(categories.get(key).containsAll(range)) return new DecisionNode(true, key);
        }
        int max = 0;
        String c = null;
        for(String key: categories.keySet()) {
            Set<Integer> copy = new HashSet<>(range);
            copy.retainAll(categories.get(key));
            int num = copy.size();
            if(num>max){
                max = num;
                c = key;
            }
        }

        if(unExploredAttr.size()==0){
            return new DecisionNode(true, c);
        }

        int selectedAttr = selectAttr(range,entropy,unExploredAttr);
        unExploredAttr.remove(selectedAttr);

        HashMap<String, Set<Integer>> subRanges = new HashMap<>();
        HashMap<String,HashMap<String,Integer>> attrsCounts = new HashMap<>();
        for(Integer index: range){
            String  attrValue = dataSet.get(index).get(selectedAttr);
            String cate = dataSet.get(index).get(0);
            HashMap<String,Integer> count = attrsCounts.getOrDefault(attrValue, new HashMap<>());
            count.put(cate, count.getOrDefault(cate,0)+1);
            attrsCounts.put(attrValue,count);
            Set<Integer> indices = subRanges.getOrDefault(attrValue, new HashSet<>());
            indices.add(index);
            subRanges.put(attrValue,indices);
        }

        DecisionNode node = new DecisionNode(false,selectedAttr);
        HashMap<String,DecisionNode> branches = node.getBranches();
        for(String attrValue: attrsValues.get(selectedAttr-1)){
            if(!subRanges.keySet().contains(attrValue)) branches.put(attrValue,new DecisionNode(true,c));
        }
        for(String key: subRanges.keySet()){
            double e = infoEntropy2(attrsCounts.get(key));
            branches.put(key,decisionTreeBuilder(subRanges.get(key),e, new HashSet<>(unExploredAttr)));
        }
        return node;

    }

    private int selectAttr(Set<Integer> range, double entropy, Set<Integer> unExploredAttr) {

        HashMap<String, HashMap<String,Integer>> currentAttr;
        double max = 0;
        int selectedAttr = 0;
        for (int i = 1; i < dataSet.get(0).size(); i++) {
            if (!unExploredAttr.contains(i)) continue;
            currentAttr = new HashMap<>();
            HashMap<String,Integer> count;
            for (Integer index : range) {
                String attrValue = dataSet.get(index).get(i);
                count = currentAttr.getOrDefault(attrValue,new HashMap<>());
                count.put(dataSet.get(index).get(0),count.getOrDefault(dataSet.get(index).get(0),0)+1);
                currentAttr.put(attrValue,count);
            }

            double gainRatio = gainRatio(range.size(), currentAttr, entropy);
            if (gainRatio > max) {
                max = gainRatio;
                selectedAttr = i;
            }
        }

        return selectedAttr;

    }

    private static double infoEntropy1(HashMap<String,Set<Integer>> map){
        double sum=0.0;
        for(String key: map.keySet()){
            sum += map.get(key).size();
        }
        double result=0.0;
        for(String key: map.keySet()){
            int size = map.get(key).size();
            if(size==0) continue;
            double p = size/sum;
            result+= -p * Math.log(p)/Math.log(2);
        }
        return result;

    }

    private static double infoEntropy2(HashMap<String,Integer> map){
        double sum=0.0;
        for(String key: map.keySet()){
            sum += map.get(key);
        }
        double result=0.0;
        for(String key: map.keySet()){
            int size = map.get(key);
            if(size==0) continue;
            double p = size/sum;
            result+= -p * Math.log(p)/Math.log(2);
        }
        return result;

    }


    private double gainRatio(int n, HashMap<String ,HashMap<String,Integer>> map, double totalEntropy){
        double attrEntro = 0.0;
        double splitInfo = 0.0;
        HashMap<String, Integer> count;
        for(String key: map.keySet()){
            count = map.get(key);
            double sum = 0.0;
            for(String x: count.keySet()){
                sum+=count.get(x);
            }
            double ratio = (sum)/(n+0.0);
            attrEntro += infoEntropy2(count)*ratio;
            splitInfo += ratio * Math.log(ratio)/Math.log(2) *(-1);
        }

        return (totalEntropy-attrEntro)/splitInfo;
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
        DecisionNode node = root;
        while(!node.terminalState){
            String attr = tuple.get(node.getAttrIndex());
            node = node.getChildBranch(attr);
        }

        return node.getLabel();
    }

    public void printDesicionTree(){
        print(root,"");
    }

    private void print(DecisionNode node,String a){
        System.out.println(a+""+node.terminalState+"  "+node.attrIndex);
        for(String key:node.getBranches().keySet()){
            DecisionNode node1 = node.getChildBranch(key);
            print(node1,a+"   ");
        }
    }



    class DecisionNode {
        private int attrIndex;
        private HashMap<String, DecisionNode> branches = new HashMap<>();
        private final boolean terminalState;
        private String label;

        public DecisionNode(boolean state, String l){
            terminalState = state;
            label = l;
        }

        public DecisionNode(boolean state, int index){
            terminalState = state;
            attrIndex = index;
        }

        public int getAttrIndex() {
            return attrIndex;
        }

        public HashMap<String, DecisionNode> getBranches() {
            return branches;
        }

        public DecisionNode getChildBranch(String key){
            return branches.get(key);
        }

        public String getLabel() {
            if(terminalState) return label;
            else{
                System.out.println("Not a leaf node");
                return null;
            }
        }
    }

    public static void main(String[] args) throws IOException {
        c45 a = new c45(args[0], args[1],args[2]);
        a.train();
        a.test();

    }
}

