package sigmultitool.popup.actions;

import java.util.*;
public class SignalProgram {
    String name;
    ArrayList<Signal> signalList;
    ArrayList<String> equationList;//方程字符串
    ArrayList<String> wordList;//程序中的关键字字符串，包括程序名，所有信号名
  //  ArrayList<Parameter> parameterList;//程序中的参数列表
    ArrayList<Equation> equations;
    public void printList(){
    	if(signalList!=null){
    		for(Signal s : signalList){
    			System.out.print(s.getName());
    			System.out.print("+"+s.getIoI());
    			if(s.getTypeofSignal()!=null){
    				System.out.println("+"+s.getTypeofSignal());
    			}
    			if(s.getInitialvalue()!=null){
    				System.out.println("+"+s.getInitialvalue());
    			}
    		}
    	}
    	if(equationList!=null){
    		for(String s : equationList){
    			System.out.println(s);
    		}
    	}
    	if(wordList!=null){
    		for(String s : wordList){
    			System.out.println(s);
    		}
    	}
    	/*if(parameterList!=null){
    		for(Parameter s : parameterList){
    			System.out.print(s.getName());
    			if(s.getTypeofParameter()!=null){
    				System.out.print("+"+s.getTypeofParameter());
    			}
    			if(s.getInitialValue()!=null)
    				System.out.println("+"+s.getInitialValue());
    		}
    	}*/
    	if(equations!=null){
    		for(Equation e: equations){
    			e.print();
    		    
    		}
    	}
    }
    public SignalProgram(String n){
    	name=n;
    	signalList=new ArrayList<Signal>();
    	equationList=new ArrayList<String>();
    	wordList=new ArrayList<String>();
    	equations=new ArrayList<Equation>();
    	addWord(n);
    	//parameterList=new ArrayList<Parameter>();
    }
    public String getName(){
    	return name;
    }
    public ArrayList<Signal> getSignalList(){
    	return signalList;
    }
    public void addSignal(Signal s){
    	addElement(signalList,s);
    	//if(signalList!=null){
    	//	signalList.add(s);
    	//}else
    	//{
    	//	signalList=new ArrayList<Signal>();
    	//	signalList.add(s);
    	//}
    }
    public ArrayList<String> getequationList(){
    	return equationList;
    }
    public void addEquation(String s){
    	addElement(equationList,s);
//    	if(equationList!=null){
//    		equationList.add(s);
//    	}else
//    	{
//    		equationList=new ArrayList<String>();
//    		equationList.add(s);
//    	}
    }
    public ArrayList<String> getWordList(){
    	return wordList;
    }
    public void addWord(String s){
    	addElement(wordList,s);
    }
   /* public ArrayList<Parameter> getParameterList(){
    	return parameterList;
    }
    public void addParameter(Parameter s){
    	addElement(parameterList,s);
    }*/
    public void test(String s){
    	addElement(equationList,s);
    }
    public void addElement(ArrayList al, Object o){
    	if(al!=null){
    		al.add(o);
    	}
    	else{
    		al=new ArrayList();
    		al.add(o);
    	}
    }
    public boolean checkWordDuplicate(String s){
    	if(wordList.contains(s)==true)
    		return true;
    	return false;
    }
    public void addEq(Equation e){
    	if(equations!=null)
    		equations.add(e);
    }
    public ArrayList<Equation> getEq(){
    	return equations;
    }
}
