package sigmultitool.popup.actions;
import java.io.*;
import java.util.*;

public class ClockAnalysis {
	ArrayList<Equation> DES;
	ArrayList<ClockEquation> CES;
	ArrayList<Clock> CSS;
	ArrayList<ClockExpr> BasicCE;//set of basic clock expr
	ArrayList<ClockEquation> NFS;
	ArrayList<ClockEquation> UNFS;
	ArrayList<Clock> definedClock;
	ArrayList<Clock> undefinedClock;
	Identification idCompare;
	ArrayList<ClockEquivalenceClass> EquivalenceClasses;
	ClockEquivalenceClass root;
	ArrayList<ReducedNF> RNFS;
	ArrayList<Equation> DDES;
	ArrayList<Assignment> DCES; 
	ArrayList<AncestorEquation> Elist;
	ClockTree tree;
	SFDG sfdg;
	public ClockAnalysis(ArrayList<Equation> des,SFDG sf){
		DES=des;//set of dataflow equations
		CSS=new ArrayList<Clock>();//set of clocks
		CES=new ArrayList<ClockEquation>();//set of clock equations
		NFS=new ArrayList<ClockEquation>();//set of normal form
		UNFS=new ArrayList<ClockEquation>();//set of unregular form
		idCompare=new Identification();
		BasicCE=new ArrayList<ClockExpr>();//set of basic clock expression(one clock)
		EquivalenceClasses=new ArrayList<ClockEquivalenceClass>();//set of clock equivalence classes
		definedClock=new ArrayList<Clock>();
		undefinedClock=new ArrayList<Clock>();
		RNFS=new ArrayList<ReducedNF>();//set of reduced normal form
		DDES=new ArrayList<Equation>();//set of memo equations
		DCES=new ArrayList<Assignment>();//set of assignment
		Elist=new ArrayList<AncestorEquation>();//sorted list combined by DCES and RNFS
		sfdg=sf;
	}
	//translate dataflow equations into clock equations
	public boolean dataflowToClock(){
		for(Equation eq:DES){
			if(eq.expr.exprtype.equals("Input")) //if the equation is typed "Input",skip
				continue;
			Clock lclock=checkClockExist(eq.left);	//find if the clock of the lhs of eq has been created
			ClockExpr cel;
			ClockExpr cer;
			if(lclock==null){//if not defined, create a new clock and the corresponding basic clock expr
				lclock=new Clock(eq.left,false);
				CSS.add(lclock);
				cel=new ClockExpr(lclock);//created the basic clock expr
				BasicCE.add(cel);
			}
			else{
				cel=getBasicCE(lclock);
				
			}
			
			ArrayList<Clock> rclock=new ArrayList<Clock>();
			ArrayList<ClockExpr> BasicE=new ArrayList<ClockExpr>();
			//BasicE.add(cel);
//			System.out.println("------");
//			eq.print();
//			System.out.println("------");
			for(Signal s: eq.right){//traverse all rhs signals and create clock
				if(s.ioi.equals("constant")==false && s.ioi.equals("parameter")==false){
					Clock cl=checkClockExist(s);
					if(cl==null){
						cl=new Clock(s,false);
						CSS.add(cl);
						cer=new ClockExpr(cl);//create the basic clock expr for cl
						BasicCE.add(cer);
					    
					}
					else{
						cer=getBasicCE(cl);
					}
					if(rclock.contains(cl)==false)
						rclock.add(cl);
					if(BasicE.contains(cer)==false)
						BasicE.add(cer);
					//BasicE.add(cer);
					//rclock.add(s);
				}
			}
			//if eq is a arithematic expression
			if(eq.expr.exprtype.equals("Arith")||eq.expr.exprtype.equals("Boolean")){							
				//ClockExpr cel=new ClockExpr(,null,"single");
				ClockExpr cerr=BasicE.get(BasicE.size()-1);//new ClockExpr(rclock.get(rclock.size()-1),null,"single");
				ClockEquation ce1=new ClockEquation(cel,cerr,eq);
				CES.add(ce1);
				for(ClockExpr cl: BasicE){
					if(cl==BasicE.get(rclock.size()-1))
						break;
					//ClockExpr celtemp=new ClockExpr(cl,null,"single");
					//ClockExpr certemp=new ClockExpr(rclock.get(rclock.size()-1),null,"single");
					ClockEquation ce1temp=new ClockEquation(cl,cerr,eq);
					CES.add(ce1temp);
				}
				//if eq returns boolean,make corresponding clock equation
				if(eq.expr.exprtype.equals("Boolean")){
					boolean r=makeBoolClockEq(eq,rclock);
				}
			}
			//else if(eq.expr.exprtype.equals("Boolean")){
				
			//}
			//x=x1 when x2
			else if(eq.expr.exprtype.equals("When")){
				Clock x1=checkClockExist(((WhenExpr)(eq.expr)).term1);
				Clock x2=checkClockExist(((WhenExpr)(eq.expr)).term2);
				ClockExpr exx1=getBasicCE(x1);
				ClockExpr exx2=getBasicCE(x2);
				Clock vx2=checkValueClockExist(((WhenExpr)(eq.expr)).term2);
				ClockExpr exvx2;
				if(vx2==null){//create value clock
					vx2=new Clock(((WhenExpr)(eq.expr)).term2,true);
					CSS.add(vx2);
					exvx2=new ClockExpr(vx2);
					BasicCE.add(exvx2);
				}
				else{
					exvx2=getBasicCE(vx2);
				}
				if(rclock.contains(vx2)==false)
					rclock.add(vx2);
				if(BasicE.contains(exvx2)==false)
					BasicE.add(exvx2);
				Clock x2t=new Clock(null,false);
				CSS.add(x2t);
				ClockExpr exx2t=new ClockExpr(x2t);
				BasicCE.add(exx2t);
				BasicE.add(exx2t);
				rclock.add(x2t);
				//ClockExpr cel=new ClockExpr(x2t,null,"single");
				//^x2t=^x2 && x2
				ClockExpr ceer=new ClockExpr(exx2,exvx2,"intersection");
				ClockEquation ce=new ClockEquation(exx2t,ceer,eq);
				//ClockExpr cel2=new ClockExpr(,null,"single");
				ClockEquation ce2;
				if(exx1!=null){
					//x=^x1 && x2t
					ClockExpr ceer2=new ClockExpr(exx1,exx2t,"intersection");
					ce2=new ClockEquation(cel,ceer2,eq);
					CES.add(ce);
					CES.add(ce2);
				}
				else{
					//^x=^x2t
					ce2=new ClockEquation(cel,ceer,eq);
					CES.add(ce2);
				}

				
			}
			//x=x1 default x2
			else if(eq.expr.exprtype.equals("Default")){
				Clock x1=checkClockExist(((DefaultExpr)(eq.expr)).term1);
				Clock x2=checkClockExist(((DefaultExpr)(eq.expr)).term2);
				ClockExpr exx1=getBasicCE(x1);
				ClockExpr exx2=getBasicCE(x2);
				//Clock vx2=checkValueClockExist(((De)(eq.expr)).term2);
				if(exx2==null)
					exx2=cel;
				Clock xdf=new Clock(null,false);
				CSS.add(xdf);
				ClockExpr exxdf=new ClockExpr(xdf);
				BasicCE.add(exxdf);
				BasicE.add(exxdf);
				rclock.add(xdf);
				//ClockExpr cel=new ClockExpr(xdf,null,"single");
				//^xdefault=^x2 \ ^x1
				ClockExpr ceer=new ClockExpr(exx2,exx1,"setminus");
				ClockEquation ce=new ClockEquation(exxdf,ceer,eq);
				//ClockExpr cel2=new ClockExpr(lclock,null,"single");
				//^x=^x1 || ^x2
				ClockExpr ceer2=new ClockExpr(exx1,exx2,"union");
				ClockEquation ce2=new ClockEquation(cel,ceer2,eq);
				CES.add(ce);
				CES.add(ce2);
			}
			else if(eq.expr.exprtype.equals("Memo")){
				Clock x1=checkClockExist(((ExprMemory)(eq.expr)).term1);
				ClockExpr exx1=getBasicCE(x1);
				//ClockExpr cel=new ClockExpr(lclock,null,"single");
				//ClockExpr ceer=new ClockExpr(x1,null,"single");
				ClockEquation ce=new ClockEquation(cel,exx1,eq);
				CES.add(ce);
			}
			else{
				System.out.println("incorrect type of the equation");
				System.exit(1);
			}
		}
		//2013.12.17
		for(ClockEquation ce:CES){
			if(ce.right.condition!=null){
				
			}
		}
		//2013.12.17
		//test
		//for(ClockEquation ce: CES){
		//	ce.print();
		//}
		return true;
	}
	
