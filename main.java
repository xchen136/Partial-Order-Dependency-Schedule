
import java.util.*;
import java.io.*;

//for the purpose of openList
class node{
	
	private int jobId;
	private node next;
	
	node(){
	}
		
	node(int j){
		jobId = j;
		next = null;
	}
		
	public int getJob(){
		return jobId;
	}
	
	public node getNext(){
		return next;
	}
	
	public void setJob(int j){
		jobId = j;
	}
	
	public void setNext(node n){
		next = n;
	}
	
	//to insert using listHead node that was declared in main
	public void insertionSort(node newNode){
		node temp;
		node spot;
		if(next == null){
			next = newNode;
			return;
		}
		else if(next.getJob() > newNode.getJob()){
			temp = next;
			next = newNode;
			newNode.setNext(temp);
			return;
		}
		
		spot = next;
		while(spot.getNext() != null){
			if(spot.getNext().getJob() < newNode.getJob()){
				spot = spot.getNext();
			}
			else{
				break;
			}
		}
		
		if(spot.getNext() == null){
			spot.setNext(newNode);
		}
		else{
			temp = spot.getNext();
			spot.setNext(newNode);
			newNode.setNext(temp);
		}
	}
	
	public int deleteHead(){
		int id = next.getJob();
		node temp = next;
		next = temp.getNext();
		temp.setNext(null);
		return id;
	}
	
	public void printList(FileWriter write2){
		PrintWriter out = new PrintWriter(write2);
		node current = next;
		while(current != null){
			out.print(current.getJob() + "\t");
			current = current.getNext();
		}
		out.println("");
	}

}

public class main {
	
