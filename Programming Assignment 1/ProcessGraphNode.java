import java.util.ArrayList;
import java.io.*;
import java.util.Arrays;
public class ProcessGraphNode {

    //point to all the parents
    private ArrayList<ProcessGraphNode> parents=new ArrayList<>();
    //point to all the children
    private ArrayList<ProcessGraphNode> children=new ArrayList<>();
    //properties of ProcessGraphNode
    private int nodeId;
    private File inputFile;
    private File outputFile;
    private String command;
    private boolean runnable;
    private boolean running;
    private boolean executed;
    public Thread runner;

    public ProcessGraphNode(int nodeId ) {
        this.nodeId = nodeId;
        this.runnable=false;
        this.executed=false;
    }

    public void setRunnable() {
        this.runnable = true;
    }

    public void setNotRunable() {this.runnable = false;}

    public void setExecuted() {
        this.executed = true;
    }
    public void setRunning() {
	this.running = true;
    }
    
    public boolean isRunnable() {
        return runnable;
    }

    public boolean isExecuted() {
        return executed;
    }
    
    public boolean isRunning() {
	return running;
    }
    
    public void addChild(ProcessGraphNode child){
        if (!children.contains(child)){
            children.add(child);
        }
    }

    public void addParent(ProcessGraphNode parent){
        if (!parents.contains(parent)){
            parents.add(parent);
        }
    }
    public void setInputFile(File inputFile) {
        this.inputFile = inputFile;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public void setOutputFile(File outputFile) {
        this.outputFile = outputFile;
    }

    public File getInputFile() {
        return inputFile;
    }

    public File getOutputFile() {
        return outputFile;
    }

    public String getCommand() {
        return command;
    }

    public ArrayList<ProcessGraphNode> getParents() {
        return parents;
    }

    public ArrayList<ProcessGraphNode> getChildren() {
        return children;
    }

    public int getNodeId() {
        return nodeId;
    }

    public synchronized boolean allParentsExecuted(){
        boolean ans=true;
        /*for (ProcessGraphNode child : this.getChildren()) {
            if (child.isExecuted()) {
                return false;
            }
        }*/
        for (ProcessGraphNode parent:this.getParents()) {
            if (!parent.isExecuted())
                ans=false;
        }

        return ans;
    }
    
    /**
     * Checks if a process can be launched and launches a new runner
     * thread for the process.
     * <p>
     * This method always returns immediately, whether or not the 
     * process can be run.
     */
    public void start() {
	// Perform check if process is ready to be run
	if(!allParentsExecuted()) {
		return;
	}
	
	// Create new runner thread for process
	runner = new Thread(new Runnable() {
		public void run() {
			try {
				// Prepare commands
				String[] commands = getCommand().split(" ");
				
				// Create ProcessBuilder
				ProcessBuilder pb = new ProcessBuilder(Arrays.asList(commands));
				pb.directory(ProcessManagement.currentDirectory);
				
				// Redirect input if required
				if(!getInputFile().getName().equals("stdin")) {
					pb.redirectInput(getInputFile()); // Potentially throws IOException, caught later
				}
				
				// Redirect output if required
				if(!getOutputFile().getName().equals("stdout")) {
					getOutputFile().createNewFile();
					pb.redirectOutput(getOutputFile());
					pb.redirectError(getOutputFile());
				}
				
				// Run process and wait for completion
				Process p = pb.start();
				p.waitFor();
				
				// Update execution status
				setExecuted();
				
				// Loop through and start child processes
				for(ProcessGraphNode c : getChildren()) {
					c.start();
				}
				
				// Wait for all children to complete before terminating
				for(ProcessGraphNode c : getChildren()) {
					if(c.runner == null) continue; // Ignore non-initialized child runners
					c.runner.join();
				}
			} catch(IOException e) { // Catch non-existent input file
				System.err.println("Unable to find input file " + getInputFile());
				System.exit(-1);
			} catch(Exception e) { // Catch any other exceptions
				e.printStackTrace();
			}
		}
	});
	
	// Launch runner and return
	runner.start();
    }
}