	//generate normal form
	public boolean clockToNF(){
		ClockEquation cetmp;
		ArrayList<ClockEquation> replace;
		ArrayList<ClockEquation> tmpNFS;
		for(ClockEquation ce: CES){
			cetmp=replaceEq(ce,NFS);//replace ce with definition in NFS
			ce.print();
			if(cetmp!=null)
				cetmp.print();
			if(cetmp.left.operator.equals("single")==false && cetmp.right.operator.equals("single")==false){
				UNFS.add(cetmp);
			}
			else if(cetmp.left.operator.equals("single")==false && cetmp.right.operator.equals("single")==true){
				ClockExpr ctmp=cetmp.left;
				cetmp.left=cetmp.right;
				cetmp.right=ctmp;
			}
			if(UNFS.contains(cetmp)==false && (checkCycleDef(cetmp.left.basicClock,cetmp.right)!=null || getDefFromNFS(cetmp.left.basicClock)!=null))
				UNFS.add(cetmp);			
			if(UNFS.contains(cetmp)==false){
				replace=new ArrayList<ClockEquation>();
				tmpNFS=new ArrayList<ClockEquation>();
				replace.add(cetmp);
				
				for(ClockEquation cce:NFS){
					ClockEquation cr=replaceEq(cce,replace);
					if(checkCycleDef(cr.left.basicClock,cr.right)==null)
						tmpNFS.add(cr);
					else{
						UNFS.add(cr);
					}
				}
				NFS=tmpNFS;
				NFS.add(cetmp);
			}
			
			ArrayList<ClockEquation> TUNFS=new ArrayList<ClockEquation>();
			for(ClockEquation ceu:UNFS){
				ClockEquation cr1=replaceEq(ceu,NFS);
				if(idCompare.IDExpr(cr1.left)!=idCompare.IDExpr(cr1.right))
					TUNFS.add(cr1);
			}
			UNFS=TUNFS;
		}
		for(ClockEquation ce:NFS){
			ce.print();
		}
		for(ClockEquation ce:UNFS){
			ce.print();
		}
		if(UNFS.size()>0)
			return false;
		for(ClockEquation ce: NFS){
			ce.print();
		}
		
		return true;
	}
	
