package sigmultitool.popup.actions;
import java.io.*;
import java.util.*;
public class ClockEquivalenceClass {
	//ArrayList<Signal> ces;
	ArrayList<Clock> ces;
	int[] noset;//下标对应程序中的所有非constant及parameter类型的Signal对象，如果为1，说明此类包含此Signal
	String clock;
	int id;//idendification of the class
	boolean root=false;//if root==true, then this class is the root clock
	ArrayList<Assignment> assignments;
	public ClockEquivalenceClass(){
		ces=new ArrayList<Clock>();
		//acs=new ArrayList<Clock>();
		assignments=new ArrayList<Assignment>();
	}
	public ClockEquivalenceClass(int num){
		clock="C_"+String.valueOf(num);
	}
	public void setClock(int num){
		clock="C_"+String.valueOf(num);
	}
	public String getClock(){
		return clock;
	}
	public void setId(int idnum){
		id=idnum;
		
	}
	public int getId(){
		return id;
	}
	public void addSignal(Clock s){
		for(Clock ss: ces){
			if(s==ss)//s.getName().equals(ss.getName()))
				return ;
		}
		ces.add(s);
	}
	public ArrayList<Assignment> getAssigns(){
		return assignments;
	}
	public void addAssign(Assignment as){
		assignments.add(as);
	}
	public void print(){
		System.out.println("C_"+id+":");
		for(Clock s: ces){
			//System.out.print(s.getName()+';');//+":"+String.valueOf(s.no)+";");
			if(s.sig!=null){
				System.out.print(s.sig.getName()+';');
				
			}
			else{
				System.out.print("xx;");
			}
		}
		System.out.println("");
	}
	public void printFile(PrintStream p){
		p.println("C_"+id+":");
		for(Clock s: ces){
		//System.out.print(s.getName()+';');//+":"+String.valueOf(s.no)+";");
			if(s.sig!=null){
				p.print(s.sig.getName()+';');
			
			}
			else{
				p.print("xx;");
			}
		}
		p.println("");
	}	
//	public void newnoset(int mark){
//		noset=new int[mark];
//		for(int i=0;i<mark;i++){
//			noset[i]=0;
//		}
//		for(Clock s :ces){
//			noset[s.getNo()]=1;
//		}
//	}
}
