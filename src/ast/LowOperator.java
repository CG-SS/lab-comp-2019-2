/*
 * Cristiano Guilherme - 609803
 * Daniel Davoli       - 610372
 */

package ast;

import lexer.Token.Symbol;

public class LowOperator extends ASTElement {
	
	private final Symbol symbol;

	public LowOperator(Symbol symbol) {
		// TODO Auto-generated constructor stub
		this.symbol = symbol;
	}

	@Override
	public void genJava(PW pw) {
		// TODO Auto-generated method stub

	}

	public Symbol getSymbol() {
		// TODO Auto-generated method stub
		return symbol;
	}

}
