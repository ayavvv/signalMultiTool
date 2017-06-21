package sigmultitool.popup.actions;
import java.io.*;
import java.util.*;
public class ClockExpr {
	ClockExpr operand1;
	ClockExpr operand2;
	Clock basicClock=null;
	String operator;// "condition","single","intersection","union","setminus",">",">=","<","<=","=","!=","and","or","not"
	int id=-1;
	Expr condition;//when operator is condition, it stores the boolean expression
	public ClockExpr(ClockExpr c1,ClockExpr c2,String oper){
		operand1=c1;
		operand2=c2;
		operator=oper;
	}
	public ClockExpr(Clock c1){
		operator="single";
		basicClock=c1;
	}
	public ClockExpr(Expr cond,String oper){
		operator=oper;
		condition=cond;
	}
	public void setId(int i){
		i=id;
	}
	public int getId(){
		return id;
	}
	public void setCondition(Expr cond){
		condition=cond; 
	}
	public void print(){
		if(operator.equals("single")){
			if(basicClock.sig!=null){
				   if(basicClock.bvalue==false){
					   System.out.print("^");
				   }
				   System.out.print(basicClock.sig.name);
				}
				else{
					System.out.print("^xx");
				}
			
		}
		else if(condition!=null){
			System.out.print("("+condition.exprs+")");
		}
		else{
			System.out.print("(");
			operand1.print();
			System.out.print(" "+operator+" ");
			operand2.print();
			System.out.print(")");		
		}
	}
	public void printFile(PrintStream p){
		if(operator.equals("single")){
			if(basicClock.sig!=null){
				   if(basicClock.bvalue==false){
					   p.print("^");
				   }
				   p.print(basicClock.sig.name);
				}
				else{
					p.print("^xx");
				}
			
		}
		else if(condition!=null){
			p.print("("+condition.exprs+")");
		}
		else{
			p.print("(");
			operand1.printFile(p);
			p.print(" "+operator+" ");
			operand2.printFile(p);
			p.print(")");		
		}
	}
//	public void print(){
//		if(operand1!=null){
//			System.out.println("oper1:");
//			if(operand1.sig!=null){
//			   if(operand1.bvalue==false){
//				   System.out.print("^");
//			   }
//			   System.out.println(operand1.sig.name);
//			}
//			else{
//				System.out.println("^xx");
//			}
//		}
//		if(operand2!=null){
//			System.out.println("oper2:");
//			if(operand2.sig!=null){
//			   if(operand2.bvalue==false){
//				   System.out.print("^");
//			   }
//			   System.out.println(operand2.sig.name);
//			}
//			else{
//				System.out.println("^xx");
//			}
//		}
//		System.out.println("operator:");
//		System.out.println(operator);
//		
//	}
}
