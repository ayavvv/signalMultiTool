//2013.01.16创建
//分析具有weak endochrony性质的Signal程序，构件SFDG，并从SFDG中生成OpenMP多线程代码
package sigmultitool.popup.actions;
import java.io.*;
import java.util.*;
//Signal bnf
/*PROCESS ::= process Name-model = INTERFACE BODY ;
INTERFACE ::=
        [ PARAMETERS ] ( INPUTS OUTPUTS ) 
PARAMETERS ::=
        { { FORMAL-PARAMETER } }
FORMAL-PARAMETER ::= DECLARATION 
INPUTS ::=
        ? { DECLARATION }
OUTPUTS ::=
        ! { DECLARATION }
BODY ::=CONFINED-PROCESS | COMPOSITION
COMPOSITION ::=
        (| [ P-EXPR { | P-EXPR } ] |)
P-EXPR ::= DEFINITION-OF-SIGNALS
CONFINED-PROCESS ::=
        COMPOSITION  DECLARATION-BLOCK
DECLARATION-BLOCK ::=
        where DECLARATION { DECLARATION } end
DEFINITION-OF-SIGNALS ::=
        DEFINED-ELEMENT := S-EXPR
DEFINED-ELEMENT ::=Name-signal
S-EXPR ::=
        | S-EXPR-DYNAMIC
        | S-EXPR-TEMPORAL
        | S-EXPR-BOOLEAN
        | S-EXPR-ARITHMETIC
        | S-EXPR-ELEMENTARY
        | ( S-EXPR )
S-EXPR-DYNAMIC ::=
        Name-signal $ init CONSTANT 
S-EXPR-TEMPORAL ::=
        MERGING
        | EXTRACTION
S-EXPR-ELEMENTARY ::=
        CONSTANT
        | Name-signal
MERGING ::=
        Name-signal default S-EXPR-ELEMENTARY
EXTRACTION ::=
        S-EXPR-ELEMENTARY when Name-signal
S-EXPR-BOOLEAN ::=
        not Name-signal
        | Name-signal or Name-signal
        | Name-signal and Name-signal
        | RELATION
RELATION ::=
        TERM = TERM
        | TERM /= TERM
        | TERM > TERM
        | TERM >= TERM
        | TERM < TERM
        | TERM <= TERM
        | TERM == TERM
S-EXPR-ARITHMETIC ::=
        TERM + TERM
        | TERM - TERM
        | TERM * TERM
        | TERM / TERM
TERM ::= Name-Signal | NUMCONSTANT
DECLARATION ::=
        SIGNAL-TYPE  DEFINITION-OF-SEQUENCE{ , DEFINITION-OF-SEQUENCE } ;
        | DEFINITION-OF-SEQUENCE { , DEFINITION-OF-SEQUENCE } ;
DEFINITION-OF-SEQUENCE ::=
        Name-signal
        | Name-signal init CONSTANT
SIGNAL-TYPE ::=
        Synchronization-type
        | Numeric-type
        | Alphabetic-type
Synchronization-type ::=
        event
        | boolean
Numeric-type ::=
        Integer-type
        | Real-type
Integer-type ::=
        short
        | integer
        | long
Real-type ::=
        real
        | dreal
Alphabetic-type ::=
        char
        | string

*/


public class Lexical {
	char[] blankchar={' ','\n','\r','\t'};
	char[] delimterchar={'(', ')','|','=',':','/'};
	SignalProgram sp;
	String[] keywords={"process","init","where","end","default","when","not","and","or"};
	//signal程序支持的数据类型
	String[] datatype={"event","boolean","short","integer","long","real","dreal"};
	/**
	 * @param args
	 */
	//辅助函数
	//检查字符数组carray中是否有c字符
	int mark=0;
	public boolean checkCharArray(char[] carray, char c){
		if(carray==null)
			return false;
		for(int i=0;i<carray.length;i++){
			if(c==carray[i])
				return true;
		}
		return false;
	}
	//检查string数组Sarray中是否有s
	public boolean checkStringArray(String[] sarray, String s){
		if(sarray==null)
			return false;
		for(int i=0;i<sarray.length;i++){
			if(s.equals(sarray[i]))
				return true;
		}
		
		return false;
	}
	//从sub下标开始，跳过program数组中的所有空字符，返回sub
	public int skipBlank(char[] program, int sub){
		if(sub>=program.length)
			return -1;
		char tmpc=program[sub];
		while(sub<program.length&&checkCharArray(blankchar,tmpc)){
			if(sub+1>=program.length)
				break;
			tmpc=program[sub+1];
			sub++;
		}
		if(checkCharArray(blankchar,program[sub]))
			return -1;
		return sub;
	}
	//从相反方向遍历跳过空字符
	public int reverseSkipBlank(char[] program, int sub){
		if(sub<=0||sub>=program.length)
			return -1;
		char tmpc=program[sub];
		while(sub>0&&checkCharArray(blankchar,tmpc)){
			tmpc=program[sub-1];
			sub--;
		}
		if(checkCharArray(blankchar,program[sub]))
			return -1;
		return sub;
	}
	//检查字符c是否是数字，字母或下划线 ，用于分割signal关键字用
	public boolean checkDL(char c){
		if(Character.isDigit(c)==true || Character.isLetter(c)==true || c=='_'){
			return true;
		}
		return false;
	}

