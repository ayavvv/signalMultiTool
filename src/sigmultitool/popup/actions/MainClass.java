package sigmultitool.popup.actions;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;


public class MainClass {
	static String  programStr="";
	static char[] cc;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Lexical smt=new Lexical();

        try{
        //��ȡsignal���򣬴洢��programStr�У���ת��Ϊ�ַ�����cc
        readLineFile("","normal.sig");
        
        }
        catch(Exception e){
        	e.printStackTrace();
        }
        cc=programStr.toCharArray();
        //�ʷ������������signal���Ե����ƣ���������������źţ��м��ź��Լ�����
        boolean smts=smt.lexcialAnalysis(cc);
        if(smts==false){
        	//System.out.println(String.valueOf(smts));
        	//System.out.println("syntax error");
        	System.exit(1);
        	
        }
        
        //smt.sp.printList();
       //�﷨�����������������ͣ����������﷨�������������ֵ���ж��Ƿ����﷨����,Ŀǰ��������ظ�ֵ
        SyntaxAnalysis sa=new SyntaxAnalysis(smt);
        
        boolean syntaxError=sa.splitEquation();
        if(syntaxError==false){
        	System.out.println("syntax error");
        	System.exit(1);
        }
        System.out.println("-------------equations---------------");
        for(Equation eq: sa.lp.sp.equations){
        	eq.print();
        	System.out.println();
        }
        System.out.println("-------------equations---------------");
        //����SFDG������Ƿ�������ѭ������
        SFDG sfdginstant=new SFDG(smt.sp.getEq());
        sfdginstant.buildGraph();
        boolean noncycle=sfdginstant.checkAndMakeTask();
        
        sfdginstant.printGraph();
        sfdginstant.printTask();
        if(noncycle==false){
        	System.out.println("cycle detected");
        	//System.exit(1);
        }
        ClockAnalysis ca=new ClockAnalysis(smt.sp.getEq(),sfdginstant);
        ca.dataflowToClock();
        System.out.println(ca.clockToNF());
        ca.calculatedIDForNFS();
        ca.mergeClock();
        ca.getRootClock();
        ca.reduction();
        ca.attachEquation();
        ca.sortEquation();
        ca.makeHiers();
        CombinedSFDG cs=new CombinedSFDG(sfdginstant,ca);
        cs.addClock();
        cs.print();
        SeqCodeGen sc=new SeqCodeGen(ca.tree,smt.sp.signalList,smt.sp.wordList,smt.sp,"");
        sc.generateSeq();
        sc.printCode();
        MultiCodeGen mc=new MultiCodeGen(ca.tree,smt.sp.signalList,smt.sp.wordList,smt.sp,cs,"");
        mc.generateMulti();
        mc.printCode();
        
        //System.out.println(cores);
        //sfdginstant.printTask();
       // smt.sp.printList();
        //�������̵�ʱ�ӣ�ȷ�������Ƿ�����endochrony����
        //ClockAnalysis ca=new ClockAnalysis(sa);
       // ca.EquivalenceAnaylsis();
        //ca.printEquivalenceClass();
        
        
	}
	public static void readLineFile(String filePath,String fileName) throws IOException 
	{ 
	FileReader fr = new FileReader(filePath+fileName); 
	BufferedReader br = new BufferedReader(fr); 
	String line = br.readLine(); 
	while(line != null) 
	{ 
		//System.out.println(String.valueOf(line.length()));
		programStr=programStr+line;//System.out.println(line); 
		//System.out.println(programStr);
		line = br.readLine(); 
		
		
	} 
	br.close(); 
	fr.close(); 
	} 

}
