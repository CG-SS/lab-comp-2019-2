/*
 * Cristiano Guilherme - 609803
 * Daniel Davoli       - 610372
 */

package ast;

import java.util.List;

public class PrimaryExprSelfIdColon extends PrimaryExpr {

	public PrimaryExprSelfIdColon(String idColonName, List<Expression> exprList, Type type) {
		// TODO Auto-generated constructor stub
		super(type);
	}

	@Override
	public void genJava(PW pw) {
		// TODO Auto-generated method stub

	}

}