	//检查字符串s是否满足keyword要求
	public boolean checkKeyword(String s){
		char[] c=s.toCharArray();
		for(int i=0;i<c.length;i++){
			if(Character.isDigit(c[i])==false && Character.isLetter(c[i])==false && c[i]!='_'){
				return false;
			}
		}
		if(Character.isDigit(c[0])==true)
			return false;
		return true;
	}
	//检查常数项类型，返回字符串"integer,real,boolean",若返回空串，说明常数项有错误
	public String checkConstant(String s){
		return "";
	}
	//public 
	public boolean lexcialAnalysis(char[] program){
		if(program==null || program.length==0){
			return false;
		}
		int elementnumber=program.length;
		if(checkProcess(program)==false){
			System.out.println("compile error");
			return false;
		}
		if(checkSignalKeyword()==true){
			return false;
		}
		//删除方程字符串里前面和后面的空字符
		eliminateEquationBlank();
		
		return true;
	}
	//从程序开始部分扫描过“process”关键字，调用CheckProcessName，获取process的名字
	public boolean checkProcess(char[] program){
		if(program==null||program.length==0)
			return false;
		char tmpc=program[0];
		int sub=0;
		sub=skipBlank(program,0);
		if(sub==-1)
			return false;
		char[] pchar=new char[7];
		for(int i=0;i<7;i++){
			if(program.length-sub>=1){
				pchar[i]=program[sub];
				sub++;
			}
			else
				break;
		}
		String ps=new String(pchar);
		if(ps.equals("process")==false)
			return false;
		 //调用processname
		if(checkProcessName(program,sub)==false)
			return false;
		return true;
	}
	
