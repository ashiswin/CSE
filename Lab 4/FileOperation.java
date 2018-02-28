// Copyright (C) Isaac Ashwin Ravindran 1002151

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.text.SimpleDateFormat;

public class FileOperation {
	private static File currentDirectory = new File(System.getProperty("user.dir"));
	public static void main(String[] args) throws java.io.IOException {

		String commandLine;

		BufferedReader console = new BufferedReader
				(new InputStreamReader(System.in));

		while (true) {
			// read what the user entered
			System.out.print("jsh>");
			commandLine = console.readLine();

			// clear the space before and after the command line
			commandLine = commandLine.trim();

			// if the user entered a return, just loop again
			if (commandLine.equals("")) {
				continue;
			}
			// if exit or quit
			else if (commandLine.equalsIgnoreCase("exit") | commandLine.equalsIgnoreCase("quit")) {
				System.exit(0);
			}

			// check the command line, separate the words
			String[] commandStr = commandLine.split(" ");
			ArrayList<String> command = new ArrayList<String>();
			for (int i = 0; i < commandStr.length; i++) {
				command.add(commandStr[i]);
			}

			if(commandStr[0].equals("create")) {
				if(commandStr.length < 2) {
					System.out.println("Insufficient paramters\nUsage: create <filename>");
					continue;
				}
				Java_create(currentDirectory, commandStr[1]);
				continue;
			}
			
			if(commandStr[0].equals("delete")) {
				if(commandStr.length < 2) {
					System.out.println("Insufficient paramters\nUsage: delete <filename>");
					continue;
				}
				Java_delete(currentDirectory, commandStr[1]);
				continue;
			}
			
			if(commandStr[0].equals("display")) {
				if(commandStr.length < 2) {
					System.out.println("Insufficient paramters\nUsage: display <filename>");
					continue;
				}
				try {
					Java_cat(currentDirectory, commandStr[1]);
				} catch(IOException e) {
					System.out.println("File does not exist");
				}
				continue;
			}
			
			if(commandStr[0].equals("list")) {
				if(commandStr.length == 1) {
					Java_ls(currentDirectory, null, null);
				}
				else if(commandStr.length == 2) {
					Java_ls(currentDirectory, commandStr[1], null);
				}
				else {
					Java_ls(currentDirectory, commandStr[1], commandStr[2]);
				}
				continue;
			}
			
			if(commandStr[0].equals("find")) {
				if(commandStr.length < 2) {
					System.out.println("Insufficient paramters\nUsage: find <keyword>");
					continue;
				}
				Java_find(currentDirectory, commandStr[1]);
				continue;
			}
			
			if(commandStr[0].equals("tree")) {
				if(commandStr.length == 1) {
					Java_tree(currentDirectory, -1, null);
				}
				else if(commandStr.length == 2) {
					try {
						Java_tree(currentDirectory, Integer.parseInt(commandStr[1]), null);
					} catch(NumberFormatException ex) {
						System.out.println("Invalid depth entered\nUsage: tree [depth] [sort_method]");
					}
				}
				else {
					try {
						Java_tree(currentDirectory, Integer.parseInt(commandStr[1]), commandStr[2]);
					} catch(NumberFormatException ex) {
						System.out.println("Invalid depth entered\nUsage: tree [depth] [sort_method]");
					}
				}
				continue;
			}

			// other commands
			ProcessBuilder pBuilder = new ProcessBuilder(command);
			pBuilder.directory(currentDirectory);
			try{
				Process process = pBuilder.start();
				// obtain the input stream
				InputStream is = process.getInputStream();
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);

				// read what is returned by the command
				String line;
				while ( (line = br.readLine()) != null)
					System.out.println(line);

				// close BufferedReader
				br.close();
			}
			// catch the IOexception and resume waiting for commands
			catch (IOException ex){
				System.out.println(ex);
				continue;
			}
		}
	}

	/**
	 * Create a file
	 * @param dir - current working directory
	 * @param command - name of the file to be created
	 */
	public static void Java_create(File dir, String name) throws IOException {
		(new File(dir, name)).createNewFile();
	}

	/**
	 * Delete a file
	 * @param dir - current working directory
	 * @param name - name of the file to be deleted
	 */
	public static void Java_delete(File dir, String name) throws IOException {
		(new File(dir, name)).delete();
	}

	/**
	 * Display the file
	 * @param dir - current working directory
	 * @param name - name of the file to be displayed
	 */
	public static void Java_cat(File dir, String name) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(new File(dir, name)));
		String line = "";
		
		while((line = reader.readLine()) != null) {
			System.out.println(line);
		}
	}

	/**
	 * Function to sort the file list
	 * @param list - file list to be sorted
	 * @param sort_method - control the sort type
	 * @return sorted list - the sorted file list
	 */
	private static File[] sortFileList(File[] list, String sort_method) {
		// sort the file list based on sort_method
		// if sort based on name
		if (sort_method.equalsIgnoreCase("name")) {
			Arrays.sort(list, new Comparator<File>() {
				public int compare(File f1, File f2) {
					return (f1.getName()).compareTo(f2.getName());
				}
			});
		}
		else if (sort_method.equalsIgnoreCase("size")) {
			Arrays.sort(list, new Comparator<File>() {
				public int compare(File f1, File f2) {
					return Long.valueOf(f1.length()).compareTo(f2.length());
				}
			});
		}
		else if (sort_method.equalsIgnoreCase("time")) {
			Arrays.sort(list, new Comparator<File>() {
				public int compare(File f1, File f2) {
					return Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
				}
			});
		}
		return list;
	}

	/**
	 * List the files under directory
	 * @param dir - current directory
	 * @param display_method - control the list type
	 * @param sort_method - control the sort type
	 */
	public static void Java_ls(File dir, String display_method, String sort_method) {
		File[] list = dir.listFiles();
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		
		if(display_method == null) {
			for(File f : list) {
				System.out.println(f.getName());
			}
		}
		else if(sort_method == null) {
			for(File f : list) {
				System.out.println(f.getName() + "\tSize: " + f.length() + "\tLast Modified: " + sdf.format(f.lastModified()));
			}
		}
		else {
			File[] sorted = sortFileList(list, sort_method);
			for(File f : sorted) {
				System.out.println(f.getName() + "\tSize: " + f.length() + "\tLast Modified: " + sdf.format(f.lastModified()));
			}
		}
	}

	/**
	 * Find files based on input string
	 * @param dir - current working directory
	 * @param name - input string to find in file's name
	 * @return flag - whether the input string is found in this directory and its subdirectories
	 */
	public static boolean Java_find(File dir, String name) {
		boolean flag = false;
		
		File[] list = dir.listFiles();
		
		for(File f : list) {
			if(f.getName().contains(name)) {
				System.out.println(f.getAbsolutePath());
				flag = true;
			}
			if(f.isDirectory()) {
				File[] sublist = f.listFiles();
				for(File subf : sublist) {
					if(subf.getName().contains(name)) {
						flag = true;
						System.out.println(subf.getAbsolutePath());
					}
				}
			}
		}
		return flag;
	}

	/**
	 * Print file structure under current directory in a tree structure
	 * @param dir - current working directory
	 * @param depth - maximum sub-level file to be displayed
	 * @param sort_method - control the sort type
	 */
	public static void Java_tree(File dir, int depth, String sort_method) {
		File[] list = dir.listFiles();
		if(sort_method != null) {
			list = sortFileList(list, sort_method);
		}
		for(File f : list) {
			System.out.println(f.getName());
			if(f.isDirectory()) {
				Java_tree_recur(f, depth, 0, sort_method);
			}
		}
	}
	
	public static void Java_tree_recur(File dir, int depth, int currentDepth, String method) {
		if(depth == currentDepth) {
			return;
		}
		
		File[] list = dir.listFiles();
		if(method != null) {
			list = sortFileList(list, method);
		}
		for(File f : list) {
			String offset = "";
			for(int i = 0; i < currentDepth; i++) {
				offset += "  ";
			}
			System.out.println(offset + "|-" + f.getName());
			
			if(f.isDirectory()) {
				Java_tree_recur(f, depth, currentDepth + 1, method);
			}
		}
	}
}