	//replace ce with definition in ace
	public ClockEquation replaceEq(ClockEquation ce,ArrayList<ClockEquation> ace){
		ClockExpr c1=ce.left;
		ClockExpr c2=ce.right;
		ClockExpr c1n=replace(c1,ace);
		ClockExpr c2n=replace(c2,ace);
		ClockEquation cen=new ClockEquation(c1n,c2n,ce.eq);
		return cen;
	}
	
	//replace ce with definition in ace
	public ClockExpr replace(ClockExpr ce,ArrayList<ClockEquation> ace){
		ClockEquation cenew;
		ClockExpr cln=null;
		ClockExpr crn=null;
		ClockExpr cc=null;
		if(ce==null)
			return null;
		if(ce.operator.equals("single")){
			for(ClockEquation cet: ace){
				if(cet.left.operator.equals("single")&&cet.left.basicClock==ce.basicClock){
					cln=cet.right;//replace ce with its definition
					return cln;  
				}
			}
			return ce;
		}
		else if(ce.condition!=null){
			return ce;//if ce is condition ,no replacement
		}
		else{//recursive call
			cln=replace(ce.operand1,ace);
			crn=replace(ce.operand2,ace);
			cc=new ClockExpr(cln,crn,ce.operator);
			
				
		}
		
		return cc;
	}
	
	
	//return class to which s belongs
	public Clock checkClockExist(Signal s){
		for(Clock cl: CSS){
			if(cl.sig!=null && cl.sig==s && cl.bvalue==false){
				return cl;
			}
		}
		return null;
	}
	//create the clock equation corresponding to the boolean dataflow equation
	public boolean makeBoolClockEq(Equation eq,ArrayList<Clock> rclock){
		Signal left=eq.left;
		ArrayList<Signal> right=eq.right;
		ArrayList<ClockExpr> BasicE=new ArrayList<ClockExpr>();
		Clock lvalueclock=checkValueClockExist(left);
		ClockExpr cel;
		ClockExpr cer;
		if(lvalueclock==null){
		    lvalueclock=new Clock(left,true);
		    CSS.add(lvalueclock);
		    cel=new ClockExpr(lvalueclock);
		    BasicCE.add(cel);
		    rclock.add(lvalueclock);
		}
		else{
			cel=getBasicCE(lvalueclock);
		}
		BasicE.add(cel);
		String booleanType=returnBooleanType(eq);
		//ClockExpr vcel=new ClockExpr(lvalueclock,null,"single");
		if(booleanType!=null){
			for(Signal s: right){
				if(s.ioi.equals("constant")==false && s.ioi.equals("parameter")==false){
					Clock cl=checkValueClockExist(s);
					if(cl==null){
						cl=new Clock(s,true);
						CSS.add(cl);
					    cer=new ClockExpr(cl);
					    BasicCE.add(cer);
					}
					else{
						cer=getBasicCE(cl);
					}
					rclock.add(cl);
					BasicE.add(cer);
					//rclock.add(s);
				}
				
			}
			//ClockExpr cer=null;
			if(booleanType.equals("not")){
				cer=new ClockExpr(BasicE.get(rclock.size()-1),null,booleanType);
			}
			else{
				cer=new ClockExpr(BasicE.get(rclock.size()-2),BasicE.get(rclock.size()-1),booleanType);
			}
			ClockEquation ce=new ClockEquation(cel,cer,eq);
			CES.add(ce);
		}
		else{
			cer=new ClockExpr(eq.expr,((BooleanExpr)eq.expr).type);
			//cer.setCondition(eq.expr);
			ClockEquation ce=new ClockEquation(cel,cer,eq);
			CES.add(ce);
		}
		
		return true;
	}
	//check if the corresponding value clock of signal s has been defined in CSS
	public Clock checkValueClockExist(Signal s){
		for(Clock cl: CSS){
			if(cl.sig==s && cl.bvalue==true){
				return cl;
			}
		}
		return null;
	}	
	//if eq is a boolean equation typed "and" "or", "not",return the type
	public String returnBooleanType(Equation eq){
		BooleanExpr be=(BooleanExpr)eq.expr;
		String s=be.type;
		if(s.equals("and")||s.equals("or")||s.equals("not"))
			return s;
		return null;
	}
	//check if clock c is used in ce(recursively)
	public Clock checkCycleDef(Clock c,ClockExpr ce){
		if(ce==null)
			return null;
		if(ce.operator.equals("single")&&ce.basicClock==c)
			return c;
		else{
			Clock c1=checkCycleDef(c,ce.operand1);
			Clock c2=checkCycleDef(c,ce.operand2);
			if(c1==c||c2==c)
				return c;
		}
		return null;
	}
	// get the definition of Clock c from NFS
	public ClockExpr getDefFromNFS(Clock c){
		if(c==null)
			return null;
		for(ClockEquation ce: NFS){
			if(ce.left.operator.equals("single")&&ce.left.basicClock==c)
				return ce.right;
		}
		return null;
	}
	
