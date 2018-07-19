# Partial-Order-Dependency-Schedule
Given a directed acyclic (dependency) graph G = [N, E], a schedule table is created based on the given dependency of nodes (jobs) in the graph. 

Note: This program is aim to handle all 4 cases in the scheduling.
1. using limited processor where each job takes only 1 unit of time
2. using unlimited processor where each job takes only 1 unit of time
3. using limited processor where jobs take variable unit of times
4. using unlimited processor where jobs take variable unit of times

Input#1 (argv[0/1]): a text file representing the dependency graph, G = [N, E]
The first number is the number of nodes in the graph, followed by a list of edges (dependency) [Ni, Nj] where Ni must be done before Nj.

Input#2 (argv[1/2]): a text file containing the time requirements for jobs.
The first number is the number of nodes in the graph, followed by a list of pairs [Ni, Ti] where Ni is the nodeID and Ti is the unit of times require by Ni.

Input#3: asks user for the number of processors needed from console.

Output#1 (argv[2/3]): the schedule table. (try different number of processors "3, 5, 20")

Output#2 (argv[3/4]): debugging results.
