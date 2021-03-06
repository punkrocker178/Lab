public class RecursiveDescentParser {
	private Lexer lexer;
	private int symbol;
	private BooleanExpression root;

	private final True t = new True();
	private final False f = new False();

	public RecursiveDescentParser(Lexer lexer) {
		this.lexer = lexer;
	}

	public BooleanExpression build() {
		expression();
		return root;
	}

	private void expression() {
		term1();
		while(symbol == Lexer.ONLYIF){
			BiCond bicond = new BiCond();
			bicond.setLeft(root);
			term1();
			bicond.setRight(root);
			root = bicond;
		}
		while(symbol == Lexer.IMPLIES){
			Cond cond = new Cond();
			cond.setLeft(root);
			term1();
			cond.setRight(root);
			root = cond;
		}
		
		
	}
	private void term1(){
		term();
		while (symbol == Lexer.OR) {
			Or or = new Or();
			or.setLeft(root);
			term();
			or.setRight(root);
			root = or;
		}
	}
	private void term() {
		factor();
		while (symbol == Lexer.AND) {
			And and = new And();
			and.setLeft(root);
			factor();
			and.setRight(root);
			root = and;
		}
	}

	private void factor() {
		symbol = lexer.nextSymbol();
		if (symbol == Lexer.TRUE) {
			root = t;
			symbol = lexer.nextSymbol();
		} else if (symbol == Lexer.FALSE) {
			root = f;
			symbol = lexer.nextSymbol();
		} else if (symbol == Lexer.NOT) {
			Not not = new Not();
			factor();
			not.setChild(root);
			root = not;
		} else if (symbol == Lexer.LEFT) {
			expression();
			symbol = lexer.nextSymbol(); // we don't care about ')'
		} else {
			throw new RuntimeException("Expression Malformed");
		}
	}
}
