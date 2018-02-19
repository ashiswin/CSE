import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
// package Week3;

public class Banker {
	private int numberOfCustomers;	// the number of customers
	private int numberOfResources;	// the number of resources

	private int[] available; 	// the available amount of each resource
	private int[][] maximum; 	// the maximum demand of each customer
	private int[][] allocation;	// the amount currently allocated
	private int[][] need;		// the remaining needs of each customer

	/**
	 * Constructor for the Banker class.
	 * @param resources          An array of the available count for each resource.
	 * @param numberOfCustomers  The number of customers.
	 */
	public Banker (int[] resources, int numberOfCustomers) {
		this.numberOfResources = resources.length;
		this.numberOfCustomers = numberOfCustomers;
		
		this.available = resources;
		
		maximum = new int[this.numberOfCustomers][this.numberOfResources];
		allocation =  new int[this.numberOfCustomers][this.numberOfResources];
		need = new int[this.numberOfCustomers][this.numberOfResources];
	}

	/**
	 * Sets the maximum number of demand of each resource for a customer.
	 * @param customerIndex  The customer's index (0-indexed).
	 * @param maximumDemand  An array of the maximum demanded count for each resource.
	 */
	public void setMaximumDemand(int customerIndex, int[] maximumDemand) {
		for(int i = 0; i < maximumDemand.length; i++) {
			maximum[customerIndex][i] = maximumDemand[i];
			need[customerIndex][i] = maximumDemand[i];
		}
	}

	/**
	 * Prints the current state of the bank.
	 */
	public void printState() {
		System.out.println("Current state:");
		System.out.println("Available:");
		System.out.println(Arrays.toString(available));
		
		System.out.println("\nMaximum:");
		for(int i = 0; i < maximum.length; i++) {
			System.out.println(Arrays.toString(maximum[i]));
		}
		
		System.out.println("\nAllocation:");
		for(int i = 0; i < allocation.length; i++) {
			System.out.println(Arrays.toString(allocation[i]));
		}
		
		System.out.println("\nNeed:");
		for(int i = 0; i < need.length; i++) {
			System.out.println(Arrays.toString(need[i]));
		}
	}

	/**
	 * Requests resources for a customer loan.
	 * If the request leave the bank in a safe state, it is carried out.
	 * @param customerIndex  The customer's index (0-indexed).
	 * @param request        An array of the requested count for each resource.
	 * @return true if the requested resources can be loaned, else false.
	 */
	public synchronized boolean requestResources(int customerIndex, int[] request) {
		System.out.println("Customer " + customerIndex + " requesting");
		System.out.println(Arrays.toString(request));
		
		if(!checkSafe(customerIndex, request)) return false;
		
		for(int i = 0; i < request.length; i++) {
			if(request[i] > need[customerIndex][i] || request[i] > available[i]) return false;
			
			available[i] -= request[i];
			need[customerIndex][i] -= request[i];
			allocation[customerIndex][i] += request[i];
		}
		
		return true;
	}

	/**
	 * Releases resources borrowed by a customer. Assume release is valid for simplicity.
	 * @param customerIndex  The customer's index (0-indexed).
	 * @param release        An array of the release count for each resource.
	 */
	public synchronized void releaseResources(int customerIndex, int[] release) {
		System.out.println("Customer " + customerIndex + " releasing");
		System.out.println(Arrays.toString(release));
		
		for(int i = 0; i < release.length; i++) {
			allocation[customerIndex][i] = Math.max(0, allocation[customerIndex][i] - release[i]);
			available[i] += release[i];
			need[customerIndex][i] += release[i];
		}
	}

