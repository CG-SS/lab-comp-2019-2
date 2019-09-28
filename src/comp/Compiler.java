
package comp;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import ast.Annot;
import ast.AnnotParam;
import ast.AssignExpr;
import ast.Expression;
import ast.LiteralInt;
import ast.MetaobjectAnnotation;
import ast.Program;
import ast.Statement;
import ast.TypeCianetoClass;
import lexer.Lexer;
import lexer.Token;
import lexer.Token.Symbol;

public class Compiler {

	public Compiler() { }

	// compile must receive an input with an character less than
	// p_input.lenght
	public Program compile(final char[] input, final PrintWriter outError) {

		ArrayList<CompilationError> compilationErrorList = new ArrayList<>();
		signalError = new ErrorSignaller(outError, compilationErrorList);
		symbolTable = new SymbolTable();
		lexer = new Lexer(input, signalError);
		signalError.setLexer(lexer);

		Program program = null;
		lexer.nextToken();
		program = program(compilationErrorList);
		return program;
	}
	
	private void assertNextToken(final Token.Symbol symbol) {
		lexer.nextToken();
		if(lexer.getCurrentToken().getSymbol() != symbol)
			error("Symbol " + symbol.toString() + " expected");
	}
	
	private boolean checkNextToken(final Token.Symbol symbol) {
		return lexer.peek(1).getSymbol() == symbol;
	}
	
	private Annot annot(final List<CompilationError> compilationErrorList) {
		this.assertNextToken(Symbol.ANNOT);
		this.assertNextToken(Symbol.ID);
		
		final List<AnnotParam> annotParamList = new ArrayList<>();
		
		if(checkNextToken(Symbol.LEFTPAR)) {
			lexer.nextToken();
			while(!checkNextToken(Symbol.RIGHTPAR)) {
				final AnnotParam annotParam = this.annotParam(compilationErrorList);
				annotParamList.add(annotParam);
			}
			lexer.nextToken();
		}
		
		return new Annot(annotParamList);
	}
	
	private AssignExpr assignExpr(final List<CompilationError> compilationErrorList) {
		final Expression expr = this.expression(compilationErrorList);
		Expression assignExpr = null;
		
		if(this.checkNextToken(Symbol.ASSIGN)) {
			lexer.nextToken();
			assignExpr = this.expression(compilationErrorList);
		}
		
		return new AssignExpr(expr, assignExpr);
	}
	
	private Expression expression(final List<CompilationError> compilationErrorList) {
		return new Expression();
	}
	
	private AnnotParam annotParam(final List<CompilationError> compilationErrorList) {
		return new AnnotParam();
	}

	private Program program(ArrayList<CompilationError> compilationErrorList) {
		ArrayList<MetaobjectAnnotation> metaobjectCallList = new ArrayList<>();
		ArrayList<TypeCianetoClass> CianetoClassList = new ArrayList<>();
		Program program = new Program(CianetoClassList, metaobjectCallList, compilationErrorList);
		boolean thereWasAnError = false;
		while ( lexer.peek(0).getSymbol() == Token.Symbol.CLASS ||
				(lexer.peek(0).getSymbol() == Token.Symbol.ID && lexer.peek(0).getValue().equals("open") ) ||
				lexer.peek(0).getSymbol() == Token.Symbol.ANNOT ) {
			try {
				while ( lexer.peek(0).getSymbol() == Token.Symbol.ANNOT ) {
					metaobjectAnnotation(metaobjectCallList);
				}
				classDec();
			}
			catch( CompilerError e) {
				// if there was an exception, there is a compilation error
				thereWasAnError = true;
				while ( lexer.peek(0).getSymbol() != Token.Symbol.CLASS && lexer.peek(0).getSymbol() != Token.Symbol.EOF ) {
					try {
						next();
					}
					catch ( RuntimeException ee ) {
						e.printStackTrace();
						return program;
					}
				}
			}
			catch ( RuntimeException e ) {
				e.printStackTrace();
				thereWasAnError = true;
			}

		}
		if ( !thereWasAnError && lexer.peek(0).getSymbol() != Token.Symbol.EOF ) {
			try {
				error("End of file expected");
			}
			catch( CompilerError e) {
			}
		}
		return program;
	}

