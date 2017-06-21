package sigmultitool.popup.actions;
import java.io.*;
import java.util.*;
//data structure of clock tree
public class ClockTree {
	ArrayList<ClockNode> clockTree;//tree
	ArrayList<AncestorEquation> Elist;//sorted list of assignments and reduced normal form
	ArrayList<ClockEquivalenceClass> CECS;//set of clock equivalence classes
	ClockEquivalenceClass root;// root of the tree
	ClockNode treeroot;// root of the tree
	ArrayList<Signal> signalList=null;
	Identification idCompare;
	ArrayList<Clock> CSS;
	public ClockTree(ArrayList<Clock> CSS,Identification idCompare,ClockEquivalenceClass root,ArrayList<AncestorEquation> aae,ArrayList<ClockEquivalenceClass> acec){
		Elist=aae;
		CECS=acec;
		this.CSS=CSS;
		this.root=root;
		signalList=new ArrayList<Signal>();
		this.idCompare=idCompare;
		clockTree=new ArrayList<ClockNode>();
	}
	//main algorithm for building the tree
	public void makeTree(){
		ArrayList<ClockNode> treepath;
		//create the root node of the tree
		ClockNode ro=new ClockNode(root,false,null);
		ro.setAncestor(ro);
		treeroot=ro;
		clockTree.add(treeroot);
		//traverse the sorted list to insert the Clock or the equation
		int i=0;
		for(AncestorEquation ae: Elist){
			Assignment as;
			ReducedNF rnf;
			//if the equation is the definition of the clock
			i++;
			if(i==79){
				System.out.print(i);
			}
			if(ae.getClass()==ReducedNF.class){
				rnf=(ReducedNF)(ae);
				rnf.print();
				treepath=genPath(ae);//generate the limit branch
				//find the node to insert
				ClockNode node=findInsertPointClock(treepath,rnf.left);
				ClockNode nnode=new ClockNode(rnf.left,false,rnf);
				//build the relation and add the new node nnode into the tree
				nnode.setAncestor(node);
				node.addDescendant(nnode);
				clockTree.add(nnode);
			}
			//if the equation is the assignment 
			else{
				as=(Assignment)ae;
				as.print();
				treepath=genPath(as);//find the limit branch
				//as.print();
				ClockEquivalenceClass cl=findClass(as);
				//find the proper node on the right side of the limit branch
				ClockNode nnode=findInsertPointData2(treepath,cl);
				if(nnode==null){//if no proper node has been found
					ClockNode node=findInsertPointClock(treepath,cl);//create a duplicated node				
					nnode=new ClockNode(cl,true,null);//findRNF(cl));
					nnode.setAncestor(node);
					node.addDescendant(nnode);
					clockTree.add(nnode);
				}
				nnode.sortedList.add(as);//add the equation into the node
			}
		}
		for(ClockNode cn: clockTree){
			//cn.sortEquations();
			cn.print();
		}
	}
	//find the class to which the assignment 'a' is attached
	public ClockEquivalenceClass findClass(Assignment a){
		for(ClockEquivalenceClass c: CECS){
			if(c.assignments.contains(a))
				return c;
    	}
    	return null;
	}
	//find the class in which s is defined.
//    public ClockEquivalenceClass findClass(Signal s){
//    	for(ClockEquivalenceClass c: CECS){
//    		for(Assignment)
////    		for(Clock cl: c.ces){
////    			if(cl.sig!=null && cl.bvalue==false && cl.sig==s)
////    				return c;
////    		}
//    	}
//    	return null;
//    }
    //generate the limit path according to the data dependency defined in the equation
    public ArrayList<ClockNode> genPath(AncestorEquation ae){
    	ClockNode cn=null;
    	Assignment as;
    	ReducedNF rnf;
    	if(ae.getClass()==Assignment.class){// if ae is an assignment
    		as=(Assignment)(ae);
    		signalList=new ArrayList<Signal>();
    		//if the assignment is a instant function
    		if(as.function==true){
    			if(as.eq.right.size()<1)//if the num of operands in rhs is less than 1,return null
    				return null;
    			else if(as.eq.right.size()==1){//if the num of operand is 1
    				cn=findNode(as.eq.right.get(0));
    			}
    			else if(as.eq.right.size()==2){//if the num is two, get two nodes finding the deeper one
    				ClockNode cn1=findNode(as.eq.right.get(0));
    				ClockNode cn2=findNode(as.eq.right.get(1));
    				cn=findPriorNode(cn1,cn2,treeroot);
    			}
    			else if(as.eq.right.size()>2)//if the num of operands in rhs is greater than 2,return null
    				return null;
    			//signalList=as.eq.right;
    		}
    		else{//if as is the assignment ,get the node
    			//if as is input equation,cn is the root of the tree
    			if(as.eq.expr.exprtype.equals("Input"))
    				cn=treeroot;
    			else
    				cn=findNode(as.right);
    			//signalList.add(as.right);
    		}
    		//cn=dpsNode(treeroot);//find the deepest node
    	}
    	else{//if ae is the definition of the class
    		rnf=(ReducedNF)(ae);
    		ClockEquivalenceClass cl1=rnf.right1;
    		ClockEquivalenceClass cl2=rnf.right2;
    		Clock c1=rnf.term1;
    		Clock c2=rnf.term2;
    		ClockNode cn1=null;
    		ClockNode cn2=null;
    		if(cl1!=null){//if the first operand is the class
    			cn1=findNode(treeroot,cl1);
    		}else{//if the first operand is signal
    		  Signal s=c1.sig;
    		  signalList.add(s);
    		  cn1=findNode(s);
    		}
    		if(cl2!=null){//if the second operand is the class
    			cn2=findNode(treeroot,cl2);
    		}else{//if the second operand is signal
    		  Signal s=c2.sig;
    		  signalList.add(s);
    		  cn2=findNode(s);
    		}
    		//compare cn1 and cn2 finding the deeper one
    		cn=findPriorNode(cn1,cn2,treeroot);//find the deeper node between cn1 and cn2
    		
    	}
    	if(cn==null){
    		ArrayList<ClockNode> path=new ArrayList<ClockNode>();
    		path.add(treeroot);
    		return path;
    	}
    	ArrayList<ClockNode> path=new ArrayList<ClockNode>();
    	//generate the path based on the node cn
    	while(cn!=treeroot){//generate the path
    		path.add(cn);
    		cn=cn.ancestor;
    	}
    	path.add(cn);
    	return path;
    }
    public ClockNode findInsertPointClock(ArrayList<ClockNode> path,ClockEquivalenceClass clock){
    	ArrayList<ClockNode> queue=new ArrayList<ClockNode>();
    	queue.add(treeroot);

    	ClockNode cn=null;
    	while(true){
    		cn=queue.get(0);
    		queue.remove(0);
    		for(ClockNode ccn:cn.descendants){
    			if(rightLimit(path,ccn)==true && checkImplication(clock,ccn.clock)==true){
    				queue.add(ccn);
    			}
    		}
    		if(queue.size()==0)
    			break;
    	}
    	return cn;
    }
    public ClockNode findInsertPointData2(ArrayList<ClockNode> path,ClockEquivalenceClass clock){
    	if(path.size()==1 && clock==path.get(0).clock)
    		return path.get(0); 
    	ArrayList<ClockNode> clockList=new ArrayList<ClockNode>();
    	ClockNode cn=null;
    	ArrayList<ClockNode> queue=new ArrayList<ClockNode>();
    	queue.add(treeroot);
    	while(true){
    		cn=queue.get(0);
    		queue.remove(0);
    		if(cn.clock!=null && cn.clock==clock)
    			clockList.add(cn);
    		for(ClockNode ccn:cn.descendants){
    			queue.add(ccn);
    		}
    		if(queue.size()==0)
    			break;
    	}
    	for(ClockNode n:clockList){
    		ClockNode cnn=findPriorNode(path.get(0),n,treeroot);
    		if(cnn==n){
    			return n;
    		}
    	}
    	return null;
    }
    public ClockNode findInsertPointData(ArrayList<ClockNode> path,ClockEquivalenceClass clock){
    	if(path.size()==1 && clock==path.get(0).clock)
    		return path.get(0);
    	ArrayList<ClockNode> queue=new ArrayList<ClockNode>();
    	queue.add(treeroot);
    	
	
    	ClockNode cn=null;
    	while(true){
    		cn=queue.get(0);
    		queue.remove(0);
    		
    		if(cn.clock==clock)
    			return cn;
    		for(ClockNode ccn:cn.descendants){
    			if(rightLimit(path,ccn)==true){// && checkImplication(clock,ccn.clock)==true){
    				queue.add(ccn);
    			}
    		}
    		if(queue.size()==0)
    			break;
    	}
    	return null;
    }
    public boolean rightLimit(ArrayList<ClockNode> path, ClockNode cn){
    	if(path.contains(cn))
    		return true;
    	for(ClockNode p: path){
        	if(findPriorNode(p,cn,treeroot)==p)
        		return false;
    	}

    	return true;
    	
    }
//    public ClockNode findInsertPointData(ArrayList<ClockNode> path,ClockEquivalenceClass clock){
//    	return null;
//    }
    
