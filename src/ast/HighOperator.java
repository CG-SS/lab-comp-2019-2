/*
 * Cristiano Guilherme - 609803
 * Daniel Davoli       - 610372
 */

package ast;

import lexer.Token.Symbol;

public class HighOperator extends ASTElement {

	private final Symbol symbol;
	
	public HighOperator(final Symbol symbol) {
		// TODO Auto-generated constructor stub
		this.symbol = symbol;
	}

	@Override
	public void genJava(PW pw) {
		// TODO Auto-generated method stub

	}
	
	public Symbol getSymbol() {
		return symbol;
	}

}
