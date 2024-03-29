package lpoProject.parser;

import static lpoProject.parser.TokenType.*;

import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class StreamTokenizer implements Tokenizer {
	private static final String regEx;
	private static final Map<String, TokenType> keywords = new HashMap<>();
	private static final Map<String, TokenType> symbols = new HashMap<>();

	private boolean hasNext = true; // any stream contains at least the EOF
									// token
	private TokenType tokenType;
	private String tokenString;
	private boolean boolValue;
	private int intValue;
	private Optional optValue;
	private final Scanner scanner;

	static {
		// remark: groups must correspond to the ordinal of the corresponding
		// token type
		final String identRegEx = "([a-zA-Z][a-zA-Z0-9]*)"; // group 1
		final String numRegEx = "([1-9][0-9]*|(?:(?:0b|0B)[01]+)|0)"; // group 2
		final String skipRegEx = "(\\s+|//.*)"; // group 3
		final String symbolRegEx = "\\+|\\*|&&|==|=|\\(|\\)|;|,|\\{|\\}|-|::|:|\\[|\\]|!";
		regEx = identRegEx + "|" + numRegEx + "|" + skipRegEx + "|" + symbolRegEx;
	}

	static {
		keywords.put("false", BOOL);
		keywords.put("for", FOR);
		keywords.put("print", PRINT);
		keywords.put("var", VAR);
		keywords.put("true", BOOL);
		keywords.put("if", IF);
		keywords.put("else", ELSE);
		keywords.put("do", DO);
		keywords.put("while", WHILE);
		keywords.put("opt", OPT);
		keywords.put("get", GET);
		keywords.put("empty", EMPTY);
		keywords.put("def", DEF);
	}

	static {
		symbols.put("+", PLUS);
		symbols.put("*", TIMES);
		symbols.put("::", PREFIX);
		symbols.put("=", ASSIGN);
		symbols.put(":", IN);
		symbols.put("(", OPEN_PAR);
		symbols.put(")", CLOSE_PAR);
		symbols.put(";", STMT_SEP);
		symbols.put(",", EXP_SEP);
		symbols.put("{", OPEN_BLOCK);
		symbols.put("}", CLOSE_BLOCK);
		symbols.put("-", MINUS);
		symbols.put("[", OPEN_LIST);
		symbols.put("]", CLOSE_LIST);
		symbols.put("&&", AND);
		symbols.put("==", EQ);
		symbols.put("!", NOT);
	}

	public StreamTokenizer(Reader reader) {
		scanner = new StreamScanner(regEx, reader);
	}

	private void checkType() {
		tokenString = scanner.group();
		if (scanner.group(IDENT.ordinal()) != null) { // IDENT or a keyword
			tokenType = keywords.get(tokenString);
			if (tokenType == null)
				tokenType = IDENT;
			if(tokenType == BOOL)
				boolValue = Boolean.parseBoolean(tokenString);
			return;
		}
		if (scanner.group(NUM.ordinal()) != null) { // NUM
			tokenType = NUM;
			if(tokenString.length() > 1 && (tokenString.charAt(1) == 'b' || tokenString.charAt(1) == 'B')) {
				tokenString = tokenString.substring(2);
				intValue = Integer.parseInt(tokenString, 2);
			}
			else
				intValue = Integer.parseInt(tokenString);
			return;
		}
		if (scanner.group(SKIP.ordinal()) != null) { // SKIP
			tokenType = SKIP;
			return;
		}
		tokenType = symbols.get(tokenString); // a symbol
		if (tokenType == null)
			throw new AssertionError("Fatal error");
	}

	@Override
	public TokenType next() throws TokenizerException {
		do {
			tokenType = null;
			tokenString = "";
			try {
				if (hasNext && !scanner.hasNext()) {
					hasNext = false;
					return tokenType = EOF;
				}
				scanner.next();
			} catch (ScannerException e) {
				throw new TokenizerException(e);
			}
			checkType();
		} while (tokenType == SKIP);
		return tokenType;
	}

	private void checkValidToken() {
		if (tokenType == null)
			throw new IllegalStateException();
	}

	private void checkValidToken(TokenType ttype) {
		if (tokenType != ttype)
			throw new IllegalStateException();
	}

	@Override
	public String tokenString() {
		checkValidToken();
		return tokenString;
	}

	@Override
	public int intValue() {
		checkValidToken(NUM);
		return intValue;
	}

	@Override
	public boolean boolValue() {
		checkValidToken(BOOL);
		return boolValue;
	}

	@Override
	public Optional optValue() {
		checkValidToken(OPT);
		return optValue;
	}

	@Override
	public TokenType tokenType() {
		checkValidToken();
		return tokenType;
	}

	@Override
	public boolean hasNext() {
		return hasNext;
	}

	@Override
	public void close() throws TokenizerException {
		try {
			scanner.close();
		} catch (ScannerException e) {
			throw new TokenizerException(e);
		}
	}
}
