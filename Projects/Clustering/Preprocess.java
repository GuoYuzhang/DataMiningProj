package Clustering;

import java.io.*;

public class Preprocess {
    public static void main(String[] args) throws IOException{
        BufferedWriter bw = new BufferedWriter(new FileWriter(args[1]));
        BufferedReader br = new BufferedReader(new FileReader(args[0]));
        String line;
        while ((line = br.readLine()) != null) {
            int index = line.lastIndexOf(",");
            line = line.substring(0,index);
            bw.write(line+"\n");
        }
        bw.close();
    }
}
