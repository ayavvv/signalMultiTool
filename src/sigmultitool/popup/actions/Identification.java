package sigmultitool.popup.actions;
import java.io.*;
import java.util.*;
import jdd.bdd.*;
public class Identification {
	BDD bdd;
	public Identification(){
		bdd= new BDD(1000,1000);
	}
	public int ID(ClockEquation ce){//calculate the id of the clock equation
		ClockExpr cl=ce.left;
		ClockExpr cr=ce.right;
		int id2=IDExpr(cr);
		if(cl.operator.equals("single")){
			cl.id=id2;
			cl.basicClock.id=id2;
			ce.id=id2;
			return id2;
		}
		return -1;
	}
	public boolean checkImplication(int i1,int i2){//check if i1->i2
		if(i1<0 || i2<0)
			return false;
//		if(i1==30&&i2==27){
//			bdd.print(i1);
//			bdd.print(i2);
//		}
		int i3=bdd.not(i1);
		//int i4=bdd.getHigh(27);
		//int i5=bdd.getLow(27);
		//bdd.print(i4);
		//bdd.print(i5);
		//bdd.print(i1);
		//bdd.print(i3);
		if(bdd.or(i3, i2)==1)// i1->i2 <=> not i1 || i2
		//if()
			return true;
		return false;
	}
	public int IDExpr(ClockExpr ce){
		
		ClockExpr clock1=ce.operand1;
		ClockExpr clock2=ce.operand2;
		int id=-1;
		if(ce.operator.equals("single")&&ce.basicClock.id<0){
		   id=createVar(ce.basicClock);
		   ce.id=id;
		}
		else if(ce.operator.equals("intersection")||ce.operator.equals("and")){
		   id=intersection(IDExpr(clock1),IDExpr(clock2));
		   ce.id=id;
		}
		else if(ce.operator.equals("union")||ce.operator.equals("or")){
		   id=union(IDExpr(clock1),IDExpr(clock2));
		   ce.id=id;
		}
		else if(ce.operator.equals("setminus")){
		   id=setminus(IDExpr(clock1),IDExpr(clock2));
		   ce.id=id;
		}
		else if(ce.operator.equals("not")){
		   id=not(IDExpr(clock1));
		   ce.id=id;
		}
		else if(ce.condition!=null&&ce.id<0){
			id=bdd.createVar();
			bdd.ref(id);
			ce.id=id;
		}
		else{
			id=ce.id;
		}
		return id;
	}
	public int createVar(Clock c1){
		if(c1.id>0)
			return c1.id;
		else{
			int i=bdd.createVar();
			bdd.ref(i);
			c1.id=i;
			return i;
		}
	}
	public int intersection(int i1,int i2){
		int i=bdd.and(i1, i2);
		bdd.ref(i);
		return i;
	}
//	public int intersection(Clock c1,Clock c2){
//		if(c1.id<0)
//			createVar(c1);
//		if(c2.id<0)
//			createVar(c2);
//		int i=bdd.and(c1.id, c2.id);
//		bdd.ref(i);
//		return i;
//	}
	//public int union(Clock c1,Clock c2){
	public int union(int i1,int i2){
//		if(c1.id<0)
//			createVar(c1);
//		if(c2.id<0)
//			createVar(c2);
		int i=bdd.or(i1, i2);
		bdd.ref(i);
		return i;
	}
	public int setminus(int i1,int i2){
	//public int setminus(Clock c1, Clock c2){
//		if(c1.id<0)
//			createVar(c1);
//		if(c2.id<0)
//			createVar(c2);
		int i2n=bdd.not(i2);
		bdd.ref(i2n);
		int i=bdd.and(i1,i2n); //c1 && not c2
		bdd.ref(i);
		return i;
	}
	public int not(int i1){
		//if(c1.id<0)
		//	createVar(c1);
		int i=bdd.not(i1);
		bdd.ref(i);
		return i;
	}
}
