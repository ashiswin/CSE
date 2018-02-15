import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
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
		
		ArrayList<Integer> sorted = new ArrayList<>();
		
		for(int i = 0; i < NumOfThread; i++) {
			threads[i].join();
			sorted = merge(sorted, threads[i].getInternal());
		}
		
		long elapsed = System.nanoTime() - start;
		
		System.out.println("The Median value is " + computeMedian(sorted));
		System.out.println("Running time is " + (elapsed / 1000000) + " milliseconds\n");
	}
	
	public static ArrayList<Integer> merge(ArrayList<Integer> left, ArrayList<Integer> right) {
		ArrayList<Integer> merged = new ArrayList<Integer>();
		int l = 0;
		int r = 0;
		
		for(int i = 0; i < left.size() + right.size(); i++) {
			if(l == left.size()) {
				for(int j = r; j < right.size(); j++) {
					merged.add(right.get(j));
				}
				break;
			}
			else if(r == right.size()) {
				for(int j = l; j < left.size(); j++) {
					merged.add(left.get(j));
				}
				break;
			}
			
			if(left.get(l) <= right.get(r)) {
				merged.add(left.get(l));
				l++;
			}
			else {
				merged.add(right.get(r));
				r++;
			}
		}
		
		return merged;
	}
	
	public static double computeMedian(ArrayList<Integer> arr) {
		if(arr.size() % 2 == 0) {
			return (arr.get(arr.size() / 2) + arr.get(arr.size() / 2 + 1)) / 2;
		}
		return arr.get(arr.size() / 2);
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
		this.list = mergeSortReal(this.list);
		
	}
	
	public ArrayList<Integer> mergeSortReal(List<Integer> array) {
		if(array.size() == 1) {
			return new ArrayList<Integer>(array);
		}
		
		ArrayList<Integer> left = mergeSortReal(array.subList(0, (array.size() / 2)));
		ArrayList<Integer> right = mergeSortReal(array.subList((array.size() / 2), array.size()));
		
		ArrayList<Integer> merged = new ArrayList<Integer>();
		int l = 0;
		int r = 0;
		
		for(int i = 0; i < left.size() + right.size(); i++) {
			if(l == left.size()) {
				for(int j = r; j < right.size(); j++) {
					merged.add(right.get(j));
				}
				break;
			}
			else if(r == right.size()) {
				for(int j = l; j < left.size(); j++) {
					merged.add(left.get(j));
				}
				break;
			}
			
			if(left.get(l) <= right.get(r)) {
				merged.add(left.get(l));
				l++;
			}
			else {
				merged.add(right.get(r));
				r++;
			}
		}
		return merged;
	}
}
