#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <signal.h>
#include <dirent.h>
#include <errno.h>
#include <netinet/in.h>
#include <netdb.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <sys/socket.h>
#define PORT 8080

int sock_fd, new_sock;
void request(char* request);
void print_http(int extension, int is_valid, FILE* file);
int get_extension_num(char* fileName);

int main(void){
  socklen_t client_len;
  struct sockaddr_in server_address;

  sock_fd = socket(AF_INET, SOCK_STREAM, 0);
  if (sock_fd < 0){
    fprintf(stderr, "Error opening socket.\n");
    exit(EXIT_FAILURE);
  }

  memset((char*)&server_address, 0, sizeof(server_address));
  server_address.sin_family = AF_INET;
  server_address.sin_addr.s_addr = htonl(INADDR_ANY);
  server_address.sin_port = htons(PORT);

  int bind_stat = bind(sock_fd, (struct sockaddr*)&server_address, sizeof(server_address));
  if (bind_stat < 0) {
    fprintf(stderr, "Error binding socket to the port.\n");
    exit(EXIT_FAILURE);
  }

  struct sockaddr_in client_address;
  socklen_t len;

  listen(sock_fd, 1);
  while (1){
    new_sock = accept(sock_fd, (struct sockaddr*)&client_address, &len);
    if (new_sock < 0) {
      fprintf(stderr, "Error accepting socket.\n");
      exit(EXIT_FAILURE);
    }

    char buff[2048];
    memset(buff, 0, 2048);
    int read_stat = read(new_sock, buff, 2048);
    if (read_stat < 0) {
      fprintf(stderr, "Error in reading.\n");
      exit(EXIT_FAILURE);
    }
    int child = fork();  // create child processes
    if (child == -1){
      exit(1);
    }
    else if (child == 0){
	    close(new_sock);
	    exit(0);
    }
    else{
      close(new_sock);
      waitpid(-1, NULL, WNOHANG);
    }
  }
}
