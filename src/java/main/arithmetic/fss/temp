package fss;

import shared.*;
import shared.Error;
import java.lang.*;
import java.io.*;
import java.util.*;

class sim_anneal{
    
    //obs int sim_anneal(const Array<Real>& table, Real lambda, MRandom& randNumGen)
    public static int sim_anneal(double[] table, double lambda, Random randNumGen) {
        // table must be greater than size one
        MLJ.ASSERT(table.length > 1,"sim_anneal:table.length <= 1.");
        
        // initialize a new array
        double[] array = MLJArray.copy(table);
        
        double sum = 0;
        double first = array[0];
        int slot;
        //obs   for(int slot = array.low(); slot <= array.high(); slot++) {
        for(slot = 0; slot < array.length; slot++) {
            array[slot] = Math.exp((array[slot] - first) / lambda);
            sum += array[slot];
        }
        
        LogOptions.GLOBLOG(3, "sum: "+sum+"  table: "+array+"\n");
        
        // throw a random number from 0 to sum
        //obs   double val = randNumGen.real(0, sum);
        double val = randNumGen.nextDouble() * sum;
        LogOptions.GLOBLOG(3, "number: "+val+"\n");
        
        // pick the slot.  It should never fall out of the array.
        //obs   for(slot = array.low(); slot <= array.high(); slot++) {
        for(slot = 0; slot < array.length; slot++) {
            if(val <= array[slot])
                break;
            else
                val -= array[slot];
        }
        MLJ.ASSERT(slot <= (array.length - 1),"sim_anneal:slot > (array.length - 1)");
        LogOptions.GLOBLOG(3, "slot: "+val+"\n");
        
        return slot;
    }
    
    
    
    private static void t_sim_anneal(double[] table, double lambda, int times) throws IOException{
        int[] hist = new int[table.length];
        int SEED = 4970798;
        Random randNumGen = new Random((long)SEED);
        
        // repeat test by getting a slot each time.  Accumulate the slots we
        // pick into the histogram.
        for(int i=0; i<times; i++) {
            int slot = sim_anneal(table, lambda, randNumGen);
            hist[slot]++;
        }
        
        // display the histogram, including percent of total
        System.out.print("Lambda: "+lambda+"\n");
        for(int i = 0; i < hist.length; i++) {
            System.out.print("Slot "+i+" ("+table[i]+"): "+hist[i]
            +" ("+(double)(hist[i])/(double)times * 100
            +"%)\n");
        }
        System.out.print("\n");
    }

    private static double tab[] = {0.96, 0.94, 0.92, 0.90, 0.85, 0.80, 0.73, 0.62, 0.50};
    private static double tabeq[] = {0.92, 0.92, 0.92, 0.92, 0.92, 0.92, 0.92, 0.92, 0.92};
    public static void main(String[] args) {
        try{
        int size = tab.length;
        double[] table = new double[size];
        for(int i=0; i<size; i++)
            table[i] = tab[i];
        System.out.print("Table: ");
        int j;
        for(j = 0; j < table.length-1; System.out.print(table[j++]+", "));
        System.out.println(table[j]);
        // run the test 1000 times for each lambda value
        t_sim_anneal(table, 0.02, 1000);
        t_sim_anneal(table, 0.05, 1000);
        t_sim_anneal(table, 0.10, 1000);
        t_sim_anneal(table, 0.20, 1000);
        
        // run again on tableeq
        size = tabeq.length;
        double[] tableeq = new double[size];
        for(int i=0; i<size; i++)
            tableeq[i] = tabeq[i];
        System.out.print("Table-Equality: ");
        int k;
        for(k = 0; k < table.length-1; System.out.print(table[k++]+", "));
        System.out.println(table[k]);
                
        // run the test 1000 times for each lambda value
        t_sim_anneal(tableeq, 0.02, 1000);
        t_sim_anneal(tableeq, 0.05, 1000);
        t_sim_anneal(tableeq, 0.10, 1000);
        t_sim_anneal(tableeq, 0.20, 1000);
        
        System.exit(0);
        }catch(IOException e){
            e.printStackTrace();
            System.exit(1);}
}
    
    
}