	/**  parses a metaobject annotation as <code>{@literal @}cep(...)</code> in <br>
     * <code>
     * {@literal @}cep(5, "'class' expected") <br>
     * class Program <br>
     *     func run { } <br>
     * end <br>
     * </code>
     *

	 */
	@SuppressWarnings("incomplete-switch")
	private void metaobjectAnnotation(ArrayList<MetaobjectAnnotation> metaobjectAnnotationList) {
		String name = lexer.peek(0).getValue();
		int lineNumber = lexer.peek(0).getLine();
		lexer.nextToken();
		ArrayList<Object> metaobjectParamList = new ArrayList<>();
		boolean getNextToken = false;
		if ( lexer.peek(0).getSymbol() == Token.Symbol.LEFTPAR ) {
			// metaobject call with parameters
			lexer.nextToken();
			while ( lexer.peek(0).getSymbol() == Token.Symbol.LITERALINT || lexer.peek(0).getSymbol() == Token.Symbol.LITERALSTRING ||
					lexer.peek(0).getSymbol() == Token.Symbol.ID ) {
				switch ( lexer.peek(0).getSymbol() ) {
				case LITERALINT:
					metaobjectParamList.add(lexer.peek(0).getValue());
					break;
				case LITERALSTRING:
					metaobjectParamList.add(lexer.peek(0).getValue());
					break;
				case ID:
					metaobjectParamList.add(lexer.peek(0).getValue());
				}
				lexer.nextToken();
				if ( lexer.peek(0).getSymbol() == Token.Symbol.COMMA )
					lexer.nextToken();
				else
					break;
			}
			if ( lexer.peek(0).getSymbol() != Token.Symbol.RIGHTPAR )
				error("')' expected after annotation with parameters");
			else {
				getNextToken = true;
			}
		}
		switch ( name ) {
		case "nce":
			if ( metaobjectParamList.size() != 0 )
				error("Annotation 'nce' does not take parameters");
			break;
		case "cep":
			if ( metaobjectParamList.size() != 3 && metaobjectParamList.size() != 4 )
				error("Annotation 'cep' takes three or four parameters");
			if ( !( metaobjectParamList.get(0) instanceof Integer)  ) {
				error("The first parameter of annotation 'cep' should be an integer number");
			}
			else {
				int ln = (Integer ) metaobjectParamList.get(0);
				metaobjectParamList.set(0, ln + lineNumber);
			}
			if ( !( metaobjectParamList.get(1) instanceof String) ||  !( metaobjectParamList.get(2) instanceof String) )
				error("The second and third parameters of annotation 'cep' should be literal strings");
			if ( metaobjectParamList.size() >= 4 && !( metaobjectParamList.get(3) instanceof String) )
				error("The fourth parameter of annotation 'cep' should be a literal string");
			break;
		case "annot":
			if ( metaobjectParamList.size() < 2  ) {
				error("Annotation 'annot' takes at least two parameters");
			}
			for ( Object p : metaobjectParamList ) {
				if ( !(p instanceof String) ) {
					error("Annotation 'annot' takes only String parameters");
				}
			}
			if ( ! ((String ) metaobjectParamList.get(0)).equalsIgnoreCase("check") )  {
				error("Annotation 'annot' should have \"check\" as its first parameter");
			}
			break;
		default:
			error("Annotation '" + name + "' is illegal");
		}
		metaobjectAnnotationList.add(new MetaobjectAnnotation(name, metaobjectParamList));
		if ( getNextToken ) lexer.nextToken();
	}

	private void classDec() {
		if ( lexer.peek(0).getSymbol() == Token.Symbol.ID && lexer.peek(0).getValue().equals("open") ) {
			// open class
		}
		if ( lexer.peek(0).getSymbol() != Token.Symbol.CLASS ) error("'class' expected");
		lexer.nextToken();
		if ( lexer.peek(0).getSymbol() != Token.Symbol.ID )
			error("Identifier expected");
		String className = lexer.peek(0).getValue();
		lexer.nextToken();
		if ( lexer.peek(0).getSymbol() == Token.Symbol.EXTENDS ) {
			lexer.nextToken();
			if ( lexer.peek(0).getSymbol() != Token.Symbol.ID )
				error("Identifier expected");
			String superclassName = lexer.peek(0).getValue();

			lexer.nextToken();
		}

		memberList();
		if ( lexer.peek(0).getSymbol() != Token.Symbol.END)
			error("'end' expected");
		lexer.nextToken();

	}

