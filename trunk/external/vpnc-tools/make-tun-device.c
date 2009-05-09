#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <unistd.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <unistd.h>
#include <stdio.h>


/* TODO: Fix this, so it uses the defines */
#define devnetdir "/dev/net"
#define devicenode "/dev/net/tun" 

#define MAJOR 10
#define MINOR 200 

int main(void) {

	/* Want to make a character device */
	mode_t mode = S_IFCHR;

	mode_t perm = 0666;
	dev_t dev = 0;

	struct stat st;

	dev = (MAJOR << 8) | MINOR;

	mode |= perm;

	if(stat( devnetdir ,&st) == 0) {
       		printf(" /dev/net directory already present\n"); 
	}
	else {	

		/* Make the path first */ 
		if (mkdir( devnetdir, S_IRWXU | S_IRWXG | S_IROTH | S_IXOTH ) == -1)  {
			printf("Can't create the /dev/net directory\n");
			_exit(-1);
		}
		
	}

	dev = (MAJOR << 8) | MINOR;

	mode |= perm;

	if(stat(devicenode ,&st) == 0) {
        		printf("already present, not creating /dev/net/tun\n");
			_exit(-1); 
	}
	else {
		/* Make the node */
		if ( mknod( "/dev/net/tun" , mode , dev) == -1) {
			printf("Cant create /dev/net/tun character device\n");
			_exit(-2); 
		}
	}

	return 0; 

}
