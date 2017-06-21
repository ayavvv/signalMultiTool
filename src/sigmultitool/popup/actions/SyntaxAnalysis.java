package sigmultitool.popup.actions;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
//��������Ҫ�����Ƿ���Signal����ķ��̣�ȷ�������ͣ���÷��̵���ֵ����ֵ������ʱ�ӵȼ���
public class SyntaxAnalysis {
	char[] blankchar={' ','\n','\r','\t'};
	//�������������жϱ��ʽ����
	 String[] logicalString={"not","and","or"};
	 String[] errorInfoArr={"Empty Equations","Undefined Signal:","Syntax Error in Expression","Type is not Matched","Duplicated Assignment"};
	 char[] relationchar={'>','<','='};
	 //��������ַ�/�����жϺ���һ���ַ��Ƿ���=������ǣ���Ϊ�߼����ʽ������Ϊ�������ʽ
	 char[] arithmeticchar={'+','-','*','/'};
	 String errorInfo="";//�洢������Ϣ
     Lexical lp;
     
     public SyntaxAnalysis(Lexical s){
    	 lp=s;
     }
     //��������
     //����ַ���s���Ƿ������ca�е�Ԫ��
     public int checkStringChar(String s, char[] ca){
    	 for(int i=0;i<ca.length;i++){
    		 if(s.indexOf(ca[i])>=0)
    			 return s.indexOf(ca[i]);
    	 }
    	 return -1;
     }
     //ɾ���ַ���s�е����п��ַ�
     public String deleteBlank(String s){
    	 if(s==null||s.equals("")==true)
    		 return null;
         Pattern p = Pattern.compile("\\s*|\t|\r|\n");
         Matcher m = p.matcher(s);
         String left = m.replaceAll("");
         return left;
     }
     //�ж��ַ����Ƿ������ֻ򲼶�ֵ,����ǲ���ֵ�����ء�boolean������������������ء�integer���������ʵ�������ء�real�������򷵻ؿ�
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
    	 //��SignalProgram�е�equationList��ÿ���ַ������н������������ֵ�Լ��ұ��ʽ
    	 for(String es : lp.sp.getequationList()){
    		 //Ѱ��es�С���=���Ӵ�λ��
    		 int index=es.indexOf(":=");
    		 if(index==-1)
    			 return false;
    		 //equation��ֵ�ź����ƣ�ȥ�������пո�
             Pattern p = Pattern.compile("\\s*|\t|\r|\n");
             Matcher m = p.matcher(es.substring(0,index));
             String left = m.replaceAll("");
             Signal leftSignal=null; //���̵���ֵ�ź�
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
             //�����ұߵı��ʽ����������ͣ������ɷ��̶���
             String ess=es.substring(index+2);
             Expr ex=checkExpr(ess);
             if(ex!=null){
            	 Equation eq=new Equation(leftSignal,ex.getSignalList(),ex);
            	 lp.sp.addEq(eq);
            	 //eq.print();
             }
             //�������
             else{
            	 errorInfo=errorInfoArr[2];
            	 System.out.println("syntax error");
            	 return false;
             }

    	 }
    	 //��鷽�̵���ֵ�ͷ���ֵ�Ƿ�����ͬһ������
         for(Equation e:lp.sp.getEq()){
        	 if(e.checkType()==false){
        		 errorInfo=errorInfoArr[3];
        		 return false;
        	 }
         }
         //����Ƿ��ж�һ��������θ�ֵ�����
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
		 //��������Ӵ���һ��Ϊ���ַ����������
		 if(term1.equals("")==true||term2.equals("")==true){
			 return null;
		 }
		 //��������������ַ���
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
		 //Arith���������ʽ
		 String rt;
		 
