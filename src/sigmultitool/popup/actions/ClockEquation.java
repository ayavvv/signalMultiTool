package sigmultitool.popup.actions;
import java.io.*;
import java.util.*;
public class ClockEquation {
	ClockExpr left;
	ClockExpr right;
	Equation eq;
	int id=-1;
	public ClockEquation(ClockExpr ce1,ClockExpr ce2,Equation e){
		left=ce1;
		right=ce2;
		eq=e;
	}
	public boolean checkEquivalent(){
		return left.id==right.id;
	}
//	public void print(){
//		System.out.println("\n --------lhs:-----------");
//		left.print();
//		System.out.println("\n--------rhs:-----------");
//		right.print();
//	}
	public void print(){
		System.out.println("----------------");
		left.print();
		System.out.print("=");
		right.print();
	}
	public void printFile(PrintStream p){
		//p.println("----------------");
		p.print("\t");
		left.printFile(p);
		p.print("=");
		right.printFile(p);
		p.print('\n');
	}
}
