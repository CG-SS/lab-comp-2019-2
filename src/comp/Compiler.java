/*
 * Cristiano Guilherme - 609803
 * Daniel Davoli       - 610372
 */

package comp;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ast.AssertStat;
import ast.AssignExpr;
import ast.BasicValue;
import ast.BreakStat;
import ast.ClassDec;
import ast.EmptyStat;
import ast.Expression;
import ast.ExpressionFactor;
import ast.Factor;
import ast.FieldDec;
import ast.HighOperator;
import ast.IfStat;
import ast.LocalDec;
import ast.LowOperator;
import ast.Member;
import ast.MetaobjectAnnotation;
import ast.MethodDec;
import ast.NilFactor;
import ast.NotFactor;
import ast.ObjectCreation;
import ast.OperatorTerm;
import ast.ParamDec;
import ast.PrimaryExpr;
import ast.PrimaryExprDoubleId;
import ast.PrimaryExprId;
import ast.PrimaryExprIdIdColon;
import ast.PrimaryExprSelf;
import ast.PrimaryExprSelfDoubleId;
import ast.PrimaryExprSelfId;
import ast.PrimaryExprSelfIdColon;
import ast.PrimaryExprSelfIdIdColon;
import ast.PrimaryExprSuperId;
import ast.PrimaryExprSuperIdColon;
import ast.PrintStat;
import ast.Program;
import ast.Qualifier;
import ast.ReadExpr;
import ast.Relation;
import ast.RelationExpression;
import ast.RepeatStat;
import ast.ReturnStat;
import ast.Signal;
import ast.SignalFactor;
import ast.SimpleExpression;
import ast.SimpleSubExpression;
import ast.Statement;
import ast.StatementList;
import ast.SumSimpleExpression;
import ast.SumSubExpression;
import ast.Term;
import ast.Type;
import ast.WhileStat;
import lexer.Lexer;
import lexer.Token;
import lexer.Token.Symbol;

public class Compiler {
	
	private HashMap<String, ClassDec> classNameMap;
	private HashMap<String, Type> localVarMap;
	private ClassDec currentClass;
	private Type currentMethodReturnType;
	private boolean foundReturn;

	// compile must receive an input with an character less than
	// p_input.lenght
	public Program compile(final char[] input, final PrintWriter outError) {
		ArrayList<CompilationError> compilationErrorList = new ArrayList<>();
		signalError = new ErrorSignaller(outError, compilationErrorList);
		classNameMap = new HashMap<>();
		lexer = new Lexer(input, signalError);
		signalError.setLexer(lexer);
		currentClass = null;
		foundReturn = false;
		localVarMap = null;
		currentMethodReturnType = null;

		//System.out.println("Program compile"); // REMOVE_LATER
		Program program = null;
		//lexer.nextToken();
		program = program(compilationErrorList);
		return program;
	}
	
	private void assertNextToken(final Token.Symbol symbol) {
		lexer.nextToken();
		
		final Symbol s = lexer.getCurrentToken().getSymbol();
		if(s != symbol) {
			if (s == Symbol.ERROR) {
				//System.out.println("Error encontrado " + lexer.getCurrentToken().getValue());
				error(lexer.getCurrentToken().getValue());
			} else {
				//System.out.println("Error at line " + lexer.getCurrentToken().getLine() + " \n " + lexer.getLine(lexer.getCurrentToken().getLine()));
				error("Symbol " + symbol.toString() + " expected, found " + lexer.getCurrentToken());
			}
		}
	}
	
	private boolean checkNextToken(final Token.Symbol symbol) {
		final Symbol s = lexer.peek(1).getSymbol();
		
		if(s == Symbol.ERROR) {
			lexer.nextToken();
			//System.out.println("Error encontrado " + lexer.getCurrentToken().getValue());
			this.error(lexer.getCurrentToken().getValue());
		}
		
		return s == symbol;
	}
	/*
	private MetaobjectAnnotation annot(final List<CompilationError> compilationErrorList) {
		this.assertNextToken(Symbol.ANNOT);
		this.assertNextToken(Symbol.ID);
		
		final Token id = lexer.getCurrentToken();
		final List<AnnotParam> annotParamList = new ArrayList<>();
		
		if(checkNextToken(Symbol.LEFTPAR)) {
			lexer.nextToken();
			while(!checkNextToken(Symbol.RIGHTPAR)) {
				final AnnotParam annotParam = this.annotParam(compilationErrorList);
				annotParamList.add(annotParam);
			}
			lexer.nextToken();
		}
		
		return new MetaobjectAnnotation(id.getValue(), annotParamList);
	}*/
	
	private String basicType() {
		if(!checkNextToken(Symbol.STRING) && !checkNextToken(Symbol.INT) && !checkNextToken(Symbol.BOOLEAN)) {
			this.error("Valid types are 'Int', 'String' or 'Boolean'");
		}
		lexer.nextToken();
		
		return lexer.peek(0).toString();
	}
	
	private BasicValue basicValue() {
		if(!checkNextToken(Symbol.LITERALINT) && !checkNextToken(Symbol.LITERALSTRING) && !checkNextToken(Symbol.TRUE) && !checkNextToken(Symbol.FALSE)) {
			this.error("Expected literal boolean, int or string");
		}
		lexer.nextToken();

		if(lexer.getCurrentToken().getSymbol() == Symbol.TRUE || lexer.getCurrentToken().getSymbol() == Symbol.FALSE)
			return new BasicValue(lexer.getCurrentToken().toString(), new Type("Boolean"));
		
		Type type;
		if(lexer.getCurrentToken().getSymbol() == Symbol.LITERALSTRING)
			type = new Type("String");
		else 
			type = new Type("Int");
		
		return new BasicValue(lexer.getCurrentToken().getValue(), type);
	}
	
	private boolean isSubclassOf(final Type t1, final Type t2) {
		if(t1 == null || t2 == null)
			return false;
		//System.out.println("T1 " + t1.getId() + " t2 " + t2.getId());
		if(t1.getId().equals(t2.getId()))
			return true;
		if(t2.getId().equals("String") && t1.getId().equals("nil"))
			return true;
		if(this.isBasicType(t1) || this.isBasicType(t2)) {
			return false;
		}
		if(!this.isBasicType(t2) && t1.getId().equals("nil")) {
			return true;
		}
		
		final ClassDec leftClassDec = classNameMap.get(t1.getId());
		final ClassDec superClass = leftClassDec.getSuperClass();
		if(superClass == null) {
			return false;
		}
		if(superClass.getName().equals(t2.getId())) {
			return true;
		}
		
		return this.isSubclassOf(new Type(superClass.getName()), t2);
	}
	
