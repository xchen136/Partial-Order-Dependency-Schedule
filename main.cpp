
#include<iostream>
#include<fstream>
using namespace std;

void print1D(ofstream& out, int* array, int size){
	for(int x=1; x<=size; x++){
		out<<"("<<x<<") "<<array[x]<<"\t";
	}
	out<<endl;
}

bool procEmpty(int* pArray, int size){
	for(int p=1; p<=size; p++){														
		if(pArray[p] != 0){                                                             
			return false;																
		}
	}
	return true;																		
}

bool allDone(int* jArray, int size){
	for(int j=1; j<=size; j++){
		if(jArray[j] == 0){
			return false;
		}
	}
	return true;
}

bool noOrphan(int* parentArray, int* doneArray, int size){
	for(int j=1; j<=size; j++){
		if(doneArray[j] == 0 && parentArray[j] == 0){
			return false;
		}
	}
	return true;
}

int availProc(int* pArray, int size){												
	for(int p=1; p<=size; p++){
		if(pArray[p] <= 0){															
			return p;															
		}
	}
	return 0;
}

void printTable(ofstream& out, int** table, int p, int t){
	out<<"\n[Current Time: "<<t<<"]\n";
	for(int proc=1; proc<=p; proc++){
		out<<"Processor "<<proc<<": \t";
		for(int time=0; time<t; time++){
			out<<table[proc][time]<<"\t";
		}
		out<<endl;
	}
}

class node{
	
	private:
		int jobId;
		node* next = NULL;
	
	public:
		node(){
		}
		
		node(int j){
			jobId = j;
		}
		
		node(int j, node* n){
			jobId = j;
			next = n;
		}
		
		int getJob(){
			return jobId;
		}
		
		node* getNext(){
			return next;
		}
		
		void setJob(int j){
			jobId = j;
		}
		
		void setNext(node* n){
			next = n;
		}
		
		void insertionSort(node* newNode){
			if(next == NULL){														
				next = newNode;
				return;
			}
			else if(next->getJob() > newNode->getJob()){							
				node* temp = next;
				next = newNode;
				newNode->setNext(temp);
				return;
			}
			
			node* current = next;
			node* spot = current;
			while(current != NULL){													
				if(current->getJob() < newNode->getJob()){
					spot = current;
					current = current->getNext();
				}
				else{
					break;
				}
			}
			if(spot->getNext() == NULL){										
				spot->setNext(newNode);
			}
			else{																
				node* temp;
				temp = spot->getNext();
				spot->setNext(newNode);
				newNode->setNext(temp);
			}
		}
		
		int deleteHead(){
			int id = next->getJob();
			node* temp = next;
			next = temp->getNext();
			temp->setNext(NULL);
			return id;
		}
		
		void printList(ofstream& write2){
			node* current = next;
			while(current != NULL){
				write2<<current->getJob()<<"\t";
				current = current->getNext();
			}
			write2<<endl;
		}
};

