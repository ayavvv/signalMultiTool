package sigmultitool.popup.actions;
import java.io.*;
import java.util.*;

public class DataDependency {
	  Equation eq;
	  ReducedNF rnf;
	  ArrayList<DataDependency> depend;//equations which depends on eq
	  ArrayList<DataDependency> depended;//equations on which eq depends
	  public DataDependency(Equation e){
		  eq=e;
		  depend=new ArrayList<DataDependency>();
		  depended=new ArrayList<DataDependency>();
	  }
	  public DataDependency(ReducedNF r){
		  rnf=r;
		  depend=new ArrayList<DataDependency>();
		  depended=new ArrayList<DataDependency>();
	  }
	  public void addDepend(DataDependency dd){
		  depend.add(dd);
	  }
	  public void addDepended(DataDependency dd){
		  depended.add(dd);
	  }
	  public Equation getEquation(){
		  return eq;
	  }
	  public ArrayList<DataDependency> getDepend(){
		  return depend;
	  }
	  public ArrayList<DataDependency> getDepended(){
		  return depended;
	  }
	  public void printRNF(){
		  System.out.println("-----NodeOfSFDG-------");
		  rnf.print();
		  System.out.println("sons:");
		  for(DataDependency dd: depend){
			  dd.rnf.print();
		  }
		  System.out.println("fathers:");
		  for(DataDependency dd: depended){
			 dd.rnf.print();
		  }
		  System.out.println("-----NodeOfSFDG-------");
	  }
	  public void print(){
		  System.out.println("-----NodeOfSFDG-------");
		  System.out.println(eq.left.name+":="+eq.expr.exprs);
		  System.out.println("sons:");
		  for(DataDependency dd: depend){
			  System.out.println(dd.eq.left.name+":="+dd.eq.expr.exprs);
		  }
		  System.out.println("fathers:");
		  for(DataDependency dd: depended){
			  System.out.println(dd.eq.left.name+":="+dd.eq.expr.exprs);
		  }
		  System.out.println("-----NodeOfSFDG-------");
	  }
	  public void printFile(PrintStream p){
		  p.println("-----NodeOfSFDG-------");
		  p.println(eq.left.name+":="+eq.expr.exprs);
		  p.println("sons:");
		  for(DataDependency dd: depend){
			  p.println(dd.eq.left.name+":="+dd.eq.expr.exprs);
		  }
		  System.out.println("fathers:");
		  for(DataDependency dd: depended){
			  p.println(dd.eq.left.name+":="+dd.eq.expr.exprs);
		  }
		  p.println("-----NodeOfSFDG-------");
	  }
	  
}
