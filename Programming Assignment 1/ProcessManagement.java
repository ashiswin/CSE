import java.io.*;
import java.util.Arrays;
import java.util.Stack;

public class ProcessManagement {

    //set the working directory
    public static File currentDirectory = new File("/home/ashiswin/Documents/CSE/Programming Assignment 1");
    //set the instructions file
    private static File instructionSet = new File("graph-file3");
    public static Object lock=new Object();
    
    public static void main(String[] args) throws InterruptedException {

        //parse the instruction file and construct a data structure, stored inside ProcessGraph class
        ParseFile.generateGraph(new File(currentDirectory + "/" + instructionSet));

        // Print the graph information
	ProcessGraph.printGraph();
	
	for(ProcessGraphNode n : ProcessGraph.nodes) {
		n.start();
	}
	
	for(int i = 0; i < visited.length; i++) {
		ProcessGraph.nodes.get(i).runner.join();
	}
	
        System.out.println("All process finished successfully");
    }

}
