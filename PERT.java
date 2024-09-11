/* Starter code for PERT algorithm (Project 4)
 * @author rbk
 */

// change to your netid
package jxp230045;

// replace sxa173731 with your netid below
import jxp230045.Graph;
import jxp230045.Graph.Vertex;
import jxp230045.Graph.Edge;
import jxp230045.Graph.GraphAlgorithm;
import jxp230045.Graph.Factory;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Scanner;

public class PERT extends GraphAlgorithm<PERT.PERTVertex> {
    LinkedList<Vertex> finishList;
	int CPL;

    public static class PERTVertex implements Factory {
	// Add fields to represent attributes of vertices here
	int lastFinish, lastStart, earliestStart, earliestFinish;
	int duration;
	Vertex parent;
	String status;

	public PERTVertex(Vertex u) {
	}
	public PERTVertex make(Vertex u) { return new PERTVertex(u); }
    }

    // Constructor for PERT is private. Create PERT instances with static method pert().
    private PERT(Graph g) {
	super(g, new PERTVertex(null));
		finishList = new LinkedList<>();
		CPL = -1;
    }

    public void setDuration(Vertex u, int d) {
		get(u).duration = d;
    }

	//wrapper method for checking graph for DAG
	public boolean isDAGAll(){
		for (Vertex v : g){
			get(v).status = "new";
		}
		for(Vertex v : g){
			if(get(v).status.equals("new")){
				if(!isDAG(v)){
					return false;
				}
			}
		}
		return true;
	}

	public boolean isDAG(Vertex v){
		get(v).status = "active";
		for(Edge e : g.incident(v)){
			Vertex u = e.otherEnd(v);
			if(get(u).status.equals("active")){
				return false;
			}
			else if(get(u).status.equals("new")){
				if(!isDAG(u)){
					return false;
				}
			}
		}
		get(v).status = "finished";
		return true;
	}
    // Implement the PERT algorithm. Returns false if the graph g is not a DAG.
    public boolean pert() {
		if(!isDAGAll()) {
			return false;
		}
		LinkedList<Vertex> f_list = topologicalOrder();
		//making a list where we are storing topological ordering of graph
		for(Vertex v : f_list){
			get(v).earliestFinish = get(v).earliestStart + get(v).duration;
			if(get(v).earliestFinish > CPL){
				CPL = get(v).earliestFinish;          //CPL get updated
			}
			//We are updating earliest finish time and earliest start time of every node in topological list
			for(Edge e : g.incident(v)){
				Vertex u = e.otherEnd(v);
				if(get(u).earliestStart < get(v).earliestFinish){
					get(u).earliestStart = get(v).earliestFinish;
				}
			}
		}
		for(Vertex v : f_list){
			get(v).lastFinish = CPL;
		}
		//we are going reverse on topological list and updating last finish time and last start time
		for(Vertex v : f_list.reversed()){
			get(v).lastStart = get(v).lastFinish - get(v).duration;
			for(Edge e : g.inEdges(v)){
				Vertex u = e.otherEnd(v);
				if(get(u).lastFinish > get(v).lastStart){
					get(u).lastFinish = get(v).lastStart;
				}
			}
		}
		return true;
    }

	public void topologicalOrder(Vertex v){
		get(v).status = "mark";
		for(Edge e : g.incident(v)){
			Vertex u = e.otherEnd(v);
			if(get(u).status.equals("unmark")){
				get(u).parent = v;
				topologicalOrder(u);
			}
		}
		finishList.addFirst(v);
	}

	// Find a topological order of g using DFS
    LinkedList<Vertex> topologicalOrder() {
		for(Vertex v : g){
			get(v).status = "unmark";
		}
		for(Vertex v : g){
			if(get(v).status.equals("unmark")){
				topologicalOrder(v);
			}
		}
		return finishList;
    }


    // The following methods are called after calling pert().

    // Earliest time at which task u can be completed
    public int ec(Vertex u) {
		return get(u).earliestFinish;
    }

    // Latest completion time of u
    public int lc(Vertex u) {
		return get(u).lastFinish;
    }

    // Slack of u
    public int slack(Vertex u) {
		return lc(u) - ec(u);
    }

    // Length of a critical path (time taken to complete project)
    public int criticalPath() {
		return CPL;
    }

    // Is u a critical vertex?
    public boolean critical(Vertex u) {
		if(slack(u) == 0){
			return true;
		}
		return false;
    }

    // Number of critical vertices of g
    public int numCritical() {
		int count = 0;
		for(Vertex v : finishList){
			if(slack(v) == 0){
				count++;
			}
		}
		return count;
    }

    /* Create a PERT instance on g, runs the algorithm.
     * Returns PERT instance if successful. Returns null if G is not a DAG.
     */
    public static PERT pert(Graph g, int[] duration) {
	PERT p = new PERT(g);
	for(Vertex u: g) {
	    p.setDuration(u, duration[u.getIndex()]);
	}
	// Run PERT algorithm.  Returns false if g is not a DAG
	if(p.pert()) {
	    return p;
	} else {
	    return null;
	}
    }

    public static void main(String[] args) throws Exception {
	String graph = "10 13   1 2 1   2 4 1   2 5 1   3 5 1   3 6 1   4 7 1   5 7 1   5 8 1   6 8 1   6 9 1   7 10 1   8 10 1   9 10 1      0 3 2 3 2 1 3 2 4 1";
	Scanner in;
	// If there is a command line argument, use it as file from which
	// input is read, otherwise use input from string.
	in = args.length > 0 ? new Scanner(new File(args[0])) : new Scanner(graph);
	Graph g = Graph.readDirectedGraph(in);
	g.printGraph(false);

	int[] duration = new int[g.size()];
	for(int i=0; i<g.size(); i++) {
	    duration[i] = in.nextInt();
	}
	PERT p = pert(g, duration);
	if(p == null) {
	    System.out.println("Invalid graph: not a DAG");
	} else {
	    System.out.println("Number of critical vertices: " + p.numCritical());
	    System.out.println("u\tDur\tEC\tLC\tSlack\tCritical");
	    for(Vertex u: g) {
		System.out.println(u + "\t" + p.get(u).duration + "\t" + p.ec(u) + "\t" + p.lc(u) + "\t" + p.slack(u) + "\t" + p.critical(u));
	    }
	}
    }
}
