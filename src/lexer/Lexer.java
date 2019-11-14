/*
 * Cristiano Guilherme - 609803
 * Daniel Davoli       - 610372
 */

package lexer;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import comp.ErrorSignaller;
import lexer.Token.Symbol;

public class Lexer {
	
	private final char[] inputAr;
	private final ErrorSignaller error;
	private int tokenPos;
	private int lineNumber;
	private List<Token> tokenList;
	
	public Lexer(final char[] input, final ErrorSignaller error) {
		input[input.length - 1] = '\0';
		this.inputAr = input;
		this.error = error;
		tokenPos = 0;
        lineNumber = 1;
        this.tokenize();
	}
	
	public List<Token> getAllAnnots(){
		final List<Token> annotList = new ArrayList<>();
		int line = 1;
		
		for(final char ch : inputAr) {
			if (ch == '\n')
				line++;
			
			if(ch == '@') {
				annotList.add(new Token(Symbol.ANNOT, line, ""));
			}
		}
		
		return annotList;
	}

    private void tokenize() {
        tokenList = new ArrayList<>();
        
        Token tok;
        tokenList.add(new Token(Token.Symbol.INIT, 0, ""));
        while((tok = parseToken(inputAr)).getSymbol() != Token.Symbol.EOF) {
        	tokenList.add(tok);
        }
        tokenList.add(new Token(Token.Symbol.EOF, tokenList.get(tokenList.size() - 1).getLine() + 1, ""));
        tokenPos = 0;
      }
    
    public Token getCurrentToken() {
    	return this.peek(0);
    }


    private static final int MaxValueInteger = 2147483647;
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
	
    private Token parseToken(final char[] input) {
        char ch;
        
        //System.out.println("Parsing " + input[tokenPos]);
        
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
               return parseToken(input);
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
            	 return new Token(Token.Symbol.ERROR, lineNumberStartComment, "Comment opened and not closed");
                /*error.showError( "Comment opened and not closed",
                      getLine(posStartComment), lineNumberStartComment);*/
             else
                tokenPos += 2;
             return parseToken(input);
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
                	return new Token(Token.Symbol.ERROR, lineNumber, "Number out of limits");
                   //error.showError("Number out of limits");
                }
                if ( numberValue > MaxValueInteger )
                	return new Token(Token.Symbol.ERROR, lineNumber, "Number out of limits");
                   //error.showError("Number out of limits");
                
                return new Token(Token.Symbol.LITERALINT, lineNumber, number.toString());
            }
            else {
                tokenPos++;
                switch ( ch ) {
                    case '+' :
                    	if(input[tokenPos] == '+') {
                    		tokenPos++;
                    		return new Token(Token.Symbol.PLUSPLUS, lineNumber, "");
                    	} else {
                    		return new Token(Token.Symbol.PLUS, lineNumber, "");
                    	}
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
                    	  return new Token(Token.Symbol.ERROR, lineNumber, "& expected");
                        //error.showError("& expected");
                      
                    case '|' :
                      if ( input[tokenPos] == '|' ) {
                         tokenPos++;
                         return new Token(Token.Symbol.OR, lineNumber, "");
                      }
                      else
                    	  return new Token(Token.Symbol.ERROR, lineNumber, "| expected");
                        //error.showError("| expected");
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
                    		return new Token(Token.Symbol.ERROR, lineNumber, "Identifier expected after '@'");
                    		//error.showError("Identifier expected after '@'");
                    	return new Token(Token.Symbol.ANNOT, lineNumber, metaobjectName);
                    case '_' :
                    	return new Token(Token.Symbol.ERROR, lineNumber, "'_' cannot start an indentifier");
                      //error.showError("'_' cannot start an indentifier");
                      
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
                    	   literalStringValue = "";
                    	   return new Token(Token.Symbol.ERROR, lineNumber, "Nonterminated string");
                          //error.showError("Nonterminated string");
                       }
                       else {
                          tokenPos++;
                          literalStringValue = s.toString();
                       }
                       
                       return new Token(Token.Symbol.LITERALSTRING, lineNumber, literalStringValue);
                    default :
                    	//System.out.println("Token pos " + tokenPos);
                    	return new Token(Token.Symbol.ERROR, lineNumber, "Invalid character: '" + ch + "'");
                      //error.showError("Invalid Character: '" + ch + "'", this.getLine(tokenPos), new Token(Token.Symbol.LITERALSTRING, lineNumber, ""));
                }
            }
          }
        
        //return new Token(null, null, null);
    }

    public String getLine( final int line ) {
        // get the line that contains input[index]. Assume input[index] is at a token, not
        // a white space or newline
    	if(line < 0)
    		return "";
    	
    	final StringBuilder sb = new StringBuilder();
    	
    	int newline = 1;
    	for(final char ch : inputAr) {
    		if(ch == '\n') {
    			newline++;
    			continue;
    		}
    		
    		if(newline == line) {
    			sb.append(ch);
    		} else if (newline > line) {
    			break;
    		}
    	}
    	
    	return sb.toString();
    }

}