	//获取process的名称
	public boolean checkProcessName(char[] program, int sub){
		int sub2=skipBlank(program,sub);
		if(sub2==-1)
			return false;
		int tmp=sub2;		
		while(tmp<program.length&&checkDL(program[tmp])==true){			
			tmp++;
		}
		char[] cl=new char[tmp-sub2];
		tmp=sub2;
		for(int i=0;i<cl.length;i++){
           cl[i]=program[sub2];
           sub2++;
		}
		String pn=new String(cl);
		if(checkKeyword(pn)==false){
			return false;			
		}		
		//在Lexical对象中新建SignalProgram对象
		sp=new SignalProgram(pn);		
		//System.out.println(sp.getName());
		//获取Signal程序的输入，输出信号
		sub2=skipBlank(program,sub2);
		//跳过空字符
		if(sub2==-1||sub2>=program.length||program[sub2]!='=')
			return false;
		sub2=skipBlank(program,sub2+1);
		//如果读取字符为'{'，则进入读取参数的函数，否则直接进入读取IO信号的函数checkIOSignals
		if(program[sub2]=='{'){
			sub2=checkParameters(program,sub2+1);
			if(sub2==-1){
				return false;
			}
			sub2=checkIOSignals(program,sub2+1);
		}
		
		else if(program[sub2]=='('){
			sub2=checkIOSignals(program,sub2);
			if(sub2==-1){
				return false;
			}
		}
		else{
			return false;
		}
		sub2=skipBlank(program,sub2);
		//(||)end;共8个字符
		if(sub2==-1||program.length-1-sub2<8||program[sub2]!='('||program[sub2+1]!='|'){
			return false;
		}
		sub2=sub2+2;		
		sub2=skipBlank(program,sub2);
		tmp=sub2;
		//找到‘ |）’
		while(sub2<=program.length-2){
			if(program[sub2]=='|'&&program[sub2+1]==')'){
				break;
			}
			sub2++;
		}
		if(sub2==program.length-1){
			return false;
		}
		if(tmp==sub2){
			System.out.println("no equation");
			return false;
		}
		//equationchar为(| |)之间的字符数组
		char[] equationchar=new char[sub2-tmp];
		for(int i=0;i<equationchar.length;i++){
			equationchar[i]=program[tmp];
			tmp++;
			                     
		}		
		String equationString=new String(equationchar);
		//esa为通过equationString分割而来的方程字符串数组
		String[] esa=equationString.split("\\|");
		for(String s : esa){
			sp.addEquation(s);
		}
		sub2+=2;
		sub2=skipBlank(program,sub2);
		if(sub2==-1||sub2>=program.length)
			return false;
		//含有where,有局部变量
		if(program[sub2]=='w'){
			tmp=sub2;
			//找到第一个单词
			while(sub2<program.length&&checkCharArray(blankchar,program[sub2])==false){
				sub2++;
			}
			char[] wherec=new char[sub2-tmp];
			for(int i=0;i<wherec.length;i++){
				wherec[i]=program[tmp];
				tmp++;
			}
			//判断是否是where
			String wheres=new String(wherec);
			if(wheres.equals("where")==false)
				return false;
			//从program最后一个字符往回找到第一个不是空字符的字符，下标为lasttmp
			int lasttmp=program.length-1;
			while(lasttmp>sub2){
				if(program[lasttmp]!=';'){
					break;
				}
				lasttmp--;
			}
			if(lasttmp==sub2)
				return false;
			//找到；前面第一个不是空字符的下标
			lasttmp=reverseSkipBlank(program,lasttmp-1);
			if(lasttmp-sub2<3)
				return false;
			//获取where与end之间的字符数组，调用splitVariable，获取中间信号
			char[] interc=new char[lasttmp-2-sub2];
			for(int i=0;i<interc.length;i++){
				interc[i]=program[sub2];
				sub2++;
			}
			if(splitVariable(interc,"intermediate")==-1)
				return false;
			
		}
		//end，无局部变量
		else if(program[sub2]=='e'){
			tmp=sub2;
			while(sub2<program.length&&program[sub2]!='d'){
				sub2++;
			}
			sub2++;
			char[] endc=new char[sub2-tmp];
			for(int i=0;i<endc.length;i++){
				endc[i]=program[tmp];
				tmp++;
			}
			//判断endc是否是“end”
			String wheres=new String(endc);
			if(wheres.equals("end")==false)
				return false;
			while(sub2<program.length&&program[sub2]!=';'){
				sub2++;
			}
			if(sub2>=program.length)
				return false;
			sub2++;
			sub2=skipBlank(program,sub2+1);
			if(sub2!=-1)
				return false;
		}
		else{
			return false;
		}
		return true;
	}
	//提取参数{}
	public int checkParameters(char[] program,int sub){
		int sub2=skipBlank(program,sub);
		int tmp=sub2;
		if(sub2==-1||sub2>=program.length)			
			return -1;
		//分理处数组{}之间的字符
		while(sub2<program.length&&program[sub2]!='}'){
			sub2++;
		}
		if(sub2==tmp){
			return sub2;
		}
		if(sub2>=program.length)
			return -1;
		char[] parameterchar=new char[sub2-tmp];
		for(int i=0;i<parameterchar.length;i++){
			parameterchar[i]=program[tmp];
			tmp++;
		}
		//调用 splitVariable
		if(splitVariable(parameterchar,"para")==-1)
			return -1;
		
	   
		return sub2;
	}
    //提取参数{}
	public int splitVariable(char[] program,String type){
		String typetmp;		
		typetmp=new String(program);
		String[] sa=typetmp.split(";");
		//sa为经过“；”分割的字符串数组，对其进行遍历
		for(int i=0;i<sa.length;i++){ 
			//变换为字符数组tmpc，进行遍历
			char[] tmpc=sa[i].toCharArray();
			//从下标0开始遍历tmpc，跳过空字符
			int sub2=skipBlank(tmpc,0);
			//返回-1或sub2超过tmpc长度时，返回错误
			if(sub2==-1 || sub2>=tmpc.length){
				continue;
			}
			//从tmpc[tmp]开始直到tmpc[tmp]为空字符
			int tmp=sub2;
			while(checkCharArray(blankchar,tmpc[tmp])==false){
				tmp++;
			}
			//建立typechar字符数组
			char[] typechar=new char[tmp-sub2];
			if(tmp==sub2)
				continue;
			for(int j=0;j<typechar.length;j++){
				typechar[j]=tmpc[sub2];
				sub2++;
			}
			//typestring为参数的类型，如果不在datatype内，则返回错误
			String typestring=new String(typechar);
			if(checkStringArray(datatype,typestring)==false){
				return -1;
			}
			//从sub2开始跳过空字符
			sub2=skipBlank(tmpc,sub2);
			if(sub2==-1 || sub2>=tmpc.length){
				return -1;
			}
			//p为sa[i]除去类型以及前空字符的子字符串
			String p=sa[i].substring(sub2);
			//使用‘，’分割字符串pa数组
			String[] pa=p.split(","); 
		    //遍历pa数组
			for(int k=0;k<pa.length;k++){
				char[] tmpp=pa[k].toCharArray();
                
				int sub3=skipBlank(tmpp,0);
				if(sub3==-1||sub3>=tmpp.length){
					return -1;
				}
				int tmp2=sub3;
				//标识位，0则字符串为参数的名称，1为init字符串，2为constant值
				int flag=0;
				String ts2=null;
				String constantvalue=null;
				int m=tmp2;
				//分离变量名称，init，constant
				while(m<tmpp.length){
					sub3=m;
					tmp2=m;
					//找到单词
					while(m<tmpp.length&&checkCharArray(blankchar,tmpp[m])==false){
						m++;
						tmp2++;
					}
					if(tmp2==sub3)
						break;
					char[] typechar2=new char[tmp2-sub3];
					
					for(int n=0;n<typechar2.length;n++){
						typechar2[n]=tmpp[sub3];
						sub3++;
					}
					//生成字符串
					String typestring2=new String(typechar2);
					if(flag==0){
						if(checkKeyword(typestring2)==false){
							return -1;
						}
						flag=1;
						ts2=typestring2;
					}
					else if(flag==1){
						if(typestring2.equals("init")==false)
							return -1;
						flag=2;
					}
					else{
						constantvalue=typestring2;
					}
					//为下一轮循环做准备
					m=skipBlank(tmpp,m);
					if(m==-1)
						break;
				}
				//检查程序中的信号名称是否重复
				if(sp.checkWordDuplicate(ts2)==true){
					return  -1;
				}
				//添加参数
				if(type.equals("para")){
					//Parameter para=new Parameter(ts2,typestring,constantvalue);
					Signal spa=new Signal(ts2,typestring,"parameter",constantvalue,-1);
					sp.addSignal(spa);
					//sp.addParameter(para);
					sp.addWord(ts2);
				}
				//添加信号
				else if(type.equals("input")||type.equals("output")||type.equals("intermediate")){
					Signal s=new Signal(ts2,typestring,type,constantvalue,mark);
					sp.addSignal(s);
					sp.addWord(ts2);
					mark++;
				}
				else{
					return -1;
				}
			}
		}

		
		return 0;
	}
	//提取程序io信号
	public int checkIOSignals(char[] program, int sub){
		int sub2=skipBlank(program,sub);
		if(sub2==-1 || sub2>=program.length)
			return -1;
		//获取( )之间的字符数组
		if(program[sub2]!='(')
			return -1;
		int tmp=sub2+1;
		while(sub2<program.length&&program[sub2]!=')'){
			sub2++;
		}
		if(sub2==tmp||sub2>=program.length){
			return -1;
		}
		char[] iochar=new char[sub2-tmp];
		for(int i=0;i<iochar.length;i++){
			iochar[i]=program[tmp];
			tmp++;
		}
		//调用splitio
		if(splitio(iochar)==-1)
			return -1;		
		return sub2+1;
	}
	//提取程序io信号
	public int splitio(char[] program){
		int sub=skipBlank(program,0);
		//tmp为？在program字符数组的下标+1
		if(sub==-1 || sub>=program.length||program[sub]!='?')
			return -1;
		int tmp=sub+1;
		//sub为！在program字符数组的下标
		while(sub<program.length&&program[sub]!='!')
			sub++;
		if(sub>=program.length)
			return-1;
		//输入信号字符数组inputchar
		if(tmp==sub)
			return -1;
		char[] inputchar=new char[sub-tmp];
		for(int i=0;i<inputchar.length;i++){
			inputchar[i]=program[tmp];
			tmp++;
		}
		//输出信号字符数组outputchar
		if(sub>=program.length-1)
			return -1;
		char[] outputchar=new char[program.length-1-sub];
		for(int i=0;i<outputchar.length;i++){
			outputchar[i]=program[sub+1];
			sub++;
		}
		if(splitVariable(inputchar,"input")==-1){
			return -1;
		}
		if(splitVariable(outputchar,"output")==-1){
			return -1;
		}
		return 0;
	}
	//检查变量值是否和signal关键字重复
	public boolean checkSignalKeyword(){
		if(sp.wordList!=null){
			for(String s : sp.getWordList()){
				if(checkStringArray(keywords,s)==true)
					return true;
			}
		}
		return false;
	}
	public void eliminateEquationBlank(){
		if(sp.equationList.size()>0){
			ArrayList<String> tas=new ArrayList<String>();
			for(String s: sp.getequationList()){
				char[] tmpc=s.toCharArray();
				int subpre=skipBlank(tmpc,0);
				int subback=reverseSkipBlank(tmpc,tmpc.length-1);
				char[] ntmpc=new char[subback-subpre+1];
				for(int i=0;i<ntmpc.length;i++){
					ntmpc[i]=tmpc[subpre];
					subpre++;
				}
				String ss=new String(ntmpc);
				tas.add(ss);
				
			}
			sp.getequationList().clear();
			for(String s: tas){
				sp.addEquation(s);
			}
		}
	}
	
}
