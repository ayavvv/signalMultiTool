package sigmultitool.popup.actions;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
//这个类的主要作用是分析Signal程序的方程，确定其类型，获得方程的左值和右值，划分时钟等价类
public class SyntaxAnalysis {
	char[] blankchar={' ','\n','\r','\t'};
	//以下数组用于判断表达式类型
	 String[] logicalString={"not","and","or"};
	 String[] errorInfoArr={"Empty Equations","Undefined Signal:","Syntax Error in Expression","Type is not Matched","Duplicated Assignment"};
	 char[] relationchar={'>','<','='};
	 //如果发现字符/还需判断后面一个字符是否是=，如果是，则为逻辑表达式，否则为算数表达式
	 char[] arithmeticchar={'+','-','*','/'};
	 String errorInfo="";//存储错误信息
     Lexical lp;
     
     public SyntaxAnalysis(Lexical s){
    	 lp=s;
     }
     //辅助函数
     //检查字符串s中是否出现了ca中的元素
     public int checkStringChar(String s, char[] ca){
    	 for(int i=0;i<ca.length;i++){
    		 if(s.indexOf(ca[i])>=0)
    			 return s.indexOf(ca[i]);
    	 }
    	 return -1;
     }
     //删除字符串s中的所有空字符
     public String deleteBlank(String s){
    	 if(s==null||s.equals("")==true)
    		 return null;
         Pattern p = Pattern.compile("\\s*|\t|\r|\n");
         Matcher m = p.matcher(s);
         String left = m.replaceAll("");
         return left;
     }
     //判断字符串是否是数字或布尔值,如果是布尔值，返回“boolean”，如果是整数，返回“integer”，如果是实数，返回“real”，否则返回空
     public String isNumOrBoolean(String s){
    	  char[] cc=s.toCharArray();
    	  int mark=0;
    	  if(s.equals("true")||s.equals("false"))
    		  return "boolean";
    	  for(int i=0;i<cc.length;i++){
    		  if(!Character.isDigit(cc[i])&&cc[i]!='-'&&cc[i]!='.')
    			  return null;
    		  if(i==0&&cc[i]=='0'){
    			  if(cc.length>1&&cc[1]!='.')
    				  return null;
    		  }
    		  if(cc[i]=='-'&&i>0)
    			  return null;
    		  if(cc[i]=='.'){
    			  if(i==0)
    				  return null;
    			  if(i==cc.length-1)
    				  return null;
    			  if(mark==0)
    				  mark=1;
    			  else{
    				  return null;
    			  }
    		  }
    	  }
    	  if(mark==0)
    		  return "integer";
    	  else    		  
    		  return "real";
     }
     public boolean splitEquation(){
    	 if(lp.sp.getequationList()==null ||lp.sp.getequationList().size()==0){
    		 errorInfo=errorInfoArr[0];
    		 return false;
    	 }
    	 //对SignalProgram中的equationList的每个字符串进行解析，分离出左值以及右表达式
    	 for(String es : lp.sp.getequationList()){
    		 //寻找es中“：=”子串位置
    		 int index=es.indexOf(":=");
    		 if(index==-1)
    			 return false;
    		 //equation左值信号名称，去掉了所有空格
             Pattern p = Pattern.compile("\\s*|\t|\r|\n");
             Matcher m = p.matcher(es.substring(0,index));
             String left = m.replaceAll("");
             Signal leftSignal=null; //方程的左值信号
			 for(Signal s:lp.sp.getSignalList()){
				 if(s.getName().equals(left)){
					 leftSignal=s;
					 break;
				 }
			 }
			 if(leftSignal==null){
				 errorInfo=errorInfoArr[1]+left;
				 return false;
			 }
			 
             char[] pc=es.substring(index+2).toCharArray();
             //方程右边的表达式，检查其类型，并生成方程对象
             String ess=es.substring(index+2);
             Expr ex=checkExpr(ess);
             if(ex!=null){
            	 Equation eq=new Equation(leftSignal,ex.getSignalList(),ex);
            	 lp.sp.addEq(eq);
            	 //eq.print();
             }
             //编译出错
             else{
            	 errorInfo=errorInfoArr[2];
            	 System.out.println("syntax error");
            	 return false;
             }

    	 }
    	 //检查方程的左值和返回值是否属于同一种类型
         for(Equation e:lp.sp.getEq()){
        	 if(e.checkType()==false){
        		 errorInfo=errorInfoArr[3];
        		 return false;
        	 }
         }
         //检查是否有对一个变量多次赋值的情况
         if(checkSingleAssignment()==false){
        	 errorInfo=errorInfoArr[4];
        	 return false;
         }
         addInputEquation();
    	 return true;
     }
     public void addInputEquation(){
    	 ArrayList<Signal> als=lp.sp.signalList;
    	 for(Signal s: als){
    		 if(s.ioi.equals("input")){
    			 Expr ex=new Expr("read("+s.name+")","Input",null);
    			 Equation eq=new Equation(s,null,ex);
    			 lp.sp.addEq(eq);
    			 lp.sp.addEquation("read("+s.name+")");
    		 }
    			 
    	 }
     }
     public Expr makeArith(String es){
		 int index=checkStringChar(es,arithmeticchar);
		 String term1=es.substring(0,index);
		 String term2=es.substring(index+1);
		 //如果两个子串有一个为空字符串，则出错
		 if(term1.equals("")==true||term2.equals("")==true){
			 return null;
		 }
		 //获得两个操作数字符串
		 term1=deleteBlank(term1);
		 term2=deleteBlank(term2);
		 Signal operand1=null;
		 Signal operand2=null;
		 String it1=isNumOrBoolean(term1);
		 String it2=isNumOrBoolean(term2);
          
		 if(it1!=null&&(it1.equals("integer")||it1.equals("real"))){   			 
			 operand1=new Signal(term1,it1,"constant",term1,-1);
		 }
		 else{
			 for(Signal s:lp.sp.getSignalList()){
				 if(s.getName().equals(term1)){
					 operand1=s;
					 break;
				 }
			 }
			 if(operand1==null){
				 return null;
			 }
			 
		 }
		 if(it2!=null&&(it2.equals("integer")||it2.equals("real"))){
			 operand2=new Signal(term2,it2,"constant",term2,-1);
		 }
		 else{
			 for(Signal s:lp.sp.getSignalList()){
				 if(s.getName().equals(term2)){
					 operand2=s;
					 break;
				 }
			 }
			 if(operand2==null){
				 return null;
			 }
		 };
		 //Arith，算数表达式
		 String rt;
		 
		 ArithmeticExpr ae=new ArithmeticExpr(es,"Arith",operand1,operand2,es.substring(index,index+1),"real");
		 ae.addSignal(operand1);
		 ae.addSignal(operand2);
		 return ae;
     }
     //针对relation生成boolean表达式
     public Expr makeBoolean(String es){
    	 //Relation
    	 char [] esc=es.toCharArray();
    	 int index;
    	 int mark=0;//如果出现/ ，置为1
    	 int mark2=0;//如果是=，置为1
		 String term1=null;
		 String term2=null;
		 String operator=null;
		 //如果两个子串有一个为空字符串，则出错

		 //获得两个操作数字符串

		 Signal operand1=null;
		 Signal operand2=null;

    	 if(checkStringChar(es,relationchar)>=0){
    		 //'/='
    		 if(lp.checkCharArray(esc, '/')==true){
    			 index=es.indexOf('/');
    			 term1=es.substring(0,index);
    			 term2=es.substring(index+2);
    			 mark=1;
    			 operator="!=";
    		 }
    		 //
    		 else if(lp.checkCharArray(esc, '>')==true){
    			 index=es.indexOf('>');
    			 term1=es.substring(0,index);
    			 
    			 if(lp.checkCharArray(esc, '=')==true){
    				 operator=">=";
    				 term2=es.substring(index+2);
    			 }
    			 else{
    				 term2=es.substring(index+1);
    				 operator=">";
    			 }
    			 
    		 }
    		 else if(lp.checkCharArray(esc, '<')==true){
    			 index=es.indexOf('<');
    			 term1=es.substring(0,index);
    			 
    			 if(lp.checkCharArray(esc, '=')==true){
    				 operator="<=";
    				 term2=es.substring(index+2);
    			 }
    			 else{
    				 operator="<";
    				 term2=es.substring(index+1);
    			 }
    		 }
    		 else{
    			 index=es.indexOf('=');
    			 term1=es.substring(0,index);
    			 term2=es.substring(index+1);
    			 mark2=1;
    			 operator="==";
    		 }
    		 if(term1.equals("")==true||term2.equals("")==true){
    			 return null;
    		 }
    		 term1=deleteBlank(term1);
    		 term2=deleteBlank(term2);
    		 String it1=isNumOrBoolean(term1);
    		 String it2=isNumOrBoolean(term2);
    		 //if(it1!=null&&(it1.equals("integer")||it1.equals("real"))){   			 
    		//	 operand1=new Signal(term1,it1,"constant",term1,-1);
    		 //}
    		 if(it1!=null){   			 
    			 operand1=new Signal(term1,it1,"constant",term1,-1);
    		 }
    		 else{
    			 for(Signal s:lp.sp.getSignalList()){
    				 if(s.getName().equals(term1)){
    					 operand1=s;
    					 
    					 break;
    				 }
    			 }
    			 if(operand1==null){
    				 return null;
    			 }


    			 
    		 }
    		 if(it2!=null)
    			 operand2=new Signal(term2,it2,"constant",term2,-1);
    		 //if(it2!=null&&(it2.equals("integer")||it2.equals("real"))){
    			// operand2=new Signal(term2,it2,"constant",term2,-1);
    		 //}
    		 else{
    			 for(Signal s:lp.sp.getSignalList()){
    				 if(s.getName().equals(term2)){
    					 operand2=s;
    					 break;
    				 }
    			 }
    			 if(operand2==null){
    				 return null;
    			 }


    		 }
    		 boolean b1=operand1.getTypeofSignal().equals("boolean")||operand1.getTypeofSignal().equals("event");
    		 boolean b2=operand2.getTypeofSignal().equals("boolean")||operand1.getTypeofSignal().equals("event");
    		 if(b1!=b2)
    			 return null;
    		 if(b1==true && (operator.equals(">")||operator.equals(">=")||operator.equals("<")||operator.equals("<=")))
    			 return null;
    		 BooleanExpr be=new BooleanExpr(es,"Boolean",operand1,operand2,operator,"boolean");
    		 be.addSignal(operand1);
    		 be.addSignal(operand2);
    		 return be;
    		 
    	 }
    	 //not, or and

    	 return null;
     }
     // //针对and or not生成boolean表达式
     public Expr makeBoolean2(String es, int index){
    	 String term1=null;
    	 String term2=null;
    	 Signal operand1=null;
    	 Signal operand2=null;
    	 String operator=null;
    	 if(es.indexOf("not")>=0){
    		 operator="!";
    		 term1=es.substring(index+3);
    		 term1=deleteBlank(term1);
			 for(Signal s:lp.sp.getSignalList()){
				 if(s.getName().equals(term1)){
					 operand1=s;
					 
					 break;
				 }
			 }
			 if(operand1==null){
				 return null;
			 }
			 if(operand1.getTypeofSignal().equals("event")==false&&operand1.getTypeofSignal().equals("boolean")==false){
				 return null;
			 }
			 
    	 }
    	 else if(es.indexOf("or")>=0){
    		 operator="||";
    		 term1=es.substring(0,index);
    		 term2=es.substring(index+2);
    		 term1=deleteBlank(term1);
    		 term2=deleteBlank(term2);
			 for(Signal s:lp.sp.getSignalList()){
				 if(s.getName().equals(term1)){
					 operand1=s;					 
					 break;
				 }
			 }
			 if(operand1==null){
				 return null;
			 }
			 if(operand1.getTypeofSignal().equals("event")==false&&operand1.getTypeofSignal().equals("boolean")==false){
				 return null;
			 }
			 for(Signal s:lp.sp.getSignalList()){
				 if(s.getName().equals(term2)){
					 operand2=s;					 
					 break;
				 }
			 }
			 if(operand2==null){
				 return null;
			 }
			 if(operand2.getTypeofSignal().equals("event")==false&&operand2.getTypeofSignal().equals("boolean")==false){
				 return null;
			 }
    	 }
    	 else if(es.indexOf("and")>=0){
    		 operator="&&";
    		 term1=es.substring(0,index);
    		 term2=es.substring(index+3);
    		 term1=deleteBlank(term1);
    		 term2=deleteBlank(term2);
			 for(Signal s:lp.sp.getSignalList()){
				 if(s.getName().equals(term1)){
					 operand1=s;					 
					 break;
				 }
			 }
			 if(operand1==null){
				 return null;
			 }
			 if(operand1.getTypeofSignal().equals("event")==false&&operand1.getTypeofSignal().equals("boolean")==false){
				 return null;
			 }
			 for(Signal s:lp.sp.getSignalList()){
				 if(s.getName().equals(term2)){
					 operand2=s;					 
					 break;
				 }
			 }
			 if(operand2==null){
				 return null;
			 }
			 if(operand2.getTypeofSignal().equals("event")==false&&operand2.getTypeofSignal().equals("boolean")==false){
				 return null;
			 }
    	 }
		 BooleanExpr be=new BooleanExpr(es,"Boolean",operand1,operand2,operator,"boolean");
		 be.addSignal(operand1);
		 if(operand2!=null)
			 be.addSignal(operand2);
		 return be;
    	 //return null;
     }

