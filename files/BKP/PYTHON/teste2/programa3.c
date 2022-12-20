#include <stdio.h>
#include <string.h>

int main() {
  // Open the file for reading
  FILE *fp = fopen("words.txt", "r");
  FILE* open = fopen("results/resultado.txt", "wt");

  // Make sure the file was opened successfully
  if (fp == NULL) {
    printf("Error opening file!\n");
    return 1;
  }

  char buffer[1024];
  char substring[1024];
  int counter = 0;

  // Read the first line of the file
  if (fgets(substring, sizeof(substring), fp) != NULL) {
    // Split the line on the newline character
    char *token = strtok(substring, "\n");
    // Store the first token (the substring) in the substring variable
    strcpy(substring, token);

    // Read the contents of the file line by line
    while (fgets(buffer, sizeof(buffer), fp) != NULL) {
      // Search for the substring in the current line
      char *result = strstr(buffer, substring);
      // If the substring was found, increment the counter
      if (result != NULL) {
        counter++;
      }
    }

    // Print the final count
    printf("Number of substrings found: %d\n", counter);
    fprintf(open, "%d\n", counter);
  }

  // Close the file
  fclose(fp);

  return 0;
}