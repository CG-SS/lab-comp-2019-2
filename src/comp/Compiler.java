/*
 * Cristiano Guilherme - 609803
 * Daniel Davoli       - 610372
 */


package comp;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
import ast.Expression;
import ast.ExpressionList;
import ast.Factor;
import ast.FieldDec;
import ast.FormalParamDec;
import ast.HighOperator;
import ast.Id;
import ast.IdList;
import ast.IfStat;
import ast.LocalDec;
import ast.LowOperator;
import ast.Member;
import ast.MemberList;
import ast.MetaobjectAnnotation;
import ast.MethodDec;
import ast.ObjectCreation;
import ast.ParamDec;
import ast.PrimaryExpr;
import ast.PrintStat;
import ast.Program;
import ast.Qualifier;
import ast.ReadExpr;
import ast.Relation;
import ast.RepeatStat;
import ast.ReturnStat;
import ast.Signal;
import ast.SignalFactor;
import ast.SimpleExpression;
import ast.Statement;
import ast.StatementList;
import ast.SumSubExpression;
import ast.Term;
import ast.Type;
import ast.WhileStat;
import lexer.Lexer;
import lexer.Token;
import lexer.Token.Symbol;

public class Compiler {
	
	private HashSet<String> classNameMap;

	// compile must receive an input with an character less than
	// p_input.lenght
	public Program compile(final char[] input, final PrintWriter outError) {

		ArrayList<CompilationError> compilationErrorList = new ArrayList<>();
		signalError = new ErrorSignaller(outError, compilationErrorList);
		classNameMap = new HashSet<>();
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
	
	private AnnotParam annotParam(final List<CompilationError> compilationErrorList) {
		
		if(!checkNextToken(Symbol.INT) || !checkNextToken(Symbol.STRING) || !checkNextToken(Symbol.ID)) {
			this.error("Expected int, string or id");
		}
		lexer.nextToken();
		return new AnnotParam(lexer.getCurrentToken().getValue());
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
			return new BasicValue(lexer.getCurrentToken().toString());
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
			rel = this.relation(compilationErrorList);
			relExpr = this.simpleExpression(compilationErrorList);
		}
		
		return new Expression(simpleExpr, rel, relExpr);
	}
	
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
	
	private Relation relation(List<CompilationError> compilationErrorList) {
		if(!checkNextToken(Symbol.EQ) || !checkNextToken(Symbol.NEQ) || !checkNextToken(Symbol.LT) || !checkNextToken(Symbol.GE) || !checkNextToken(Symbol.GT) || !checkNextToken(Symbol.LE)) {
			this.error("Relation symbol expected");
		}
		lexer.nextToken();
		
		return new Relation(lexer.getCurrentToken().toString());
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
		if(classNameMap.contains(className))
			this.error(className + " already declared");
		classNameMap.add(className);
		
		if (checkNextToken(Symbol.EXTENDS)) {
			extendsClassBol = true;
			lexer.nextToken();
			if (!checkNextToken(Symbol.ID)) {
				this.error("Expected ID before Extends");
			}
			lexer.nextToken();
		}
		
		final MemberList memberListVar = this.memberList(compilationErrorList); // compilationErrorList  (passar parametro
		
		if (!checkNextToken(Symbol.END)) {
			this.error("Expected 'end'");
		}
		lexer.nextToken();
		
		return new ClassDec(openClassBol, extendsClassBol, className);
	}
	
	private CompStatement compStatement(final List<CompilationError> compilationErrorList) {
		
		assertNextToken(Symbol.LEFTCURBRACKET);
		final StatementList statList = this.statementList(compilationErrorList);
		assertNextToken(Symbol.RIGHTCURBRACKET);
		
		return new CompStatement(statList);
	}
	