     //检查表达式类型
     public Expr checkExpr(String es){
    	 char[] program=es.toCharArray();
    	 int sub=lp.skipBlank(program,0);
    	 // 首先检查是否是算数表达式
    	 if(checkStringChar(es,arithmeticchar)>=0&&checkStringChar(es,relationchar)<0){
    		 //TODO:将两个操作数分离出来，建立数学表达式对象
    		 ArithmeticExpr ae=(ArithmeticExpr)makeArith(es);
    		 return ae;
    	 }
    	 //检查是否是逻辑表达式，包含>,<,=
    	 else if(checkStringChar(es,relationchar)>=0){
    		 //TODO:分离操作数，建立逻辑表达式对象
    		 BooleanExpr be=(BooleanExpr)makeBoolean(es);
    		 return be;
    	 }
    	 //需要通过program字符数组，分离字符串进行分析，判断表达式类型
    	 else{
    		 //not or and
    		 if(checkBoolean(es)>=0){
    			 BooleanExpr be=(BooleanExpr)makeBoolean2(es,checkBoolean(es));
    			 return be;
    		 }
    		 //c1:= c2 $ init c
    		 else if(es.indexOf('$')>0){
    			 ExprMemory em=(ExprMemory)makeMemory(es);
    			 return em;
    		 }
    		 //c1:= a1 default a2
    		 else if(es.indexOf("default")>0&&es.indexOf("when")<0){
    			 DefaultExpr de=(DefaultExpr)makeDefault(es);
    			 return de;
    		 }
    		 //c1:= a2 when c3
    		 else if(es.indexOf("when")>0){
    			 WhenExpr we=(WhenExpr)makeWhen(es);
    			 return we;
    		 }
    		 return null;
    	 }
    	 
     }
     //构造when表达式
     public Expr makeWhen(String es){
    	 char[] esc=es.toCharArray();
    	 Signal operand1=null;
    	 Signal operand2=null;
    	 int front=lp.skipBlank(esc, 0);
    	 int rear=lp.reverseSkipBlank(esc, esc.length-1);
    	 //截取前面和后面的空白字符
    	 String esn=es.substring(front,rear+1);
    	 int i=front;
    	 //根据空白字符将expr分成三部分
    	 while(i<esc.length){
    		 if(lp.checkCharArray(blankchar, esc[i])==true)
    			 break;
    		 i++;
    	 }
    	 int j=rear;
    	 while(j>=0){
    		 if(lp.checkCharArray(blankchar, esc[j])==true)
    			 break;
    		 j--;
    	 }
    	 //分成oprand1，when，和
    	 String term1=es.substring(0,i);
    	 String when=es.substring(i,j);
    	 String term2=es.substring(j);
    	 if(when.indexOf("when")<0)
    		 return null;
    	 term1=deleteBlank(term1);
    	 when=deleteBlank(when);
    	 term2=deleteBlank(term2);
    	 if(when.equals("when")==false)
    		 return null;
    	 //检查operand2
		 for(Signal s:lp.sp.getSignalList()){
			 if(s.getName().equals(term2)){
				 operand2=s;					 
				 break;
			 }
		 }
		 if(operand2==null)
			 return null;
		 //必须为boolean或event类型
		 if(operand2.getTypeofSignal().equals("event")==false&&operand2.getTypeofSignal().equals("boolean")==false)
		     return null;
		 String it1=isNumOrBoolean(term1);
		 if(it1!=null){
			 operand1=new Signal(term1,it1,"constant",term1,-1);
		 }
		 else{
			 for(Signal s:lp.sp.getSignalList()){
				 if(s.getName().equals(term1)){
					 operand1=s;
					 break;
				 }
			 }
			 if(operand1==null){
				 return null;
			 }


		 };
		 WhenExpr de=new WhenExpr(es,"When",operand1,operand2,operand1.getTypeofSignal());

		 de.addSignal(operand1);
		 de.addSignal(operand2);
    	 return de;   	 
    	
     }
     //构造default表达式
     public Expr makeDefault(String es){
    	 char[] esc=es.toCharArray();
    	 Signal operand1=null;
    	 Signal operand2=null;
    	 int front=lp.skipBlank(esc, 0);
    	 int rear=lp.reverseSkipBlank(esc, esc.length-1);
    	 //截取前面和后面的空白字符
    	 String esn=es.substring(front,rear+1);
    	 int i=front;
    	 //根据空白字符将expr分成三部分
    	 while(i<esc.length){
    		 if(lp.checkCharArray(blankchar, esc[i])==true)
    			 break;
    		 i++;
    	 }
    	 int j=rear;
    	 while(j>=0){
    		 if(lp.checkCharArray(blankchar, esc[j])==true)
    			 break;
    		 j--;
    	 }
    	 //分成oprand1，when，和
    	 String term1=es.substring(0,i);
    	 String defaults=es.substring(i,j);
    	 String term2=es.substring(j);
    	 if(defaults.indexOf("default")<0)
    		 return null;
    	 term1=deleteBlank(term1);
    	 defaults=deleteBlank(defaults);
    	 term2=deleteBlank(term2);
    	 String it2=isNumOrBoolean(term2);
    	 String it1=isNumOrBoolean(term1);
    	 if(defaults.equals("default")==false)
    		 return null;
    	 if(it1!=null)
    		 operand1=new Signal(term1,it1,"constant",term1,-1);
    	 else{
			 for(Signal s:lp.sp.getSignalList()){
				 if(s.getName().equals(term1)){
					 operand1=s;					 
					 break;
				 }
			 }
    	 }
		 if(operand1==null)
			 return null;
		 
		 if(it2!=null){
			 operand2=new Signal(term2,it2,"constant",term2,-1);
		 }
		 else{
			 for(Signal s:lp.sp.getSignalList()){
				 if(s.getName().equals(term2)){
					 operand2=s;
					 break;
				 }
			 }
			 if(operand2==null){
				 return null;
			 }


		 };
		 DefaultExpr de=new DefaultExpr(es,"Default",operand1,operand2,operand1.getTypeofSignal());
		 if(de.checkType()==false)
			 return null;
		 de.addSignal(operand1);
		 de.addSignal(operand2);
    	 return de;
    	 
     }
     //生成memory表达式
     public Expr makeMemory(String es){
    	 int indexdollar;
    	 int indexinit;
    	 indexdollar=es.indexOf('$');
    	 String inith=es.substring(indexdollar+2);
    	 indexinit=inith.indexOf("init");
    	 indexinit=indexinit+indexdollar+2;
    	 if(es.length()<10||indexdollar<=0)
    		 return null;
    	 //检查$前后一个字符是否是空字符，如果不是，语法错误
    	 if(lp.checkCharArray(blankchar, es.charAt(indexdollar-1))==false||lp.checkCharArray(blankchar, es.charAt(indexdollar+1))==false)
    		 return null;
    	//检查init字符串的前后一个字符是否是空字符，如果不是，语法错误
    	 if(lp.checkCharArray(blankchar, es.charAt(indexinit-1))==false||lp.checkCharArray(blankchar, es.charAt(indexinit+4))==false)
    	     return null;
    	//检查$与init之间的字符字符是否都是空字符，如果有一个不是，语法错误
    	 for(int i=indexdollar+1;i<indexinit;i++){
    		 if(lp.checkCharArray(blankchar, es.charAt(i))==false)
    			 return null;
    	 }
    	 String term1=es.substring(0,indexdollar);
    	 String term2=es.substring(indexinit+5);
    	 term1=deleteBlank(term1);
    	 term2=deleteBlank(term2);
    	 Signal operand1=null;
    	 Signal operand2=null;
		 for(Signal s:lp.sp.getSignalList()){
			 if(s.getName().equals(term1)){
				 operand1=s;					 
				 break;
			 }
		 }
		 if(operand1==null){
			 return null;
		 }
		 String it2=isNumOrBoolean(term2);
		 if(it2==null)
			 return null;
		 operand2=new Signal(term2,it2,"constant",term2,-1);
		 boolean b1=operand1.getTypeofSignal().equals("boolean")||operand1.getTypeofSignal().equals("event");
		 boolean b2=operand2.getTypeofSignal().equals("boolean")||operand1.getTypeofSignal().equals("event");
		 if(b1!=b2)
			 return null;		 
		 ExprMemory em=new ExprMemory(es,"Memo",operand1,operand2,operand1.getTypeofSignal());
		 em.addSignal(operand1);
		 em.addSignal(operand2);
    	 return em;
     }
     