	private AssignExpr assignExpr() {
		//System.out.println("AssignExpr " + lexer.getCurrentToken().getLine()); // REMOVE_LATER
		final Expression expr = this.expression();
		Expression assignExpr = null;
		
		if(this.checkNextToken(Symbol.ASSIGN)) {
			//System.out.println("AssignExpr = " + lexer.getCurrentToken().getLine()); // REMOVE_LATER
			lexer.nextToken();

			if(expr instanceof SignalFactor) {
				final SignalFactor signalFactor = (SignalFactor) expr;
				if(signalFactor.getFactor() instanceof PrimaryExprSelfId) {
					final PrimaryExprSelfId pesi = (PrimaryExprSelfId) signalFactor.getFactor();
					if(pesi.isMethod()) {
						this.error("Illegal assignment expression");
					}
				} else if(!(signalFactor.getFactor() instanceof PrimaryExprId)) {
					this.error("Illegal assignment expression");
				}
			} else {
				this.error("Illegal assignment expression");
			}
			
			assignExpr = this.expression();
			
			if(expr.getType() == null) {
				this.error("Cannot assign to null");
			}
			if(assignExpr.getType() == null) {
				this.error("Illegal right-side of expression, must have a type");
			}
			if(!this.isSubclassOf(assignExpr.getType(), expr.getType())) {
				this.error("Illegal assignment, both sides must be of the same type or the right side must be a subclass of left");
			}
			/*
			if(assignExpr.getType().getId().equals(Symbol.NIL.toString())) {
				final SignalFactor signalFactor = (SignalFactor) expr;
				if(signalFactor.getFactor() instanceof PrimaryExprSelfId) {
					final PrimaryExprSelfId pesi = (PrimaryExprSelfId) signalFactor.getFactor();
					System.out.println("Nil : " + pesi.getId());
					isNil.add(pesi.getId());
				} else {
					final PrimaryExprId pesi = (PrimaryExprId) signalFactor.getFactor();
					System.out.println("Nil : " + pesi.getId());
					isNil.add(pesi.getId());
				}
			}*/
			
		} else {
			if(expr instanceof SignalFactor) {
				final SignalFactor signalFactor = (SignalFactor) expr;
				if(signalFactor.getFactor() instanceof ExpressionFactor) {
					
				} else if(expr.getType() != null) {
					this.error("Illegal expression, must be assigned to a variable");
				}
			} else if(expr.getType() != null) {
				this.error("Illegal expression, must be assigned to a variable");
			}
		}
		
		return new AssignExpr(expr, assignExpr);
	}
	
	private boolean isBasicType(final Type type) {
		return (type.getId().equals("Int") || type.getId().equals("String") || type.getId().equals("Boolean"));
	}
	
	private Expression expression() {
		final SimpleExpression simpleExpr = this.simpleExpression();
		
		if(this.checkNextToken(Symbol.EQ) || this.checkNextToken(Symbol.NEQ) || this.checkNextToken(Symbol.LT) || this.checkNextToken(Symbol.GE) || this.checkNextToken(Symbol.GT) || this.checkNextToken(Symbol.LE)) {
			final Relation rel = this.relation();
			final SimpleExpression relExpr = this.simpleExpression();
			
			Type type;
			if(rel.getSymbol() == Symbol.GE || rel.getSymbol() == Symbol.GT || rel.getSymbol() == Symbol.LE || rel.getSymbol() == Symbol.LT) {
				if(!simpleExpr.getType().getId().equals("Int") || !relExpr.getType().getId().equals("Int")) {
					this.error("Operators <, <=, >, >= must be used between Ints");
				}
				type = new Type("Boolean");
			} else  {
				final Type lftType = simpleExpr.getType();
				final Type rightType = relExpr.getType();
				
				if(this.isBasicType(lftType) && this.isBasicType(rightType)) {
					if(!lftType.getId().equals(rightType.getId())) {
						this.error("Illegal types for operator == and !=");
					}
					type = new Type("Boolean");
				} else if(lftType.getId().equals("String") && rightType.getId().equals("nil")) {
					type = new Type("Boolean");
				} else if(lftType.getId().equals("nil") && rightType.getId().equals("String")) {
					type = new Type("Boolean");
				} else if(lftType.getId().equals("nil") && rightType.getId().equals("nil")) {
					this.error("Can't compare nil and nil");
					type = null;
				} else if(this.isSubclassOf(lftType, rightType) || this.isSubclassOf(rightType, lftType)) {
					type = new Type("Boolean");
				} else {
					this.error("Illegal types for operator == and !=");
					type = null;
				}
			}
			
			return new RelationExpression(simpleExpr, rel, relExpr, type);
		} else {
			return simpleExpr;
		}
	}
	
	private AssertStat assertStat() {
		this.assertNextToken(Symbol.ASSERT);
		final Expression expr = this.expression();
		this.assertNextToken(Symbol.COMMA);
		this.assertNextToken(Symbol.LITERALSTRING);
		final String message = lexer.getCurrentToken().getValue();
		
		return new AssertStat(expr, message);
	}
	
	private Relation relation() {
		if(!checkNextToken(Symbol.EQ) && !checkNextToken(Symbol.NEQ) && !checkNextToken(Symbol.LT) && !checkNextToken(Symbol.GE) && !checkNextToken(Symbol.GT) && !checkNextToken(Symbol.LE)) {
			this.error("Relation symbol expected");
		}
		lexer.nextToken();
		
		return new Relation(lexer.getCurrentToken().getSymbol());
	}

	private List<Expression> expressionList() {
		final Expression expr = this.expression();
		final List<Expression> exprList = new ArrayList<>();
		
		exprList.add(expr);
		
		while(this.checkNextToken(Symbol.COMMA)) {
			lexer.nextToken();
			exprList.add(this.expression());
		}
		
		return exprList;
	}
	
	private Factor factor() {
		//System.out.println("Factor"); // REMOVE_LATER
		
		if(checkNextToken(Symbol.LITERALINT) || checkNextToken(Symbol.LITERALSTRING) || checkNextToken(Symbol.TRUE) || checkNextToken(Symbol.FALSE)) {
			//System.out.println("Factor lit"); // REMOVE_LATER
			return this.basicValue();
		} else if(checkNextToken(Symbol.ID) && lexer.peek(2).getSymbol() == Symbol.DOT && (lexer.peek(3).getSymbol() == Symbol.ID && lexer.peek(3).getValue().equals("new"))) {
			return this.objectCreation();
		} else if(checkNextToken(Symbol.ID) || checkNextToken(Symbol.SUPER) || this.checkNextToken(Symbol.SELF) || (this.checkNextToken(Symbol.ID) && lexer.peek(1).getValue().equals("in"))) {
			//System.out.println("Factor id super " + lexer.peek(0) + " " + lexer.peek(1) + " " + lexer.peek(2)); // REMOVE_LATER
			final PrimaryExpr primaryExpr = this.primaryExpression();
			
			return primaryExpr;
		} else if(checkNextToken(Symbol.LEFTPAR)) {
			//System.out.println("Factor leftpar"); // REMOVE_LATER
			lexer.nextToken();
			final Expression expression = this.expression();
			this.assertNextToken(Symbol.RIGHTPAR);
			
			return new ExpressionFactor(expression);
		} else if(checkNextToken(Symbol.NOT)) {
			//System.out.println("not"); // REMOVE_LATER
			lexer.nextToken();
			final Factor factor = this.factor();
			
			if(!factor.getType().getId().equals("Boolean")) {
				this.error("Operator ! must only be used on Booleans");
			}
			
			return new NotFactor(factor);
		} else {
			//System.out.println("Factor null " + lexer.peek(1).toString()); // REMOVE_LATER
			
			assertNextToken(Symbol.NIL);
			return new NilFactor();
		}
	}
	
