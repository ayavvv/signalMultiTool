package sigmultitool.popup.actions;
import java.util.*;
import java.io.*;

public class MultiCodeGen extends CodeGeneration{
	CombinedSFDG comSFDG;
	int cores;
	String name;
	String[] lHead={"#include <stdio.h>","#include <math.h>","#include <stdlib.h>","#include <omp.h>"};
	public MultiCodeGen(ClockTree c,ArrayList<Signal> as,ArrayList<String> word,SignalProgram sp, CombinedSFDG sfdg,String path){
		super(c,as,word,sp,"");
		comSFDG=sfdg;
		cores=Runtime.getRuntime().availableProcessors();
		name=path+"_multi.c";
	}
	
	public void generateMulti(){
		super.generateFront();//生成变量声明，io读取函数
		super.generateItFront();//生成迭代函数
		generateCore();
		super.generateItRear();
	}
	public void generateCore(){
		String funcName="core";
		String returnValue="int";
		ArrayList<String> para=new ArrayList<String>();
		ArrayList<String> lv=new ArrayList<String>();
		ArrayList<String> statements=new ArrayList<String>();
		Function f=new Function(funcName,returnValue,para,lv,statements);
		String s="int mark=0;";
		statements.add(s);
		for(ParallelTask pt: comSFDG.paratask){
			if(pt.clockRelation.size()>0){
				if(pt.clockPara==true||pt.clockRelation.size()<2){
					for(ReducedNF rnf: pt.clockRelation){
						StringBuffer sb=new StringBuffer();
						sb.append("C_"+rnf.left.id+"=");
						   if(rnf.right1!=null){
							   sb.append("C_"+rnf.right1.id);
						   }
						   else if(rnf.term1!=null){
							   sb.append(rnf.term1.sig.name);
						   }
						   if(rnf.operator!=null&&rnf.operator.equals("")==false){
							   sb.append(super.changeValue(rnf.operator));
							   if(rnf.right2!=null){
								   sb.append("C_"+rnf.right2.id);
							   }
							   else if(rnf.term2!=null){
								   sb.append(rnf.term2.sig.name);
							   }
						   }
						   sb.append(";");
						   statements.add(sb.toString());
					}
				}
				else{
					StringBuffer sb=new StringBuffer();
					int size=pt.clockRelation.size();
		
					sb.append("#pragma omp parallel sections");
					statements.add(sb.toString());
					
					String lb="{";
					statements.add(lb);
					
					for(ReducedNF rnf: pt.clockRelation){
						
						String sec="\t#pragma omp section";
						statements.add(sec);
						String llb="\t{";
						statements.add(llb);						
						String rs=generateClockEq(rnf);
						rs="\t\t"+rs;
						statements.add(rs);	
						String rrb="\t}";
						statements.add(rrb);
					}
					String rb="}";
					statements.add(rb);
				}
			}
				if(pt.assignments.size()>0||pt.paraClockR.size()>0){
					StringBuffer sb=new StringBuffer();
					int size=pt.assignments.size()+pt.paraClockR.size();
					if(size>1){
						sb.append("#pragma omp parallel sections");
					}
					int assignNum=0;
					//if(size<cores){//是否要设定线程数量？						
						//sb.append(" num_threads("+size+")");
					//用于控制一个section中最多串行的执行语句数量
						assignNum=1;
					//}
					//else
						//assignNum=size/cores;
					int tmpa=0;
					statements.add(sb.toString());
					
					
					String lb="{";
					if(size>1)
						statements.add(lb);
					LinkedList<AncestorEquation> lae=new LinkedList<AncestorEquation>();//进行分组
					for(Assignment as: pt.assignments){
						lae.add(as);
					}
					for(ReducedNF as: pt.paraClockR){
						lae.add(as);
					}
					int k=0;
					int mark=0;
					while(true){						
						String sec="\t#pragma omp section";
						if(size>1){
							statements.add(sec);
							String llb="\t{";
							statements.add(llb);
						}
						
						for(tmpa=0;tmpa<assignNum;tmpa++){
							Assignment as;
							ReducedNF trnf;
							if(k>=lae.size())
								break;
							if(lae.get(k).getClass()==Assignment.class){
								as=(Assignment)lae.get(k);
								ClockEquivalenceClass cec=as.cl;
								if(as.left.ioi.equals("input")){
									mark=1;
								}
								if(cec!=null&&ct.root!=cec){
									String c="\t\tif(C_"+cec.id+"!=0){";
									statements.add(c);
								}
								StringBuffer sbb=new StringBuffer("");
								generatedAssignEq(sbb,statements,as);
								if(cec!=null&&ct.root!=cec){
									String c="\t\t}";
									statements.add(c);
								}
							}
							else if(lae.get(k).getClass()==ReducedNF.class){							
								trnf=(ReducedNF)lae.get(k);
								String rs=generateClockEq(trnf);
								rs="\t\t"+rs;
								statements.add(rs);	
							}
							k++;
						}
						
						String rrb="\t}";
						if(size>1)
							statements.add(rrb);	
						if(k>=lae.size())
							break;
					}
					String rb="}";
					if(size>1)
						statements.add(rb);
					if(mark==1){
						String markAdd="if(mark==1)";
						statements.add(markAdd);
						String markAdd2="\treturn 0;";
						statements.add(markAdd2);
					}
					
				}
			}
			
		s="return 1;";
		statements.add(s);
		funcList.add(f);
	}
	protected void generatedAssignEq(StringBuffer table,ArrayList<String> statements,Assignment as){
		StringBuffer tsb1=new StringBuffer();
		  tsb1.append(table);//add tab;
		  int mark=0;
		  if(as.function==false&&as.right==null){//输入函数
			  if(as.left.ioi.equals("input")){
				  mark=1;
				  tsb1.append("\t\tif("+funcRead+as.left.name+"()==EOF)");
				  statements.add(tsb1.toString());
				  tsb1=new StringBuffer();
				  tsb1.append(table+"\t");
				  tsb1.append("mark=1;");

			  }
				  
		  }
		  else if(as.function==false){			
			  String s=as.right.name;
			  if(s.equals("true"))
				  s="1";
			  else if(s.equals("false"))
				  s="0";
			  tsb1.append(as.left.name+"="+s+";");
		  }		  
		  else{
			  tsb1.append(as.eq.left.name+"=");
			  if(as.eq.expr.getClass()==BooleanExpr.class){
				  BooleanExpr be=(BooleanExpr)as.eq.expr;
				  if(be.type.equals("!")){
					  tsb1.append(be.type+be.term1.name);
				  }
				  else{
					  String s1=be.term1.name;
					  String s2=be.term2.name;
					  if(s1.equals("true"))
						  s1="1";
					  else if(s1.equals("false"))
						  s1="0";
					  if(s2.equals("true"))
						  s2="1";
					  else if(s1.equals("false"))
						  s2="0";
					  tsb1.append(s1+be.type+s2);
				  }
			  }
			  else if(as.eq.expr.getClass()==ArithmeticExpr.class){
				  ArithmeticExpr ae=(ArithmeticExpr)as.eq.expr;
				  tsb1.append(ae.term1.name+ae.type+ae.term2.name);
			  }
			  tsb1.append(";");
			  //tsb1.append(as.eq.expr.)
		  }
		  String ta;
		  if(as.cl!=null&&ct.root!=as.cl)
			  ta="\t\t\t";
		  else
			  ta="\t\t";
		  statements.add(ta+tsb1.toString());
		  if(as.left.ioi.equals("output")){//if left signal is an output signal, do the output
			  statements.add("\t\t\t"+table.toString()+funcWrite+as.left.name+"();");
		  }
	}
	public void printCode(){
	    //File file=new File(name);
	    
	    try{
	    	FileOutputStream out=new FileOutputStream(name);
            PrintStream p=new PrintStream(out);
            for(int i=0;i<lHead.length;i++)
                p.println(lHead[i]);
            //print file pointers;
            p.print("//definition of pointers to the input files\n");
            for(String s: FILEDef){
            	p.print(s);
            }
            p.print("\n//definitions of clock variables\n");
            //print clock variables
            for(String s: clockDef){
            	p.print(s);
            }
            p.print("\n//definition of parameters\n");
            //print para variables
            for(String s: paraDef){
            	p.print(s);
            }
            p.print("\n//definition of input variables\n");
            //print input variables
            for(String s: inputDef){
            	p.print(s);
            }
            p.print("\n//definition of output variables\n");
            //print output variables
            for(String s: outputDef){
            	p.print(s);
            }
            p.print("\n//definition of intermediate variables\n");
            //print intermediate variables
            for(String s: interDef){
            	p.print(s);
            }
            p.print("\n");
            //print functions
            for(Function f: funcList){
            	if(f!=null)
            	f.printFunc(p);
            }
	       // FileOutputStream out=new FileOutputStream(file,true);  
	       // StringBuffer sb=new StringBuffer();
	        
	       // for(int i=0;i<lHead.length;i++){
	       // 	sb.append(lHead[i]);
	       // 	out.write(sb.toString().getBytes("utf-8"));
	      //  }     
	        out.close();
	    }catch(Exception e){
	    	e.printStackTrace();
	    }

	} 
}
