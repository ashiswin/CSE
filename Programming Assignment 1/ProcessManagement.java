import java.io.*;
import java.util.Arrays;
import java.util.Stack;

public class ProcessManagement {
    //set the working directory
    public static File currentDirectory = new File("./src");
    //set the instructions file
    private static File instructionSet = new File("graph-file1");

    public static void main(String[] args) throws InterruptedException {

        //parse the instruction file and construct a data structure, stored inside ProcessGraph class
        ParseFile.generateGraph(new File(currentDirectory + "/" + instructionSet));
        // Print the graph information
	    ProcessGraph.printGraph();
	
	    // Loop through and launch every process' runner
        for(ProcessGraphNode n : ProcessGraph.nodes) {
            n.start();
        }
	
        // Wait for all runner threads to complete
        for(int i = 0; i < ProcessGraph.nodes.size(); i++) {
            // Ignore invalid runners
            if(ProcessGraph.nodes.get(i).runner == null) continue;
            ProcessGraph.nodes.get(i).runner.join();
        }

        // Print termination message
            System.out.println("All process finished successfully");
        }

}