	private ClassDec classDec() {
		String className;
		boolean openClassBol = false;
		
		if (this.checkNextToken(Symbol.ID) && lexer.peek(1).getValue().equals("open")) {
			openClassBol = true;
			lexer.nextToken();
		}
		
		this.assertNextToken(Symbol.CLASS);
		this.assertNextToken(Symbol.ID);
		className = lexer.getCurrentToken().getValue();
		if(classNameMap.containsKey(className)) {
			this.error("'" + className + "' already exists.");
		}
		final ClassDec newClassDec = new ClassDec(className);
		newClassDec.setOpenClass(openClassBol);
		currentClass = newClassDec;
		classNameMap.put(className, newClassDec);
		
		if(this.checkNextToken(Symbol.EXTENDS)) {
			newClassDec.setExtendsClass(true);
			lexer.nextToken();
			this.assertNextToken(Symbol.ID);
			final String extendsClassName = lexer.getCurrentToken().getValue();
			
			if(extendsClassName.equals(className)) {
				this.error("A class cannot extend itself");
			}
			if(!classNameMap.containsKey(extendsClassName)) {
				this.error("Undefined class '" + extendsClassName + "'");
			}
			
			final ClassDec extendsClass = classNameMap.get(extendsClassName);
			
			if(!extendsClass.isOpen()) {
				this.error("Can only inherit from an open class");
			}
			
			newClassDec.setSuperClassName(extendsClass);
		}
		
		// MemberList
		while(checkNextToken(Symbol.PUBLIC) || checkNextToken(Symbol.PRIVATE) || checkNextToken(Symbol.FINAL) || checkNextToken(Symbol.OVERRIDE) || checkNextToken(Symbol.VAR) || checkNextToken(Symbol.FUNC)) {
			Qualifier qualifier = null;
			
			if(checkNextToken(Symbol.PUBLIC) || checkNextToken(Symbol.PRIVATE) || checkNextToken(Symbol.FINAL) || checkNextToken(Symbol.OVERRIDE)) {
				qualifier = this.qualifier();
			}
			
			//Member
			if ( checkNextToken(Symbol.VAR) ) {
				if(qualifier != null && !qualifier.getString().equals(Symbol.PRIVATE.toString())) {
					this.error("Var member can only be private");
				}
				//FieldDec
				this.assertNextToken(Symbol.VAR);
				final Type type = this.type();
				
				this.assertNextToken(Symbol.ID);
				
				final String firstId = lexer.getCurrentToken().getValue();
				if(currentClass.hasMember(firstId)) {
					this.error("Redeclaration of var " + firstId);
				}
				newClassDec.addMember(new FieldDec(firstId, new Qualifier(Symbol.PRIVATE.toString(), false, false), type));
				
				while(this.checkNextToken(Symbol.COMMA)) {
					lexer.nextToken();
					
					this.assertNextToken(Symbol.ID);
					final String secondId = lexer.getCurrentToken().getValue();
					if(currentClass.hasMember(secondId)) {
						this.error("Redeclaration of var " + secondId);
					}
					newClassDec.addMember(new FieldDec(secondId, new Qualifier(Symbol.PRIVATE.toString(), false, false), type));
				}
				
				if(this.checkNextToken(Symbol.SEMICOLON)) {
					lexer.nextToken();
				}// FieldDec
			}
			else { // MethodDec
				if(qualifier == null) {
					qualifier = new Qualifier(Symbol.PUBLIC.toString(), false ,false);
				}
				
				this.assertNextToken(Symbol.FUNC);
				
				String idColon = null;
				List<ParamDec> formalParamDec = null;
				Type type = null;
				currentMethodReturnType = null;
				
				localVarMap = new HashMap<>();
				
				final List<Statement> statList = new ArrayList<>();
				
				if(this.checkNextToken(Symbol.IDCOLON)) {
					lexer.nextToken();
					
					idColon = lexer.getCurrentToken().getValue();
					
					if(idColon.equals("run:") && currentClass.getName().equals("Program")) {
						this.error("Method run of class Program cannot take parameters");
					}
					
					if(qualifier.getString().equals(Symbol.PRIVATE.toString())) {
						if(currentClass.getPrivateFunction(idColon) != null) {
							this.error("Redeclaration of method " + idColon);
						} else if(currentClass.getPrivateVar(idColon) != null) {
							this.error("Method " + idColon + " has same id as previously declared member");
						}
					} else {
						if(qualifier.isOverride()) {
							final MethodDec superClassMethod = currentClass.getSuperClass().getPublicFunction(idColon);
							
							if(superClassMethod == null) {
								this.error("Can't override method that doesn't exist on any superclass");
							}
						} else if(currentClass.getSuperClass() != null) {
							final MethodDec superClassMethod = currentClass.getSuperClass().getPublicFunction(idColon);
							
							if(superClassMethod != null) {
								this.error("Keyword override is needed to override method from superclass");
							}
						}
						
						if(currentClass.hasMember(idColon)) {
							this.error("Redeclaration of member " + idColon);
						}
					}
					
					formalParamDec = this.formalParamDec();
					
					if(qualifier.isOverride()) {
						final MethodDec superClassMethod = currentClass.getSuperClass().getPublicFunction(idColon);
						final List<ParamDec> superClassMethodParams = superClassMethod.getParameters();
						
						if(superClassMethodParams.size() != formalParamDec.size()) {
							this.error("Number of arguments differs from superclass method declaration");
						}
						for(int i = 0; i < superClassMethodParams.size(); i++) {
							if(!this.isSubclassOf(formalParamDec.get(i).getType(), superClassMethodParams.get(i).getType())) {
								this.error("Incompatible types with superclass method definition");
							}
						}
					}
				} else {
					this.assertNextToken(Symbol.ID);
					
					idColon = lexer.getCurrentToken().getValue();
					
					if(!qualifier.getString().equals(Symbol.PUBLIC.toString()) && idColon.equals("run") && currentClass.getName().equals("Program")) {
						this.error("Method run of class Program must be public");
					}
					
					if(qualifier.isOverride()) {
						final MethodDec superClassMethod = currentClass.getSuperClass().getPublicFunction(idColon);
						
						if(superClassMethod == null) {
							this.error("Can't override method that doesn't exist on any superclass");
						}
					} else if(currentClass.getSuperClass() != null) {
						final MethodDec superClassMethod = currentClass.getSuperClass().getPublicFunction(idColon);
						
						if(superClassMethod != null) {
							this.error("Keyword override is needed to override method from superclass");
						}
					}
					
					if(currentClass.hasMember(idColon)) {
						this.error("Name " + idColon + " already being used by previously declared member");
					}
				}
				
				if(this.checkNextToken(Symbol.MINUS_GT)) { // Return type
					lexer.nextToken();
					
					if(idColon.equals("run") && currentClass.getName().equals("Program")) {
						this.error("Method run of class Program cannot have a return value");
					}
					
					type = this.type();
					
					if(qualifier.isOverride()) {
						final MethodDec superClassMethod = currentClass.getSuperClass().getPublicFunction(idColon);
						if(!this.isSubclassOf(type, superClassMethod.getType())) {
							this.error("Return type differs from superclass method declaration");
						}
					}
					
					currentMethodReturnType = type;
				}
				
				final MethodDec newMethodMember = new MethodDec(idColon, formalParamDec, type, qualifier);
				newClassDec.addMember(newMethodMember);
				
				foundReturn = false;
				this.assertNextToken(Symbol.LEFTCURBRACKET);
				while(this.checkNextToken(Symbol.IF)
						|| this.checkNextToken(Symbol.WHILE)
						|| this.checkNextToken(Symbol.RETURN)
						|| (this.checkNextToken(Symbol.ID) && lexer.peek(1).getValue().equals("In"))
						|| this.checkNextToken(Symbol.BREAK)
						|| this.checkNextToken(Symbol.SEMICOLON)
						|| this.checkNextToken(Symbol.REPEAT)
						|| this.checkNextToken(Symbol.VAR)
						|| this.checkNextToken(Symbol.ASSERT)
						|| this.checkNextToken(Symbol.PLUS)
						|| this.checkNextToken(Symbol.MINUS)
						|| this.checkNextToken(Symbol.LEFTPAR)
						|| this.checkNextToken(Symbol.NOT)
						|| this.checkNextToken(Symbol.NIL)
						|| this.checkNextToken(Symbol.LITERALINT)
						|| this.checkNextToken(Symbol.LITERALSTRING)
						|| this.checkNextToken(Symbol.TRUE)
						|| this.checkNextToken(Symbol.FALSE)
						|| this.checkNextToken(Symbol.ID)
						|| this.checkNextToken(Symbol.SUPER)
						|| this.checkNextToken(Symbol.SELF)) {
					final Statement stat = this.statement(false);
					if(stat instanceof BreakStat) {
						this.error("Illegal statement, break can only be used inside a loop");
					}
					if(stat instanceof ReturnStat) {
						foundReturn = true;
					}
					statList.add(stat);
				}
				
				this.assertNextToken(Symbol.RIGHTCURBRACKET);
				
				if(currentMethodReturnType != null && !foundReturn) {
					this.error("Missing return stat");
				}
				
				newMethodMember.setStatList(statList);
				//newClassDec.addMember(new MethodDec(idColon, formalParamDec, type, statList, qualifier));
			}// Member
		}// MemberList
		
		//final List<MemberList> memberList = this.memberList();
		
		this.assertNextToken(Symbol.END);
		if(currentClass.getName().equals("Program")) {
			final Member runMember = currentClass.getMember("run");
			
			if(runMember == null) {
				this.error("Couldn't find method run not found on class Program");
			}
			if(!(runMember instanceof MethodDec)) {
				this.error("Couldn't find method run not found on class Program");
			}
		}
		
		classNameMap.put(className, newClassDec);
		
		return newClassDec;
	}
	
