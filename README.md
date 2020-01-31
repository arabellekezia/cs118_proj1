## Name: Arabelle Siahaan
## ID: 604928106

The design of my server starts in the main function, where it sets up a connection between the client and server 
using sockets. To do this, I used the code that was provided online in the GeeksforGeeks website (https://www.geeksforgeeks.org/socket-programming-cc/), as well as the code in Beej's Guide to Network Programming to set up
a server. Then, I added my own implementation to dump the HTTP request in a separate helper function called 
request that takes in the request, obtained when reading the socket in the main function. 
This function parse the request to get the file name and its extension. I also parsed the string for any 
spaces by checking for '%20', as this represents space. With this request function and the main function, my code is able
to connect to localhost and prints the HTTP request from browser. Lastly, I added another function called print_HTTP 
which gives the HTTP response to display any files or images that is requested from the browser. 
I also added an HTML code that writes "Error 404! File not found" in HTML if the client asks for a file that is non existent. 
Altogether, my server.c file is able to connect the client and server, request for a file/image and displays
it in a browser, and dumps the HTTP request message.

All the libraries I used are: stdio, stdlib, string, signal, dirent, errno, netinet/in, netdb, unistd and sys, and these are 
all to help with string, dirent, error, read, server and socket methods. All of these libraries are also provided in 
the GeeksforGeeks website I mentioned previously.

When writing my code, I ran through some problems, such as error binding sockets, and errors in transmitting files to 
the browser. I noticed that if I just ran my server, I have to wait for a few minutes to rerun the server as the 
sockets are still in use, hence the error with binding sockets. When transmitting files, I also had to make sure 
the order in which I write the HTTP response and how I parse the file names should be correct. 
One of the errors I made was to include the '/' characters when parsing the file name, and hence the program gives 
off an Error 404 all the time. Then I changed the code so that it would only take the file name after the '/'.