	private void memberList() {
		while ( true ) {
			qualifier();
			if ( lexer.peek(0).getSymbol() == Token.Symbol.VAR ) {
				fieldDec();
			}
			else if ( lexer.peek(0).getSymbol() == Token.Symbol.FUNC ) {
				methodDec();
			}
			else {
				break;
			}
		}
	}

	private void error(String msg) {
		this.signalError.showError(msg);
	}


	private void next() {
		lexer.nextToken();
	}

	private void check(Token.Symbol shouldBe, String msg) {
		if ( lexer.peek(0).getSymbol() != shouldBe ) {
			error(msg);
		}
	}

	private void methodDec() {
		lexer.nextToken();
		if ( lexer.peek(0).getSymbol() == Token.Symbol.ID ) {
			// unary method
			lexer.nextToken();

		}
		else if ( lexer.peek(0).getSymbol() == Token.Symbol.IDCOLON ) {
			// keyword method. It has parameters

		}
		else {
			error("An identifier or identifer: was expected after 'func'");
		}
		if ( lexer.peek(0).getSymbol() == Token.Symbol.MINUS_GT ) {
			// method declared a return type
			lexer.nextToken();
			type();
		}
		if ( lexer.peek(0).getSymbol() != Token.Symbol.LEFTCURBRACKET ) {
			error("'{' expected");
		}
		next();
		statementList();
		if ( lexer.peek(0).getSymbol() != Token.Symbol.RIGHTCURBRACKET ) {
			error("'{' expected");
		}
		next();

	}

	private void statementList() {
		  // only '}' is necessary in this test
		while ( lexer.peek(0).getSymbol() != Token.Symbol.RIGHTCURBRACKET && lexer.peek(0).getSymbol() != Token.Symbol.END ) {
			statement();
		}
	}

	private void statement() {
		boolean checkSemiColon = true;
		switch ( lexer.peek(0).getSymbol() ) {
		case IF:
			ifStat();
			checkSemiColon = false;
			break;
		case WHILE:
			whileStat();
			checkSemiColon = false;
			break;
		case RETURN:
			returnStat();
			break;
		case BREAK:
			breakStat();
			break;
		case SEMICOLON:
			next();
			break;
		case REPEAT:
			repeatStat();
			break;
		case VAR:
			localDec();
			break;
		case ASSERT:
			assertStat();
			break;
		default:
			if ( lexer.peek(0).getSymbol() == Token.Symbol.ID && lexer.peek(0).getValue().equals("Out") ) {
				writeStat();
			}
			else {
				expr();
			}

		}
		if ( checkSemiColon ) {
			check(Token.Symbol.SEMICOLON, "';' expected");
		}
	}

	private void localDec() {
		next();
		type();
		check(Token.Symbol.ID, "A variable name was expected");
		while ( lexer.peek(0).getSymbol() == Token.Symbol.ID ) {
			next();
			if ( lexer.peek(0).getSymbol() == Token.Symbol.COMMA ) {
				next();
			}
			else {
				break;
			}
		}
		if ( lexer.peek(0).getSymbol() == Token.Symbol.ASSIGN ) {
			next();
			// check if there is just one variable
			expr();
		}

	}

	private void repeatStat() {
		next();
		while ( lexer.peek(0).getSymbol() != Token.Symbol.UNTIL && lexer.peek(0).getSymbol() != Token.Symbol.RIGHTCURBRACKET && lexer.peek(0).getSymbol() != Token.Symbol.END ) {
			statement();
		}
		check(Token.Symbol.UNTIL, "missing keyword 'until'");
	}

	private void breakStat() {
		next();

	}

	private void returnStat() {
		next();
		expr();
	}

	private void whileStat() {
		next();
		expr();
		check(Token.Symbol.LEFTCURBRACKET, "missing '{' after the 'while' expression");
		next();
		while ( lexer.peek(0).getSymbol() != Token.Symbol.RIGHTCURBRACKET && lexer.peek(0).getSymbol() != Token.Symbol.END ) {
			statement();
		}
		check(Token.Symbol.RIGHTCURBRACKET, "missing '}' after 'while' body");
	}

