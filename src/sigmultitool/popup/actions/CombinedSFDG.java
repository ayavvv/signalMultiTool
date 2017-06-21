package sigmultitool.popup.actions;
import java.io.*;
import java.util.*;
public class CombinedSFDG {
	SFDG graph;
	ClockAnalysis clock;
	ArrayList<ParallelTask> paratask;
	//ArrayList<ParallelTask> newParaTask;
	public CombinedSFDG(SFDG sf,ClockAnalysis cl){
		graph=sf;
		clock=cl;
		paratask=graph.paraTask;
		//newParaTask=new ArrayList<ParallelTask>();
	}
	public void addClock(){
		//ArrayList<ParallelTask> pt=graph.paraTask;
		ArrayList<ClockEquivalenceClass> CEC=clock.EquivalenceClasses;
		ArrayList<AncestorEquation> Elist=clock.Elist;
		
		for(int i=0;i<Elist.size();i++){
			AncestorEquation ae=Elist.get(i);
			ReducedNF rnf=null;
			Assignment as=null;
			/*if(ae.getClass()==ReducedNF.class && i!=Elist.size()-1){
				rnf=(ReducedNF)(ae);
				if(Elist.get(i+1).getClass()==Assignment.class){
					as=(Assignment)Elist.get(i+1);
					paraClock(as,rnf);//添加时钟于SFDG中
				}
			}
			else*/ if(ae.getClass()==Assignment.class){
				as=(Assignment)ae;
				addAssignment(as);
			}
		}
		ArrayList<ReducedNF> added=new ArrayList<ReducedNF>();
		for(ParallelTask pt: paratask){
			if(pt!=paratask.get(0)){
				int i=0;
				Assignment ass=null;
				ArrayList<ReducedNF> tmp1=new ArrayList<ReducedNF>();
				ArrayList<ReducedNF> tmp2=new ArrayList<ReducedNF>();
				for(AncestorEquation ae:Elist){
					if(ae.getClass()==Assignment.class){
						Assignment as=(Assignment)(ae);
						if(pt.assignments.contains(as)){
							ass=as;
							//break;
						}
					}
				}
				if(ass!=null){
					for(AncestorEquation ae:Elist){
						if(ae.getClass()==Assignment.class && ass==(Assignment)(ae))
							break;
						if(ae.getClass()==ReducedNF.class)
							tmp1.add((ReducedNF)(ae));
						
					}
					for(ReducedNF rf:tmp1){
						if(added.contains(rf)==false)
							tmp2.add(rf);
					}
					for(ReducedNF rf:tmp2){
						added.add(rf);
					}
					pt.clockRelation=tmp2;
				}
				
			}
		}
		checkClockPara();
		//与1014添加，合并RNF与assignment
		for(ParallelTask pt:paratask){
			ArrayList<ReducedNF> arr;
			if(pt!=paratask.get(paratask.size()-1)){//如果不是最后一个元素
				if(pt.next.clockPara==false){//如果时钟间没有依赖关系
					arr=pt.next.clockRelation;
					if(checkDep(pt.assignments,arr)==false){//如果没有数据依赖关系，加入到前一个task中
						for(ReducedNF rnf:pt.next.clockRelation){
							pt.paraClockR.add(rnf);
						}
						pt.next.clockRelation=new ArrayList<ReducedNF>();//清空
						
					}
				}
				
			}
		}
		/*ParallelTask p1=paratask.get(0);
		ArrayList<Assignment> ast=new ArrayList<Assignment>();
		for(Assignment as: p1.assignments){
			if(as.cl!=clock.root){
				ast.add(as);
			}
		}
		if(ast.size()>0){
			ParallelTask nT=new ParallelTask();
			for(Assignment as: ast){
				p1.assignments.remove(as);
				nT.addAssign(as);
				if(as.eq!=null)
					nT.addEquation(as.eq);
			}
			paratask.add(1,nT);
		}*/
		
		
		
	}
	public boolean checkDep(ArrayList<Assignment> aa,ArrayList<ReducedNF> acr){//检查acr是否依赖aa
		for(Assignment a: aa){
			for(ReducedNF rnf:acr){
				if(rnf.term1!=null&&a.left==rnf.term1.sig)
					return true;
				if(rnf.term2!=null&&a.left==rnf.term2.sig)
					return true;
			}
		}
		return false;
	}
	public void addAssignment(Assignment as){
		Equation eq=as.eq;
		for(ParallelTask pt: paratask){
			if(pt.eqTask.contains(eq)){
				pt.addAssign(as);
				return ;
			}
		}		
	}
	public void paraClock(Assignment as,ReducedNF rnf){
		Equation eq=as.eq;
		for(ParallelTask pt: paratask){
			if(pt.eqTask.contains(eq)){
				pt.addClock(rnf);
				return ;
			}
		}
		
	}
	public void checkClockPara(){
		for(ParallelTask pt: paratask){
			if(pt.clockRelation.size()>0){
				ReducedNF[] lrnf=new ReducedNF[pt.clockRelation.size()];
				for(int i=0;i<pt.clockRelation.size();i++){
					lrnf[i]=pt.clockRelation.get(i);
				}
				int mark=0;
				int size=pt.clockRelation.size();
				for(int i=0;i<pt.clockRelation.size()-1;i++){
					for(int j=i+1;j<pt.clockRelation.size();j++){
						if(checkDependency(lrnf[i],lrnf[j])==true){
							pt.clockPara=true;//if there is data dependency relation, set it to true
							mark=1;
							break;
						}
					}
					if(mark==1)
						break;
				}
				if(mark==0)//如果没有数据依赖关系，直接放回
					return;
		    	for(int k=1;k<pt.clockRelation.size();k++){//否则对rnf列表进行重新排序
		    		//AncestorEquation tmpae=ae[k];
		    		ReducedNF tmp=lrnf[k];
		    		int j=0;
		    		for(j=0;j<k;j++){
		    			if(checkDependency2(lrnf[k],lrnf[j])==true)
		    				break;
		    		}
		    		for(int m=k;m>j;m--){
		    			lrnf[m]=lrnf[m-1];
		    		}
		    		lrnf[j]=tmp;  
		    	}
		    	pt.clockRelation=new ArrayList<ReducedNF>();
		    	
				for(int i=0;i<size;i++){//重新排序
					pt.clockRelation.add(lrnf[i]);
				}
				
			}
			
		}
	}
	public boolean checkDependency2(ReducedNF r1,ReducedNF r2){//check if r2 depends on r1
		ClockEquivalenceClass r1l=r1.left;
		ClockEquivalenceClass r2l=r2.left;
		if(r2.right1!=null && r1l==r2.right1)
			return true;
		if(r2.right2!=null && r1l==r2.right2)
			return true;
		return false;
		
	}
	public boolean checkDependency(ReducedNF r1,ReducedNF r2){//check if r2 depends on r1
		ClockEquivalenceClass r1l=r1.left;
		ClockEquivalenceClass r2l=r2.left;
		if(r2.right1!=null && r1l==r2.right1)
			return true;
		if(r2.right2!=null && r1l==r2.right2)
			return true;
		if(r1.right1!=null && r2l==r1.right1)
			return true;
		if(r1.right2!=null && r2l==r1.right2)
			return true;
		return false;
		
	}
    public ReducedNF findRNF(ClockEquivalenceClass cec){
    	for(AncestorEquation ae: clock.Elist){
    		if(ae.getClass()==ReducedNF.class ){
    			ReducedNF rnf=(ReducedNF)(ae);
    			if(rnf.left==cec)
    				return rnf;
    		}
    			
    	}
    	return null;
    }
	public void print(){
		System.out.println("SFDG:\n");
		for(ParallelTask p: paratask){
			p.print();
		}
	}
	public void printFile(PrintStream p){
		p.println("SFDG:\n");
		for(ParallelTask pt: paratask){
			pt.print();
		}
	}
	
	
}
