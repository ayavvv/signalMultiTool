package sigmultitool.popup.actions;
import java.io.*;
import java.util.*;
public class Assignment extends AncestorEquation{
	Equation eq;
	Boolean function;
	Signal left;
	Signal right;
	ClockEquivalenceClass cl;
	public Assignment(Equation e,Boolean func,Signal l,Signal r){
		eq=e;
		function=func;
		left=l;
		right=r;
	}
	public void setClass(ClockEquivalenceClass c){
		cl=c;
	}
	public void print(){
		System.out.println("--------------");
		if(function==true)
			eq.print();
		else if(eq.expr.exprtype.equals("Input")){
			System.out.println("\tread("+left.name+")");
		}
		else{
			System.out.print(left.name+"=");
			System.out.print(right.name);
		}
		System.out.println("");
	}
	public void printFile(PrintStream p){
		p.println("--------------");
		if(function==true)
			eq.printFile(p);
		else if(eq.expr.exprtype.equals("Input")){
			p.println("\tread("+left.name+")");
		}
		else{
			p.print("\t"+left.name+"=");
			p.print(right.name);
			p.println();
		}
		System.out.println("");
	}
}