    //find the definition of cec from Elist
    public ReducedNF findRNF(ClockEquivalenceClass cec){
    	for(AncestorEquation ae: Elist){
    		if(ae.getClass()==ReducedNF.class ){
    			ReducedNF rnf=(ReducedNF)(ae);
    			if(rnf.left==cec)
    				return rnf;
    		}
    			
    	}
    	return null;
    }
    //using depth-first-search find the first definition of 
    public ClockNode findNode(ClockNode cn, ClockEquivalenceClass cc){
    	if(cn.clock==cc && cn.duplicated==false)
    		return cn;
    	for(ClockNode c:cn.descendants){
    		ClockNode n=findNode(c,cc);
    		if(n!=null)
    			return n;
    	}
    	return null;   	
    }
    //find node defining s
    public ClockNode findNode(Signal s){
    	int mark=0;
    	ClockNode cn1=null;
    	ClockNode cn2=null;
    	
    	for(ClockNode cn: clockTree){
    		for(Assignment as: cn.sortedList){
    			if(as.left==s){
    				if(mark==0){
    				  mark=1;
    				  cn1=cn;
    				}else{//find another definition of s
    				  cn2=cn;
    				}
    			}
    		
    		}
    	}

    	if(cn1==null && cn2==null){
    		for(Clock c: CSS){//for memory signal
    			if(c.sig!=null && c.sig==s){
    				for(ClockEquivalenceClass cec:CECS){
    					if(cec.ces.contains(c)){
    						for(ClockNode cn:clockTree){
    							if(cn.clock==cec)
    								return cn;
    						}
    					}
    				}
    			}
    		}
    		return null;
    	}	
    	else if(cn2==null)
    		return cn1;
    	else{//if duplicated definitions are found(at most two definitions),find the deeper node
    		cn1=findPriorNode(cn1,cn2,treeroot);
    		return cn1;
    	}
    }
    
