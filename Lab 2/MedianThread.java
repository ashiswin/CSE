import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;


public class MedianThread {

	public static void main(String[] args) throws InterruptedException, FileNotFoundException  {
		String filename = args[0];
		
		ArrayList<Integer> integers = new ArrayList<>();
		Scanner scanner = new Scanner(new File(filename));
		
		while(scanner.hasNextInt()){
			integers.add(scanner.nextInt());
		}
				
		// define number of threads
		int NumOfThread = Integer.valueOf(args[1]);// this way, you can pass number of threads as 
		     // a second command line argument at runtime.
	
		MedianMultiThread[] threads = new MedianMultiThread[NumOfThread];
		
		for(int i = 0; i < NumOfThread; i++) {
			ArrayList<Integer> subArray = new ArrayList<Integer>();
			
			for(int j = 0; j < integers.size() / NumOfThread; j++) {
				subArray.add(integers.get(j +  (i * integers.size() / NumOfThread)));
			}
			
			threads[i] = new MedianMultiThread(subArray);
		}
		
		long start = System.nanoTime();
		
		for(int i = 0; i < NumOfThread; i++) {
			threads[i].start();
		}
		
		int[] arr = new int[integers.size()];
		
		// TODO: implement mergeing of sorted arrays
		for(int i = 0; i < NumOfThread; i++) {
			threads[i].join();
		}
		
		long elapsed = System.nanoTime() - start;
		
		System.out.println("The Median value is " + computeMedian(arr));
		System.out.println("Running time is " + (elapsed / 1000000) + " milliseconds\n");
	}
	
	public static double computeMedian(int[] arr) {
		if(arr.length % 2 == 0) {
			return (arr[arr.length / 2] + arr[arr.length / 2 + 1]) / 2;
		}
		return arr[arr.length / 2];
	}
}

// extend Thread
class MedianMultiThread extends Thread {
	private ArrayList<Integer> list;

	public ArrayList<Integer> getInternal() {
		return list;
	}

	MedianMultiThread(ArrayList<Integer> array) {
		list = array;
	}

	public void run() {
		// called by object.start()
		mergeSort(list);
		
	}
	
	// TODO: implement merge sort here, recursive algorithm
	public void mergeSort(ArrayList<Integer> array) {
		
	}
}
