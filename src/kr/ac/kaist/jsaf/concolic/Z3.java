/*******************************************************************************
    Copyright (c) 2012-2013, KAIST, S-Core.
    All rights reserved.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.
 ******************************************************************************/

package kr.ac.kaist.jsaf.concolic;

import java.util.*;
import edu.rice.cs.plt.tuple.Option;
import com.microsoft.z3.*;

import kr.ac.kaist.jsaf.scala_src.useful.Options.*;

public final class Z3 {
    @SuppressWarnings("serial")
    class TestFailedException extends Exception
    {
        public TestFailedException()
        {
            super("Check FAILED");
        }
    };
	
	public List<Integer> ConstraintSolver(Context ctx, List<ConstraintForm> conslist, Integer inum) throws Z3Exception, TestFailedException 
	{
		System.out.println("ConstraintSolver");

		Map<String, IntExpr> exprMap = new HashMap<String, IntExpr>();
		Solver solver = ctx.mkSolver();
		while (!conslist.isEmpty()) {
			ConstraintForm constraint = conslist.remove(0);
			if (constraint.getOp().isSome()) {
				String op = constraint.getOp().unwrap();
				String lhs = constraint.getLhs();
				exprMap.put(lhs, ctx.mkIntConst(lhs));
				if (constraint.getRhs().isSome()) {
					ConstraintForm c = constraint.getRhs().unwrap();
					String rhs = c.getLhs();
					if (rhs.contains("s") || rhs.contains("i"))
						exprMap.put(rhs, ctx.mkIntConst(rhs));
					else
						exprMap.put(rhs, ctx.mkInt(Integer.parseInt(rhs)));
					
					switch (op.charAt(0)) {
						case '=' :
							if (op.length() > 1 && op.charAt(1) == '=') 
								solver.assert_(ctx.mkEq(exprMap.get(lhs), exprMap.get(rhs)));
							else {
								if (c.getOp().isSome()) {
									if (c.getRhs().isSome()) {
										String v = c.getRhs().unwrap().getLhs();	
										if (v.contains("s") || v.contains("i"))
											exprMap.put(v, ctx.mkIntConst(v));
										else
											exprMap.put(v, ctx.mkInt(Integer.parseInt(v)));
										
										switch (c.getOp().unwrap().charAt(0)) {
											case '+':
												solver.assert_(ctx.mkEq(exprMap.get(lhs), ctx.mkAdd(new ArithExpr[] { exprMap.get(rhs), exprMap.get(v)})));
												break;
											case '-':	
												solver.assert_(ctx.mkEq(exprMap.get(lhs), ctx.mkSub(new ArithExpr[] { exprMap.get(rhs), exprMap.get(v)})));
												break;
											case '*':
												solver.assert_(ctx.mkEq(exprMap.get(lhs), ctx.mkMul(new ArithExpr[] { exprMap.get(rhs), exprMap.get(v)})));
												break;
											case '/':
												solver.assert_(ctx.mkEq(exprMap.get(lhs), ctx.mkDiv((ArithExpr) exprMap.get(rhs), (ArithExpr) exprMap.get(v))));
												break;
											default:
												System.out.println("Not yet supported");
												throw new TestFailedException();
										}
									}
									else {
										System.out.println("Wrong constraint form" + c);
										throw new TestFailedException();
									}
								}
								else
									solver.assert_(ctx.mkEq(exprMap.get(lhs), exprMap.get(rhs)));
								
							}
							break;
						case '<':
							if (op.length() > 1 && op.charAt(1) == '=')
								solver.assert_(ctx.mkLe(exprMap.get(lhs), exprMap.get(rhs)));
							else 
								solver.assert_(ctx.mkLt(exprMap.get(lhs), exprMap.get(rhs)));
							break;
						case '>':
							if (op.length() > 1 && op.charAt(1) == '=')
								solver.assert_(ctx.mkGe(exprMap.get(lhs), exprMap.get(rhs)));
							else 
								solver.assert_(ctx.mkGt(exprMap.get(lhs), exprMap.get(rhs)));
							break;
						case '!':
							if (op.length() > 1 && op.charAt(1) == '=')
								solver.assert_(ctx.mkDistinct(new Expr[] { exprMap.get(lhs), exprMap.get(rhs)}));
							else {
							   	System.out.println("Wrong constraint form" + op);   	   
								throw new TestFailedException();
							}
							break;
						default:
							System.out.println("Not yet supported");
							throw new TestFailedException();
					}		
				}
				else { 	
					System.out.println("Wrong constraint form" + constraint);
					throw new TestFailedException();
				}
			}
			else { 	
				System.out.println("Wrong constraint form" + constraint);
				throw new TestFailedException();
			}
		}
		
		Model model = null;
		if (Status.SATISFIABLE == solver.check()) {
			model = solver.getModel();
			System.out.println("Solver = " + solver);
			System.out.println("Model = " + model);
			List<Integer> result = new ArrayList<Integer>(); 
			for (int i=0; i<inum; i++) {
				result.add(Integer.parseInt(model.getConstInterp(exprMap.get("i"+i)).toString()));
			}
			return result;			
		}
		else {
			System.out.println("BUG, the constraints are satisfiable.");
			throw new TestFailedException();
		}
	}	

	public Option<List<Integer>> solve(List<ConstraintForm> constraints, Integer inum) {
		try {
			HashMap<String, String> cfg = new HashMap<String, String>();
			cfg.put("model", "true");
			Context ctx = new Context(cfg);
			return Option.<List<Integer>>some(this.ConstraintSolver(ctx, constraints, inum));
		} catch (Z3Exception ex) {
            System.out.println("TEST CASE FAILED: " + ex.getMessage());
            System.out.println("Stack trace: ");
            ex.printStackTrace(System.out);
			return Option.<List<Integer>>none();
        } catch (Exception ex) {
            System.out.println("Unknown Exception: " + ex.getMessage());
            System.out.println("Stack trace: ");
            ex.printStackTrace(System.out);
			return Option.<List<Integer>>none();
        }
	}
}