	//get the clock expression from BasicCE
	public ClockExpr getBasicCE(Clock c){
		for(ClockExpr ce: BasicCE){
			if(ce.operator.equals("single")==false)
				return null;
			else{
				if(c==ce.basicClock)
					return ce;
			}
		}
		return null;
	}
	
	//calculate ID for equations in NFS
	public void calculatedIDForNFS(){
		for(ClockEquation ce: NFS){
			idCompare.ID(ce);
			idCompare.bdd.print(ce.id);
			ce.print();
		}
//		for(ClockEquation ce:NFS){
//			ce.print();
//			System.out.println(ce.id);
//			System.out.println(ce.left.id);
//		}
//		for(ClockEquation ce: CES){
//			ce.print();
//		}
		
	}
	
	//created clock equivalence classes from NFS
	public boolean mergeClock(){
		Clock tclock;
		ClockEquivalenceClass tclass;
		for(ClockEquation ce:NFS){//traverse NFS
			if(ce.left.basicClock.bvalue==false){//no class for value clock
				tclass=containClass(ce.id);
				if(tclass!=null){
					tclass.ces.add(ce.left.basicClock);
					ce.left.basicClock.clockclass=tclass;
				}
				else{//if no class,create new one
					tclass=new ClockEquivalenceClass();
					tclass.id=ce.id;
					tclass.ces.add(ce.left.basicClock);
					ce.left.basicClock.clockclass=tclass;
					EquivalenceClasses.add(tclass);
				}
				if(definedClock.contains(ce.left.basicClock)==false)
					definedClock.add(ce.left.basicClock);
			}
		}
		int mark=0;
		for(Clock c: CSS){//find the clock undefined and put them into the class or create new class for if
			if(definedClock.contains(c))
				continue;
			for(ClockEquivalenceClass ec: EquivalenceClasses){
				if(c.bvalue==false&&ec.id==c.id&&ec.ces.contains(c)==false){
					ec.addSignal(c);
					c.clockclass=ec;
					mark=1;					
					undefinedClock.add(c);
					break;
				}
				
			}
			if(mark==0&&c.bvalue==false&&c.id>0){
				tclass=new ClockEquivalenceClass();
				EquivalenceClasses.add(tclass);
				tclass.addSignal(c);
				tclass.id=c.id;
				c.clockclass=tclass;
				undefinedClock.add(c);
			}
			mark=0;
		}
		for(ClockEquivalenceClass ce: EquivalenceClasses){
			ce.print();
		}
		if(undefinedClock.size()>1)//check endochrony 
			return false;

		return true;
	}
	
	//check if existing classes have the id of i
	public ClockEquivalenceClass containClass(int i){
		for(ClockEquivalenceClass cl: EquivalenceClasses){
			if(cl.id==i)
				return cl;
		}
		return null;
	}
	//set the root of the clock tree
	public void getRootClock(){
		if(undefinedClock.size()<1||undefinedClock.get(0).id<0)
			System.exit(1);
		Clock c=undefinedClock.get(0);
		root=containClass(c.id);
		root.print();
		root.root=true;
	}
	//generate RNFS
	public boolean reduction(){
		ClockEquivalenceClass tcl;
		ClockEquivalenceClass tcr1=null;
		ClockEquivalenceClass tcr2=null;
		Clock term1=null;
		Clock term2=null;
		for(ClockEquation ce: CES){
			//do not translate equation of value clock
			tcl=null;
			tcr1=null;
			tcr2=null;
			term1=null;
			term2=null;
			if(ce.left.basicClock.bvalue==true)
				continue;
			tcl=containClass(ce.left.id);
			if(tcl==null)
				return false;
			//ce.print();
			//if it belongs to the root class,it has no definition
			if(root.ces.contains(ce.left.basicClock))
				continue;
			//if rhs of ce is a clock, continue
			if(ce.right.operator.equals("single"))
				continue;
			if(ce.right.operand1!=null && ce.right.operand1.operator.equals("single")&&ce.right.operand1.basicClock.bvalue==false)
				tcr1=containClass(ce.right.operand1.basicClock.id);
			else if(ce.right.operand1!=null && ce.right.operand1.operator.equals("single") && ce.right.operand1.basicClock.bvalue==true)
				term1=ce.right.operand1.basicClock;
			if(ce.right.operand2!=null && ce.right.operand2.operator.equals("single")&&ce.right.operand2.basicClock.bvalue==false)
				tcr2=containClass(ce.right.operand2.basicClock.id);
			else if(ce.right.operand2!=null && ce.right.operand2.operator.equals("single") && ce.right.operand2.basicClock.bvalue==true)
				term2=ce.right.operand2.basicClock;
			ReducedNF rnf=new ReducedNF(ce.right.operator,tcl,tcr1,tcr2,term1,term2);
			if(findDefFromRNFS(tcl)==null)//if it is a new definition, add it
				RNFS.add(rnf);
		}
		for(ReducedNF rnf:RNFS)
			rnf.print();
		return true;
	}
    public ClockEquivalenceClass findDefFromRNFS(ClockEquivalenceClass c){
    	for(ReducedNF rnf: RNFS){
    		if(rnf.left==c)
    			return rnf.left;
    	}
    	return null;
    }
    
