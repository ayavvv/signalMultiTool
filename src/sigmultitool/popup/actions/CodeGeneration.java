package sigmultitool.popup.actions;

import java.io.*;
import java.util.*;




public class CodeGeneration {
	ClockTree ct;
	String name;
	SignalProgram program;
	ArrayList<Signal> sigList;
	ArrayList<String> wordList;
	String[] lHead={"#include <stdio.h>","#include <math.h>","#include <stdlib.h>"};//头文件字符串数组
	ArrayList<Function> funcList;
	ArrayList<String> paraDef;
	ArrayList<String> FILEDef;
	ArrayList<String> inputDef;
	ArrayList<String> outputDef;
	ArrayList<String> interDef;
	ArrayList<String> FILEvar;
	LinkedList<String> clockDef;
	//constant string
	String funcParaOpen="open_para_";
	String funcParaRead="read_para_"; 
	String funcReadOpen="open_r_";
	String funcRead="read_";
	String funcWriteOpen="open_w_";
	String funcWrite="write_";
	LinkedList<ClockEquivalenceClass> clockAssign;//store the clocks of which the definition have been generated in the code
	protected void generatedAssignEq(StringBuffer table,ArrayList<String> statements,Assignment as){
		StringBuffer tsb1=new StringBuffer();
		  tsb1.append(table);//add tab;
		  if(as.function==false&&as.right==null){//输入函数
			  if(as.left.ioi.equals("input")){
				  tsb1.append("if("+funcRead+as.left.name+"()==EOF)");
				  statements.add(tsb1.toString());
				  tsb1=new StringBuffer();
				  tsb1.append(table+"\t");
				  tsb1.append("return 0;");

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
					  if(s1.equals("true")){
						  s1="1";
					  }
					  else if(s1.equals("false"))
						  s1="0";
					  if(s2.equals("true")){
						  s2="1";
					  }
					  else if(s2.equals("false"))
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
		  statements.add(tsb1.toString());
		  if(as.left.ioi.equals("output")){//if left signal is an output signal, do the output
			  statements.add(table.toString()+funcWrite+as.left.name+"();");
		  }
		  System.out.println("----ass-----------");
		  as.print();
		  System.out.println(tsb1);
		  System.out.println("----ass-----------");
	}
	protected String generateClockEq(ReducedNF trnf){
		 
		StringBuffer sb=new StringBuffer();
		//trnf=(ReducedNF)lae.get(k);
		//if(table.equals("")==false)
		//	  sb.append(table.toString().substring(1));//add tab
		sb.append("C_"+trnf.left.id+"=");
		if(trnf.right1!=null){
			sb.append("C_"+trnf.right1.id);
		}
		else if(trnf.term1!=null){
			sb.append(trnf.term1.sig.name);
		}
		if(trnf.operator!=null&&trnf.operator.equals("")==false){
			sb.append(changeValue(trnf.operator));
			if(trnf.right2!=null){
			   sb.append("C_"+trnf.right2.id);
			}
		    else if(trnf.term2!=null){
			    sb.append(trnf.term2.sig.name);
			}
		}
		sb.append(";");
		return sb.toString();
	}
	//String 
	public CodeGeneration(ClockTree c,ArrayList<Signal> as,ArrayList<String> word,SignalProgram sp,String path){
		ct=c;
		sigList=as;
		program=sp;
		name=path+"_seq.c";
		wordList=word;
		funcList=new ArrayList<Function>();
		clockDef=new LinkedList<String>();
		FILEDef=new ArrayList<String>();
		paraDef=new ArrayList<String>();
		inputDef=new ArrayList<String>();
		outputDef=new ArrayList<String>();
		interDef=new ArrayList<String>();
		FILEvar=new ArrayList<String>();
		clockAssign=new LinkedList<ClockEquivalenceClass>();
	}

	//change types of SIGNAL into types of C
	protected String changeType(String s){
		if(s==null)
			return null;
		if(s.equals("integer")||s.equals("short")||s.equals("long"))
			return "int";
		if(s.equals("real")||s.equals("dreal"))
			return "double";
		if(s.equals("boolean")||s.equals("event"))
			return "int";
		if(s.equals("char"))
			return "char";
		if(s.equals("boolean"))
			return "int";
		if(s.equals("event"))
			return "int";
		return "";
	}
	//change boolean values of "true" and "false" into "1" and "0"
	protected String changeValue(String s){
		if(s==null)
			return null;
		if(s.equals("true"))
			return "1";
		if(s.equals("false"))
			return "0";
		if(s.equals("intersection"))
			return "&&";
		if(s.equals("union"))
			return "||";
		if(s.equals("setminus"))
			return "&& !";
		return s;
	}
	//check if the name is duplicated, and make a proper name using "s_s_s...", s is the initial parameter
	private String makeProperName(String s){
		if(s==null)
			return null;
		boolean duplicated=false;
		while(true){
			for(String word: wordList){
				if(s.equals(word)){
					duplicated=true;
					break;
				}
			}
			if(duplicated==false)
				break;
			if(duplicated==true){
				s="_"+s;//make new name;
				duplicated=false;
			}
		}
		wordList.add(s);
		return s;
	}
	
	//generate if expression
	private String makeIfExpr(String cond){
		if(cond==null){
			return null;
		}
		return "if("+cond+"){";
	}
	//
	private Function makeOpenFunction(String File,String funcName,String name,String rw,String fileType, String type){
		ArrayList<String> ras=new ArrayList<String>();
	    ArrayList<String> para=new ArrayList<String>();
	    ArrayList<String> localv=new ArrayList<String>();
	    String statement1=File+"=fopen(\""+name+fileType+"\","+"\""+rw+"\");";
	    ras.add(statement1);
	    String statement2=makeIfExpr(File+"==NULL");
	    ras.add(statement2);
	    String statement3="\treturn 0;\n\t}";
	    ras.add(statement3);
	    if(fileType.equals(".PAR")){
		    String statement="";
		    statement="fscanf("+File+",\"%";
		    if(type.equals("int"))
		    	statement+="d\",";
		    else if(type.equals("char"))
		    	statement+="c\",";
		    else if(type.equals("double"))
		    	statement+="lf\",";
		    	statement+="&"+name+");";
		    ras.add(statement);
		}
	    String statement4="return 1;";
	    ras.add(statement4);

	    
	    Function f=new Function(funcName,"int",para,localv,ras);
		return f;
	}
	//make read or write function type:{"int","char",""}
	private Function makeRWFunction(String File,String funcName,String name,String type,String rw){
		ArrayList<String> ras=new ArrayList<String>();
	    ArrayList<String> para=new ArrayList<String>();
	    ArrayList<String> localv=new ArrayList<String>();
	    String statement;
	    String tp;
	    if(rw.equals("r")){
	    	 tp="int";
	    	 statement="return (fscanf("+File+",\"%";
	    	 if(type.equals("int"))
	    		 statement+="d\",";
	    	 else if(type.equals("char"))
	    		 statement+="c\",";
	    	 else if(type.equals("double"))
	    		 statement+="lf\",";
	    	 statement+="&"+name+"));";
	    }
	    else{
	    	tp="void";
	    	statement="fprintf("+File+",\"%";
	    	 if(type.equals("int"))
	    		 statement+="d \",";
	    	 else if(type.equals("char"))
	    		 statement+="c \",";
	    	 else if(type.equals("double"))
	    		 statement+="lf \",";
	    	 statement+=name+");";
	    }
	    ras.add(statement);
	    Function f=new Function(funcName,tp,para,localv,ras);

		return f;
	}
	private void paraIO(Signal s,String rw){
		String temp="";
		String type=changeType(s.typeofSignal);
		temp=temp+type+" "+s.name;
		String funcOpenName=s.name;
		String funcRWName=s.name;
		if(s.initialvalue!=null && s.initialvalue.equals("")==false)
			temp=temp+"="+changeValue(s.initialvalue)+";";
		else
			temp=temp+";";
		if(s.ioi.equals("parameter")){
			paraDef.add(temp);
			funcOpenName=funcParaOpen+funcOpenName;
			funcRWName=funcParaRead+funcRWName;
		}
		else if(s.ioi.equals("input")){
			inputDef.add(temp);
			funcOpenName=funcReadOpen+funcOpenName;
			funcRWName=funcRead+funcRWName;
		}
		else if(s.ioi.equals("output")){
			outputDef.add(temp);
			funcOpenName=funcWriteOpen+funcOpenName;
			funcRWName=funcWrite+funcRWName;
		}
		//generate FILE pointer
		String temp2=makeProperName("fp_"+s.name);//name of FILE pointer
		FILEvar.add(temp2);
		temp="FILE * "+temp2+";";
		FILEDef.add(temp);
		//generate function for reading parameters
		String ft;
		if(s.ioi.equals("parameter"))
			ft=".PAR";
		else
			ft=".txt";
		Function f=makeOpenFunction(temp2,funcOpenName,s.name,rw,ft,type);
		funcList.add(f);
		Function ff=makeRWFunction(temp2,funcRWName,s.name,type,rw);
		funcList.add(ff);
	}
	private void interDef(Signal s){
		String temp="";
		String type=changeType(s.typeofSignal);
		temp=temp+type+" "+s.name;
		if(s.initialvalue!=null && s.initialvalue.equals("")==false)
			temp=temp+"="+changeValue(s.initialvalue)+";";
		else
			temp=temp+";";
		interDef.add(temp);
	}
	public void generateInitialIO(){
		String funcName="initialIO";
		String returnValue="int";
		ArrayList<String> para=new ArrayList<String>();
		ArrayList<String> lv=new ArrayList<String>();
		ArrayList<String> statements=new ArrayList<String>();
		String s2="\treturn 0;";
		for(Function s: funcList){
			String s1="";
			String s3="}";
			//String ss=s.funcName.substring(0,10);
			if(s.funcName.length()>7&&s.funcName.substring(0,7).equals(funcReadOpen)){
				s1=makeIfExpr(s.funcName+"()==0");
			}
			else if(s.funcName.length()>7&&s.funcName.substring(0, 7).equals(funcWriteOpen)){
				s1=makeIfExpr(s.funcName+"()==0");
			}
			else if(s.funcName.length()>10&&(s.funcName.substring(0, 10)).equals(funcParaOpen)){
				s1=makeIfExpr(s.funcName+"()==0");
			}
			else if(s.funcName.length()>10&&s.funcName.substring(0, 10).equals(funcParaRead)){
				s1=makeIfExpr(s.funcName+"()==EOF");
			}
			else
				continue;
			statements.add(s1);
			statements.add(s2);
			statements.add(s3);
		}
		statements.add("return 1;");
		Function f=new Function(funcName,returnValue,para,lv,statements);
		funcList.add(f);
	}
	public void generateCloseIO(){
		String funcName="closeIO";
		String returnValue="void";
		ArrayList<String> para=new ArrayList<String>();
		ArrayList<String> lv=new ArrayList<String>();
		ArrayList<String> statements=new ArrayList<String>();
		for(String s:FILEvar){
			String cond=s+"!=NULL";
			String ifc=makeIfExpr(cond);
			String action="\tfclose("+s+");";
			String s3="}";
			statements.add(ifc);
			statements.add(action);
			statements.add(s3);
		}
		Function f=new Function(funcName,returnValue,para,lv,statements);
		funcList.add(f);
	}
	public void generateFront(){
		String temp=null;
		//generate definitions for clock variables
		temp="int C_"+ct.root.id+"=1;";
		clockDef.add(temp);
		for(ClockEquivalenceClass cec: ct.CECS){
			if(cec!=ct.root){
				temp="int C_"+cec.id+"=0;";
				clockDef.add(temp);
			}
		}
		//generate IO functions
		for(Signal s: sigList){
			if(s.ioi.equals("parameter")){
				paraIO(s,"r");
			}
			else if(s.ioi.equals("input")){
				paraIO(s,"r");
			}
			else if(s.ioi.equals("output")){
				paraIO(s,"w");
			}
			else if(s.ioi.equals("intermediate")){
				interDef(s);
			}
		}
		//generate initial IO
		generateInitialIO();
		//generate close IO
		generateCloseIO();
	}
	public void generateItFront(){
		//
		generateInitialState();
		generateIntialStep();
	}
	public void generateItRear(){
		//

		generateFinalStep();
		generateIteration();
		generateMain();
	}
   public void generateInitialState(){
		String funcName="initialState";
		String returnValue="void";
		ArrayList<String> para=new ArrayList<String>();
		ArrayList<String> lv=new ArrayList<String>();
		ArrayList<String> statements=new ArrayList<String>();
		StringBuffer st;
		for(Equation eq:program.equations){
			if(eq.expr.getClass()==ExprMemory.class){
				st=new StringBuffer(eq.left.name+"=");//+((ExprMemory)(eq.expr)).term2.initialvalue+";");
				String s=((ExprMemory)(eq.expr)).term2.initialvalue;
				if(s.equals("true"))
					s="1";
				else if(s.equals("false"))
					s="0";
				st.append(s+";");
				statements.add(st.toString());
			}
		}
		Function f=new Function(funcName,returnValue,para,lv,statements);
		funcList.add(f);
   }

   public void generateIntialStep(){
		String funcName="initialStep";
		String returnValue="void";
		ArrayList<String> para=new ArrayList<String>();
		ArrayList<String> lv=new ArrayList<String>();
		ArrayList<String> statements=new ArrayList<String>();
		Function f=new Function(funcName,returnValue,para,lv,statements);
		funcList.add(f);
   }
   
   public void generateFinalStep(){
		String funcName="finalStep";
		String returnValue="void";
		ArrayList<String> para=new ArrayList<String>();
		ArrayList<String> lv=new ArrayList<String>();
		ArrayList<String> statements=new ArrayList<String>();
		StringBuffer st;
		//update the memory value
		for(Equation eq:program.equations){
			if(eq.expr.getClass()==ExprMemory.class){
				st=new StringBuffer(eq.left.name+"="+((ExprMemory)(eq.expr)).term1.name+";");
				statements.add(st.toString());
			}
		}
		for(ClockEquivalenceClass cec: ct.CECS){
			if(cec!=ct.root){
				statements.add("C_"+cec.id+"=0;");
			}
		}
		Function f=new Function(funcName,returnValue,para,lv,statements);
		funcList.add(f);
   }
   public void generateIteration(){
		String funcName="iteration";
		String returnValue="void";
		ArrayList<String> para=new ArrayList<String>();
		ArrayList<String> lv=new ArrayList<String>();
		ArrayList<String> statements=new ArrayList<String>();
		statements.add("initialState();");
		statements.add("while(1){");
		statements.add("\tinitialStep();");
		statements.add("\tif(core()==0)");
		statements.add("\t\tbreak;");
		statements.add("\tfinalStep();");
		statements.add("}");
		Function f=new Function(funcName,returnValue,para,lv,statements);
		funcList.add(f);
   }

   public void generateMain(){
		String funcName="main";
		String returnValue="int";
		ArrayList<String> para=new ArrayList<String>();
		ArrayList<String> lv=new ArrayList<String>();
		ArrayList<String> statements=new ArrayList<String>();	
		statements.add("if(initialIO()==0){");
		statements.add("\tprintf(\"files not found\");");
		statements.add("\texit(1);");
		statements.add("}");
		statements.add("iteration();");
		statements.add("closeIO();");
		statements.add("return 0;");
		Function f=new Function(funcName,returnValue,para,lv,statements);
		funcList.add(f);
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

 


