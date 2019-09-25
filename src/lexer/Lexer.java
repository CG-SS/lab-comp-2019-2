package lexer;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import comp.ErrorSignaller;

public class Lexer {
	
	private final List<Token> tokenList;
	private int tokenPos;
	private int lineNumber;
	private ErrorSignaller error;

    public Lexer(final char []input, final ErrorSignaller error ) {
          // add an end-of-file label to make it easy to do the lexer
        input[input.length - 1] = '\0';
          // number of the current line
        tokenPos = 0;
        lineNumber = 1;
        this.error = error;
        tokenList = new ArrayList<>();
        
        Token tok;
        tokenList.add(new Token(Token.Symbol.INIT, 0, ""));
        while((tok = nextToken(input)).getSymbol() != Token.Symbol.EOF) {
        	tokenList.add(tok);
        }
        tokenPos = 0;
      }


    private static final int MaxValueInteger = 32767;
      // contains the keywords
    static private Hashtable<String, Token.Symbol> keywordsTable;

     // this code will be executed only once for each program execution
	static {
		keywordsTable = new Hashtable<String, Token.Symbol>();

		for ( final Token.Symbol s : Token.Symbol.values() ) {
			String kw = s.toString();
			if ( Character.isAlphabetic(kw.charAt(0)) )
				keywordsTable.put( s.toString(), s);
		}


	}
	
	public void nextToken() {
		tokenPos++;
	}

	public Token peek(final int index) {
		final int finalIndex = index + tokenPos;
		
		if(finalIndex < 0)
			return tokenList.get(0);
		if(finalIndex > tokenList.size())
			return tokenList.get(tokenList.size() - 1);
		
		return tokenList.get(finalIndex);
	}
	
