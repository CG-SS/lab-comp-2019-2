/*
 * Cristiano Guilherme - 609803
 * Daniel Davoli       - 610372
 */

package ast;

import lexer.Token.Symbol;

public class LowOperator extends ASTElement {
	
	private final Symbol symbol;

	public LowOperator(Symbol symbol) {
		this.symbol = symbol;
	}

	@Override
	public void genJava(PW pw) {
		pw.print(symbol.toString());
	}

	public Symbol getSymbol() {
		return symbol;
	}

}
