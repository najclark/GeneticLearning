package me.najclark.gll.nn;

import java.io.Serializable;
import java.math.BigDecimal;

import com.udojava.evalex.Expression;

public class ActivationFunction implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3219002195413323958L;
	public static ActivationFunction sigmoid = new ActivationFunction("1/(1+e^(-1*x))"); // (0,1)
	public static ActivationFunction tanh = new ActivationFunction("2/(1+e^(-2*x))-1"); // (-1,1)
	public static ActivationFunction sinusoid = new ActivationFunction("sin(x)"); // [-1,1]
	public static ActivationFunction softsign = new ActivationFunction("x/(1+abs(x))"); // (-1,1)
	public static ActivationFunction binary = new ActivationFunction("x>0"); // {0,1}
	public static ActivationFunction linear = new ActivationFunction("x");

	public String equation;

	public ActivationFunction(String equation) {
		this.equation = equation;
	}

	public double getOutput(double x) {
		//equation = equation.replaceAll("x", String.valueOf(x));
		return Double.valueOf(String.valueOf(new Expression(equation).with("x", new BigDecimal(x)).eval()));
	}
	
	@Override
	public String toString(){
		return equation;
	}

}