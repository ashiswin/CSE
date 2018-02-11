import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.File;

public class MeanThread {	
	public static void main(String[] args) throws InterruptedException, FileNotFoundException {
		String filename = args[0];
		
		ArrayList<Integer> integers = new ArrayList<>();
		Scanner scanner = new Scanner(new File(filename));
		
		while(scanner.hasNextInt()){
			integers.add(scanner.nextInt());
		}
		
		// define number of threads
		int NumOfThread = Integer.valueOf(args[1]);// this way, you can pass number of threads as 
		     // a second command line argument at runtime.
		
		MeanMultiThread[] threads = new MeanMultiThread[NumOfThread];
		
		for(int i = 0; i < NumOfThread; i++) {
			ArrayList<Integer> subArray = new ArrayList<Integer>();
			
			for(int j = 0; j < integers.size() / NumOfThread; j++) {
				subArray.add(integers.get(j +  (i * integers.size() / NumOfThread)));
			}
			
			threads[i] = new MeanMultiThread(subArray);
		}
		
		long start = System.nanoTime();
		
		for(int i = 0; i < NumOfThread; i++) {
			threads[i].start();
		}
		
		double sum = 0;
		
		for(int i = 0; i < NumOfThread; i++) {
			threads[i].join();
			System.out.println("Temporal mean value of thread " + i + " is " + threads[i].getMean());
			sum += threads[i].getMean();
		}
		
		long elapsed = System.nanoTime() - start;
		
		System.out.println("The global mean value is " + (sum / NumOfThread));
		System.out.println("Elapsed time: " + (elapsed / 1000000) + "ms");
	}
}
//Extend the Thread class
class MeanMultiThread extends Thread {
	private ArrayList<Integer> list;
	private double mean;
	MeanMultiThread(ArrayList<Integer> array) {
		list = array;
	}
	public double getMean() {
		return mean;
	}
	public void run() {
		mean = computeMean(list);
	}
	public double computeMean(ArrayList<Integer> list) {
		double sum = 0;
		for(Integer i : list) {
			sum += i;
		}
		return sum / list.size();
	}
}
