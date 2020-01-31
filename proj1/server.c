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
      request(buff);
	    close(new_sock);
	    exit(0);
    }
    else{
      close(new_sock);
      waitpid(-1, NULL, WNOHANG);
    }
  }
}

void request(char* request)
{
  printf("%s", request);
  char* token = strtok(request, "\r\n");
  // count how many words and store in get
  char* get_words[30];
  char* split_words = strtok(token, " ");
  get_words[0] = split_words;
  int words = 1;
  while (split_words != NULL)
  {
      get_words[words-1] = split_words;
      split_words = strtok(NULL, " ");
      words++;
  }

  int file_len = strlen(get_words[1]);
  char name[2048];
  int name_ctr = 0;
  for (int i = 1; i < file_len; i++){
    if (((i+2) < file_len) && (get_words[1][i] == '%' && get_words[1][i+1] == '2' && get_words[1][i+2] == '0'))
    {
      name[name_ctr] = ' ';
      i += 2;
      name_ctr++;
    }
    else{
      name[name_ctr] = get_words[1][i];
      name_ctr++;
    }
  }
  name[name_ctr] = '\0';

  DIR *dir;
  dir = opendir("./");
  FILE *file;
  struct dirent *file_dir;
  int is_valid = 0;
  int extension = -1;
  if (dir != NULL){
    while ((file_dir = readdir(dir))){
      if (strcasecmp(file_dir->d_name, name) == 0){
        file = fopen(file_dir->d_name, "r");
        extension = get_extension_num(file_dir->d_name);
        if (file == NULL){
          fprintf(stderr, "Error opening file.\n");
        }
        else{
          is_valid = 1;
        }
      }
    }
    (void)closedir(dir);
  }
  print_http(extension, is_valid, file);
}

int get_extension_num(char* file){
char* s = strtok(file, ".");
s = strtok(NULL, ".");
if (s == NULL)
  return 1;
if ((strcmp("html", s) == 0) ||(strcmp("htm", s) == 0))
  return 0;
else if (strcmp("png", s) == 0)
  return 2;
else if (strcmp("jpg", s) == 0)
  return 3;
else if (strcmp("jpeg", s) == 0)
  return 4;
else if (strcmp("gif", s) == 0)
  return 5;
else
  return 1;
}

void print_http(int extension, int is_valid, FILE *file){
  char* file_type;
  switch (extension){
    case 1:
      file_type = "Content-Type: text/plain\r\n\0";
      break;
    case 2:
      file_type = "Content-Type: image/jpg\r\n\0";
      break;
    case 3:
      file_type = "Content-Type: image/jpeg\r\n\0";
      break;
    case 4:
      file_type = "Content-Type: image/png\r\n\0";
      break;
    case 5:
      file_type = "Content-Type: image/gif\r\n\0";
      break;
    case 0:
    default:
      file_type = "Content-Type: text/html\r\n\0";
      break;
  }
  char* header = "HTTP/1.1 200 OK\rn\n\0";
  char* not_found_header = "HTTP/1.1 404 Not Found\r\n\0";
  char* not_found_error_html = "<html><body><b>Error 404!</b><br>File requested not found.</body></html>";
  int length;
  char* buff = NULL;
  if (is_valid){
    if (fseek(file, 0, SEEK_END) != 0){
      fprintf(stderr, "Error using fseek.\n");
    }
    length = ftell(file);
    buff = malloc(sizeof(char)*(length+1));
    if (fseek(file, 0, SEEK_SET) != 0){
      fprintf(stderr, "Error using fseek.\n");
    }
    fread(buff, sizeof(char), length, file);
    buff[length] = '\0';
  }

  if (is_valid){
    if (write(new_sock, header, strlen(header)) < 0){
      fprintf(stderr, "Error writing header.\n");
    }
  }
  else{
    if (write(new_sock, not_found_header, strlen(not_found_header)) < 0){
      fprintf(stderr, "Error writing header for file not found.\n");
    }
  }

  if (write(new_sock, file_type, strlen(file_type)) < 0){
    fprintf(stderr, "Error writing file extension.\n");
  }
  if (write(new_sock, "\r\n\0", strlen("\r\n\0")) < 0){
    fprintf(stderr, "Error writing newline and carriage return.\n");
  }
  if (is_valid){
    if (write(new_sock, buff, length) < 0){
      fprintf(stderr, "Error writing buffer.\n");
    }
  }
  else{
    if (write(new_sock, not_found_error_html, strlen(not_found_error_html)) < 0){
      fprintf(stderr, "Error writing 404 error in HTML.\n");
    }
  }

  free(buff);
}
