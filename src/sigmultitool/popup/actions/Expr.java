package sigmultitool.popup.actions;
import java.io.*;
import java.util.*;
public class Expr {
    String exprs;//表达式字符串表示
    String exprtype;//表达式类型，"Arith","Memo","Boolean","Default,"When","Input"
    ArrayList<Signal> signalList;
    String returntype;//表达式返回值
    public Expr(String e,String et,String ty){
    	exprs=e;
    	exprtype=et;
    	returntype=ty;
    	signalList=new ArrayList<Signal>();
    }
    public ArrayList<Signal> getSignalList(){
    	return signalList;
    }
    public void addSignal(Signal s){
    	if(signalList!=null){
    		signalList.add(s);
    	}
    }
    public void print(){
    	System.out.println("ExprType:"+exprtype);
    	System.out.println("returnType:"+returntype);
    }
    
}
