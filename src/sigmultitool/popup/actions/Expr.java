package sigmultitool.popup.actions;
import java.io.*;
import java.util.*;
public class Expr {
    String exprs;//���ʽ�ַ�����ʾ
    String exprtype;//���ʽ���ͣ�"Arith","Memo","Boolean","Default,"When","Input"
    ArrayList<Signal> signalList;
    String returntype;//���ʽ����ֵ
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