    //find if s is defined in n
    public boolean inNode(Signal s,ClockNode n){
    	for(Assignment as:n.sortedList){
    		if(as.left==s)
    			return true;
    	}
    	return false;
    }
    
    //find the deeper node from signalList
//    public ClockNode dpsNode(ClockNode cn){
//    	if(signalList.size()==1 && inNode(signalList.get(0),cn)){//if only one signal remains in signalList
//    		//and it is defined in cn, then return it
//    		signalList.remove(0);
//    		return cn;
//    	}
//    	int i=-1;
//    	for(Signal s:signalList){
//    		if(inNode(s,cn)){//if cn contains s, then remove s
//    			i=signalList.indexOf(s);
//    			break;
//    		}
//    	}
//    	if(i>=0){
//    		signalList.remove(i);
//    	}
//    	for(ClockNode n: cn.descendants){//depth-first-search
//    		ClockNode nn=dpsNode(n);
//    		if(nn!=null)//get the proper node and return it
//    			return nn;
//    	}
//    	return null;
//    }
    
    //find the deeper node in the tree from pair n1 and n2
    public ClockNode findPriorNode(ClockNode n1,ClockNode n2,ClockNode cn){
    	if(n1==null)
    		return n2;
    	else if(n2==null)
    		return n1;
    	if(cn==n1)//if n1 is found first, return n2
    		return n2;
    	else if(cn==n2)//if n2 is found first, return n1
    		return n1;
    	for(ClockNode n: cn.descendants){//depth first search
    		ClockNode tcn=findPriorNode(n1,n2,n);
    		if(tcn!=null)//if tcn is not null return it
    			return tcn;
    	}
    	return null;//if no node is found ,return null
    }
    public boolean checkImplication(ClockEquivalenceClass cn1,ClockEquivalenceClass cn2){//check if cn1->cn2
    	int icn1=cn1.id;
    	int icn2=cn2.id;
    	return idCompare.checkImplication(icn1, icn2);
    }
}
