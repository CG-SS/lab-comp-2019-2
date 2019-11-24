/*
 * Cristiano Guilherme - 609803
 * Daniel Davoli       - 610372
 */

package ast;

import lexer.Token.Symbol;

public class Relation extends ASTElement {
	
	private final Symbol symbol;

	public Relation(Symbol symbol) {
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