		 ArithmeticExpr ae=new ArithmeticExpr(es,"Arith",operand1,operand2,es.substring(index,index+1),"real");
		 ae.addSignal(operand1);
		 ae.addSignal(operand2);
		 return ae;
     }
     //���relation����boolean���ʽ
     public Expr makeBoolean(String es){
    	 //Relation
    	 char [] esc=es.toCharArray();
    	 int index;
    	 int mark=0;//�������/ ����Ϊ1
    	 int mark2=0;//�����=����Ϊ1
		 String term1=null;
		 String term2=null;
		 String operator=null;
		 //��������Ӵ���һ��Ϊ���ַ����������

		 //��������������ַ���

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
     // //���and or not����boolean���ʽ
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

     //�����ʽ����
     public Expr checkExpr(String es){
    	 char[] program=es.toCharArray();
    	 int sub=lp.skipBlank(program,0);
    	 // ���ȼ���Ƿ����������ʽ
    	 if(checkStringChar(es,arithmeticchar)>=0&&checkStringChar(es,relationchar)<0){
    		 //TODO:���������������������������ѧ���ʽ����
    		 ArithmeticExpr ae=(ArithmeticExpr)makeArith(es);
    		 return ae;
    	 }
    	 //����Ƿ����߼����ʽ������>,<,=
    	 else if(checkStringChar(es,relationchar)>=0){
    		 //TODO:����������������߼����ʽ����
    		 BooleanExpr be=(BooleanExpr)makeBoolean(es);
    		 return be;
    	 }
    	 //��Ҫͨ��program�ַ����飬�����ַ������з������жϱ��ʽ����
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
     //����when���ʽ
     public Expr makeWhen(String es){
    	 char[] esc=es.toCharArray();
    	 Signal operand1=null;
    	 Signal operand2=null;
    	 int front=lp.skipBlank(esc, 0);
    	 int rear=lp.reverseSkipBlank(esc, esc.length-1);
    	 //��ȡǰ��ͺ���Ŀհ��ַ�
    	 String esn=es.substring(front,rear+1);
    	 int i=front;
    	 //���ݿհ��ַ���expr�ֳ�������
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
    	 //�ֳ�oprand1��when����
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
    	 //���operand2
		 for(Signal s:lp.sp.getSignalList()){
			 if(s.getName().equals(term2)){
				 operand2=s;					 
				 break;
			 }
		 }
		 if(operand2==null)
			 return null;
		 //����Ϊboolean��event����
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
     //����default���ʽ
     public Expr makeDefault(String es){
    	 char[] esc=es.toCharArray();
    	 Signal operand1=null;
    	 Signal operand2=null;
    	 int front=lp.skipBlank(esc, 0);
    	 int rear=lp.reverseSkipBlank(esc, esc.length-1);
    	 //��ȡǰ��ͺ���Ŀհ��ַ�
    	 String esn=es.substring(front,rear+1);
    	 int i=front;
    	 //���ݿհ��ַ���expr�ֳ�������
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
    	 //�ֳ�oprand1��when����
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
     //����memory���ʽ
     public Expr makeMemory(String es){
    	 int indexdollar;
    	 int indexinit;
    	 indexdollar=es.indexOf('$');
    	 String inith=es.substring(indexdollar+2);
    	 indexinit=inith.indexOf("init");
    	 indexinit=indexinit+indexdollar+2;
    	 if(es.length()<10||indexdollar<=0)
    		 return null;
    	 //���$ǰ��һ���ַ��Ƿ��ǿ��ַ���������ǣ��﷨����
    	 if(lp.checkCharArray(blankchar, es.charAt(indexdollar-1))==false||lp.checkCharArray(blankchar, es.charAt(indexdollar+1))==false)
    		 return null;
    	//���init�ַ�����ǰ��һ���ַ��Ƿ��ǿ��ַ���������ǣ��﷨����
    	 if(lp.checkCharArray(blankchar, es.charAt(indexinit-1))==false||lp.checkCharArray(blankchar, es.charAt(indexinit+4))==false)
    	     return null;
    	//���$��init֮����ַ��ַ��Ƿ��ǿ��ַ��������һ�����ǣ��﷨����
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
     
     //�����ʽ�Ƿ���not or and
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
     //�������з����Ƿ�����single assignment����
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
