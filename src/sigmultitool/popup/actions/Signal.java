package sigmultitool.popup.actions;
//信号包括，输入，输出，中间以及常值信号
public class Signal {
    String name;//当为constant信号时，name为常数值本身
    String typeofSignal;//当为constant信号时，typeofsignal为"integer","real","boolean"
    String ioi;//various from "input", "output", "intermediate","constant","parameter"
    String initialvalue;//当为constant信号时，initialvalue为常数值本身
    Boolean ifMemory=null;//true: left-hand side value of operator '$'
    int no;//signal编号，当为constant或parameter时，为-1，否则在词法分析lexical中累加
    public Signal(String n, String t, String i, String iv,int nu){
    	name=n;
    	typeofSignal=t;
    	ioi=i;
    	initialvalue=iv;
    	no=nu;
    }
    public void setNo(int n){
    	no=n;
    }
    public int getNo(){
    	return no;
    }
    public void setName(String n){
    	name=n;
    }
    public String getName(){
    	return name;
    }
    public void setTypeofSignal(String n){
    	typeofSignal=n;
    }
    public String getTypeofSignal(){
    	return typeofSignal;
    }
    public String getInitialvalue(){
    	return initialvalue;
    }
    public void setInitialvalue(String n){
    	initialvalue=n;
    }
    public void setIoI(String n){
    	ioi=n;
    }    
    public String getIoI(){
    	return ioi;
    }
    public void setIfMemory(Boolean n){
    	ifMemory=n;
    }    
    public Boolean getIfMemory(){
    	return ifMemory;
    }
}
