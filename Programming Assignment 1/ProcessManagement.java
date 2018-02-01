import java.io.*;
import java.util.Arrays;
import java.util.Stack;

public class ProcessManagement {

    //set the working directory
    private static File currentDirectory = new File("/home/ashiswin/Documents/CSE/Programming Assignment 1");
    //set the instructions file
    private static File instructionSet = new File("test4.txt");
    public static Object lock=new Object();
    
    public static void toposort(int v, boolean[] visited, Stack<ProcessGraphNode> stack) {
	visited[v] = true;
	
	for(ProcessGraphNode c : ProcessGraph.nodes.get(v).getChildren()) {
		if(!visited[c.getNodeId()]) {
			toposort(c.getNodeId(), visited, stack);
		}
	}
	
	stack.push(ProcessGraph.nodes.get(v));
    }
    
    public static void main(String[] args) throws InterruptedException {

        //parse the instruction file and construct a data structure, stored inside ProcessGraph class
        ParseFile.generateGraph(new File(currentDirectory + "/"+instructionSet));

        // Print the graph information
	ProcessGraph.printGraph();
	
	Stack<ProcessGraphNode> stack = new Stack<>();
	boolean[] visited = new boolean[ProcessGraph.nodes.size()];
	for(int i = 0; i < visited.length; i++) {
		visited[i] = false;
	}
	
	for(int i = 0; i < visited.length; i++) {
		if(!visited[i]) {
			toposort(i, visited, stack);
		}
	}
	
	while(!stack.empty()) {
		final ProcessGraphNode n = stack.pop();
		new Thread(new Runnable() {
			public void run() {
				try {
					while(!n.allParentsExecuted()) {
						Thread.sleep(500);
					}
					
					String[] commands = n.getCommand().split(" ");
					
					ProcessBuilder pb = new ProcessBuilder(Arrays.asList(commands));
					pb.directory(currentDirectory);
					if(!n.getInputFile().getName().equals("stdin")) {
						pb.redirectInput(n.getInputFile());
					}
					if(!n.getOutputFile().getName().equals("stdout")) {
						n.getOutputFile().createNewFile();
						pb.redirectOutput(n.getOutputFile());
						pb.redirectError(n.getOutputFile());
					}
					Process p = pb.start();
					p.waitFor();
					n.setExecuted();
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}
	
	for(int i = 0; i < visited.length; i++) {
		if(!ProcessGraph.nodes.get(i).isExecuted()) {
			Thread.sleep(500);
			i = 0;
		}
	}
	
        System.out.println("All process finished successfully");
    }

}
