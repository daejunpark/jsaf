/*******************************************************************************
    Copyright (c) 2012-2013, KAIST.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ******************************************************************************/

package kr.ac.kaist.jsaf.concolic;

import java.util.StringTokenizer;
import java.util.LinkedList;
import java.util.TreeMap;

public class Yices {
	private native String yicesSolveLinear (String constraint);

	static {
		System.loadLibrary("yicesSafe");
	}

        public void testYices(String constraint) {
	    System.out.println(yicesSolveLinear(constraint));
	}

	public String solveArithmeticConstraint (String constraint)
	{
		String solution;

		if (constraint.equals(""))
			solution = "empty";
		else
			solution = yicesSolveLinear(constraint);
		
		solution = parseSolution(solution);
		
		return solution;
	}
	
	private String parseSolution (String string) 
	{
		if (string.equals("unsat") || string.equals("empty"))
			return string;

		StringTokenizer tokenizer = null;
		TreeMap<Integer, String> inputValues = null;
		String result = "";
		int i = 1;

		tokenizer = new StringTokenizer(string);
		inputValues = new TreeMap<Integer, String>();

		while (tokenizer.hasMoreTokens()) 
		{
			String input = tokenizer.nextToken();
			String value = tokenizer.nextToken();

			if (input.contains("i")) 
			{
				input = input.substring(input.indexOf("i") + 1);
				inputValues.put(Integer.valueOf(input), value);
			}
		}

		while (!inputValues.isEmpty()) 
		{
			int input = inputValues.firstKey();
			String value = inputValues.remove(input);

			if (input < i) 
			{
				System.err.println("ERROR: Yices.parseSolution functioned incorrectly or received a non-valid input");
				System.exit(-1);
			}

			while (input > i) 
			{
				result += 0 + " ";
				i++;
			}

			result += value + " ";
			i++;
		}
		return result;
  	}
}
