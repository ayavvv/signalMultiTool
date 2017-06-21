package sigmultitool.popup.actions;
import java.io.*;
public class ReducedNF extends AncestorEquation{
	ClockEquivalenceClass left;
	ClockEquivalenceClass right1;
	ClockEquivalenceClass right2;
	Clock term1;
	Clock term2;
	String operator;
	public ReducedNF(String oper,ClockEquivalenceClass cec1,ClockEquivalenceClass cec2,ClockEquivalenceClass cec3,Clock t1,Clock t2){
		left=cec1;
		right1=cec2;
		right2=cec3;
		term1=t1;
		term2=t2;
		operator=oper;
	}
	public void printFile(PrintStream p){
		p.println("---------------");
		p.print("C_"+left.id+"=");
		if(right1!=null){
			p.print("C_"+right1.id);
		}
		if(term1!=null){
			p.print(term1.sig.name);
		}
		p.print(" "+operator+" ");
		if(right2!=null){
			p.print("C_"+right2.id);
		}
		if(term2!=null){
			p.print(term2.sig.name);
		}
		p.println("");
	}
	public void print(){
		System.out.println("---------------");
		System.out.print("C_"+left.id+"=");
		if(right1!=null){
			System.out.print("C_"+right1.id);
		}
		if(term1!=null){
			System.out.print(term1.sig.name);
		}
		System.out.print(" "+operator+" ");
		if(right2!=null){
			System.out.print("C_"+right2.id);
		}
		if(term2!=null){
			System.out.print(term2.sig.name);
		}
		System.out.println("");
		
	}
}

