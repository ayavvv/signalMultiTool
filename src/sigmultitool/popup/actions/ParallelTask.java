package sigmultitool.popup.actions;
import java.io.*;
import java.util.*;
public class ParallelTask {
	ArrayList<Equation> eqTask;
	ParallelTask next;
	ArrayList<ReducedNF> clockRelation;
	ArrayList<Assignment> assignments;
	ArrayList<ReducedNF> paraClockR;
	boolean clockPara=false;//if there is data dependency relation, set it to true
	public ParallelTask(){
		eqTask=new ArrayList<Equation>();
		next=null;
		clockRelation=new ArrayList<ReducedNF>();
		assignments=new ArrayList<Assignment>();
		paraClockR=new ArrayList<ReducedNF>();
	}
	public void addAssign(Assignment as){
		assignments.add(as);
	}
	public void addClock(ReducedNF rnf){
		clockRelation.add(rnf);
		
	}
	public ArrayList<ReducedNF> getClocks(){
		return clockRelation;
	}
	public ParallelTask(ArrayList<Equation> ae){
		eqTask=ae;
		clockRelation=new ArrayList<ReducedNF>();
		assignments=new ArrayList<Assignment>();
		paraClockR=new ArrayList<ReducedNF>();
	}
	public void addEquation(Equation eq){
		eqTask.add(eq);
	}
	public ArrayList<Equation> returnEquationList(){
		return eqTask;
	}
	public void setNext(ParallelTask pt){
		next=pt;
	}
	public ParallelTask returnNext(){
		return next;
	}
	public void print(){
		System.out.println("---RNF---");
		if(clockRelation!=null)
		for(ReducedNF rnf: clockRelation){
			rnf.print();
		}
		System.out.println("---RNF---");
		System.out.println("--------Task-------------");

		for(Equation eq: eqTask)
			System.out.println(eq.left.name+":="+eq.expr.exprs);
		System.out.println("--------Task-------------");
		
		System.out.println("---Assignments---");
		for(Assignment as: assignments){
			as.print();
		}
		System.out.println("---Assignments---");
		System.out.println("---ParallelRNF---");
		for(ReducedNF rnf: paraClockR){
			rnf.print();
		}
		System.out.println("---ParallelRNF---");
	}
	public void printFile(PrintStream p){
		p.println("---RNF---");
		if(clockRelation!=null)
			for(ReducedNF rnf: clockRelation){
				rnf.printFile(p);
			}
		p.println("---RNF---");
		//p.println("--------Task Node-------------");
		//for(Equation eq: eqTask)
		//	p.println(eq.left.name+":="+eq.expr.exprs);
		//p.println("--------Task-------------");
		
		p.println("---Assignments---");
		for(Assignment as: assignments){
			as.printFile(p);
		}
		p.println("---Assignments---");
		p.println("---ParallelRNF---");
		for(ReducedNF rnf: paraClockR){
			rnf.printFile(p);
		}
		p.println("---ParallelRNF---");
	}
}
