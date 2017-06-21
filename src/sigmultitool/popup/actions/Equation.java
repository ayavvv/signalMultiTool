package sigmultitool.popup.actions;
import java.io.*;
import java.util.*;
public class Equation {
	Signal left;
	Expr expr;
	ArrayList<Signal> right;
	public Equation(Signal l,ArrayList<Signal> as, Expr e){
		left=l;
		right=as;
		expr=e;
	}
	
	public void print(){
	    System.out.println("leftsignal:"+left.getName() );
	    if(right!=null)
	    for(Signal s: right){
	    	System.out.println("rightsignal:"+s.getName());
	    }
	    //System.out.println("ExprType:"+expr.exprtype);
	    expr.print();
	}
	public void printFile(PrintStream p){
	    p.print("\t"+left.getName()+":=");
	   // if(right!=null)
	   // for(Signal s: right){
	   // 	p.println("rightsignal:"+s.getName());
	  //  }
	    p.println(expr.exprs);
	    //System.out.println("ExprType:"+expr.exprtype);
	   // expr.print();
	}
	public boolean checkType(){
		String l=left.getTypeofSignal();
		boolean lb=l.equals("event")||l.equals("boolean");
		boolean rb;
		rb=expr.returntype.equals("event")||expr.returntype.equals("boolean");
		if(lb!=rb)
			return false;
		return true;
	}
}