	private void ifStat() {
		next();
		expr();
		check(Token.Symbol.LEFTCURBRACKET, "'{' expected after the 'if' expression");
		next();
		while ( lexer.peek(0).getSymbol() != Token.Symbol.RIGHTCURBRACKET && lexer.peek(0).getSymbol() != Token.Symbol.END && lexer.peek(0).getSymbol() != Token.Symbol.ELSE ) {
			statement();
		}
		check(Token.Symbol.RIGHTCURBRACKET, "'}' was expected");
		if ( lexer.peek(0).getSymbol() == Token.Symbol.ELSE ) {
			next();
			check(Token.Symbol.LEFTCURBRACKET, "'{' expected after 'else'");
			next();
			while ( lexer.peek(0).getSymbol() != Token.Symbol.RIGHTCURBRACKET ) {
				statement();
			}
			check(Token.Symbol.RIGHTCURBRACKET, "'}' was expected");
		}
	}

	/**

	 */
	private void writeStat() {
		next();
		check(Token.Symbol.DOT, "a '.' was expected after 'Out'");
		next();
		check(Token.Symbol.IDCOLON, "'print:' or 'println:' was expected after 'Out.'");
		String printName = lexer.peek(0).getValue();
		expr();
	}

	private void expr() {

	}

	private void fieldDec() {
		lexer.nextToken();
		type();
		if ( lexer.peek(0).getSymbol() != Token.Symbol.ID ) {
			this.error("A field name was expected");
		}
		else {
			while ( lexer.peek(0).getSymbol() == Token.Symbol.ID  ) {
				lexer.nextToken();
				if ( lexer.peek(0).getSymbol() == Token.Symbol.COMMA ) {
					lexer.nextToken();
				}
				else {
					break;
				}
			}
		}

	}

	private void type() {
		if ( lexer.peek(0).getSymbol() == Token.Symbol.INT || lexer.peek(0).getSymbol() == Token.Symbol.BOOLEAN || lexer.peek(0).getSymbol() == Token.Symbol.STRING ) {
			next();
		}
		else if ( lexer.peek(0).getSymbol() == Token.Symbol.ID ) {
			next();
		}
		else {
			this.error("A type was expected");
		}

	}


	private void qualifier() {
		if ( lexer.peek(0).getSymbol() == Token.Symbol.PRIVATE ) {
			next();
		}
		else if ( lexer.peek(0).getSymbol() == Token.Symbol.PUBLIC ) {
			next();
		}
		else if ( lexer.peek(0).getSymbol() == Token.Symbol.OVERRIDE ) {
			next();
			if ( lexer.peek(0).getSymbol() == Token.Symbol.PUBLIC ) {
				next();
			}
		}
		else if ( lexer.peek(0).getSymbol() == Token.Symbol.FINAL ) {
			next();
			if ( lexer.peek(0).getSymbol() == Token.Symbol.PUBLIC ) {
				next();
			}
			else if ( lexer.peek(0).getSymbol() == Token.Symbol.OVERRIDE ) {
				next();
				if ( lexer.peek(0).getSymbol() == Token.Symbol.PUBLIC ) {
					next();
				}
			}
		}
	}
	/**
	 * change this method to 'private'.
	 * uncomment it
	 * implement the methods it calls
	 */
	public Statement assertStat() {

		lexer.nextToken();
		int lineNumber = lexer.peek(0).getLine();
		expr();
		if ( lexer.peek(0).getSymbol() != Token.Symbol.COMMA ) {
			this.error("',' expected after the expression of the 'assert' statement");
		}
		lexer.nextToken();
		if ( lexer.peek(0).getSymbol() != Token.Symbol.LITERALSTRING ) {
			this.error("A literal string expected after the ',' of the 'assert' statement");
		}
		String message = lexer.peek(0).getValue();
		lexer.nextToken();
		if ( lexer.peek(0).getSymbol() == Token.Symbol.SEMICOLON )
			lexer.nextToken();

		return null;
	}




	private LiteralInt literalInt() {

		LiteralInt e = null;

		// the number value is stored in lexer.getToken().value as an object of
		// Integer.
		// Method intValue returns that value as an value of type int.
		int value = Integer.parseInt(lexer.peek(0).getValue());
		lexer.nextToken();
		return new LiteralInt(value);
	}

	private static boolean startExpr(Token.Symbol token) {

		return token == Token.Symbol.FALSE || token == Token.Symbol.TRUE
				|| token == Token.Symbol.NOT || token == Token.Symbol.SELF
				|| token == Token.Symbol.LITERALINT || token == Token.Symbol.SUPER
				|| token == Token.Symbol.LEFTPAR || token == Token.Symbol.NULL
				|| token == Token.Symbol.ID || token == Token.Symbol.LITERALSTRING;

	}

	private SymbolTable		symbolTable;
	private Lexer			lexer;
	private ErrorSignaller	signalError;

}
