#include <stdio.h>
#include <stdlib.h>
#include <errno.h>
#include <unistd.h>
#include <string.h>
#include <ctype.h>
#define MAX_INPUT 8192

// Structure to hold history entries
struct historyEntry {
	struct historyEntry *before; // Pointer to earlier command
	char* command; // Pointer to store command string
};

void main(int argc, char** argv) {
	char command[MAX_INPUT]; // String to store user entered command
	char tokenable[MAX_INPUT]; // Copy of command string for tokenization
	
	struct historyEntry *head; // Pointer to head of history stack
	int historySize = 0; // Counter of history stack size
	
	// Print program details if entering CLI mode (no command line arguments passed)
	if(argc == 1) {
		printf("Copyright (C) 2018 Isaac Ashwin Ravindran (1002151)\n");
		printf("csh Version 1.0.0\n\n");
	}
	
	// Begin main program loop
	while(1) {
		if(argc > 1) { // Check if commands were passed as arguments and execute those instead
			strcpy(command, argv[1]);
		}
		else {
			printf("csh> "); // Print prompt
			fgets(command, MAX_INPUT,stdin); // Read user input
		}
		
		strcpy(tokenable, command); // Get a tokenizable copy
		
		char* comm = strtok(tokenable, " "); // Get the first token of the command string
		if(comm[strlen(comm) - 1] == '\n') comm[strlen(comm) - 1] = 0; // Truncate any newlines
		
		// Perform history command checking
		if(strcmp(comm, "!!") == 0) {
			// Check for any prior commands
			if(head == NULL) {
				printf("Error: No prior command executed\n");
				if(argc > 1) return; // If run with args, quit
				continue;
			}
			strcpy(command, head->command); // Set the current command to the previous command
		}
		else if(strcmp(comm, "history") == 0) { // Check if need to print history
			struct historyEntry *iteration = head;
			int i = 1;
			
			while(iteration != NULL) {
				printf("%d %s", i, iteration->command);
				iteration = iteration->before;
				
				i++;
			}
			if(argc > 1) return; // If run with args, quit
			continue;
		}
		else if(atoi(comm) != 0) { // Check if integer was entered
			int historyIndex = atoi(comm); // Parse integer value
			
			// Check if requested command exists
			if(historyIndex > historySize) {
				printf("Error: Going too far back in history, command doesn't exist\n");
				if(argc > 1) return; // If run with args, quit
				continue;
			}
			
			// Reduce back to 0-indexing
			historyIndex--;
			
			struct historyEntry *iteration = head; // Get a copy to the head of the stack
			
			// Iterate to the entry requested
			for(int i = 0; i < historyIndex; i++) {
				iteration = iteration->before;
			}
			
			// Set current command to historic command
			strcpy(command, iteration->command);
		}
		
		if(strcmp(comm, "cd") == 0) { // Check for change directory command
			char dir[MAX_INPUT]; // Allocate array to hold the directory
			
			int len = 0; // Variable to hold last index of dir string
			char *dirPart = strtok(NULL, " "); // Continue tokenizing string
			
			while(dirPart != NULL) { // Loop until no more tokens exist
				// Truncate newline from end of string
				if(dirPart[strlen(dirPart) - 1] == '\n') {
					dirPart[strlen(dirPart) - 1] = 0;
				}
				
				// Check if it is the first directory party
				if(len > 0) { // If it is not, splice token with a space
					*(dir + len) = ' ';
					strcpy(dir + len + 1, dirPart);
					len += strlen(dirPart) + 1;
				}
				else { // Otherwise splice token alone
					strcpy(dir + len, dirPart);
					len += strlen(dirPart);
				}
				
				dirPart = strtok(NULL, " "); // Continue tokenizing string
			}
			
			if(dir[0] == '~') { // Check if we need to use the home directory
				char* temp = malloc(strlen(getenv("HOME")) + strlen(dir)); // Allocate string for the expanded location
				dir[0] = '/';
				sprintf(temp, "%s%s", getenv("HOME"), dir); // Build the new string
				strcpy(dir, temp); // Copy the newly formed directory into the initial directory string
			}
			
			if(strcmp(dir, "..") == 0) { // Check if we need to change to parent directory
				int i; // For-loop index
				
				for(i = strlen(dir) - 1; i >= 0; i--) { // Loop through string backwards
					if(dir[i] == '\\' || dir[i] == '/') { // Truncate string at last '/' or '\' character and break
						dir[i] = 0;
						break;
					}
				}
				chdir(dir); // Change to the parent
			}
			else { // Fallback to change to child directory
				if(chdir(dir) != 0) { // Check for successful change
					printf("Error: Directory %s does not exist!\n", dir); // Display any error and continue to next loop
					if(argc > 1) return; // If run with args, quit
					continue;
				}
			}
			
		}
		else if(strcmp(comm, "exit") == 0) { // Check for exit
			break; // Quit
		}
		else {
			if(system(command) != 0) { // Check for successful command execution
				if(argc > 1) return; // If run with args, quit
				continue; // Move on to next iteration
			}
		}
		
		// Reach here if a valid command was executed
		
		// Push the command onto the history stack
		struct historyEntry *newCommand = (struct historyEntry*) malloc(sizeof(struct historyEntry)); // Allocate for new command entry
		
		newCommand->command = (char*) malloc(sizeof(command)); // Allocate space for command string
		strcpy(newCommand->command, command); // Copy command string into the structure
		
		if(head == NULL) { // Check if head of stack exists
			head = newCommand; // Set new head
			newCommand->before = NULL;
		}
		else { // Push current command onto the stack
			newCommand->before = head;
			head = newCommand;
		}
		historySize++; // Increment history counter
		
		if(argc > 1) return; // If run with args, quit
	}
	
	// Free up history entries allocated
	while(head != NULL) {
		struct historyEntry *temp = head; // Store current head
		head = head->before; // Shift head backward
		
		// Free allocated string then the structure itself
		free(temp->command);
		free(temp);
	}
}
