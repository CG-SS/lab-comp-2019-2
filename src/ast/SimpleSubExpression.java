/*
 * Cristiano Guilherme - 609803
 * Daniel Davoli       - 610372
 */

package ast;

import java.util.List;

public class SimpleSubExpression extends SumSubExpression {
	
	private final Term term;
	private final List<LowOperator> opList;
	private final List<Term> termList;

	public SimpleSubExpression(Term term, List<LowOperator> opList, List<Term> termList, Type type) {
		super(type);
		this.term = term;
		this.opList = opList;
		this.termList = termList;
	}

	@Override
	public void genJava(PW pw) {
		term.genJava(pw);
		for(int i = 0; i < opList.size(); i++) {
			pw.print(" ");
			opList.get(i).genJava(pw);
			pw.print(" ");
			termList.get(i).genJava(pw);
		}
	}

}