	/**
	 * Checks if the request will leave the bank in a safe state.
	 * @param customerIndex  The customer's index (0-indexed).
	 * @param request        An array of the requested count for each resource.
	 * @return true if the requested resources will leave the bank in a
	 *         safe state, else false
	 */
	private synchronized boolean checkSafe(int customerIndex, int[] request) {
		int[] tempAvail = new int[available.length];
		int[][] tempNeed = new int[need.length][need[0].length];
		int[][] tempAllocation = new int[allocation.length][allocation[0].length];
		
		for(int i = 0; i < available.length; i++) {
			tempAvail[i] = available[i] - request[i];
		}
		for(int i = 0; i < need.length; i++) {
			for(int j = 0; j < need[0].length; j++) {
				if(i == customerIndex) {
					tempNeed[i][j] = need[i][j] - request[j];
					tempAllocation[i][j] = allocation[i][j] + request[j];
				}
				else {
					tempNeed[i][j] = need[i][j];
					tempAllocation[i][j] = allocation[i][j];
				}
			}
		}
		
		int[] work = tempAvail;
		boolean[] finish = new boolean[need.length];
		boolean possible = true;
		
		while(possible) {
			possible = false;
			for(int i = 0; i < need.length; i++) {
				boolean lessThanWork = true;
				for(int j = 0; j < need[0].length; j++) {
					if(tempNeed[i][j] > work[j]) {
						lessThanWork = false;
						break;
					}
				}
				
				if(finish[i] == false && lessThanWork) {
					possible = true;
					for(int j = 0; j < need[0].length; j++) {
						work[j] += tempAllocation[i][j];
					}
					finish[i] = true;
				}
			}
		}
		
		for(int i = 0; i < finish.length; i++) {
			if(!finish[i]) return false;
		}
		
		return true;
	}

	/**
	 * Parses and runs the file simulating a series of resource request and releases.
	 * Provided for your convenience.
	 * @param filename  The name of the file.
	 */
	public static void runFile(String filename) {

		try {
			BufferedReader fileReader = new BufferedReader(new FileReader(filename));

			String line = null;
			String [] tokens = null;
			int [] resources = null;

			int n, m;

			try {
				n = Integer.parseInt(fileReader.readLine().split(",")[1]);
			} catch (Exception e) {
				System.out.println("Error parsing n on line 1.");
				fileReader.close();
				return;
			}

			try {
				m = Integer.parseInt(fileReader.readLine().split(",")[1]);
			} catch (Exception e) {
				System.out.println("Error parsing n on line 2.");
				fileReader.close();
				return;
			}

			try {
				tokens = fileReader.readLine().split(",")[1].split(" ");
				resources = new int[tokens.length];
				for (int i = 0; i < tokens.length; i++)
					resources[i] = Integer.parseInt(tokens[i]);
			} catch (Exception e) {
				System.out.println("Error parsing resources on line 3.");
				fileReader.close();
				return;
			}

			Banker theBank = new Banker(resources, n);

			int lineNumber = 4;
			while ((line = fileReader.readLine()) != null) {
				tokens = line.split(",");
				if (tokens[0].equals("c")) {
					try {
						int customerIndex = Integer.parseInt(tokens[1]);
						tokens = tokens[2].split(" ");
						resources = new int[tokens.length];
						for (int i = 0; i < tokens.length; i++)
							resources[i] = Integer.parseInt(tokens[i]);
						theBank.setMaximumDemand(customerIndex, resources);
					} catch (Exception e) {
						System.out.println("Error parsing resources on line "+lineNumber+".");
						fileReader.close();
						return;
					}
				} else if (tokens[0].equals("r")) {
					try {
						int customerIndex = Integer.parseInt(tokens[1]);
						tokens = tokens[2].split(" ");
						resources = new int[tokens.length];
						for (int i = 0; i < tokens.length; i++)
							resources[i] = Integer.parseInt(tokens[i]);
						theBank.requestResources(customerIndex, resources);
					} catch (Exception e) {
						e.printStackTrace();
						System.out.println("Error parsing resources on line "+lineNumber+".");
						fileReader.close();
						return;
					}
				} else if (tokens[0].equals("f")) {
					try {
						int customerIndex = Integer.parseInt(tokens[1]);
						tokens = tokens[2].split(" ");
						resources = new int[tokens.length];
						for (int i = 0; i < tokens.length; i++)
							resources[i] = Integer.parseInt(tokens[i]);
						theBank.releaseResources(customerIndex, resources);
					} catch (Exception e) {
						System.out.println("Error parsing resources on line "+lineNumber+".");
						fileReader.close();
						return;
					}
				} else if (tokens[0].equals("p")) {
					theBank.printState();
				}
			}
			fileReader.close();
		} catch (IOException e) {
			System.out.println("Error opening: "+filename);
		}

	}

	/**
	 * Main function
	 * @param args  The command line arguments
	 */
	public static void main(String [] args) {
		if (args.length > 0) {
			runFile(args[0]);
		}
	}

}