    //attach equations to the class
    public void attachEquation(){
    	Assignment asg;
    	Assignment asg2;
    	ClockEquivalenceClass cec;
    	
    	for(Equation eq: DES){//traverse the data flow equations
    		eq.print();
    		if(eq.expr.exprtype.equals("Memo"))//for $, do nothing
    			DDES.add(eq);//add memory equation into DDES
    		else if(eq.expr.exprtype.equals("Default")){//s1=s2 default s2
    			Signal s1=eq.left;
    			Signal s2=((DefaultExpr)(eq.expr)).term1;
    			Signal s3=((DefaultExpr)(eq.expr)).term2;
    			//s1=s2
    			asg=new Assignment(eq,false,s1,s2);
    			cec=findClass(s2);//get the class containing s2 and put asg into it
    			if(cec!=null&&cec.getAssigns().contains(asg)==false){
    				cec.addAssign(asg);
    			}
    			DCES.add(asg);
    			asg.setClass(cec);
    			cec=findClass(s2,s3);//find class s3\s2
    			//s1=s3
    			asg=new Assignment(eq,false,s1,s3);
    			if(cec!=null&&cec.getAssigns().contains(asg)==false){
    				cec.addAssign(asg);
    			}
    			asg.setClass(cec);
    			DCES.add(asg);
    		}
    		else if(eq.expr.exprtype.equals("When")){//s1=s2 when s3
    			Signal s1=eq.left;
    			Signal s2=((WhenExpr)(eq.expr)).term1;
    			Signal s3=((WhenExpr)(eq.expr)).term2;
    			asg=new Assignment(eq,false,s1,s2);
    			cec=findClass(s1);//find class of s1 and put asg into it
    			if(cec!=null&&cec.getAssigns().contains(asg)==false){
    				cec.addAssign(asg);
    			}
    			DCES.add(asg);
    			asg.setClass(cec);
    		}
    		else if(eq.expr.exprtype.equals("Input")){//read(s)
    			Signal s=eq.left;
    			cec=findClass(s);
    			asg=new Assignment(eq,false,s,null);
    			//find class of s and put asg into it
    			if(cec!=null&&cec.getAssigns().contains(asg)==false){
    				cec.addAssign(asg);
    			}
    			DCES.add(asg);
    			asg.setClass(cec);
    		}
    		else{//instant function
    			Signal s=eq.left;
    			cec=findClass(s);
    			asg=new Assignment(eq,true,s,null);
    			//find class of s and put asg into it
    			if(cec!=null&&cec.getAssigns().contains(asg)==false){
    				cec.addAssign(asg);
    			}
    			DCES.add(asg);
    			asg.setClass(cec);
    		}
    	}
//    	for(Assignment as: DCES){
//    		as.print();
//    	}
    	for(ClockEquivalenceClass ccec: EquivalenceClasses){
    		System.out.println("\nC_"+ccec.id);
    		if(ccec.assignments.size()>0){
    			for(Assignment as: ccec.assignments){
    				as.print();
    			}
    		}
    	}
    }
    
    //find the class defining s
    public ClockEquivalenceClass findClass(Signal s){
    	for(ClockEquivalenceClass c: EquivalenceClasses){
    		for(Clock cl: c.ces){
    			if(cl.sig!=null && cl.bvalue==false && cl.sig==s)
    				return c;
    		}
    	}
    	return null;
    }
    
