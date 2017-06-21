package sigmultitool.popup.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.*;

public class MultiCode implements IObjectActionDelegate {
	org.eclipse.core.resources.IFile iFile;
	static String  programStr="";
	static char[] cc;
	String path2="";
	/**
	 * Constructor for Action1.
	 */
	public MultiCode() {
		super();
	}

	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		
		Shell shell = new Shell();
		String path="";
		//String path2="";
		programStr="";
		while(true){
			try{
				
				path=iFile.getLocation().toFile().getAbsolutePath();
				String fileName=iFile.getName();
				int io=path.lastIndexOf('.');
				path2=path.substring(0,io);
				readLineFile(path,"");
			}catch(Exception e){
				e.printStackTrace();
			}
			Lexical smt=new Lexical();
	        cc=programStr.toCharArray();
	        //词法分析，分离出signal语言的名称，参数，输入输出信号，中间信号以及方程
	        boolean smts=smt.lexcialAnalysis(cc);
	        if(smts==false){
	        	//System.out.println(String.valueOf(smts));
	        	//System.out.println("syntax error");
	    		MessageDialog.openInformation(
	    				shell,
	    				"Multicore Code Generation",
	    				"compile error");
	        	break;
	        	
	        }
			SyntaxAnalysis sa=new SyntaxAnalysis(smt);
	        
	        boolean syntaxError=sa.splitEquation();
	        if(syntaxError==false){
	        	System.out.println("syntax error");
	    		MessageDialog.openInformation(
	    				shell,
	    				"Multicore Code Generation",
	    				"compile error"+sa.errorInfo);
	        	break;
	        }
	        //System.out.println("-------------equations---------------");
	        //for(Equation eq: sa.lp.sp.equations){
	        //	eq.print();
	        //	System.out.println();
	        //}
	        //System.out.println("-------------equations---------------");
	        //建立SFDG并检测是否有数据循环依赖
	        SFDG sfdginstant=new SFDG(smt.sp.getEq());
	        sfdginstant.buildGraph();
	        boolean noncycle=sfdginstant.checkAndMakeTask();
	        
	        sfdginstant.printGraph();
	        sfdginstant.printTask();
	        if(noncycle==false){
	        	System.out.println("cycle detected");
	    		MessageDialog.openInformation(
	    				shell,
	    				"Multicore Code Generation",
	    				"cycle detected");
	    		printSFDG(sfdginstant);
	        	break;
	        	//System.exit(1);
	        }
	        ClockAnalysis ca=new ClockAnalysis(smt.sp.equations,sfdginstant);
	        ca.dataflowToClock();
	        boolean clkConsistency=ca.clockToNF();
	        if(clkConsistency==false){
	        	System.out.println("clock is not consistent");
	    		MessageDialog.openInformation(
	    				shell,
	    				"Multicore Code Generation",
	    				"the clock is not consistent");
	    		printUNFS(ca);
	    		break;
	        }
	       // System.out.println(ca.clockToNF());
	        ca.calculatedIDForNFS();
	        boolean singleroot=ca.mergeClock();
	        if(singleroot==false){
	        	System.out.println("not endochrony");
	    		MessageDialog.openInformation(
	    				shell,
	    				"Multicore Code Generation",
	    				"the program is not endochrony");
	    		printUndefinedClk(ca);
	    		break;
	        }
	        ca.getRootClock();
	        ca.reduction();
	        ca.attachEquation();
	        ca.sortEquation();
	        ca.makeHiers();
	        CombinedSFDG cs=new CombinedSFDG(sfdginstant,ca);
	        cs.addClock();
	        cs.print();
	        //SeqCodeGen sc=new SeqCodeGen(ca.tree,smt.sp.signalList,smt.sp.wordList,smt.sp,path2);
	        //sc.generateSeq();
	       // sc.printCode();
	        MultiCodeGen mc=new MultiCodeGen(ca.tree,smt.sp.signalList,smt.sp.wordList,smt.sp,cs,path2);
	        mc.generateMulti();
	        mc.printCode();
	        printClockCalculus(ca);
	        printCombinedSFDG(cs);
	        refresh();
			MessageDialog.openInformation(
					shell,
					"Multicore Code Generation",
					path2+"_multi.c");
			break;
		}
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
	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		TreeSelection treeSelection = (TreeSelection) selection;
		iFile = (IFile) treeSelection.getFirstElement();
	}
	public void refresh(){
		IWorkspace workspace = ResourcesPlugin.getWorkspace(); 
		IProject[] projects = workspace.getRoot().getProjects(); 
		   for (IProject project : projects) { 
		     try { 
		       project.refreshLocal(IResource.DEPTH_INFINITE, null); 
		     } catch (CoreException e1) { 
		     }
		} 
	}
	public void printSFDG(SFDG g){
		 String name=path2+"_err.txt";
		 try{
		    	FileOutputStream out=new FileOutputStream(name);
	            PrintStream p=new PrintStream(out);
	            g.printGraphFile(p,g.duplicatedGraph);
	            
		 }catch(Exception e){
			 e.printStackTrace();
		 }
	}
	public void printUNFS(ClockAnalysis ca){
		String name=path2+"_err.txt";
		 try{
		    	FileOutputStream out=new FileOutputStream(name);
	            PrintStream p=new PrintStream(out);
	    		for(ClockEquation ce:ca.UNFS){
	    			ce.eq.printFile(p);
	    			ce.printFile(p);
	    		};
	            
		 }catch(Exception e){
			 e.printStackTrace();
		 }

	}
	public void printUndefinedClk(ClockAnalysis ca){
		String name=path2+"_err.txt";
		 try{
		    	FileOutputStream out=new FileOutputStream(name);
	            PrintStream p=new PrintStream(out);
	            p.println("undefined clocks:");
	    		for(Clock c:ca.undefinedClock){
	    			if(c.sig!=null)
	    				p.println(c.sig.name);
	    		}
	            
		 }catch(Exception e){
			 e.printStackTrace();
		 }
	}
	public void printClockCalculus(ClockAnalysis ca){
		String name=path2+"_intermediate.txt";
		 try{
		    	FileOutputStream out=new FileOutputStream(name);
	            PrintStream p=new PrintStream(out);
	            p.println("NFS:");
	            for(ClockEquation ce:ca.NFS){
	            	p.println("---------");
	            	if(ce.eq!=null){
	            		p.println("Equation:");
	            		ce.eq.printFile(p);
	            	}
	            	p.println("NF:");
	            	ce.printFile(p);
	            	p.println();
	            }
	            //print clock equivalence classes
	            p.println("\nClock Equivalence Class:");
	            for(ClockEquivalenceClass cc:ca.EquivalenceClasses){
	               cc.printFile(p);
	            }
	            //print RNFS:
	            p.println("RNFS:");
	            for(ReducedNF rnf:ca.RNFS){
		            rnf.printFile(p);
		        }
	            p.println("Elist:");
	            for(AncestorEquation ae: ca.Elist){
	            	
	            		if(ae.getClass()==ReducedNF.class)
	            			((ReducedNF)(ae)).printFile(p);
	            		if(ae.getClass()==Assignment.class)
	            			((Assignment)(ae)).printFile(p);
	            	
	            }
	            
		 }catch(Exception e){
			 e.printStackTrace();
		 }
	}
	public void printCombinedSFDG(CombinedSFDG cs){
		String name=path2+"_paraTask.txt";
		 try{
		    	FileOutputStream out=new FileOutputStream(name);
	            PrintStream p=new PrintStream(out);
	    		for(ParallelTask pt: cs.paratask){
	    			pt.printFile(p);
	    		}
	            
		 }catch(Exception e){
			 e.printStackTrace();
		 }

	}
}