     //检查表达式是否是not or and
     public int checkBoolean(String es){
    	 int index;
    	 
    	 if(es.indexOf("not")>=0){
    		 if(lp.checkCharArray(blankchar, es.charAt(es.indexOf("not")+3))==false){
    			 return -1;
    		 }
    		 else{
    			 return es.indexOf("not");
    		 }
    	 }
    	 else if(es.indexOf("or")>=0){
    		 index=es.indexOf("or");
    		 if(index<=0||es.length()<3)
    			 return -1;
    		 else{
        		 if(lp.checkCharArray(blankchar, es.charAt(index-1))==false||lp.checkCharArray(blankchar, es.charAt(index+2))==false){
        			 return -1;
        		 } 
        		 else
        			 return index;
    		 }
    	 }
    	 else if(es.indexOf("and")>=0){
    		 index=es.indexOf("and");
    		 if(index<=0||es.length()<3)
    			 return -1;
    		 else{
        		 if(lp.checkCharArray(blankchar, es.charAt(index-1))==false||lp.checkCharArray(blankchar, es.charAt(index+3))==false){
        			 return -1;
        		 } 
        		 else
        			 return index;
    		 }
    	 }
    	 return -1;
     }
     //检查程序中方程是否满足single assignment属性
     public boolean checkSingleAssignment(){
    	 int[] signalchar=new int[lp.mark];
    	 for(int i=0;i<lp.mark;i++){
    		 signalchar[i]=0;
    	 }
    	 for(Equation e:lp.sp.getEq()){
    		 signalchar[e.left.getNo()]++;
    	 }
    	 for(int i=0;i<lp.mark;i++){
    		 if(signalchar[i]>1)
    			 return false;
    	 }
    	 return true;
     }
     
}