    //find the class defined by s2\s1
    public ClockEquivalenceClass findClass(Signal s1,Signal s2){
    	ArrayList<ClockEquation> ace=new ArrayList<ClockEquation>();
    	Clock cc=null;
    	//find the clock defined by "s2\s1"
    	for(ClockEquation ce: CES){
    		if(ce.right.operator.equals("setminus")){
    			if(ce.right.operand1.basicClock.sig==s2 && ce.right.operand2.basicClock.sig==s1)
    				cc=ce.left.basicClock;
    		}
    	}
    	//get the class containing 
    	for(ClockEquivalenceClass c: EquivalenceClasses){
    		for(Clock cl: c.ces){
    			if(cl==cc)//cl.sig!=null && cl.bvalue==false && cl.sig==s1)
    				return c;
    		}
    	}
    	return null;
    }
    //called by sortEquation, sort RNFS
    public ReducedNF[] sortRNFS(){
    	int size=RNFS.size();
    	ReducedNF[] rnfs=new ReducedNF[size];
    	int numRNF=0;
    	ArrayList<DataDependency> graph=new ArrayList<DataDependency>();
    	for(ReducedNF r:RNFS){
    		DataDependency dd=new DataDependency(r);
    		for(DataDependency datadd: graph){
    			if(r.right1!=null && r.right1==datadd.rnf.left){
    				datadd.depend.add(dd);
    				dd.depended.add(datadd);
    			}
    			else if(r.right2!=null && r.right2==datadd.rnf.left){
    				datadd.depend.add(dd);
    				dd.depended.add(datadd);
    			}
    			else if(datadd.rnf.right1!=null && datadd.rnf.right1==r.left){
    				dd.depend.add(datadd);
    				datadd.depended.add(dd);
    			}
    			else if(datadd.rnf.right2!=null && datadd.rnf.right2==r.left){
    				dd.depend.add(datadd);
    				datadd.depended.add(dd);
    			}
    		}
    		graph.add(dd);
    	}
    	//for(DataDependency dd: graph){
    	//	dd.printRNF();
    	//}
    	while(true){
    		ArrayList<DataDependency> nodegree=new ArrayList<DataDependency>();
    		for(DataDependency dd:graph){
    			if(dd.depended.size()<1){
    				nodegree.add(dd);
    			}
    		}
    		for(DataDependency dd:nodegree){
    			graph.remove(dd);
    			for(DataDependency dc:dd.depend){
    				dc.depended.remove(dd);
    			}
    			rnfs[numRNF++]=dd.rnf;
    		}
    		if(graph.size()<1)
    			break;
    	}
    	return rnfs;
    	
    }
    public void sortEquation(){//sort equations according to the data dependency and clock definition
    	int size=RNFS.size()+DCES.size();
    	AncestorEquation[] ae=new AncestorEquation[size];
    	Assignment[]  assignments=new Assignment[DCES.size()];
    	int numass=0;
    	ReducedNF[] rfs=sortRNFS();
    	ArrayList<ReducedNF> added=new ArrayList<ReducedNF>();
    	int k=0,num=0;
    	int count=0;
    	for(ParallelTask pt: sfdg.paraTask){
    		System.out.println(count);
    		count++;
    		int count2=0;
    		ArrayList<ClockEquivalenceClass> acc=new ArrayList<ClockEquivalenceClass>();
    		for(Equation eq:pt.eqTask){
    			System.out.println(count2);
    			count2++;
    			int count3=0;
    			for(Assignment as:DCES){
    				
    				if(as.eq!=null && as.eq==eq){
    					assignments[numass++]=as;
    					if(acc.contains(as.cl)==false)
    						acc.add(as.cl);
    				}
    			}
    			for(int i=0;i<rfs.length;i++){
    				if(acc.contains(rfs[i].left)&&added.contains(rfs[i])==false){
    					added.add(rfs[i]);
    					ae[k++]=rfs[i];
    				}
    			}
    			for(int i=num;i<numass;i++){
    				ae[k++]=assignments[i];
    			}
    			num=numass;
    			
    		}
    	}
    	 System.out.println(numass);
    	 System.out.println(k);
    	for(int rfi=0;rfi<rfs.length;rfi++){
    		if(added.contains(rfs[rfi])==false)
    			ae[k++]=rfs[rfi];
    	}
        System.out.println(numass);
        System.out.println(k);
    	boolean dp=true;
    	int dpcount=0;
    	while(dp){
    		dpcount++;
    		for(int m=0;m<k-1;m++){
    			int index=-1;
    			for(int kk=m+1;kk<k;kk++){
    				if(ae[m].getClass()==ReducedNF.class){
    					ReducedNF trnf=(ReducedNF)(ae[m]);
    					if(ae[kk].getClass()==ReducedNF.class){
    						ReducedNF ttrnf=(ReducedNF)(ae[kk]);
    						if(trnf.right1!=null && ttrnf.left==trnf.right1){
    							index=kk;
    							break;
    						}
    						else if((trnf.right2!=null && ttrnf.left==trnf.right2)){
    							index=kk;
    							break;    							
    						}
    					}
    					else{
    						Assignment ttas=(Assignment)(ae[kk]);
    						if(trnf.term1!=null && trnf.term1.sig!=null && ttas.left==trnf.term1.sig){
    							index=kk;
    							break;
    						}
    						else if(trnf.term2!=null && trnf.term2.sig!=null && ttas.left==trnf.term2.sig){
    							index=kk;
    							break;    							
    						}
    					}
    				}
    				else{
    					Assignment tas=(Assignment)(ae[m]);
    					if(tas.eq.right==null || tas.eq.right.size()==0)
    						continue;
    					if(ae[kk].getClass()==ReducedNF.class){
    						ReducedNF ttrnf=(ReducedNF)(ae[kk]);
    						if(tas.cl==ttrnf.left){
    							index=kk;
    							break;
    						}
    					}
    					else{
    						Assignment ttas=(Assignment)(ae[kk]);
    						if(tas.eq.right.contains(ttas.left)){
    							index=kk;
    							break;
    						}
    						else if(tas.eq.right.contains(ttas.left)){
    							index=kk;
    							break;    							
    						}
    					}
    				}
    			}
    			if(index>=0){
    				AncestorEquation aet=ae[m];
    				for(int mw=m;mw<index;mw++){
    					ae[mw]=ae[mw+1];
    				}
    				ae[index]=aet;
    			}
    		}
    		System.out.println("test");
        	for(int kk=0;kk<k;kk++){
        		if(ae[kk].getClass()==ReducedNF.class)
        			((ReducedNF)(ae[kk])).print();
        		if(ae[kk].getClass()==Assignment.class)
        			((Assignment)(ae[kk])).print();
        	}
        	boolean markd=false;
    		for(int m=0;m<k-1;m++){
    			
    			if(m==33){
    				m++;
    				m--;
    			}
    			int countkk=0;
    			for(int kk=m+1;kk<k;kk++){
    				if(m==33 && kk==107){
    					kk++;
    					kk--;
    				}
    				if(ae[m].getClass()==ReducedNF.class){
    					ReducedNF trnf=(ReducedNF)(ae[m]);
    					if(ae[kk].getClass()==ReducedNF.class){
    						ReducedNF ttrnf=(ReducedNF)(ae[kk]);
    						if(trnf.right1!=null && ttrnf.left==trnf.right1){
    							markd=true;
    							break;
    						}
    						else if((trnf.right2!=null && ttrnf.left==trnf.right2)){
    							markd=true;
    							break;    							
    						}
    					}
    					else{
    						Assignment ttas=(Assignment)(ae[kk]);
    						if(trnf.term1!=null && trnf.term1.sig!=null && ttas.left==trnf.term1.sig){
    							markd=true;
    							break;
    						}
    						else if(trnf.term2!=null && trnf.term2.sig!=null && ttas.left==trnf.term2.sig){
    							markd=true;
    							break;    							
    						}
    					}
    				}
    				else{
    					Assignment tas=(Assignment)(ae[m]);
    					if(tas.eq.right==null || tas.eq.right.size()==0)
    						continue;
    					if(ae[kk].getClass()==ReducedNF.class){
    						ReducedNF ttrnf=(ReducedNF)(ae[kk]);
    						if(tas.cl==ttrnf.left){
    							markd=true;
    							break;
    						}
    					}
    					else{
    						Assignment ttas=(Assignment)(ae[kk]);
    						if(tas.eq.right.contains(ttas.left)){
    							markd=true;
    							break;
    						}
    						else if(tas.eq.right.contains(ttas.left)){
    							markd=true;
    							break;    							
    						}
    					}
    				}

    			}
    			if(markd==true)
    				break;
    		}
    		if(markd==false)
    		   break;
    		}
    		System.out.println("dpcount:"+dpcount);
    	
    	/*for(int i=0;i<RNFS.size();i++){
    		ae[i]=rfs[i];
    	}
    	for(int i=RNFS.size();i<size;i++){
    		ae[i]=assignments[i-RNFS.size()];
    	}


    	int i=0;
    	//combine assignments and reduced normal form
    	int front=0,rear=0;
    		k=0;int mark=0;
    		while(i<size){
    			if(ae[i].getClass()==ReducedNF.class){
    				ReducedNF rf=(ReducedNF)(ae[i]);
    				Signal s=null;
    				if(rf.term1!=null && rf.term1.sig!=null)
    					s=rf.term1.sig;
    				else if(rf.term2!=null && rf.term2.sig!=null)
    					s=rf.term2.sig;
    				for(k=i+1;k<size;k++){
    					if(ae[k].getClass()==Assignment.class){
    						Assignment as=(Assignment)(ae[k]);
    						if(as.left==s){
    							mark=1;
    							break;
    						}
    					}
    				}
    				if(mark==1){
    					AncestorEquation a=ae[i];
    					for(int j=i+1;j<=k;j++)
    						ae[j-1]=ae[j];
    					ae[k]=a;
    					
    				}
    			}
    			if(mark==1){
    				i=0;
    				mark=0;
    			}
    			else
    				i++;
    		}
    		i=0;
    		while(i<size){
    			if(ae[i].getClass()==ReducedNF.class){
    				ReducedNF rf=(ReducedNF)(ae[i]);
    				ClockEquivalenceClass s1=null;
    				ClockEquivalenceClass s2=null;
    				if(rf.right1!=null)
    					s1=rf.right1;
    				if(rf.right2!=null)
    					s2=rf.right2;
    				for(k=i+1;k<size;k++){
    					if(ae[k].getClass()==ReducedNF.class){
    						ReducedNF as=(ReducedNF)(ae[k]);
    						if(as.left==s1 || as.left==s2){
    							mark=1;
    							break;
    						}
    					}
    				}
    				if(mark==1){
    					AncestorEquation a=ae[i];
    					for(int j=i+1;j<=k;j++)
    						ae[j-1]=ae[j];
    					ae[k]=a;
    					
    				}
    			}
    			if(mark==1){
    				i=0;
    				mark=0;
    			}
    			else
    				i++;
    		}
    		*/
    		/*while(i<size){
    			if(ae[i].getClass()==ReducedNF.class){
    				ReducedNF rf=(ReducedNF)(ae[i]);
    				Signal s=null;
    				if(rf.term1!=null && rf.term1.sig!=null)
    					s=rf.term1.sig;
    				else if(rf.term2!=null && rf.term2.sig!=null)
    					s=rf.term2.sig;
    				for(k=i+1;k<size;k++){
    					if(ae[k].getClass()==Assignment.class){
    						Assignment as=(Assignment)(ae[k]);
    						if(as.left==s){
    							mark=1;
    							break;
    						}
    					}
    				}
    				if(mark==1){
    					AncestorEquation a=ae[i];
    					for(int j=i+1;j<=k;j++)
    						ae[j-1]=ae[j];
    					ae[k]=a;
    					mark=0;
    				}
    			}
    			i++;
    		}*/
       /* while(i<size || rfsindex<RNFS.size() || dcesindex<DCES.size()){
        	
        	if(rfs[rfsindex].term1==null && rfs[rfsindex].term2==null){
        		ae[i++]=rfs[rfsindex++];
        	}
        	else if(rfs[rfsindex].term1!=null && rfs[rfsindex].term1.sig!=null){
        		while(dcesindex<DCES.size() && assignments[dcesindex].left!=rfs[rfsindex].term1.sig){
        			ae[i++]=assignments[dcesindex++];
        		}
        		ae[i++]=assignments[dcesindex++];
        		ae[i++]=rfs[rfsindex++];
        	}
        	else if(rfs[rfsindex].term2!=null && rfs[rfsindex].term2.sig!=null){
        		for(int j=0;j<i;j++){
        			
        		}
        		while(dcesindex<DCES.size()&& assignments[dcesindex].left!=rfs[rfsindex].term2.sig){
        			ae[i++]=assignments[dcesindex++];
        		}
        		ae[i++]=assignments[dcesindex++];
        		ae[i++]=rfs[rfsindex++];
        	}
        	
        }
        if(dcesindex>=DCES.size()){
        	for(int k=rfsindex;k<RNFS.size();k++){
        		ae[i++]=rfs[k];
        	}
        }
        else if(rfsindex>=RNFS.size()){
        	for(int k=dcesindex;k<DCES.size();k++){
        		ae[i++]=assignments[k];
        	}
        }*/
    	/*for(int k=RNFS.size();k<size;k++){
    		ae[k]=DCES.get(k-RNFS.size());
    	}*/
    	System.out.println("after:");
    	for(k=0;k<size;k++){
    		if(ae[k].getClass()==ReducedNF.class)
    			((ReducedNF)(ae[k])).print();
    		if(ae[k].getClass()==Assignment.class)
    			((Assignment)(ae[k])).print();
    	}
    	
    	//put them into Elist 
    	for(k=0;k<size;k++){
    		Elist.add(k, ae[k]);
    	}
    		
    	
    	
    }
    