int main (int argc, char** argv){
	ifstream read1;
	ifstream read2;
	ofstream write1;
	ofstream write2;
	
	read1.open(argv[1]);
	read2.open(argv[2]);
	write1.open(argv[3]);
	write2.open(argv[4]);
	
	int procNeed;																	//maximum number of processors needed
	int numJob;																		
	int totalTime = 0;
	int procUsed = 0;																//number of processors being used currently
	int time = 0;
	int timeCost;
	int currentJob;
	node* newNode;
	int kid;
	node* tempN;
	
	int** scheduleTable;															//final result for output
	node* hashTable;																//dependency graph
	node* openList;																	//openList for orphan jobs waiting to go on the processor						
	int* processJob;																//specifies the job on each processor
	int* processTime;																//specifies the remaining time of job on each processor
	int* parentCount;																//number of parents of each job
	int* jobTime;																	//timeCost of each job
	int* jobDone;																	//whether each job is done
	int* jobMarked;																	//whether each job is on the processor
	
	//check the number of arguments
	if (argc != 5) {																
        cerr << "Error: Number of arguments unsatisfied."<<endl;
        exit(1);
    }
	
	cout<<"Number of Fixed Processors: \n";											//first asks for the number of processors available
	cin>>procNeed;
	
	int job1, job2;
	
	//reading dependency pairs
	if(read1>>numJob){			
	
		//if given processors are more than the number of jobs ->unlimited, extra is not needed 													
		if(procNeed>numJob){														
			procNeed = numJob;														
		}																			
	}
	
	//initialize arrays
	hashTable = new node[numJob+1];												
	processJob = new int[procNeed+1]();												
	processTime = new int[procNeed+1]();												
	parentCount = new int[numJob+1]();
	jobTime = new int[numJob+1]();
	jobDone = new int[numJob+1]();
	jobMarked = new int[numJob+1]();
		
	//store the children (depended on parent) of job1 under hashTable in order
	while(read1>>job1 && read1>>job2){											
		newNode = new node(job2);												
		hashTable[job1].insertionSort(newNode);									
		parentCount[job2]++;													
	}
	
	int temp;
	//reading timeCost of each job
	if(read2>>temp && temp==numJob){
		while(read2>>job1 && read2>>timeCost){
			jobTime[job1] = timeCost;
			totalTime += timeCost;
		}
	}
	else{
		cout<<"Input File#2 is Corrupted.\n";
		exit(0);
	}
	
	write2<<"Total Jobs: "<<numJob<<endl<<"Total Job Time: "<<totalTime<<endl<<"Total Processors: "<<procNeed<<endl;
	write2<<"\n[Dependency Table]\n";
	for(int i=1; i<=numJob; i++){													
		write2<<"Job "<<i<<":\t";
		hashTable[i].printList(write2);
	}
	
	//initialize the schedule Table	
	scheduleTable = new int*[procNeed+1];												
	for(int i=0; i<=procNeed; i++){
		scheduleTable[i] = new int[totalTime+1]();
	}
	
	int p;
	//while there are still jobs not done, find orphans -> add to openList, find avail processors -> put on processor, time++, update
	while(!allDone(jobDone, numJob)){													
			
		//Find an orphan job and add to openList 
		for(int j=1; j<=numJob; j++){													
			if(parentCount[j] == 0 && jobDone[j] == 0 && jobMarked[j] == 0){			
				newNode = new node(j);
				openList->insertionSort(newNode);										
			}
		}
		
		//place all orphan jobs onto an available processor if possible
		while(openList->getNext() != NULL){												
			currentJob = openList->deleteHead();
			
			//finding an available processor within the used processors
			if(procUsed != 0){															
				if((p = availProc(processJob, procUsed)) != 0){							
					processJob[p] = currentJob;																		
					processTime[p] = jobTime[currentJob];								
					for(int t=time; t<time+jobTime[currentJob]; t++){					
						scheduleTable[p][t] = currentJob;
					}	
					jobMarked[currentJob] = 1;			
					continue;								
				}	
			}
			
			//Add an additional processor if possible
			if(jobMarked[currentJob] == 0 && (procUsed + 1) <= procNeed){												
				procUsed++;
				processJob[procUsed] = currentJob;
				processTime[procUsed] = jobTime[currentJob];
				for(int t=time; t<time+jobTime[currentJob]; t++){						
					scheduleTable[procUsed][t] = currentJob;
				}
				jobMarked[currentJob] = 1;												
			}
			else{																		
				break;
			}
		}	
		
		//schedule Table after all placing jobs onto the processor
		printTable(write2, scheduleTable, procUsed, time);								
		
		//print current states for debugging purpose
		write2<<endl<<"[Time: "<<time<<"   ProcUsed: "<<procUsed<<"] \n";
		write2<<"processJob:\t";
		print1D(write2, processJob, procUsed);
		write2<<"processTime:\t";
		print1D(write2, processTime, procUsed);
		write2<<"parentCount:\t";
		print1D(write2, parentCount, numJob);
		write2<<"jobTime:\t";
		print1D(write2, jobTime, numJob);
		write2<<"jobDone:\t";
		print1D(write2, jobDone, numJob);
		write2<<"jobMarked:\t";
		print1D(write2, jobMarked, numJob);
		write2<<endl;
		
		time++;
		for(int proc=1; proc<=procUsed; proc++){												
			processTime[proc]--;
		}
		
		//find all jobs that are done under currentTime
		for(int proc=1; proc<=procUsed; proc++){												
			if(processTime[proc] == 0){												
				currentJob = processJob[proc]; 										
				processJob[proc] = 0;													
				jobDone[currentJob] = 1;											
				jobMarked[currentJob] = 0;											
				while(hashTable[currentJob].getNext() != NULL){						
					kid = hashTable[currentJob].getNext()->getJob();
					parentCount[kid]--;											
					tempN = hashTable[currentJob].getNext();
					hashTable[currentJob].setNext(tempN->getNext());
				}
			}
		}	
		
		if(noOrphan(parentCount, jobDone, numJob) && !allDone(jobDone, numJob) && procEmpty(processJob, procUsed)){
			cout<<"There is a cycle in the dependency graph.\n";
			exit(0);
		}	
	}	
	
	write1<<"---------------------------------------------------------------------Schedule Table-----------------------------------------------------------------------\n";
	printTable(write1, scheduleTable, procUsed, time);	
	
    for (int i = 0; i <= procNeed ; i++){
   		delete[] scheduleTable[i];
	}
	delete []hashTable;
	delete []processJob;
	delete []processTime;
	delete []parentCount;
	delete []jobTime;
	delete []jobDone;
	delete []jobMarked;
	delete newNode;
	
	read1.close();
	read2.close();
	write1.close();
	write2.close();
}
