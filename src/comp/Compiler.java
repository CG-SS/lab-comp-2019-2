
package comp;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ast.Annot;
import ast.AnnotParam;
import ast.AssertStat;
import ast.AssignExpr;
import ast.BasicType;
import ast.BasicValue;
import ast.BooleanValue;
import ast.ClassDec;
import ast.CompStatement;
import ast.Digit;
import ast.Expression;
import ast.ExpressionList;
import ast.Factor;
import ast.FormalParamDec;
import ast.HighOperator;
import ast.IdList;
import ast.LiteralInt;
import ast.LocalDec;
import ast.Member;
import ast.MetaobjectAnnotation;
import ast.ObjectCreation;
import ast.ParamDec;
import ast.PrimaryExpr;
import ast.Program;
import ast.Qualifer;
import ast.ReadExpr;
import ast.Relation;
import ast.RepeatStat;
import ast.ReturnStats;
import ast.Signal;
import ast.SimpleExpression;
import ast.Statement;
import ast.Type;
import ast.TypeCianetoClass;
import lexer.Lexer;
import lexer.Token;
import lexer.Token.Symbol;

public class Compiler {
	
	private HashMap<String, String> classNameMap;

	// compile must receive an input with an character less than
	// p_input.lenght
	public Program compile(final char[] input, final PrintWriter outError) {

		ArrayList<CompilationError> compilationErrorList = new ArrayList<>();
		signalError = new ErrorSignaller(outError, compilationErrorList);
		symbolTable = new SymbolTable();
		classNameMap = new HashMap<>();
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
	
	private BasicType basicType(final List<CompilationError> compilationErrorList) {
		if(!checkNextToken(Symbol.STRING) || !checkNextToken(Symbol.INT) || !checkNextToken(Symbol.BOOLEAN)) {
			this.error("Valid types are 'Int', 'String' or 'Boolean'");
		}
		lexer.nextToken();
		
		return new BasicType(lexer.peek(0).toString());
	}
	
	private BasicValue basicValue(final List<CompilationError> compilationErrorList) {
		if(!checkNextToken(Symbol.LITERALINT) || !checkNextToken(Symbol.LITERALSTRING) || !checkNextToken(Symbol.TRUE) || !checkNextToken(Symbol.FALSE)) {
			this.error("Expected literal boolean, int or string");
		}
		lexer.nextToken();

		if(lexer.getCurrentToken().getSymbol() == Symbol.TRUE || lexer.getCurrentToken().getSymbol() == Symbol.FALSE)
			return new BasicValue(Symbol.TRUE.toString());
		return new BasicValue(lexer.getCurrentToken().getValue());
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
		final SimpleExpression simpleExpr = this.simpleExpression(compilationErrorList);
		Relation rel = null;
		SimpleExpression relExpr = null;
		
		if(checkNextToken(Symbol.EQ) || checkNextToken(Symbol.NEQ) || checkNextToken(Symbol.LT) || checkNextToken(Symbol.GE) || checkNextToken(Symbol.GT) || checkNextToken(Symbol.LE)) {
			lexer.nextToken();
			rel = this.relation(compilationErrorList);
			relExpr = this.simpleExpression(compilationErrorList);
		}
		
		return new Expression(simpleExpr, rel, relExpr);
	}
	
	private ExpressionList expressionList(final List<CompilationError> compilationErrorList) {
		final Expression expr = this.expression(compilationErrorList);
		final List<Expression> exprList = new ArrayList<>();
		
		while(checkNextToken(Symbol.COMMA)) {
			lexer.nextToken();
			exprList.add(this.expression(compilationErrorList));
		}
		
		return new ExpressionList(expr, exprList);
	}
	
	private Factor factor(final List<CompilationError> compilationErrorList) {
		BasicValue basicValue = null;
		Expression expression = null;
		Factor factor = null;
		ObjectCreation objCreation = null;
		PrimaryExpr primaryExpr = null;
		String nil = null;
		
		if(checkNextToken(Symbol.LITERALINT) || checkNextToken(Symbol.LITERALSTRING) || checkNextToken(Symbol.TRUE) || checkNextToken(Symbol.FALSE)) {
			basicValue = this.basicValue(compilationErrorList);
		} else if(checkNextToken(Symbol.ID) && lexer.peek(1).getSymbol() == Symbol.DOT && lexer.peek(2).getSymbol() == Symbol.NEW) {
			objCreation = this.objectCreation(compilationErrorList);
		} else if(checkNextToken(Symbol.ID)) {
			primaryExpr = this.primaryExpression(compilationErrorList);
		} else if(checkNextToken(Symbol.LEFTPAR)) {
			lexer.nextToken();
			expression = this.expression(compilationErrorList);
			this.assertNextToken(Symbol.RIGHTPAR);
		} else if(checkNextToken(Symbol.NOT)) {
			lexer.nextToken();
			factor = this.factor(compilationErrorList);
		} else {
			assertNextToken(Symbol.NULL);
			nil = lexer.getCurrentToken().toString();
		}
		
		return new Factor(basicValue, expression, factor, objCreation, primaryExpr, nil);
	}
	
	private FormalParamDec formalParamDec(List<CompilationError> compilationErrorList) {
		final ParamDec paramDec = this.paramDec(compilationErrorList);
		final List<ParamDec> paramDecList = new ArrayList<>();
		while(checkNextToken(Symbol.COMMA)) {
			lexer.nextToken();
			paramDecList.add(this.paramDec(compilationErrorList));
		}
		
		return new FormalParamDec(paramDec, paramDecList);
	}
	
	private HighOperator highOperator(List<CompilationError> compilationErrorList) {
		if(!this.checkNextToken(Symbol.MULT) || !this.checkNextToken(Symbol.DIV) || !this.checkNextToken(Symbol.AND)) {
			this.error("Expected '*', '/' or '&&'");
		}
		lexer.nextToken();
		
		return new HighOperator(lexer.getCurrentToken().toString());
	}
	
	private LocalDec localDec(List<CompilationError> compilationErrorList) {
		this.assertNextToken(Symbol.VAR);
		final Type type = this.type();
		final IdList idList = this.idList();
		Expression expression = null;
		
		if(checkNextToken(Symbol.ASSIGN)) {
			next();
			expression = this.expression(compilationErrorList);
		}
		
		return new LocalDec(type, idList, expression);
	}
	
	// IdList ::= Id { “,” Id }
	private IdList idList(List<CompilationError> compilationErrorList) {
		assertNextToken(Symbol.ID);
		List<String> idList = new ArrayList<>();
		while(checkNextToken(Symbol.COMMA)) {
			lexer.nextToken();
			idList.add(this.id(compilationErrorList));
		}
		return new IdList(idList);
	}
	// ParamDec ::= Type Id
	private ParamDec paramDec(List<CompilationError> compilationErrorList) {
		type();
		assertNextToken(Symbol.ID);
		String idName = lexer.getCurrentToken().getValue();
		return new ParamDec(idName);
	}
	// PrimaryExpr ::= 
	//		“super” “.” IdColon ExpressionList |
	//		“super” “.” Id |
	//		Id |
	//		Id “.” Id |
	//		Id “.” IdColon ExpressionList |
	//		“self” |
	//		“self” “.” Id |
	//		“self” ”.” IdColon ExpressionList |
	//		“self” ”.” Id “.” IdColon ExpressionList |
	//		“self” ”.” Id “.” Id |
	private PrimaryExpr primaryExpression(List<CompilationError> compilationErrorList) {
		String idColon;
		String idFirst;
		String idSecond;
		ExpressionList expList = new ExpressionList();
		// super
		if (checkNextToken(Symbol.SUPER)) {
			lexer.nextToken();
			assertNextToken(Symbol.DOT);
			if (checkNextToken(Symbol.IDCOLON)) {
				lexer.nextToken();
				idColon = lexer.getCurrentToken().getValue();
				expList =  this.expressionList(compilationErrorList);
			}
			if (checkNextToken(Symbol.ID)) {
				lexer.nextToken();
				idFirst = lexer.getCurrentToken().getValue();
			}
		} else if (checkNextToken(Symbol.ID)) {
			lexer.nextToken();
			idFirst = this.lexer.getCurrentToken().getValue();
			if (checkNextToken(Symbol.DOT)) {
				lexer.nextToken();
				if (checkNextToken(Symbol.ID)) {
					lexer.nextToken();
					idSecond = this.lexer.getCurrentToken().getValue();
					if (checkNextToken(Symbol.DOT)) {
						
					}
				} else if (checkNextToken(Symbol.IDCOLON)) {
					lexer.nextToken();
					idColon = this.lexer.getCurrentToken().getValue();
					expList =  this.expressionList(compilationErrorList);
				}
			}
			//		“self” |
			//		“self” “.” Id |
			//		“self” ”.” IdColon ExpressionList |
			//		“self” ”.” Id “.” IdColon ExpressionList |
			//		“self” ”.” Id “.” Id |
		} else if (checkNextToken(Symbol.SELF)) {
			lexer.nextToken();
			if (checkNextToken(Symbol.DOT)) {
				lexer.nextToken();
				if (checkNextToken(Symbol.ID)) {
					lexer.nextToken();
					idFirst = lexer.getCurrentToken().getValue();
					if (checkNextToken(Symbol.DOT)) {
						lexer.nextToken();
						if (checkNextToken(Symbol.ID)) {
							lexer.nextToken();
							idSecond = lexer.getCurrentToken().getValue();
						} else if (checkNextToken(Symbol.IDCOLON)) {
							lexer.nextToken();
							idColon = this.lexer.getCurrentToken().getValue();
							expList =  this.expressionList(compilationErrorList);
						}
					}
				} else if (checkNextToken(Symbol.IDCOLON)) {
					lexer.nextToken();
					idColon = this.lexer.getCurrentToken().getValue();
					expList =  this.expressionList(compilationErrorList);
				}
			}
		}
		return new PrimaryExpr(expList, idColon, idFirst, idSecond);
	}
	
	private String id(List<CompilationError> compilationErrorList) {
		assertNextToken(Symbol.ID);
		return lexer.getCurrentToken().getValue();
	}
	// ReadExpr ::= “In” “.” ( “readInt” | “readString” )
	private ReadExpr readExpr(List<CompilationError> compilationErrorList) {
		assertNextToken(Symbol.IN);
		assertNextToken(Symbol.DOT);
		if (!checkNextToken(Symbol.READINT) || !checkNextToken(Symbol.READSTRING)) {
			this.error("Expected readInt or readString");
		}
		assertNextToken(Symbol.LEFTPAR);
		
		return new ReadExpr();
	}
	// ObjectCreation ::= Id “.” “new”
	private ObjectCreation objectCreation(List<CompilationError> compilationErrorList) {
		assertNextToken(Symbol.ID);
		String idName = lexer.getCurrentToken().getValue();
		assertNextToken(Symbol.DOT);
		assertNextToken(Symbol.NEW);
		return new ObjectCreation(idName);
	}

	private Relation relation(List<CompilationError> compilationErrorList) {
		// TODO Auto-generated method stub
		return null;
	}

	private SimpleExpression simpleExpression(List<CompilationError> compilationErrorList) {
		// TODO Auto-generated method stub
		return null;
	}

	private AnnotParam annotParam(final List<CompilationError> compilationErrorList) {
		
		if(!checkNextToken(Symbol.INT) || !checkNextToken(Symbol.STRING) || !checkNextToken(Symbol.ID)) {
			this.error("Expected int, string or id");
		}
		lexer.nextToken();
		return new AnnotParam(lexer.getCurrentToken().getValue());
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

	/* CompStatement ::= “{” { Statement } “}” */
	private CompStatement compStatement(final List<CompilationError> compilationErrorList) {
		
		assertNextToken(Symbol.LEFTCURBRACKET);
		this.statementList();
		assertNextToken(Symbol.RIGHTCURBRACKET);
		
		return new CompStatement();
	}
	
	// Digit ::= “0” | ... | “9”
	private Digit digit(final List<CompilationError> compilationErrorList) {
		
		String numString = lexer.peek(1).getValue();
		if (!numString.equals('0') || !numString.equals('1') || !numString.equals('2') || 
			!numString.equals('3') || !numString.equals('4') || !numString.equals('5') ||
			!numString.equals('6') || !numString.equals('7') || !numString.equals('8') ||  
			!numString.equals('8')){
			this.error("Not a Digit");
		}
		lexer.nextToken();
		
		return new Digit(numString);
	}
	
	private ClassDec classDec(final List<CompilationError> compilationErrorList) {
		boolean openClassBol = false;
		boolean extendsClassBol = false;
		String className = null;
		
		if (checkNextToken(Symbol.OPEN)) {
			openClassBol = true;
			lexer.nextToken();
		}
		
		assertNextToken(Symbol.CLASS);
		assertNextToken(Symbol.ID);
		className = lexer.getCurrentToken().getValue();
		
		if (checkNextToken(Symbol.EXTENDS)) {
			extendsClassBol = true;
			lexer.nextToken();
			if (!checkNextToken(Symbol.ID)) {
				this.error("Expected ID before Extends");
			}
			lexer.nextToken();
		}
		
		final MemberList memberListVar = this.memberList(); // compilationErrorList  (passar parametro
		
		if (!checkNextToken(Symbol.END)) {
			this.error("Expected 'end'");
		}
		lexer.nextToken();
		
		return new ClassDec();
	}
	// Member ::= FieldDec | MethodDec
	private Member member(final List<CompilationError> compilationErrorList) {
		
		if ( checkNextToken(Symbol.VAR) ) {
		 this.fieldDec();
		}
		else if ( checkNextToken(Symbol.FUNC) ) {
			methodDec(compilationErrorList);
		}
		
		return new Member();
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
	// MethodDec ::= “func” IdColon FormalParamDec [ “->” Type ] “{” StatementList “}” 
	//				 | “func” Id [ “->” Type ] “{” StatementList “}”
	private void methodDec(final List<CompilationError> compilationErrorList) {
		assertNextToken(Symbol.FUNC);
		if ( checkNextToken(Symbol.IDCOLON)) { 
			lexer.nextToken();
			this.formalParamDec(compilationErrorList);
		}
		else if ( checkNextToken(Symbol.ID) ) {
			// keyword method. It has parameters
			lexer.nextToken();
		}
		else {
			error("An identifier or identifer: was expected after 'func'");
		}
		if (checkNextToken(Symbol.MINUS_GT) ) {
			// method declared a return type
			lexer.nextToken();
			type();
		}
		assertNextToken(Symbol.LEFTCURBRACKET);
		statementList();
		assertNextToken(Symbol.RIGHTCURBRACKET);
		
		// return new MethodDec();

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
	// RepeatStat ::= “repeat” StatementList “until” Expression
	private RepeatStat repeatStat(final List<CompilationError> compilationErrorList) {
		assertNextToken(Symbol.REPEAT);
		ArrayList<StatementList> statList = this.statementList();
		assertNextToken(Symbol.UNTIL);
		this.expression(compilationErrorList);
		return new RepeatStat(statList);
		/*
		next();
		while ( lexer.peek(0).getSymbol() != Token.Symbol.UNTIL && lexer.peek(0).getSymbol() != Token.Symbol.RIGHTCURBRACKET && lexer.peek(0).getSymbol() != Token.Symbol.END ) {
			statement();
		}
		check(Token.Symbol.UNTIL, "missing keyword 'until'");
		*/
	}

	private void breakStat() {
		next();

	}
	// ReturnStat ::= “return” Expression
	private ReturnStats returnStat(final List<CompilationError> compilationErrorList) {
		assertNextToken(Symbol.RETURN);
		Expression exp = this.expression(compilationErrorList);
		return new ReturnStats(exp);
	}
	
	private Signal signal(final List<CompilationError> compilationErrorList) {
		String sinal = "";
		if (checkNextToken(Symbol.PLUS) || checkNextToken(Symbol.MINUS)) {
			lexer.nextToken();
			sinal = lexer.getCurrentToken().getValue();
		}
		return new Signal(sinal);
	}
	//  “private”
	//“public”
	//“override”
	//“override” “public”
	//“final”
	//“final” “public”
	//“final” “override”
	//“final” “override” “public”
	//“shared” “private”
	//“shared” “public”
	private Qualifer qualifer(final List<CompilationError> compilationErrorList) {
		ArrayList<String> arrListQualifer = new ArrayList<String>();
		if (checkNextToken(Symbol.PRIVATE)) {
			lexer.nextToken();
			arrListQualifer.add(lexer.getCurrentToken().getValue());
		} else if (checkNextToken(Symbol.PUBLIC)) {
			lexer.nextToken();
			arrListQualifer.add(lexer.getCurrentToken().getValue());
		} else if (checkNextToken(Symbol.OVERRIDE)) {
			lexer.nextToken();
			arrListQualifer.add(lexer.getCurrentToken().getValue());
			if (checkNextToken(Symbol.PUBLIC)) {
				lexer.nextToken();
				arrListQualifer.add(lexer.getCurrentToken().getValue());
			}
		} else if (checkNextToken(Symbol.FINAL)) {
			lexer.nextToken();
			arrListQualifer.add(lexer.getCurrentToken().getValue());
			if (checkNextToken(Symbol.PUBLIC)) {
				lexer.nextToken();
				arrListQualifer.add(lexer.getCurrentToken().getValue());
			} else if (checkNextToken(Symbol.OVERRIDE)) {
				lexer.nextToken();
				arrListQualifer.add(lexer.getCurrentToken().getValue());
				if (checkNextToken(Symbol.PUBLIC)) {
					lexer.nextToken();
					arrListQualifer.add(lexer.getCurrentToken().getValue());
				}
			}
		} else if (checkNextToken(Symbol.SHARED)) {
			lexer.nextToken();
			arrListQualifer.add(lexer.getCurrentToken().getValue());
			if (checkNextToken(Symbol.PUBLIC)) {
				lexer.nextToken();
				arrListQualifer.add(lexer.getCurrentToken().getValue());
			} else if (checkNextToken(Symbol.PRIVATE)) {
				lexer.nextToken();
				arrListQualifer.add(lexer.getCurrentToken().getValue());
			}
		}
		
		return new Qualifer(arrListQualifer);
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

	// IfStat ::= “if” Expression “{” Statement “}” 	[ “else” “{” Statement “}” ]
	private void ifStat(final List<CompilationError> compilationErrorList) {
		
		assertNextToken(Symbol.IF);
		this.expression(compilationErrorList); // Expression exp = this.expression(compilationErrorList);
		assertNextToken(Symbol.LEFTCURBRACKET);
		this.statement();
		assertNextToken(Symbol.RIGHTCURBRACKET);
		
		if (checkNextToken(Symbol.ELSE)) {
			lexer.nextToken();
			assertNextToken(Symbol.LEFTCURBRACKET);
			this.statement();
			assertNextToken(Symbol.RIGHTCURBRACKET);
		}
		
		/*next();
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
		}*/
	}

	
	private LowOperator lowOperator(final List<CompilationError> compilationErrorList) {
		if (!checkNextToken(Symbol.PLUS) || !checkNextToken(Symbol.MINUS) || !checkNextToken(Symbol.OR)) {
			this.error("Expected +, - or || ");
		}
		lexer.nextToken();
		return new LowOperator(this.lexer.getCurrentToken().getValue());
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

	// FieldDec ::= “var” Type IdList [ “;” ]
	private FieldDec fieldDec() {
		if (!checkNextToken(Symbol.VAR)) {
			this.error("Expected a 'var'");
		}
		lexer.nextToken();
		this.type();
		if (!checkNextToken(Symbol.ID)) {
			this.error("A field name was expected");
		}
		else {
			while ( checkNextToken(Symbol.ID) ) {
				lexer.nextToken();
				if ( checkNextToken(Symbol.COMMA) ) {
					lexer.nextToken();
				}
				else {
					break;
				}
			}
		}
		return new FieldDec();
	}

	private Type type() {
		return null;
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
	// AssertStat ::= “assert” Expression “,” StringValue
	private AssertStat assertStat(List<CompilationError> compilationErrorList) {

		assertNextToken(Symbol.ASSERT);
		lexer.nextToken();
		int lineNumber = lexer.peek(0).getLine();
		this.expression(compilationErrorList);
		if ( !checkNextToken(Symbol.COMMA) ) {
			this.error("',' expected after the expression of the 'assert' statement");
		}
		lexer.nextToken();
		if ( !checkNextToken(Symbol.LITERALSTRING) ) { 
			this.error("A literal string expected after the ',' of the 'assert' statement");
		}
		String message = lexer.getCurrentToken().getValue();
		return new AssertStat(message);
	}

	private BooleanValue booleanValue() {
		
		if (!checkNextToken(Symbol.TRUE) || !checkNextToken(Symbol.FALSE)){
			this.error("Expected true or false");
		}
		lexer.nextToken();
		return new BooleanValue(this.lexer.getCurrentToken().getValue());
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
