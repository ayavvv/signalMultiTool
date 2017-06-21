package sigmultitool.popup.actions;
import java.io.*;
import java.util.*;

public class ClockNode {
	ArrayList<Assignment> sortedList;
	ArrayList<ClockNode> descendants;
	ClockNode ancestor;
	boolean duplicated;
	ClockEquivalenceClass clock;
	ReducedNF rnf;
	public ClockNode(ClockEquivalenceClass c,boolean dup,ReducedNF rnf){
		clock=c;
		duplicated=dup;
		sortedList=new ArrayList<Assignment>();
		descendants=new ArrayList<ClockNode>();
		this.rnf=rnf;
	}
	public void setAncestor(ClockNode cn){
		ancestor=cn;
	}
	public ClockNode getAncestor(){
		return ancestor;
	}
	public void addDescendant(ClockNode cn){
		descendants.add(cn);
	}
	public ArrayList<ClockNode> getDescendants(){
		return descendants;
	}
	public void sortEquations(){//sort the equations in the node
		int size=sortedList.size();
		if(size==0)
			return;
		
		Assignment[] ae=new Assignment[size];//(Assignment[])(sortedList.toArray());
		for(int i=0;i<size;i++){
			ae[i]=sortedList.get(i);
		}
		for(int k=1;k<size;k++){
    		Assignment tmpae=ae[k];
    		int j=0;
    		for(j=0;j<k;j++){
    			if(compare(ae[j],tmpae)==true)
    				break;
    		}
    		for(int m=k;m>j;m--){
    			ae[m]=ae[m-1];
    		}
    		ae[j]=tmpae;  
    	}
		for(int k=0;k<size;k++){
			sortedList.add(k, ae[k]);
		}
	}
	public boolean compare(Assignment a1,Assignment a2){//if a1 depends on a2,return true
		ArrayList<Signal> as=getRightSignals(a1);		
		if(as.contains(a2.left)==true)
			return true;
		
		return false;
	}
    public ArrayList<Signal> getRightSignals(Assignment as){//get the rhs of as
    	ArrayList<Signal> asa=new ArrayList<Signal>();
    	if(as.eq.expr.exprtype.equals("Input")){
    		return asa;
    	}
    	else{
    		return as.eq.right;
    	}
    }
    public void print(){
    	
    	if(rnf!=null){
    		System.out.println("---RNF---");
    		rnf.print();
    	}
    	System.out.println("---Assignment---");
    	for(Assignment as:sortedList){
    		as.print();
    	}
    	System.out.println("---father---");
    	if(ancestor!=null)
    		System.out.println("C_"+ancestor.clock.id);
    	System.out.println("---direct children---");
    	for(ClockNode cn: descendants){
    		System.out.print("C_"+cn.clock.id+";");
    	}
    	System.out.println("");
    }
}