	public static void main(String[] args)throws IOException{
		File inFile1 = new File(args[0]);
		File inFile2 = new File(args[1]);
		FileWriter outFile1 = new FileWriter(args[2]);
		FileWriter outFile2 = new FileWriter(args[3]);
		Scanner read1 = new Scanner(inFile1);
		Scanner read2 = new Scanner(inFile2);
		Scanner input = new Scanner(System.in);
		PrintWriter write1 = new PrintWriter(outFile1);
		PrintWriter write2 = new PrintWriter(outFile2);
		
		int procNeed;																	//maximum number of processors needed
		int numJob = 0;																		
		int totalTime = 0;
		int procUsed = 0;																//number of processors being used currently
		int p;
		int time = 0;
		int timeCost;
		int currentJob;
		int kid;
		
		node newNode;
		node tempN;
		node openList;																	//openList for orphan jobs waiting to go on the processor						
		node[] hashTable;																//dependency graph
		int[] processJob;																//specifies the job on each processor
		int[] processTime;																//specifies the remaining time of job on each processor
		int[] parentCount;																//number of parents of each job
		int[] jobTime;																	//timeCost of each job
		int[] jobDone;																	//whether each job is done
		int[] jobMarked;																//whether each job is on the processor
		int[][] scheduleTable;															//final result for output
		
		//first asks for the number of given processors 
		System.out.print("Number of Fixed Processors: ");							
		procNeed = input.nextInt();
		
		int job1, job2;
		
		//reading dependency pairs
		if(read1.hasNextInt()){			
			numJob = read1.nextInt();
			
			//if given processors are more than the number of jobs ->unlimited, extra is not needed 													
			if(procNeed>numJob){														
				procNeed = numJob;														
			}		
		}
		
		//initialize arrays
		hashTable = new node[numJob+1];		
		for(int x=0; x<=numJob; x++){
			hashTable[x] = new node(0);
		}
		
		processJob = new int[procNeed+1];												
		processTime = new int[procNeed+1];												
		parentCount = new int[numJob+1];
		jobTime = new int[numJob+1];
		jobDone = new int[numJob+1];
		jobMarked = new int[numJob+1];

		
		//store the job2 (children) who depends on job1 (parent) under hashTable in order
		while(read1.hasNextInt() && read1.hasNextInt()){	
			job1 = read1.nextInt();
			job2 = read1.nextInt();
			newNode = new node(job2);												
			hashTable[job1].insertionSort(newNode);									
			parentCount[job2]++;													
		}
		
		int temp;
		//reading timeCost of each job
		if(read2.hasNextInt()){ 
			temp = read2.nextInt();
			if(temp == numJob){
				while(read2.hasNextInt() && read2.hasNextInt()){
					job1 = read2.nextInt();
					timeCost = read2.nextInt();
					jobTime[job1] = timeCost;
					totalTime += timeCost;
				}
			}
			else{
				System.out.println("Data and Time file must contain the same number of jobs.\n");
				System.exit(0);
			}
		}
		else{
			System.out.println("Corrupted Input File #2");
			System.exit(0);
		}

		
		write2.println("Total Jobs: " + numJob);
		write2.println("Total Job Time: " + totalTime);
		write2.println("Total Processors: " + procNeed);
		write2.println("");
		write2.println("[Dependency Table]");
		for(int i=1; i<=numJob; i++){													
			write2.print("Job " + i + ":\t");
			hashTable[i].printList(outFile2 );
		}
		
		//initialize the schedule Table	
		scheduleTable = new int[procNeed+1][totalTime+1];												

		
		openList = new node(0);
		//while there are still jobs not done, find orphans -> add to openList, find avail processors -> put on processor, time++, update
		while(!allDone(jobDone, numJob)){													
				
			//Find an orphan job and add to openList 
			for(int j=1; j<=numJob; j++){													
				if(parentCount[j] == 0 && jobDone[j] == 0 && jobMarked[j] == 0){			
					newNode = new node(j);
					openList.insertionSort(newNode);										
				}
			}
			
			//place all orphan jobs onto an available processor if possible
			while(openList.getNext() != null){												
				currentJob = openList.deleteHead();
				
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
			printTable(outFile2, scheduleTable, procUsed, time);								
			
			//print current states for debugging purpose
			write2.println("");
			write2.println("[Time: " + time + "   ProcUsed: " + procUsed + "] \n");
			write2.print("processJob:\t");
			print1D(outFile2, processJob, procUsed);
			write2.print("processTime:\t");
			print1D(outFile2, processTime, procUsed);
			write2.print("parentCount:\t");
			print1D(outFile2, parentCount, numJob);
			write2.print("jobTime:\t");
			print1D(outFile2, jobTime, numJob);
			write2.print("jobDone:\t");
			print1D(outFile2, jobDone, numJob);
			write2.print("jobMarked:\t");
			print1D(outFile2, jobMarked, numJob);
			write2.println("");
			
			time++;
			for(p=1; p<=procUsed; p++){												
				processTime[p]--;
			}
			
			//find all jobs that are done under currentTime
			for(p=1; p<=procUsed; p++){												
				if(processTime[p] == 0){												
					currentJob = processJob[p]; 										
					processJob[p] = 0;													
					jobDone[currentJob] = 1;											
					jobMarked[currentJob] = 0;											
					while(hashTable[currentJob].getNext() != null){						
						kid = hashTable[currentJob].getNext().getJob();
						parentCount[kid]--;											
						tempN = hashTable[currentJob].getNext();
						hashTable[currentJob].setNext(tempN.getNext());
					}
				}
			}	
			
			if(noOrphan(parentCount, jobDone, numJob) && !allDone(jobDone, numJob) && procEmpty(processJob, procUsed)){
				System.out.println("There is a cycle in the dependency graph.\n");
				System.exit(0);
			}	
		}	
		
		write1.println("---------------------------------------------------------------------Schedule Table-----------------------------------------------------------------------\n");
		printTable(outFile1, scheduleTable, procUsed, time);	
		
		read1.close();
		read2.close();
		write1.close();
		write2.close();
	}
	
	public static void print1D(FileWriter out, int[] dArray, int size){
		PrintWriter write = new PrintWriter(out);
		for(int x=1; x<=size; x++){
			write.print("(" + x + ") " + dArray[x] + "\t");
		}
		write.println("");
	}

	public static boolean procEmpty(int[] pArray, int size){
		for(int p=1; p<=size; p++){														
			if(pArray[p] != 0){                                                             
				return false;																
			}
		}
		return true;																		
	}

	public static boolean allDone(int[] jArray, int size){
		for(int j=1; j<=size; j++){
			if(jArray[j] == 0){
				return false;
			}
		}
		return true;
	}

	public static boolean noOrphan(int[] parentArray, int[] doneArray, int size){
		for(int j=1; j<=size; j++){
			if(doneArray[j] == 0 && parentArray[j] == 0){
				return false;
			}
		}
		return true;
	}

	public static int availProc(int[] pArray, int size){												
		for(int p=1; p<=size; p++){
			if(pArray[p] <= 0){															
				return p;															
			}
		}
		return 0;
	}

	public static void printTable(FileWriter out, int[][] table, int p, int t){
		PrintWriter write = new PrintWriter(out);
		write.println("");
		write.println("[Current Time: " + t + "]");
		for(int proc=1; proc<=p; proc++){
			write.print("Processor " + proc + ": \t");
			for(int time=0; time<t; time++){
				write.print(table[proc][time] + "\t");
			}
			write.println("");
		}
	}
}

