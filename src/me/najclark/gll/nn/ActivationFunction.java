package me.najclark.gll.nn;

import java.io.Serializable;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class ActivationFunction implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3219002195413323958L;
	public static ActivationFunction sigmoid = new ActivationFunction("1/(1+Math.pow(Math.E, -(x)))"); //(0,1)
	public static ActivationFunction tanh = new ActivationFunction("2/(1+Math.pow(2.71828, -2*(x)))-1"); //(-1,1)
	public static ActivationFunction sinusoid = new ActivationFunction("Math.sin(x)"); //[-1,1]
	public static ActivationFunction gaussian = new ActivationFunction("Math.pow(Math.E, Math.pow(-x, 2))"); //(0,1]
	public static ActivationFunction softsign = new ActivationFunction("x/(1+Math.abs(x))"); //(-1,1)
	public static ActivationFunction binary = new ActivationFunction("if(x<0)0;else 1;"); //{0,1}
	public static ActivationFunction linear = new ActivationFunction("x");
	
	private ScriptEngineManager mgr;
	private ScriptEngine engine;
	private String equation = "x";
	
	public ActivationFunction(String equation){
		mgr = new ScriptEngineManager();
	    engine = mgr.getEngineByName("JavaScript");
		this.equation = equation;
	}
	
	public double getOutput(double x){
	    try {
			equation = equation.replaceAll("x", String.valueOf(x));
			return Double.valueOf(String.valueOf(engine.eval(equation)));
		} catch (ScriptException e) {
			e.printStackTrace();
		}
	    return 0;
	}
	
}