	private FieldDec fieldDec(final List<CompilationError> compilationErrorList) {
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
	
	private IdList idList(List<CompilationError> compilationErrorList) {
		assertNextToken(Symbol.ID);
		List<Id> idList = new ArrayList<>();
		while(checkNextToken(Symbol.COMMA)) {
			lexer.nextToken();
			idList.add(this.id(compilationErrorList));
		}
		return new IdList(idList);
	}
	

	private Id id(List<CompilationError> compilationErrorList) {
		this.assertNextToken(Symbol.ID);
		
		return new Id(lexer.getCurrentToken().getValue());
	}

	private IfStat ifStat(final List<CompilationError> compilationErrorList) {
		
		assertNextToken(Symbol.IF);
		this.expression(compilationErrorList); // Expression exp = this.expression(compilationErrorList);
		assertNextToken(Symbol.LEFTCURBRACKET);
		this.statement(compilationErrorList);
		assertNextToken(Symbol.RIGHTCURBRACKET);
		
		if (checkNextToken(Symbol.ELSE)) {
			lexer.nextToken();
			assertNextToken(Symbol.LEFTCURBRACKET);
			this.statement(compilationErrorList);
			assertNextToken(Symbol.RIGHTCURBRACKET);
		}
		
		return new IfStat();
		
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
	
	private MethodDec methodDec(final List<CompilationError> compilationErrorList) {
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
		statementList(compilationErrorList);
		assertNextToken(Symbol.RIGHTCURBRACKET);
		
		return new MethodDec();

	}
	
	private ParamDec paramDec(List<CompilationError> compilationErrorList) {
		type();
		assertNextToken(Symbol.ID);
		String idName = lexer.getCurrentToken().getValue();
		return new ParamDec(idName);
	}
	
	
	
	private ObjectCreation objectCreation(List<CompilationError> compilationErrorList) {
		assertNextToken(Symbol.ID);
		String idName = lexer.getCurrentToken().getValue();
		assertNextToken(Symbol.DOT);
		assertNextToken(Symbol.NEW);
		return new ObjectCreation(idName);
	}
	
	private Member member(final List<CompilationError> compilationErrorList) {
		
		if ( checkNextToken(Symbol.VAR) ) {
		 this.fieldDec();
		}
		else if ( checkNextToken(Symbol.FUNC) ) {
			methodDec(compilationErrorList);
		}
		
		return new Member();
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
		final IdList idList = this.idList(compilationErrorList);
		Expression expression = null;
		
		if(checkNextToken(Symbol.ASSIGN)) {
			next();
			expression = this.expression(compilationErrorList);
		}
		
		return new LocalDec(type, idList, expression);
	}
	
	private Type type(List<CompilationError> compilationErrorList) {
		Id id = null;
		BasicType basicType = null;
		
		if(checkNextToken(Symbol.ID)) {
			lexer.nextToken();
			id = this.id(compilationErrorList);
		} else {
			basicType = this.basicType(compilationErrorList);
		}
		
		return new Type(id, basicType);
	}

	private Program program(ArrayList<CompilationError> compilationErrorList) {
		ArrayList<MetaobjectAnnotation> metaobjectCallList = new ArrayList<>();
		Program program = new Program(metaobjectCallList, compilationErrorList);
		boolean thereWasAnError = false;
		while ( lexer.peek(0).getSymbol() == Token.Symbol.CLASS ||
				(lexer.peek(0).getSymbol() == Token.Symbol.ID && lexer.peek(0).getValue().equals("open") ) ||
				lexer.peek(0).getSymbol() == Token.Symbol.ANNOT ) {
			try {
				while ( lexer.peek(0).getSymbol() == Token.Symbol.ANNOT ) {
					metaobjectAnnotation(metaobjectCallList);
				}
				classDec(compilationErrorList);
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

	private MemberList memberList(List<CompilationError> compilationErrorList) {
		Qualifier qualifier = null;
		Member member = null;
		
		if(checkNextToken(Symbol.PUBLIC) || checkNextToken(Symbol.PRIVATE) || checkNextToken(Symbol.FINAL) || checkNextToken(Symbol.OVERRIDE)) {
			qualifier = this.qualifier();
		}
		
		if(checkNextToken(Symbol.VAR) || checkNextToken(Symbol.FUNC)) {
			member = this.member(compilationErrorList);
		}
		
		return new MemberList(qualifier, member);
	}
	
	private SignalFactor signalFactor(final List<CompilationError> compilationErrorList) {
		Signal signal = null;
		
		if(checkNextToken(Symbol.PLUS) || checkNextToken(Symbol.MINUS)) {
			signal = this.signal(compilationErrorList);
		}
		
		final Factor factor = this.factor(compilationErrorList);
		return new SignalFactor(signal, factor);
	}
	
	private SumSubExpression sumSubExpression(final List<CompilationError> compilationErrorList) {
		final Term term = this.term(compilationErrorList);
		final List<LowOperator> opList = new ArrayList<>();
		final List<Term> termList = new ArrayList<>();
		
		while(checkNextToken(Symbol.PLUS) || checkNextToken(Symbol.MINUS) || checkNextToken(Symbol.OR)) {
			opList.add(this.lowOperator(compilationErrorList));
			termList.add(this.term(compilationErrorList)); 
		}
		
		return new SumSubExpression(term, opList, termList);
	}

	private SimpleExpression simpleExpression(final List<CompilationError> compilationErrorList) {
		final SumSubExpression sumSubExpr = this.sumSubExpression(compilationErrorList);
		final List<SumSubExpression> sumSubExpressionList = new ArrayList<>();
		
		while(checkNextToken(Symbol.PLUSPLUS)) {
			lexer.nextToken();
			sumSubExpressionList.add(this.sumSubExpression(compilationErrorList));
		}
		
		return new SimpleExpression(sumSubExpr, sumSubExpressionList);
	}
	
	private Statement statement(List<CompilationError> compilationErrorList) {
		AssignExpr assignExpr = null;
		IfStat ifStat = null;
		WhileStat whileStat = null;
		ReturnStat returnStat = null;
		PrintStat printStat = null;
		RepeatStat repeatStat = null;
		LocalDec localDec = null;
		AssertStat assertStat = null;
		String brk = null;
		
		if(checkNextToken(Symbol.IF)) {
			ifStat = this.ifStat(compilationErrorList);
		} else if(checkNextToken(Symbol.WHILE)) {
			whileStat = this.whileStat(compilationErrorList);
		} else if(checkNextToken(Symbol.RETURN)) {
			returnStat = this.returnStat(compilationErrorList);
		} else if(checkNextToken(Symbol.OUT)) {
			printStat = this.printStat(compilationErrorList);
		} else if(checkNextToken(Symbol.BREAK)) {
			this.assertNextToken(Symbol.BREAK);
			brk = "break";
		} else if(checkNextToken(Symbol.REPEAT)) {
			repeatStat = this.repeatStat(compilationErrorList);
		} else if(checkNextToken(Symbol.VAR)) {
			localDec = this.localDec(compilationErrorList);
		} else if(checkNextToken(Symbol.ASSERT)) {
			assertStat = this.assertStat(compilationErrorList);
		} else if(!checkNextToken(Symbol.SEMICOLON)) {
			assignExpr = this.assignExpr(compilationErrorList);
		}
		
		this.assertNextToken(Symbol.SEMICOLON);
		
		return new Statement(assignExpr, ifStat, whileStat, returnStat, printStat, repeatStat, localDec, assertStat, brk);
	}

	private WhileStat whileStat(List<CompilationError> compilationErrorList) {
		this.assertNextToken(Symbol.WHILE);
		final Expression expr = this.expression(compilationErrorList);
		this.assertNextToken(Symbol.LEFTCURBRACKET);
		final StatementList statList = this.statementList(compilationErrorList);
		this.assertNextToken(Symbol.RIGHTCURBRACKET);
		
		return new WhileStat(expr, statList);
	}
	
	private StatementList statementList(List<CompilationError> compilationErrorList) {
		final List<Statement> statList = new ArrayList<>();
		
		while(this.checkNextToken(Symbol.IF)
				|| this.checkNextToken(Symbol.WHILE)
				|| this.checkNextToken(Symbol.RETURN)
				|| this.checkNextToken(Symbol.OUT)
				|| this.checkNextToken(Symbol.BREAK)
				|| this.checkNextToken(Symbol.SEMICOLON)
				|| this.checkNextToken(Symbol.REPEAT)
				|| this.checkNextToken(Symbol.VAR)
				|| this.checkNextToken(Symbol.ASSERT)
				|| this.checkNextToken(Symbol.PLUS)
				|| this.checkNextToken(Symbol.MINUS)
				|| this.checkNextToken(Symbol.LEFTPAR)
				|| this.checkNextToken(Symbol.NOT)
				|| this.checkNextToken(Symbol.NULL)
				|| this.checkNextToken(Symbol.LITERALINT)
				|| this.checkNextToken(Symbol.LITERALSTRING)
				|| this.checkNextToken(Symbol.TRUE)
				|| this.checkNextToken(Symbol.FALSE)
				|| this.checkNextToken(Symbol.ID)
				|| this.checkNextToken(Symbol.SUPER)
				|| this.checkNextToken(Symbol.SELF)) {
			statList.add(this.statement(compilationErrorList));
		}
		
		return new StatementList(statList);
	}
	
	private Term term(List<CompilationError> compilationErrorList) {
		final SignalFactor signalFactor = this.signalFactor(compilationErrorList);
		final List<HighOperator> opList = new ArrayList<>();
		final List<SignalFactor> signalList = new ArrayList<>();
		
		while(this.checkNextToken(Symbol.MULT) || this.checkNextToken(Symbol.DIV) || this.checkNextToken(Symbol.AND)) {
			opList.add(this.highOperator(compilationErrorList));
			signalList.add(this.signalFactor(compilationErrorList));
		}
		
		return new Term(signalFactor, opList, signalList);
	}
	
	private PrintStat printStat(List<CompilationError> compilationErrorList) {
		this.assertNextToken(Symbol.OUT);
		this.assertNextToken(Symbol.DOT);
		String func = null;
		if(this.checkNextToken(Symbol.PRINT) || this.checkNextToken(Symbol.PRINTLN)) {
			lexer.nextToken();
			func = lexer.getCurrentToken().toString();
		}
		final Expression expr = this.expression(compilationErrorList);
		final List<Expression> exprList = new ArrayList<>();
		while(checkNextToken(Symbol.COMMA)) {
			lexer.nextToken();
			exprList.add(this.expression(compilationErrorList));
		}
		
		return new PrintStat(func, expr, exprList);
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

	private void breakStat() {
		next();

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

	private Type type() {
		return null;
	}

	private Qualifier qualifier() {
		if ( lexer.peek(1).getSymbol() == Token.Symbol.PRIVATE ) {
			next();
		}
		else if ( lexer.peek(1).getSymbol() == Token.Symbol.PUBLIC ) {
			next();
		}
		else if ( lexer.peek(1).getSymbol() == Token.Symbol.OVERRIDE ) {
			next();
			if ( lexer.peek(1).getSymbol() == Token.Symbol.PUBLIC ) {
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
		
		return new Qualifier();
	}
	
	private ReadExpr readExpr(List<CompilationError> compilationErrorList) {
		assertNextToken(Symbol.IN);
		assertNextToken(Symbol.DOT);
		if (!checkNextToken(Symbol.READINT) || !checkNextToken(Symbol.READSTRING)) {
			this.error("Expected readInt or readString");
		}
		assertNextToken(Symbol.LEFTPAR);
		
		return new ReadExpr();
	}
	
	private RepeatStat repeatStat(final List<CompilationError> compilationErrorList) {
		assertNextToken(Symbol.REPEAT);
		StatementList statList = this.statementList(compilationErrorList);
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
	
	
	
	private PrimaryExpr primaryExpression(List<CompilationError> compilationErrorList) {
		String idColon = null;
		String idFirst = null;
		String idSecond = null;
		ExpressionList expList = null;
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
	
	private Signal signal(final List<CompilationError> compilationErrorList) {
		String sinal = "";
		if (checkNextToken(Symbol.PLUS) || checkNextToken(Symbol.MINUS)) {
			lexer.nextToken();
			sinal = lexer.getCurrentToken().getValue();
		}
		return new Signal(sinal);
	}

	private ReturnStat returnStat(final List<CompilationError> compilationErrorList) {
		assertNextToken(Symbol.RETURN);
		Expression exp = this.expression(compilationErrorList);
		return new ReturnStat(exp);
	}

	private static boolean startExpr(Token.Symbol token) {

		return token == Token.Symbol.FALSE || token == Token.Symbol.TRUE
				|| token == Token.Symbol.NOT || token == Token.Symbol.SELF
				|| token == Token.Symbol.LITERALINT || token == Token.Symbol.SUPER
				|| token == Token.Symbol.LEFTPAR || token == Token.Symbol.NULL
				|| token == Token.Symbol.ID || token == Token.Symbol.LITERALSTRING;

	}

	private Lexer			lexer;
	private ErrorSignaller	signalError;

}