    private Token nextToken(final char[] input) {
        char ch;
        
        while (  (ch = input[tokenPos]) == ' ' || ch == '\r' ||
                 ch == '\t' || ch == '\n')  {
            // count the number of lines
          if ( ch == '\n')
            lineNumber++;
          tokenPos++;
          }
        if ( ch == '\0')
          return new Token(Token.Symbol.EOF, lineNumber, "");
        else
          if ( input[tokenPos] == '/' && input[tokenPos + 1] == '/' ) {
                // comment found
               while ( input[tokenPos] != '\0'&& input[tokenPos] != '\n' )
                 tokenPos++;
               return nextToken(input);
          }
          else if ( input[tokenPos] == '/' && input[tokenPos + 1] == '*' ) {
             int posStartComment = tokenPos;
             int lineNumberStartComment = lineNumber;
             tokenPos += 2;
             while ( (ch = input[tokenPos]) != '\0' &&
                 (ch != '*' || input[tokenPos + 1] != '/') ) {
                if ( ch == '\n' )
                   lineNumber++;
                tokenPos++;
             }
             if ( ch == '\0' )
                error.showError( "Comment opened and not closed",
                      getLine(posStartComment), lineNumberStartComment);
             else
                tokenPos += 2;
             return nextToken(input);
          }
          else {
            if ( Character.isLetter( ch ) ) {
                // get an identifier or keyword
                final StringBuffer ident = new StringBuffer();
                while ( Character.isLetter( ch = input[tokenPos] ) ||
                        Character.isDigit(ch) ||
                        ch == '_' ) {
                    ident.append(input[tokenPos]);
                    tokenPos++;
                }
                if ( input[tokenPos] == ':' ) {
                    ident.append(input[tokenPos]);
                    tokenPos++;
                	final String stringValue = ident.toString();
                	return new Token( Token.Symbol.IDCOLON, lineNumber, stringValue);
                }
                else {
                	final String stringValue = ident.toString();
                    // if identStr is in the list of keywords, it is a keyword !
                	final Token.Symbol value = keywordsTable.get(stringValue);
                	if ( value == null )
                		return new Token(Token.Symbol.ID, lineNumber, stringValue);
                	else
                		return new Token(value, lineNumber, "");
                }
            }
            else if ( Character.isDigit( ch ) ) {
                // get a number
                StringBuffer number = new StringBuffer();
                while ( Character.isDigit( input[tokenPos] ) ) {
                    number.append(input[tokenPos]);
                    tokenPos++;
                }

                int numberValue = 0;
                try {
                   numberValue = Integer.valueOf(number.toString()).intValue();
                } catch ( NumberFormatException e ) {
                   error.showError("Number out of limits");
                }
                if ( numberValue > MaxValueInteger )
                   error.showError("Number out of limits");
                
                return new Token(Token.Symbol.LITERALINT, lineNumber, number.toString());
            }
            else {
                tokenPos++;
                switch ( ch ) {
                    case '+' :
                    	return new Token(Token.Symbol.PLUS, lineNumber, "");
                    case '-' :
                      if ( input[tokenPos] == '>' ) {
                          tokenPos++;
                          return new Token(Token.Symbol.MINUS_GT, lineNumber, "");
                      }
                      else {
                    	  return new Token(Token.Symbol.MINUS, lineNumber, "");
                      }
                    case '*' :
                    	return new Token(Token.Symbol.MULT, lineNumber, "");
                    case '/' :
                    	return new Token(Token.Symbol.DIV, lineNumber, "");
                    case '<' :
                      if ( input[tokenPos] == '=' ) {
                        tokenPos++;
                        return new Token(Token.Symbol.LE, lineNumber, "");
                      }
                      else {
                    	  return new Token(Token.Symbol.LT, lineNumber, "");
                      }
                    case '>' :
                      if ( input[tokenPos] == '=' ) {
                        tokenPos++;
                        return new Token(Token.Symbol.GE, lineNumber, "");
                      }
                      else
                    	  return new Token(Token.Symbol.GT, lineNumber, "");
                    case '=' :
                      if ( input[tokenPos] == '=' ) {
                        tokenPos++;
                        return new Token(Token.Symbol.EQ, lineNumber, "");
                      }
                      else
                    	  return new Token(Token.Symbol.ASSIGN, lineNumber, "");
                    case '!' :
                      if ( input[tokenPos] == '=' ) {
                         tokenPos++;
                         return new Token(Token.Symbol.NEQ, lineNumber, "");
                      }
                      else
                    	  return new Token(Token.Symbol.NOT, lineNumber, "");
                    case '(' :
                    	return new Token(Token.Symbol.LEFTPAR, lineNumber, "");
                    case ')' :
                    	return new Token(Token.Symbol.RIGHTPAR, lineNumber, "");
                    case ',' :
                    	return new Token(Token.Symbol.COMMA, lineNumber, "");
                    case ';' :
                    	return new Token(Token.Symbol.SEMICOLON, lineNumber, "");
                    case '.' :
                    	return new Token(Token.Symbol.DOT, lineNumber, "");
                    case '&' :
                      if ( input[tokenPos] == '&' ) {
                         tokenPos++;
                         return new Token(Token.Symbol.AND, lineNumber, "");
                      }
                      else
                        error.showError("& expected");
                      break;
                    case '|' :
                      if ( input[tokenPos] == '|' ) {
                         tokenPos++;
                         return new Token(Token.Symbol.OR, lineNumber, "");
                      }
                      else
                        error.showError("| expected");
                      break;
                    case '{' :
                    	return new Token(Token.Symbol.LEFTCURBRACKET, lineNumber, "");
                    case '}' :
                    	return new Token(Token.Symbol.RIGHTCURBRACKET, lineNumber, "");
                    case '@' :
                    	String metaobjectName = "";
                    	while ( Character.isAlphabetic(input[tokenPos]) ) {
                    		metaobjectName += input[tokenPos];
                    		++tokenPos;
                    	}
                    	if ( metaobjectName.length() == 0 )
                    		error.showError("Identifier expected after '@'");
                    	return new Token(Token.Symbol.ANNOT, lineNumber, metaobjectName);
                    case '_' :
                      error.showError("'_' cannot start an indentifier");
                      break;
                    case '"' :
                       StringBuffer s = new StringBuffer();
                       String literalStringValue;
                       while ( input[tokenPos] != '\0' && input[tokenPos] != '\n' )
                          if ( input[tokenPos] == '"' )
                             break;
                          else
                             if ( input[tokenPos] == '\\' ) {
                                if ( input[tokenPos+1] != '\n' && input[tokenPos+1] != '\0' ) {
                                   s.append(input[tokenPos]);
                                   tokenPos++;
                                   s.append(input[tokenPos]);
                                   tokenPos++;
                                }
                                else {
                                   s.append(input[tokenPos]);
                                   tokenPos++;
                                }
                             }
                             else {
                                s.append(input[tokenPos]);
                                tokenPos++;
                             }

                       if ( input[tokenPos] == '\0' || input[tokenPos] == '\n' ) {
                          error.showError("Nonterminated string");
                          literalStringValue = "";
                       }
                       else {
                          tokenPos++;
                          literalStringValue = s.toString();
                       }
                       
                       return new Token(Token.Symbol.LITERALSTRING, lineNumber, literalStringValue);
                    default :
                      error.showError("Invalid Character: '" + ch + "'", false);
                }
            }
          }
        
        return new Token(null, null, null);
    }

    private String getLine( int index ) {
        // get the line that contains input[index]. Assume input[index] is at a token, not
        // a white space or newline

    	if(index < 0)
        	return "-1";
        if(index > tokenList.size())
        	return tokenList.get(tokenList.size()).getLine().toString();
    	
    	return tokenList.get(index).getLine().toString();
    }

}
