/*
 * Cristiano Guilherme - 609803
 * Daniel Davoli       - 610372
 */

package lexer;

public class Token {

	public enum Symbol {
		AND("&&"),
	    ANNOT("~annotation"),
	    ASSERT("assert"),
	    ASSIGN("="),
	    BOOLEAN("Boolean"),
	    BREAK("break"),
	    CLASS("class"),
	    COMMA(","),
	    DIV("/"),
	    DOT("."),
	    ELSE("else"),
	    END("end"),
	    EOF("~eof"),
	    EQ("=="),
	    EXTENDS("extends"),
	    ERROR("~error"),
	    FALSE("false"),
	    FINAL("final"),
	    FUNC("func"),
	    GE(">="),
	    GT(">"),
	    ID("~ident"),
	    IDCOLON("~ident:"),
	    IF("if"),
	    INT("Int"),
	    INIT("~init"),
	    LE("<="),
	    LEFTCURBRACKET("{"),
	    LEFTPAR("("),
	    LITERALINT("~number"),
	    LITERALSTRING("~literalString"),
	    LT("<"),
	    MINUS("-"),
	    MINUS_GT("->"),
	    MULT("*"),
	    NEQ("!="),
	    NIL("nil"),
	    NOT("!"),
	    OR("||"),
	    OVERRIDE("override"),
	    PLUS("+"),
	    PLUSPLUS("++"),
	    PRIVATE("private"),
	    PUBLIC("public"),
	    REPEAT("repeat"),
	    RETURN("return"),
	    RIGHTCURBRACKET("}"),
	    RIGHTPAR(")"),
	    SELF("self"),
	    SHARED("shared"),
	    SEMICOLON(";"),
	    STRING("String"),
	    SUPER("super"),
	    TRUE("true"),
	    UNTIL("until"),
	    VAR("var"),
	    WHILE("while");

		private final String name;

		Symbol(final String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}

	}

	public Token(final Symbol symbol, final Integer line, final String value) {
		this.name = symbol.name;
		this.value = value;
		this.line = line;
		this.symbol = symbol;
	}

	@Override
	public String toString() {
		return name;
	}

	private final String name;
	private final String value;
	private final Integer line;
	private final Symbol symbol;

	public Symbol getSymbol() {
		return symbol;
	}

	public Integer getLine() {
		return line;
	}

	public String getValue() {
		return value;
	}
}