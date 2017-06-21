package sigmultitool.popup.actions;
import java.io.*;
import java.util.*;
public class DefaultExpr extends Expr{
    Signal term1;
    Signal term2;
  //  String returntype;//返回值类型

	public DefaultExpr(String es, String et, Signal t1, Signal t2,String rt){
		super(es,et,rt);
		term1=t1;
		term2=t2;
		//type=ty;
	}
    public void print(){
    	super.print();
    	System.out.println("term1:"+term1.getName());
    	if(term2!=null)
    		System.out.println("term2:"+term2.getName());
    	//System.out.println("returntype:"+returntype);
    	//System.out.println("type:"+type);
    }
    public boolean checkType(){
    	String t1=term1.getTypeofSignal();
    	String t2=term2.getTypeofSignal();
    	boolean tb1=(t1.equals("boolean")||t1.equals("event"));
    	boolean tb2=(t2.equals("boolean")||t2.equals("event"));
    	if(tb1==tb2)
    		return true;
    	return false;
    }
}
