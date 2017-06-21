package sigmultitool.popup.actions;
import java.io.*;
import java.util.*;
public class Function {
	String funcName; //name of the function
	String returnType; //type of the return value: boolean, int, double
	ArrayList<String> parameters; //list of parameters;
	ArrayList<String> localVariables;//list of local variables;
	ArrayList<String> statements; //list of statements;
	public Function(String name,String rt,ArrayList<String> para,ArrayList<String> lv,ArrayList<String> st ){
		funcName=name;
		returnType=rt;
		parameters=para;
		localVariables=lv;
		statements=st;
	}
	public void printFunc(PrintStream p){
		if(p!=null){
			p.print(returnType+" ");
			p.print(funcName+"(");
			int mark=0;
			for(String s: parameters){
				if(mark==0){
					p.print(s);
					mark=1;
				}
				else{
					p.print(","+s);
				}
			}
			p.print("){\n");
			for(String s:statements){
				p.print("\t");
				p.println(s);
			}
			p.print("}\n");
		}
	}
}
