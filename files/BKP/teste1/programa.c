#include <stdio.h>
#include <stdlib.h>

int main(int argc, char const *argv[])
{
	
	char ch;
	int characters = 0;
	FILE* words = fopen("words.txt", "rt");
	FILE* open = fopen("results/resultado.txt", "wt");

	if(words != NULL){
		while((ch = fgetc(words)) != EOF){
			characters++;
		}
	}

	fprintf(open, "%d\n", characters);
	

	fclose(words);
	fclose(open);

	return 0;
}