    //compare if ae1 depends on ae2
    public boolean compare(AncestorEquation ae1, AncestorEquation ae2){//if ae2 is prior to ae1,return true
    	//four cases
    	if(ae1.getClass()==Assignment.class){//if ae1 is assignment
    		Assignment an1=(Assignment)ae1;
    		ArrayList<Signal> as=getRightSignals(an1);
    		if(ae2.getClass()==Assignment.class){//if ae2 is assignment
    			Assignment an2=(Assignment)ae2;
    			if(as.contains(an2.left)==true)//if lhs of ae2 is in the rhs of ae1, return true
    				return true;
    		}
    		else if(ae2.getClass()==ReducedNF.class){//if ae2 is the definition of class
    			ClockEquivalenceClass cec=((ReducedNF)ae2).left;
    			if(cec.getAssigns().contains(an1)){//if the assignments in lhs of ae2 contains ae1,return true
    				return true;
    			}
    		}
    	}
    	else if(ae1.getClass()==ReducedNF.class){//if ae1 is definition of class
    		ReducedNF rnf=(ReducedNF)ae1;
    		if(ae2.getClass()==Assignment.class){//if ae2 is assignment   			
    			Assignment an=(Assignment)ae2;
    			if(rnf.term1==null && rnf.term2==null)
    				return false;
    			else{//if in ae1 there are signals
    				Signal s1=null;
    				Signal s2=null;
    				if(rnf.term1!=null){
    					s1=rnf.term1.sig;
    				}
    				if(rnf.term2!=null){
    					s2=rnf.term2.sig;
    				}
    				if(s1!=null){//if s1 is the lhs of ae2, return true
    					if(an.left==s1)
    						return true;
    				}
    				if(s2!=null){//if s2 is the lhs of ae2, return true
    					if(an.left==s2){
    						return true;
    					}
    				}
    			}
    		}
    		else if(ae2.getClass()==ReducedNF.class){//if ae2 is definition of class
    			ReducedNF rnf2=(ReducedNF)(ae2);
    			//if lhs of ae2 is one of the rhs in ae1,return true
    			if(rnf.right1!=null){
    				if(rnf.right1==rnf2.left)
    					return true;
    			}
    			if(rnf.right2!=null){
    				if(rnf.right2==rnf2.left)
    					return true;
    			}
    		}
    	}
    	return false;
    }
    
    //get the rhs of as;if as is the input, return empty set
    public ArrayList<Signal> getRightSignals(Assignment as){
    	ArrayList<Signal> asa=new ArrayList<Signal>();
    	if(as.eq.expr.exprtype.equals("Input")){
    		return asa;
    	}
    	else{
    		return as.eq.right;
    	}
    }
    public void makeHiers(){//build the clock tree
    	tree=new ClockTree(CSS,idCompare,root,Elist,EquivalenceClasses);
    	tree.makeTree();
    }
}
