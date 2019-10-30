package FrequentMining;

import java.io.*;
import java.util.*;


public class Apriori {

    private LinkedList<Myset<Integer>> dataSet;
    private Myset<Integer> remainingItems;
    private String outputFileName;
    private HashMap<Myset<Integer>, Integer> L1;
    private final int support;


    public Apriori(String input, int support, String output) {
        this.support = support;
        outputFileName = output;
        try{
            L1 = init(input);
        } catch (IOException e){
            System.out.println("Cannot read input file:");
            e.printStackTrace();
        }
    }

    private HashMap<Myset<Integer>, Integer> init(String input) throws IOException{
        dataSet = new LinkedList<>();
        BufferedReader br = new BufferedReader(new FileReader(input));
        HashMap<Myset<Integer>, Integer> itemCount = new HashMap<>();
        String line;
        while ((line = br.readLine()) != null) {
            StringTokenizer stringTokenizer = new StringTokenizer(line, " ");
            Myset<Integer> transaction = new Myset<>();
            while (stringTokenizer.hasMoreTokens()) {
                Integer item = Integer.parseInt(stringTokenizer.nextToken());
                transaction.add(item);
                Myset<Integer> itemSet = new Myset<>();
                itemSet.add(item);
                itemCount.put(itemSet, itemCount.getOrDefault(itemSet, 0) + 1);
            }
            dataSet.add(transaction);
        }

        itemCount.entrySet().removeIf(entry -> entry.getValue() < support);
        remainingItems = combine(itemCount.keySet());
        updateDB();
        return itemCount;
    }

    public void solve(){
        System.out.println("AAAAAA");
        HashMap<Myset<Integer>, Integer> L = L1;
        int k = 2;
        while(!L.isEmpty()){
            writeToFile(L);
            L = new HashMap<>();
            ArrayList<Myset<Integer>> listOfSubsets;
            for(HashSet<Integer> tran: dataSet){
                listOfSubsets = subSetsOfSizek(new ArrayList<>(tran), k);
                for(Myset<Integer> subset: listOfSubsets){
                    L.put(subset, L.getOrDefault(subset,0)+1);
                }
            }
            HashMap<Integer,Integer> map = new HashMap<>();
            for(Set<Integer> x : L.keySet()){
                map.put(x.hashCode(), map.getOrDefault(x.hashCode(),0)+1);
            }
            System.out.println(map);
            L.entrySet().removeIf(entry -> entry.getValue() < support);
            updateRemainingItems(L, k);
            updateDB();
            k++;

        }


    }

    private ArrayList<Myset<Integer>> subSetsOfSizek(ArrayList<Integer> superSet, int k){
        ArrayList<Myset<Integer>> listOfSubsets = new ArrayList<>();
        subSetsOfSizek(listOfSubsets, superSet, new Myset<>(),k,0);
        return  listOfSubsets;

    }

    private void subSetsOfSizek(ArrayList<Myset<Integer>> listOfSubsets, ArrayList<Integer> superSet, Myset<Integer> subset,int k,int index){
        if(subset.size()==k){
            listOfSubsets.add(subset);
            return;
        }
        else if(subset.size()+ superSet.size()-index<k) return;

        for(int i = index; i<superSet.size(); i++){
            Myset<Integer> newSubset= new Myset<>(subset);
            newSubset.add(superSet.get(i));
            subSetsOfSizek(listOfSubsets, superSet, newSubset,k,i+1);
        }


    }

    private Myset<Integer> combine(Set<Myset<Integer>> itemSets){
        Myset<Integer> combine = new Myset<>();
        for(Myset<Integer> x: itemSets){
            combine.addAll(x);
        }
        return combine;
    }

    private void updateRemainingItems(HashMap<Myset<Integer>,Integer> frequentItemSets, int size){
        HashMap<Integer, Integer> count = new HashMap<>();
        for(Myset<Integer> itemSet: frequentItemSets.keySet()){
            for(Integer item: itemSet){
                count.put(item, count.getOrDefault(item,0)+1);
            }
        }
        count.entrySet().removeIf(entry -> entry.getValue() < size);
        remainingItems = new Myset<>(count.keySet());

    }

    private void updateDB(){
        Iterator<Myset<Integer>> iterator = dataSet.iterator();
        while (iterator.hasNext()) {
            Myset<Integer> tran = iterator.next();
            tran.removeIf((Integer x) -> !remainingItems.contains(x));
            if (tran.isEmpty()) iterator.remove();
        }
    }

    private void writeToFile(HashMap<Myset<Integer>, Integer> L){
        try(BufferedWriter bw = new BufferedWriter(new FileWriter(outputFileName, true))){
            StringBuilder s = new StringBuilder();
            for(Set<Integer> itemSet: L.keySet()){
                ArrayList<Integer> l = new ArrayList<>(itemSet);
                Collections.sort(l);
                for(Integer item: l){
                    s.append(item+" ");
                }
                s.append("("+L.get(itemSet)+")");
                bw.write(s.toString());
                bw.newLine();
                s.setLength(0);
            }

        }catch (IOException e){
            System.out.println("Error when writing to output file: ");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        Apriori a = new Apriori("T10I4D100K.txt", 500, "T10I4");
        a.solve();
        long end = System.currentTimeMillis();
        System.out.println("time is:" +(double)(end-start)/1000+"s");

    }

    public class Myset<E> extends HashSet<E>{
        public Myset(){}

        public Myset(Collection<? extends E> a){
            super(a);
        }
        @Override
        public int hashCode() {
            int h = 1;
            Iterator<E> i = iterator();
            while (i.hasNext()) {
                E obj = i.next();
                if (obj != null)
                    h *= (obj.hashCode()+1);
            }
            return h;
        }
    }


}