	/*
	private FieldDec fieldDec(final Qualifier qualifier) {
		this.assertNextToken(Symbol.VAR);
		final Type type = this.type();
		final IdList idList = this.idList();
		if(this.checkNextToken(Symbol.SEMICOLON)) {
			lexer.nextToken();
		}
		
		return new FieldDec(type, idList);
	}*/
	/*
	private List<String> idList() {
		this.assertNextToken(Symbol.ID);
		final List<String> idList = new ArrayList<>();
		idList.add(lexer.getCurrentToken().getValue());
		
		while(this.checkNextToken(Symbol.COMMA)) {
			lexer.nextToken();
			
			this.assertNextToken(Symbol.ID);
			idList.add(lexer.getCurrentToken().getValue());
		}
		
		return idList;
	}*/

	private IfStat ifStat(final boolean isInsideLoop) {
		
		this.assertNextToken(Symbol.IF);
		final Expression expr = this.expression(); // Expression exp = this.expression(compilationErrorList);
		
		if(!expr.getType().getId().equals("Boolean")) {
			this.error("If expression must be Boolean");
		}
		
		this.assertNextToken(Symbol.LEFTCURBRACKET);
		
		final StatementList ifStatList = this.statementList(isInsideLoop);
		/*
		final Statement stat = this.statement(isInsideLoop);
		if(!isInsideLoop) {
			if(stat instanceof BreakStat) {
				this.error("Illegal statement, break can only be used inside a loop");
			}
		}*/
		
		this.assertNextToken(Symbol.RIGHTCURBRACKET);
		
		StatementList elseStatList = null;
		if (this.checkNextToken(Symbol.ELSE)) {
			lexer.nextToken();
			assertNextToken(Symbol.LEFTCURBRACKET);
			
			elseStatList = this.statementList(isInsideLoop);
			/*
			elseStat = this.statement(isInsideLoop);
			
			if(!isInsideLoop) {
				if(elseStat instanceof BreakStat) {
					this.error("Illegal statement, break can only be used inside a loop");
				}
			}*/
			
			assertNextToken(Symbol.RIGHTCURBRACKET);
		}
		
		return new IfStat(expr, ifStatList, elseStatList);
		
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

	private LowOperator lowOperator() {
		if (!checkNextToken(Symbol.PLUS) && !checkNextToken(Symbol.MINUS) && !checkNextToken(Symbol.OR)) {
			this.error("Expected +, - or || ");
		}
		lexer.nextToken();
		
		return new LowOperator(this.lexer.getCurrentToken().getSymbol());
	}
	
	/*private MethodDec methodDec() {
		this.assertNextToken(Symbol.FUNC);
		
		String idColon = null;
		FormalParamDec formalParamDec = null;
		Type type = null;
		final List<Statement> statList = new ArrayList<>();
		
		if(this.checkNextToken(Symbol.IDCOLON)) {
			lexer.nextToken();
			idColon = lexer.getCurrentToken().getValue();
			formalParamDec = this.formalParamDec();
		} else {
			this.assertNextToken(Symbol.ID);
		}
		if(this.checkNextToken(Symbol.MINUS_GT)) {
			lexer.nextToken();
			type = this.type();
		}
		this.assertNextToken(Symbol.LEFTCURBRACKET);
		while(this.checkNextToken(Symbol.IF)
				|| this.checkNextToken(Symbol.WHILE)
				|| this.checkNextToken(Symbol.RETURN)
				|| (this.checkNextToken(Symbol.ID) && lexer.peek(1).getValue().equals("In"))
				|| this.checkNextToken(Symbol.BREAK)
				|| this.checkNextToken(Symbol.SEMICOLON)
				|| this.checkNextToken(Symbol.REPEAT)
				|| this.checkNextToken(Symbol.VAR)
				|| this.checkNextToken(Symbol.ASSERT)
				|| this.checkNextToken(Symbol.PLUS)
				|| this.checkNextToken(Symbol.MINUS)
				|| this.checkNextToken(Symbol.LEFTPAR)
				|| this.checkNextToken(Symbol.NOT)
				|| this.checkNextToken(Symbol.NIL)
				|| this.checkNextToken(Symbol.LITERALINT)
				|| this.checkNextToken(Symbol.LITERALSTRING)
				|| this.checkNextToken(Symbol.TRUE)
				|| this.checkNextToken(Symbol.FALSE)
				|| this.checkNextToken(Symbol.ID)
				|| this.checkNextToken(Symbol.SUPER)
				|| this.checkNextToken(Symbol.SELF)) {
			statList.add(this.statement());
		}
		this.assertNextToken(Symbol.RIGHTCURBRACKET);
		
		return new MethodDec(idColon, formalParamDec, type, statList);

	}*/
	
	private ParamDec paramDec() {
		final Type type = this.type();
		this.assertNextToken(Symbol.ID);
		final String idName = lexer.getCurrentToken().getValue();
		
		if(localVarMap.containsKey(idName)) {
			this.error("Redeclaration of var " + idName);
		}
		
		localVarMap.put(idName, type);
		
		return new ParamDec(type, idName);
	}
	
	
	
	private ObjectCreation objectCreation() {
		//System.out.println("ObjCreation"); // REMOVE_LATER
		this.assertNextToken(Symbol.ID);
		final String idName = lexer.getCurrentToken().getValue();
		
		if(!classNameMap.containsKey(idName)) {
			this.error("Class " + idName + " not defined");
		}
		
		this.assertNextToken(Symbol.DOT);
		this.assertNextToken(Symbol.ID);
		if(!lexer.getCurrentToken().getValue().equals("new")) {
			this.error("Expected 'new' keyword on object creation.");
		}
		
		return new ObjectCreation(idName);
	}
	
	/*private Member member(final Qualifier qualifier) {
		if ( checkNextToken(Symbol.VAR) ) {
			return fieldDec = this.fieldDec(qualifier);
		}
		else {
			return methodDec = methodDec(qualifier);
		}
	}*/

	private List<ParamDec> formalParamDec() {
		final ParamDec paramDec = this.paramDec();
		final List<ParamDec> paramDecList = new ArrayList<>();
		
		paramDecList.add(paramDec);
		
		while(checkNextToken(Symbol.COMMA)) {
			lexer.nextToken();
			paramDecList.add(this.paramDec());
		}
		
		return paramDecList;
	}
	
	private HighOperator highOperator() {
		if(!this.checkNextToken(Symbol.MULT) && !this.checkNextToken(Symbol.DIV) && !this.checkNextToken(Symbol.AND)) {
			this.error("Expected '*', '/' or '&&'");
		}
		lexer.nextToken();
		
		return new HighOperator(lexer.getCurrentToken().getSymbol());
	}
	
	private LocalDec localDec() {
		//System.out.println("Local dec");
		
		this.assertNextToken(Symbol.VAR);
		final Type type = this.type();
		
		this.assertNextToken(Symbol.ID);
		final List<String> idList = new ArrayList<>();
		final String firstId = lexer.getCurrentToken().getValue();
		if(localVarMap.containsKey(firstId)) {
			this.error("Redeclaration of var " + firstId);
		}
		localVarMap.put(firstId, type);
		idList.add(firstId);
		
		while(this.checkNextToken(Symbol.COMMA)) {
			lexer.nextToken();
			
			this.assertNextToken(Symbol.ID);
			final String secondId = lexer.getCurrentToken().getValue();
			if(localVarMap.containsKey(secondId)) {
				this.error("Redeclaration of var " + secondId);
			}
			localVarMap.put(secondId, type);
			idList.add(secondId);
		}
		
		Expression expression = null;
		
		if(checkNextToken(Symbol.ASSIGN)) {
			lexer.nextToken();
			expression = this.expression();
		}
		
		//System.out.println("Out LocalDec " + lexer.getCurrentToken() + " val " + lexer.getCurrentToken().getValue());
		
		return new LocalDec(type, idList, expression);
	}
	
	private Type type() {
		
		if(checkNextToken(Symbol.ID)) {
			lexer.nextToken();
			final String id = lexer.getCurrentToken().getValue();

			if(!classNameMap.containsKey(id)) {
				this.error("Undefined type '" + id + "'");
			}
			return new Type(id);
		} else {
			return new Type(this.basicType());
		}
	}

	private Program program(final ArrayList<CompilationError> compilationErrorList) {
		final ArrayList<MetaobjectAnnotation> metaobjectCallList = new ArrayList<>();
		final ArrayList<ClassDec> cianetoClassList = new ArrayList<>();
		
		final Program program = new Program(cianetoClassList, metaobjectCallList, compilationErrorList);
		boolean thereWasAnError = false;
		
		try {
			while ( this.checkNextToken(Symbol.CLASS) ||
					(this.checkNextToken(Symbol.ID) && lexer.peek(1).getValue().equals("open") ) ||
					this.checkNextToken(Symbol.ANNOT)) {
				
					while ( this.checkNextToken(Symbol.ANNOT) ) {
						metaobjectAnnotation(metaobjectCallList);
					}
	
					final ClassDec dec = classDec();
					program.addClassDec(dec);
			}
			
			boolean hasProgram = false;
			for(final ClassDec classD : cianetoClassList) {
				if(classD.getName().equals("Program")) {
					hasProgram = true;
					break;
				}
			}
			
			if(!hasProgram) {
				this.error("Missing class Program");
			}
		}
		catch( CompilerError e) {
			// if there was an exception, there is a compilation error
			thereWasAnError = true;
		}
		catch ( RuntimeException e ) {
			e.printStackTrace();
			thereWasAnError = true;
		}
		
		if ( !thereWasAnError && !this.checkNextToken(Symbol.EOF) ) {
			lexer.nextToken();
			try {
				error("End of file expected");
			}
			catch( CompilerError e) {
			}
		}
		
		
		
		//System.out.println("Stop");
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
	private void metaobjectAnnotation(final ArrayList<MetaobjectAnnotation> metaobjectAnnotationList) {
		//System.out.println("Annot " + lexer.getCurrentToken()); // REMOVE_LATER
		this.assertNextToken(Symbol.ANNOT);
		//System.out.println("Annot " + lexer.getCurrentToken().getValue()); // REMOVE_LATER
		final String name = lexer.getCurrentToken().getValue();
		final int lineNumber = lexer.getCurrentToken().getLine();
		
		final ArrayList<Object> metaobjectParamList = new ArrayList<>();
		
		//boolean getNextToken = false;
		if ( this.checkNextToken(Symbol.LEFTPAR) ) {
			// metaobject call with parameters
			lexer.nextToken();
			while ( this.checkNextToken(Symbol.LITERALINT) || this.checkNextToken(Symbol.LITERALSTRING) ||
					this.checkNextToken(Symbol.ID) ) {
				lexer.nextToken();
				
				switch ( lexer.peek(0).getSymbol() ) {
				case LITERALINT:
					final Integer intVal = Integer.parseInt(lexer.getCurrentToken().getValue());
					metaobjectParamList.add(intVal);
					break;
				case LITERALSTRING:
					metaobjectParamList.add(lexer.peek(0).getValue());
					break;
				case ID:
					metaobjectParamList.add(lexer.peek(0).getValue());
				}

				if ( this.checkNextToken(Symbol.COMMA) ) {
					lexer.nextToken();
				}
				//System.out.println("End" + lexer.getCurrentToken());
				//System.out.println("End" + lexer.peek(1));
			}
			
			//System.out.println("End" + lexer.getCurrentToken());
			
			if ( !this.checkNextToken(Symbol.RIGHTPAR) )
				error("')' expected after annotation with parameters, found " + lexer.getCurrentToken());
			else {
				lexer.nextToken();
				//getNextToken = true;
			}
		}
		switch ( name ) {
		case "nce":
			if ( metaobjectParamList.size() != 0 )
				error("Annotation 'nce' does not take parameters");
			break;
		case "cep":
			int sizeParamList = metaobjectParamList.size();
			if ( sizeParamList < 2 || sizeParamList > 4 )
			error("Annotation 'cep' takes two, three, or four parameters");

			if ( !( metaobjectParamList.get(0) instanceof Integer) ) {
			error("The first parameter of annotation 'cep' should be an integer number");
			}
			else {
			int ln = (Integer ) metaobjectParamList.get(0);
			metaobjectParamList.set(0, ln + lineNumber);
			}
			if ( !( metaobjectParamList.get(1) instanceof String) )
			error("The second parameter of annotation 'cep' should be a literal string");
			if ( sizeParamList >= 3 && !( metaobjectParamList.get(2) instanceof String) )
			error("The third parameter of annotation 'cep' should be a literal string");
			if ( sizeParamList >= 4 && !( metaobjectParamList.get(3) instanceof String) )
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
	}

	/*private ArrayList<MemberList> memberList() {
		final ArrayList<MemberList> memberList = new ArrayList<>(); 
		
		while(checkNextToken(Symbol.PUBLIC) || checkNextToken(Symbol.PRIVATE) || checkNextToken(Symbol.FINAL) || checkNextToken(Symbol.OVERRIDE) || checkNextToken(Symbol.VAR) || checkNextToken(Symbol.FUNC)) {
			Qualifier qualifier = null;
			Member member = null;
			
			if(checkNextToken(Symbol.PUBLIC) || checkNextToken(Symbol.PRIVATE) || checkNextToken(Symbol.FINAL) || checkNextToken(Symbol.OVERRIDE)) {
				qualifier = this.qualifier();
			}
			
			member = this.member();
			final MemberList ml = new MemberList(qualifier, member);
			memberList.add(ml);
			
			if(member.getFieldDec() != null) {
				final FieldDec fieldDec = member.getFieldDec();
				
			}
		}
		
		return memberList;
	}*/
	
	private SignalFactor signalFactor() {
		Signal signal = null;
		
		if(this.checkNextToken(Symbol.PLUS) || this.checkNextToken(Symbol.MINUS)) {
			signal = this.signal();
		}
		
		final Factor factor = this.factor();
		if(signal != null && !factor.getType().getId().equals("Int")) {
			this.error("Operator + and - can only be used on Ints");
		}
		
		return new SignalFactor(signal, factor, factor.getType());
	}
	
	private SumSubExpression sumSubExpression() {
		final Term term = this.term();
		
		if(!checkNextToken(Symbol.PLUS) && !checkNextToken(Symbol.MINUS) && !checkNextToken(Symbol.OR)) {
			return term;
		}
		
		final List<LowOperator> opList = new ArrayList<>();
		final List<Term> termList = new ArrayList<>();
		
		while(checkNextToken(Symbol.PLUS) || checkNextToken(Symbol.MINUS) || checkNextToken(Symbol.OR)) {
			final LowOperator lowOp = this.lowOperator();
			final Term rightTerm = this.term();
			
			opList.add(lowOp);
			termList.add(rightTerm);
			
			if(lowOp.getSymbol() == Symbol.PLUS || lowOp.getSymbol() == Symbol.MINUS) {
				if(!term.getType().getId().equals("Int") || !rightTerm.getType().getId().equals("Int")) {
					this.error("Operator + and - can only be used between Ints");
				}
			} else {
				if(!term.getType().getId().equals("Boolean") || !rightTerm.getType().getId().equals("Boolean")) {
					this.error("Operator || can only be used between Booleans");
				}
			}
		}
		
		return new SimpleSubExpression(term, opList, termList, term.getType());
	}

	private SimpleExpression simpleExpression() {
		final SumSubExpression sumSubExpr = this.sumSubExpression();
		
		if(!this.checkNextToken(Symbol.PLUSPLUS)) {
			return sumSubExpr;
		}
		
		final List<SumSubExpression> sumSubExpressionList = new ArrayList<>();
		
		if(!sumSubExpr.getType().getId().equals("Int") && !sumSubExpr.getType().getId().equals("String")) {
			this.error("Operator ++ can only be used on String or Int");
		}
		
		while(this.checkNextToken(Symbol.PLUSPLUS)) {
			lexer.nextToken();
			
			final SumSubExpression sumSubExpression = this.sumSubExpression();
			if(!sumSubExpression.getType().getId().equals("Int") && !sumSubExpression.getType().getId().equals("String")) {
				this.error("Operator ++ can only be used on String or Int");
			}
			sumSubExpressionList.add(sumSubExpression);
		}
		
		return new SumSimpleExpression(sumSubExpr, sumSubExpressionList, sumSubExpr.getType());
	}
	
	private void assertBeforeNextToken(final Symbol symbol) {
		final Symbol s = lexer.peek(1).getSymbol();
		if(s != symbol) {
			if (s == Symbol.ERROR) {
				//System.out.println("Error encontrado " + lexer.peek(1).getValue());
				error(lexer.peek(1).getValue());
			} else {
				//System.out.println("Error at line " + lexer.peek(1).getLine());
				error("Symbol " + symbol.toString() + " expected, found " + lexer.peek(1));
			}
		}
		
		lexer.nextToken();
	}
	
	private Statement statement(final boolean isInsideLoop) {
		
		if(checkNextToken(Symbol.IF)) {
			return this.ifStat(isInsideLoop);
		} else if(checkNextToken(Symbol.WHILE)) {
			return this.whileStat();
		} else if(checkNextToken(Symbol.RETURN)) {
			final ReturnStat returnStat = this.returnStat();
			
			this.assertBeforeNextToken(Symbol.SEMICOLON);
			
			return returnStat;
		} else if(checkNextToken(Symbol.ID) && lexer.peek(1).getValue().equals("Out")) {
			final PrintStat printStat = this.printStat();
			
			this.assertBeforeNextToken(Symbol.SEMICOLON);
			
			return printStat;
		} else if(checkNextToken(Symbol.BREAK)) {
			this.assertNextToken(Symbol.BREAK);
			this.assertBeforeNextToken(Symbol.SEMICOLON);
			
			return new BreakStat();
		} else if(checkNextToken(Symbol.REPEAT)) {
			final RepeatStat repeatStat = this.repeatStat();
			
			this.assertBeforeNextToken(Symbol.SEMICOLON);
			
			return repeatStat;
		} else if(checkNextToken(Symbol.VAR)) {
			final LocalDec localDec = this.localDec();
			
			this.assertBeforeNextToken(Symbol.SEMICOLON);
			
			return localDec;
		} else if(checkNextToken(Symbol.ASSERT)) {
			final AssertStat assertStat = this.assertStat();
			
			this.assertBeforeNextToken(Symbol.SEMICOLON);
			
			return assertStat;
		} else if(checkNextToken(Symbol.SEMICOLON)) {
			lexer.nextToken();
			
			return new EmptyStat();
		}
		
		final AssignExpr assignExpr = this.assignExpr();
		this.assertBeforeNextToken(Symbol.SEMICOLON);
		
		return assignExpr;
}

	private WhileStat whileStat() {
		this.assertNextToken(Symbol.WHILE);
		final Expression expr = this.expression();
		if(!expr.getType().getId().equals("Boolean")) {
			this.error("Expression in while must be Boolean");
		}
		this.assertNextToken(Symbol.LEFTCURBRACKET);
		
		final StatementList statList = this.statementList(true);
		
		this.assertNextToken(Symbol.RIGHTCURBRACKET);
		
		return new WhileStat(expr, statList);
	}
	
	private StatementList statementList(final boolean isInsideLoop) {
		final List<Statement> statList = new ArrayList<>();
		
		while(this.checkNextToken(Symbol.IF)
				|| this.checkNextToken(Symbol.WHILE)
				|| this.checkNextToken(Symbol.RETURN)
				|| (this.checkNextToken(Symbol.ID) && lexer.peek(1).getValue().equals("Out"))
				|| this.checkNextToken(Symbol.BREAK)
				|| this.checkNextToken(Symbol.SEMICOLON)
				|| this.checkNextToken(Symbol.REPEAT)
				|| this.checkNextToken(Symbol.VAR)
				|| this.checkNextToken(Symbol.ASSERT)
				|| this.checkNextToken(Symbol.PLUS)
				|| this.checkNextToken(Symbol.MINUS)
				|| this.checkNextToken(Symbol.LEFTPAR)
				|| this.checkNextToken(Symbol.NOT)
				|| this.checkNextToken(Symbol.NIL)
				|| this.checkNextToken(Symbol.LITERALINT)
				|| this.checkNextToken(Symbol.LITERALSTRING)
				|| this.checkNextToken(Symbol.TRUE)
				|| this.checkNextToken(Symbol.FALSE)
				|| this.checkNextToken(Symbol.ID)
				|| this.checkNextToken(Symbol.SUPER)
				|| this.checkNextToken(Symbol.SELF)) {
			final Statement stat = this.statement(isInsideLoop);
			if(stat instanceof BreakStat && !isInsideLoop) {
				this.error("Illegal statement, break can only be used inside a loop");
			}
			if(stat instanceof ReturnStat) {
				foundReturn = true;
			}
			statList.add(stat);
		}
		
		return new StatementList(statList);
	}
	
	private Term term() {
		final SignalFactor signalFactor = this.signalFactor();
		
		if(!this.checkNextToken(Symbol.MULT) && !this.checkNextToken(Symbol.DIV) && !this.checkNextToken(Symbol.AND)) {
			return signalFactor;
		}
		
		final List<HighOperator> opList = new ArrayList<>();
		final List<SignalFactor> signalList = new ArrayList<>();
		
		while(this.checkNextToken(Symbol.MULT) || this.checkNextToken(Symbol.DIV) || this.checkNextToken(Symbol.AND)) {
			final HighOperator ho = this.highOperator();
			final SignalFactor sf = this.signalFactor();
			
			opList.add(ho);
			signalList.add(sf);
			
			if(ho.getSymbol() == Symbol.MULT || ho.getSymbol() == Symbol.DIV) {
				if(!signalFactor.getType().getId().equals("Int") || !sf.getType().getId().equals("Int")) {
					this.error("Operators * and / can only be used between Ints");
				}
			} else {
				if(!signalFactor.getType().getId().equals("Boolean") || !sf.getType().getId().equals("Boolean")) {
					this.error("Operator && can only be used between Booleans");
				}
			}
		}
		
		return new OperatorTerm(signalFactor, opList, signalList, signalFactor.getType());
	}
	
	private PrintStat printStat() {
		this.assertNextToken(Symbol.ID);
		if(!lexer.getCurrentToken().getValue().equals("Out")) {
			this.error("Expected 'Out' for printing");
		}
		this.assertNextToken(Symbol.DOT);
		boolean newline;
		
		//System.out.println("PrintStat " + lexer.peek(1).toString());
		
		this.assertNextToken(Symbol.IDCOLON);
		// TODO
		if(lexer.getCurrentToken().getValue().equals("print:")) {
			newline = false;
		} else if(lexer.getCurrentToken().getValue().equals("println:")){
			newline = true;
		} else {
			newline = false;
			this.error("Print functions must be either print or println, found " + lexer.getCurrentToken().getValue());
		}
		
		final Expression expr = this.expression();
		if(!expr.getType().getId().equals("Int") && !expr.getType().getId().equals("String")) {
			this.error("Can only print Int or String");
		}
		final List<Expression> exprList = new ArrayList<>();
		while(checkNextToken(Symbol.COMMA)) {
			lexer.nextToken();
			
			final Expression newExpr = this.expression();
			
			if(!newExpr.getType().getId().equals("Int") && !newExpr.getType().getId().equals("String")) {
				this.error("Can only print Int or String");
			}
			exprList.add(newExpr);
		}
		
		return new PrintStat(newline, expr, exprList);
	}

	private void error(String msg) {
		this.signalError.showError(msg);
	}

	private Qualifier qualifier() {
		if(this.checkNextToken(Symbol.PRIVATE)) {
			lexer.nextToken();
			
			return new Qualifier(Symbol.PRIVATE.toString(), false, false);
		} else if (this.checkNextToken(Symbol.PUBLIC)) {
			lexer.nextToken();
			
			return new Qualifier(Symbol.PUBLIC.toString(), false, false);
		} else if (this.checkNextToken(Symbol.OVERRIDE)) {
			lexer.nextToken();
			
			if(currentClass.getSuperClass() == null) {
				this.error("Invalid qualifier, 'override' can only be used if a class has a superclass");
			}
			
			if(this.checkNextToken(Symbol.PUBLIC)) {
				lexer.nextToken();
			}
			return new Qualifier(Symbol.PUBLIC.toString(), false, true);
		} else {
			this.assertNextToken(Symbol.FINAL);
			
			if(!currentClass.isOpen()) {
				this.error("Invalid qualifier, 'final' can only be used on open classes");
			}
			
			if(this.checkNextToken(Symbol.PUBLIC)) {
				lexer.nextToken();
				
				return new Qualifier(Symbol.PUBLIC.toString(), true, false);
			} else if(this.checkNextToken(Symbol.OVERRIDE)) {
				lexer.nextToken();
				
				if(currentClass.getSuperClass() == null) {
					this.error("Invalid qualifier, 'override' can only be used if a class has a superclass");
				}
				
				if(this.checkNextToken(Symbol.PUBLIC)) {
					lexer.nextToken();
				}
				return new Qualifier(Symbol.PUBLIC.toString(), true, true);
			} else {
				return new Qualifier(Symbol.PUBLIC.toString(), true, false);
			}
		}
	}
	
	private ReadExpr readExpr() {
		//System.out.println("Read expr"); // REMOVE_LATER
		
		assertNextToken(Symbol.ID);
		if(!lexer.getCurrentToken().getValue().equals("In")) {
			this.error("Expected 'In'");
		}
		assertNextToken(Symbol.DOT);
		this.assertNextToken(Symbol.ID);
		
		if (!lexer.getCurrentToken().getValue().equals("readInt") && !lexer.getCurrentToken().getValue().equals("readString")) {
			this.error("Expected readInt or readString");
		}
		final Token read = lexer.getCurrentToken();
		
		Type type;
		//System.out.println("Read expr value " + read.getValue());
		if(read.getValue().equals("readInt")) {
			type = new Type("Int");
		} else {
			type = new Type("String");
		}
		
		//System.out.println("Read expr type " + type.getId());
		
		return new ReadExpr(read, type);
	}
	
	private RepeatStat repeatStat() {
		assertNextToken(Symbol.REPEAT);

		StatementList statList = this.statementList(true);

		assertNextToken(Symbol.UNTIL);
		final Expression expr = this.expression();
		
		if(!expr.getType().getId().equals("Boolean")) {
			this.error("Expression on repeat unil must be Boolean");
		}
		
		return new RepeatStat(statList, expr);
		/*
		next();
		while ( lexer.peek(0).getSymbol() != Token.Symbol.UNTIL && lexer.peek(0).getSymbol() != Token.Symbol.RIGHTCURBRACKET && lexer.peek(0).getSymbol() != Token.Symbol.END ) {
			statement();
		}
		check(Token.Symbol.UNTIL, "missing keyword 'until'");
		*/
	}
	
	private MethodDec getSuperclassMethod(final ClassDec clDec, final String id) {
		final ClassDec superClass = clDec.getSuperClass();
		if(superClass == null)
			return null;
		
		final MethodDec methodDec = superClass.getPublicFunction(id);
		if(methodDec == null)
			return this.getSuperclassMethod(superClass, id);
		return methodDec;
	}
	
	private PrimaryExpr primaryExpression() { 
		//System.out.println("Primary expr " + lexer.getCurrentToken().getLine());
		
		if(this.checkNextToken(Symbol.SUPER)) {
			lexer.nextToken();
			this.assertNextToken(Symbol.DOT);
			
			final ClassDec superClassName = currentClass.getSuperClass();
			if(superClassName == null) {
				this.error("Class '" + currentClass.getName() + "' has no superclass");
			}
			
			if(this.checkNextToken(Symbol.IDCOLON)) {
				lexer.nextToken();
				
				final String idColonName = lexer.getCurrentToken().getValue();
				
				final MethodDec superMethod = this.getSuperclassMethod(currentClass, idColonName);
				if(superMethod == null) {
					this.error("Superclass of '" + currentClass.getName() + "' and its superclasses has no public method '" + idColonName + "'");
				}
				
				final List<Expression> exprList = this.expressionList();
				final List<ParamDec> superMethodParams = superMethod.getParameters();
				
				if(exprList.size() != superMethod.getParameters().size()) {
					this.error("Number of parameters for method '" + superMethod.getId() + "' differs in size, expected " + superMethodParams.size() + ", found " + exprList.size());
				}

				for(int i = 0; i < exprList.size(); i++) {
					if(!this.isSubclassOf(exprList.get(i).getType(), superMethodParams.get(i).getType())) {
						this.error("Wrong parameter type for method '" + superMethod.getId() + "'");
					}
				}
				
				return new PrimaryExprSuperIdColon(idColonName, exprList, superMethod.getType());
			} else {
				this.assertNextToken(Symbol.ID);
				final String id = lexer.getCurrentToken().getValue();
				
				final MethodDec superMethod = this.getSuperclassMethod(currentClass, id);
				if(superMethod == null) {
					this.error("Superclass of '" + currentClass.getName() + "' and its superclasses has no public method '" + id + "'");
				}
				
				return new PrimaryExprSuperId(id, superMethod.getType());
			}
		} else if(this.checkNextToken(Symbol.ID) && !lexer.peek(1).getValue().equals("In")) {
			lexer.nextToken();
			
			final String idName = lexer.getCurrentToken().getValue();
			
			final Type leftType = localVarMap.get(idName);
			if(leftType == null) {
				this.error("Unknown token '" + idName + "'");
			}
			
			//System.out.println("PrimaryExprId " + idName + " " + lexer.getCurrentToken().getLine());
			
			if(this.checkNextToken(Symbol.DOT)) {
				lexer.nextToken();
				
				if(leftType.getId().equals("Boolean") || leftType.getId().equals("Int") || leftType.getId().equals("String")) {
					this.error("Can't send message to primitive types Boolean, Int and String.");
				}
				
				if(this.checkNextToken(Symbol.ID)) {
					lexer.nextToken();
					
					final String secondIdName = lexer.getCurrentToken().getValue();
					final ClassDec leftClass = classNameMap.get(leftType.getId());
					
					final MethodDec methodDec = leftClass.getPublicFunction(secondIdName);
					if(methodDec == null) {
						this.error("Class " + leftType.getId() + " has no method " + secondIdName);
					}
					
					return new PrimaryExprDoubleId(idName, secondIdName, methodDec.getType());
				} else {
					this.assertNextToken(Symbol.IDCOLON);
					
					final String idColonName = lexer.getCurrentToken().getValue();
					
					final ClassDec leftClass = classNameMap.get(leftType.getId());
					final MethodDec methodDec = leftClass.getPublicFunction(idColonName);
					if(methodDec == null) {
						this.error("'" + leftType.getId() + "' has no method '" + idColonName + "'");
					}
					
					final List<Expression> exprList = this.expressionList();
					final List<ParamDec> paramList = methodDec.getParameters();
					
					if(exprList.size() != paramList.size()) {
						this.error("Number of parameters differ from method definition.");
					}
					
					for(int i = 0; i < exprList.size(); i++) {
						if(!this.isSubclassOf(exprList.get(i).getType(), paramList.get(i).getType())) {
							this.error("Type of parameter differ from method definition.");
						}
					}
					
					return new PrimaryExprIdIdColon(idName, idColonName, exprList, methodDec.getType());
				}
			} else {
					return new PrimaryExprId(idName, leftType);
			}
		} else if (this.checkNextToken(Symbol.SELF)) {
			lexer.nextToken();
			
			if(this.checkNextToken(Symbol.DOT)) {
				lexer.nextToken();
				
				if(this.checkNextToken(Symbol.ID)) {
					lexer.nextToken();
					
					final String idName = lexer.getCurrentToken().getValue();
					
					if(this.checkNextToken(Symbol.DOT)) {
						lexer.nextToken();
						
						final FieldDec leftFieldDec = currentClass.getPrivateVar(idName);
						if(leftFieldDec == null) {
							this.error("Field member " + idName + " not declared");
						}
						
						final Type leftType = leftFieldDec.getType();
						
						if(leftType.getId().equals("Boolean") || leftType.getId().equals("Int") || leftType.getId().equals("String")) {
							this.error("Can't send message to Boolean, Int or String");
						}
						
						if(this.checkNextToken(Symbol.ID)) { 
							lexer.nextToken();
							
							final String secondIdName = lexer.getCurrentToken().getValue();
							
							final ClassDec leftClassDec = classNameMap.get(leftType.getId());
							final MethodDec rightMethod = leftClassDec.getPublicFunction(secondIdName);
							if(rightMethod == null) {
								this.error("Expected method " + secondIdName);
							}
							
							return new PrimaryExprSelfDoubleId(idName, secondIdName, rightMethod.getType());
						} else {
							this.assertNextToken(Symbol.IDCOLON);
							
							final String idColonName = lexer.getCurrentToken().getValue();
							
							final ClassDec leftClassDec = classNameMap.get(leftType.getId());
							final MethodDec rightMethod = leftClassDec.getPublicFunction(idColonName);
							if(rightMethod == null) {
								this.error("Expected method " + idColonName);
							}
							
							final List<Expression> exprList = this.expressionList();
							final List<ParamDec> paramList = rightMethod.getParameters();
							
							if(exprList.size() != paramList.size()) {
								this.error("Number of parameters differs from method declaration");
							}
							for(int i = 0; i < exprList.size(); i++) {
								if(!this.isSubclassOf(exprList.get(i).getType(), paramList.get(i).getType())) {
									this.error("Type of parameter differ from method definition.");
								}
							}
							
							return new PrimaryExprSelfIdIdColon(idName, idColonName, exprList, rightMethod.getType());
						}
					} else {
						final FieldDec fieldDec = currentClass.getPrivateVar(idName);
						//System.out.println("FieldDec " + idName +": " + fieldDec == null);
						MethodDec methodDec = null;
						Type type;
						if(fieldDec == null) {
							methodDec = currentClass.getPrivateFunction(idName);
							if(methodDec == null) {
								methodDec = currentClass.getPublicFunction(idName);
								if(methodDec == null) {
									this.error("Undefined member " + idName);
									type = null;
								} else {
									type = methodDec.getType();
								}
							} else {
								type = methodDec.getType();
							}
						} else {
							type = fieldDec.getType();
						}
						
						return new PrimaryExprSelfId(idName, type, methodDec != null, fieldDec != null);
					}
				} else {
					this.assertNextToken(Symbol.IDCOLON);
					
					final String idColonName = lexer.getCurrentToken().getValue();
					
					MethodDec methodDec = currentClass.getPrivateFunction(idColonName);
					if(methodDec == null) {
						methodDec = currentClass.getPublicFunction(idColonName);
						if(methodDec == null) {
							this.error("Undefined method " + idColonName);
						}
					}
					
					final List<Expression> exprList = this.expressionList();
					final List<ParamDec> paramList = methodDec.getParameters();
					
					if(exprList.size() != paramList.size()) {
						this.error("Number of parameters differs from method declaration");
					}
					for(int i = 0; i < exprList.size(); i++) {
						if(!this.isSubclassOf(exprList.get(i).getType(), paramList.get(i).getType())) {
							this.error("Type of parameter differ from method definition.");
						}
					}
					
					return new PrimaryExprSelfIdColon(idColonName, exprList, methodDec.getType());
				}
			} else {
				return new PrimaryExprSelf(new Type(currentClass.getName()));
			}
		} else {
			final ReadExpr readExpr = this.readExpr();
			
			return readExpr;
		}
	}
	
	private Signal signal() {
		Symbol signal;
		
		if(this.checkNextToken(Symbol.PLUS)) {
			lexer.nextToken();
			
			signal = Symbol.PLUS;
		} else {
			this.assertNextToken(Symbol.MINUS);
			
			signal = Symbol.MINUS;
		}
		
		return new Signal(signal);
	}

	private ReturnStat returnStat() {
		assertNextToken(Symbol.RETURN);
		final Expression exp = this.expression();
		
		if(exp.getType() == null) {
			this.error("Illegal expression");
		}
		
		if(currentMethodReturnType == null) {
			this.error("Illegal statement, return in non returning method");
		} else if(!this.isSubclassOf(exp.getType(), currentMethodReturnType)) {
			this.error("Wrong return type, expected subclass of " + currentMethodReturnType.getId() + ", found " + exp.getType().getId());
		}
		
		
		return new ReturnStat(exp);
	}

	private Lexer			lexer;
	private ErrorSignaller	signalError;

}
