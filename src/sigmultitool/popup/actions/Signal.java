package sigmultitool.popup.actions;
//�źŰ��������룬������м��Լ���ֵ�ź�
public class Signal {
    String name;//��Ϊconstant�ź�ʱ��nameΪ����ֵ����
    String typeofSignal;//��Ϊconstant�ź�ʱ��typeofsignalΪ"integer","real","boolean"
    String ioi;//various from "input", "output", "intermediate","constant","parameter"
    String initialvalue;//��Ϊconstant�ź�ʱ��initialvalueΪ����ֵ����
    Boolean ifMemory=null;//true: left-hand side value of operator '$'
    int no;//signal��ţ���Ϊconstant��parameterʱ��Ϊ-1�������ڴʷ�����lexical���ۼ�
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
