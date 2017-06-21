package sigmultitool.popup.actions;
import java.io.*;
import java.util.*;




public class SeqCodeGen extends CodeGeneration{
	/*ClockTree ct;
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
	LinkedList<ClockEquivalenceClass> clockAssign;//store the clocks of which the definition have been generated in the code*/
	
	//String 
	public SeqCodeGen(ClockTree c,ArrayList<Signal> as,ArrayList<String> word,SignalProgram sp,String path){
		super(c,as,word,sp,path);

	}
	public void generateSeq(){
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
		//find the root node
		for(ClockNode cn: ct.clockTree){
			if(cn.ancestor==cn){
				DFSGen(0,statements,cn);
				statements.add("return 1;");
				break;
			}
		}
		Function f=new Function(funcName,returnValue,para,lv,statements);
		funcList.add(f);
   }
   //core of the sequential code generation
   public void DFSGen(int tab,ArrayList<String> statements,ClockNode cn){//tab is the number of table needed to add on the statements
	   ReducedNF rnf=null;
	   String rnfstr=null;
	   StringBuffer sb=new StringBuffer();
	   boolean markif=false;
	   StringBuffer table=new StringBuffer();
	   //if(cn.sortedList.size()<1&&cn.descendants.size()<1)
		//   return ;
	   for(int i=0;i<tab;i++){
		   table.append("\t");
	   }
	   if(cn.clock!=ct.root){
		 //  markif=true;
		   rnf=cn.rnf;
		   if(clockAssign.contains(cn.clock)==false){//generate definition of clock				
			   if(table.equals("")==false)
				   sb.append(table.toString().substring(1));//add tab
			   if(rnf!=null){
				   String s=generateClockEq(rnf);
				   sb.append(s);
			   }
			   statements.add(sb.toString());
			   
		   }
		   if(cn.sortedList.size()>0||cn.descendants.size()>0){
			   markif=true;
			   if(table.equals("")==false)
				   statements.add(table.toString().substring(1)+"if(C_"+cn.clock.id+"!=0){");
		   }
		   
		   
	   }
	   
	   for(Assignment as:cn.sortedList){
		   generatedAssignEq(table,statements,as);
	   }
	   for(ClockNode c:cn.descendants){
		   DFSGen(tab+1,statements,c);
	   }
	   if(markif==true){
		   
		   if(table.equals("")==false)
			   statements.add(table.toString().substring(1)+"}");
	   }
   }
   
}
