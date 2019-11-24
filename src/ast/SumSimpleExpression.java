/*
 * Cristiano Guilherme - 609803
 * Daniel Davoli       - 610372
 */

package ast;

import java.util.List;

public class SumSimpleExpression extends SimpleExpression {
	
	private final SumSubExpression sumSubExpr;
	private final List<SumSubExpression> sumSubExpressionList;

	public SumSimpleExpression(SumSubExpression sumSubExpr, List<SumSubExpression> sumSubExpressionList, Type type) {
		super(type);
		
		this.sumSubExpr = sumSubExpr;
		this.sumSubExpressionList = sumSubExpressionList;
	}

	@Override
	public void genJava(PW pw) {
		sumSubExpr.genJava(pw);
		for(final SumSubExpression s : sumSubExpressionList) {
			pw.print(" + ");
			s.genJava(pw);
		}
	}

}
