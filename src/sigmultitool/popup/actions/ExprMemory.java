package sigmultitool.popup.actions;

public class ExprMemory extends Expr{
    Signal term1;
    Signal term2;
  //  String returntype;//返回值类型
	public ExprMemory(String s, String et, Signal t1, Signal t2,String rt){
		super(s,et,rt);
		term1=t1;
		term2=t2;
	}
    public void print(){
    	super.print();
    	System.out.println("term1:"+term1.getName());
    	if(term2!=null)
    		System.out.println("term2:"+term2.getName());
    	//System.out.println("returntype:"+returntype);
 
    }
}
