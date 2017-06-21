package sigmultitool.popup.actions;

import java.io.*;
import java.util.*;


public class SFDG {
   ArrayList<DataDependency> Graph;
   ArrayList<DataDependency> duplicatedGraph;
   ArrayList<Equation> eqList;
   ArrayList<ParallelTask> paraTask;
   public SFDG(ArrayList<Equation> eql){
	   Graph=new ArrayList<DataDependency>();
	   duplicatedGraph=new ArrayList<DataDependency>();
	   paraTask=new ArrayList<ParallelTask>();
	   eqList=eql;
   }
   public boolean buildGraph(){
	   ArrayList<DataDependency> tDepend=null;
	   ArrayList<DataDependency> tDepended=null;
	   if(eqList.size()<1)
		   return false;
	   for(Equation eq: eqList){
		   if(eq.expr.exprtype.equals("Memo"))
			   continue;
		   DataDependency ndd=new DataDependency(eq);
		   tDepend=returnDepend(eq.left,Graph);
		   tDepended=returnDepended(eq,Graph);
		   Graph.add(ndd);
		   for(DataDependency dd: tDepend){
			   if(ndd.getDepend().contains(dd)==false)
				   ndd.addDepend(dd);
			   if(dd.getDepended().contains(ndd)==false)
				   dd.addDepended(ndd);
			   
		   }
		   for(DataDependency dd: tDepended){
			   if(dd.getDepend().contains(ndd)==false)
				   dd.addDepend(ndd);
			   if(ndd.getDepended().contains(dd)==false)
				   ndd.addDepended(dd);			   
		   }
	   }
	   buildDuplicatedGraph();
	   return true;
   }
   public boolean buildDuplicatedGraph(){
	   ArrayList<DataDependency> tDepend=null;
	   ArrayList<DataDependency> tDepended=null;
	   if(eqList.size()<1)
		   return false;
	   for(Equation eq: eqList){
		   if(eq.expr.exprtype.equals("Memo"))
			   continue;
		   DataDependency ndd=new DataDependency(eq);
		   tDepend=returnDepend(eq.left,duplicatedGraph);
		   tDepended=returnDepended(eq,duplicatedGraph);
		   duplicatedGraph.add(ndd);
		   for(DataDependency dd: tDepend){
			   if(ndd.getDepend().contains(dd)==false)
				   ndd.addDepend(dd);
			   if(dd.getDepended().contains(ndd)==false)
				   dd.addDepended(ndd);
			   
		   }
		   for(DataDependency dd: tDepended){
			   if(dd.getDepend().contains(ndd)==false)
				   ndd.addDepend(ndd);
			   if(ndd.getDepended().contains(dd)==false)
				   ndd.addDepended(dd);			   
		   }
	   }
	   return true;
   }
   //make parallel tasks
   public boolean checkAndMakeTask(){
	   boolean mark=false;
	   ArrayList<DataDependency> nTask=null;
	   ParallelTask prePt=null;
	   while(true){
		   nTask=returnNoInDegree();
		   if(nTask.size()==0)
			   break;
		   deleteDependencyRelation(nTask);
		   ArrayList<Equation> ae=new ArrayList<Equation>();
		   for(DataDependency dd: nTask){
			   ae.add(dd.getEquation());
		   }
		   ParallelTask pt=new ParallelTask(ae);
		   paraTask.add(pt);
		   if(prePt!=null){
			   prePt.next=pt;
		   }
		   prePt=pt;
	   }
	   if(duplicatedGraph.size()==0)
		   return true;
	   return false;
	   //return false;
   }
   //return nodes with no indegree in the graph
   public ArrayList<DataDependency> returnNoInDegree(){
	   ArrayList<DataDependency> add=new ArrayList<DataDependency>();
	   for(DataDependency dd: duplicatedGraph){
		   if(dd.depended.size()==0){
			   add.add(dd);
		   }
	   }
	   return add;
   }
   //topological sort
   public void deleteDependencyRelation(ArrayList<DataDependency> add){
	   ArrayList<DataDependency> temp=new ArrayList<DataDependency>();
	   for(DataDependency dd: duplicatedGraph){
		   if(add.contains(dd)==false){
			   temp.add(dd);
			   for(DataDependency ddd: add){
				   if(dd.depended.contains(ddd))
					   dd.depended.remove(ddd);
			   }
		   }
	   }
	   duplicatedGraph=temp;
   }
   //return the set of nodes in graph which depends on l
   public ArrayList<DataDependency> returnDepend(Signal l, ArrayList<DataDependency> Graph){
	   ArrayList<DataDependency> rDD=new ArrayList<DataDependency>();
	   for(DataDependency dd: Graph){
		   if(dd.getEquation().expr.exprtype.equals("Input"))
			   continue;
		   if(dd.getEquation().right.contains(l))
			   rDD.add(dd);
	   }
	   return rDD;
   }
   
   //return the set of nodes in graph on which eq depends
   public ArrayList<DataDependency> returnDepended(Equation eq,ArrayList<DataDependency> Graph){
	   ArrayList<DataDependency> rDD=new ArrayList<DataDependency>();
	   if(eq.expr.exprtype.equals("Input"))
		   return rDD;
	   for(DataDependency dd: Graph){
		   if(eq.right.contains(dd.getEquation().left)){
			   rDD.add(dd);
		   }
	   }
	   return rDD;
   }
   
   public void printGraph(){
	   for(DataDependency dd: Graph){
		   dd.print();
	   }
   }
   public void printGraphFile(PrintStream p,ArrayList<DataDependency> g){
	   for(DataDependency dd: g){
		   dd.printFile(p);
	   }
   }
   public void printTask(){
	   for(ParallelTask pt: paraTask){
		   pt.print();
	   }
   }
	
}
