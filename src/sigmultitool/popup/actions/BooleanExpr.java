package sigmultitool.popup.actions;
import java.io.*;
import java.util.*;
public class BooleanExpr extends Expr{
    Signal term1;
    Signal term2;
    //String returntype;//返回值类型
    String type;//"!","&&","||",">",">=","<","<=","==","!="
	public BooleanExpr(String es, String et, Signal t1, Signal t2, String ty,String rt){
		super(es,et,rt);
		term1=t1;
		term2=t2;
		type=ty;
		//returntype=rt;
	}
    public void print(){
    	super.print();
    	System.out.println("term1:"+term1.getName());
    	if(term2!=null)
    		System.out.println("term2:"+term2.getName());
    	//System.out.println("returntype:"+returntype);
    	System.out.println("type:"+type);
    }
}
