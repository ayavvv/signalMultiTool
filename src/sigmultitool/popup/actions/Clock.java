package sigmultitool.popup.actions;
import java.util.*;
import java.io.*;
public class Clock {
    Signal sig;
    boolean bvalue;
    int id=-1;
    ClockEquivalenceClass clockclass=null;
    public Clock(Signal s,boolean b){
    	sig=s;
    	bvalue=b;
    }
}
