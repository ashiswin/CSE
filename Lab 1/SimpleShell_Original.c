#include <stdio.h>
#include<stdlib.h>
#include<errno.h>
#include<unistd.h>
#include<string.h>
#include<ctype.h>
#define MAX_INPUT 8192
void main(){//start main

char command[MAX_INPUT];//to store users command
//while loop to keep asking user for more inputs
while(1){
	//Q1
	printf("csh>");
	fgets(command,MAX_INPUT,stdin);//take input from user
	//printf("command %s\n",command);

/*TO DO LIST:*/
/*
////----------------Case 1, create the external process and execute the command in that process----------------------

////----------------Case 2, change directory----------------------
//starts with cd

////--------------Case3, History-------------------------
//check if user enteres history option
*/

	}//end while1
}// end main
