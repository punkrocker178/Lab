import java.io.*;
import java.io.ByteArrayInputStream;
import java.util.*;
import java.lang.*;

class Lexer {
	private StreamTokenizer input;

	private int sym = NONE;
	public static final int EOL = -3;
	public static final int EOF = -2;
	public static final int INVALID = -1;

	public static final int NONE = 0;

	public static final int OR = 1;
	public static final int AND = 2;
	public static final int NOT = 3;

	public static final int TRUE = 4;
	public static final int FALSE = 5;

	public static final int LEFT_PARENTHESES = 6;
	public static final int RIGHT_PARENTHESES = 7;
	
	public static final int IMPLIES = 8;
	public static final int ONLYIF = 9;

	public static final String TRUE_LITERAL = "true";
	public static final String FALSE_LITERAL = "false";

	public Lexer(InputStream in) {
		Reader r = new BufferedReader(new InputStreamReader(in));
		input = new StreamTokenizer(r);

		input.resetSyntax();
		input.wordChars('a', 'z');
		input.wordChars('A', 'Z');
		input.whitespaceChars('\u0000', ' ');
		input.whitespaceChars('\n', '\t');

		input.ordinaryChar('(');
		input.ordinaryChar(')');
		input.ordinaryChar('&');
		input.ordinaryChar('|');
		input.ordinaryChar('!');
		input.ordinaryChar('>');
		input.ordinaryChar('=');
	}

	public int nextSymbol() {
		try {
			switch (input.nextToken()) {
				case StreamTokenizer.TT_EOL:
					sym = EOL;
					break;
				case StreamTokenizer.TT_EOF:
					sym = EOF;
					break;
				case StreamTokenizer.TT_WORD: {
					if (input.sval.equalsIgnoreCase(TRUE_LITERAL)) sym = TRUE;
					else if (input.sval.equalsIgnoreCase(FALSE_LITERAL)) sym = FALSE;
					break;
				}
				case '(':
					sym = LEFT_PARENTHESES;
					break;
				case ')':
					sym = RIGHT_PARENTHESES;
					break;
				case '&':
					sym = AND;
					break;
				case '|':
					sym = OR;
					break;
				case '!':
					sym = NOT;
					break;
				case '>':
					sym = IMPLIES;
					break;
				case '=':
					sym = ONLYIF;
					break;
				default:
					sym = INVALID;
			}
		} catch (IOException e) {
			sym = EOF;
		}

		return sym;
	}


	public String toString() {
		return input.toString();
	}
}
interface BooleanExp{
	public boolean interpret();
}
abstract class LogicOperator implements BooleanExp{
	BooleanExp left,right;
	public void setLeft(BooleanExp left){
		this.left=left;
	}
	public void setRight(BooleanExp right){
		this.right=right;
	}

}
class False implements BooleanExp{
	boolean f;
	public False(){
		this.f = false;
	}
	public String toString(){
		return "false";
	}
	public boolean interpret(){
		return f;
	}
}
class True implements BooleanExp{
	boolean t;
	public True(){
		this.t =true;
	}
	public String toString(){
		return "true";
	}
	public boolean interpret(){
		return t;
	}
}
class And extends LogicOperator{
	public boolean interpret(){
		return left.interpret()&&right.interpret();
	}
	public String toString(){
		return String.format("(%s & %s)",left,right);
	}
}
class Or extends LogicOperator{
	public boolean interpret(){
		return left.interpret()||right.interpret();
	}
	public String toString(){
		return String.format("(%s | %s)",left,right);
	}
}
class Not extends LogicOperator{
	public void setTerm(BooleanExp term){
		setLeft(term);
	}
	public void setRight(BooleanExp term){
		try{
			throw new UnsupportedOperationException();
		}catch(UnsupportedOperationException ex){
			System.err.printf("Not operator is badly placed!");
		}
	}
	public boolean interpret(){
		return !left.interpret();
	}

	public String toString(){
		return String.format("(!%s)",left);
	}
}
class Condition extends LogicOperator{
	public boolean interpret(){
		return !left.interpret()||right.interpret();
	}
	public String toString(){
		return String.format("(%s -> %s)",left,right);
	}
}
class BiCondition extends LogicOperator{
	public boolean interpret(){
		return (!left.interpret()||right.interpret())&&(left.interpret()&&(!right.interpret()));
	}
	public String toString(){
		return String.format("(%s <-> %s)",left,right);
	}
}
class RecursiveParseRule{
	private Lexer lex;
	private int sym;
	private BooleanExp root;
	private final True t = new True();
	private final False f = new False();
	public RecursiveParseRule(Lexer lex){
		this.lex = lex;
	}
	public BooleanExp buildExpression(){
		exp();
		return root;
	}
	private void exp(){
		term1();
		while(sym!=Lexer.EOF){
			if(sym==Lexer.ONLYIF){
				BiCondition bicond = new BiCondition();
				bicond.setLeft(root);
				term1();
				bicond.setRight(root);
				root = bicond;
			}else if(sym==Lexer.IMPLIES){
				Condition cond = new Condition();
				cond.setLeft(root);
				term1();
				cond.setRight(root);
				root = cond;
			}
		}
		
		/*while(sym == Lexer.ONLYIF){
			BiCondition bicond = new BiCondition();
			bicond.setLeft(root);
			term1();
			bicond.setRight(root);
			root = bicond;
		}
		while(sym == Lexer.IMPLIES){
			Condition cond = new Condition();
			cond.setLeft(root);
			term1();
			cond.setRight(root);
			root = cond;
		}
		*/
	}
	private void term1(){
		term();
		while (sym == Lexer.OR) {
			Or or = new Or();
			or.setLeft(root);
			term();
			or.setRight(root);
			root = or;
		}
	}
	private void term() {
		factor();
		while (sym == Lexer.AND) {
			And and = new And();
			and.setLeft(root);
			factor();
			and.setRight(root);
			root = and;
		}
	}

	private void factor() {
		sym = lex.nextSymbol();
		if (sym == Lexer.TRUE) {
			root = t;
			sym = lex.nextSymbol();
		} else if (sym == Lexer.FALSE) {
			root = f;
			sym = lex.nextSymbol();
		} else if (sym == Lexer.NOT) {
			Not not = new Not();
			factor();
			not.setTerm(root);
			root = not;
		} else if (sym == Lexer.LEFT_PARENTHESES) {
			exp();
			sym = lex.nextSymbol(); // we don't care about ')'
		} else {
			throw new RuntimeException("Expression Malformed");
		}
	}
}
public class Assignment2{
	public static void main(String[] args) {
		String exp ="Hieu|b&(ac=as)!as";

		String[] var = args[0].split("[^a-zA-z]");
		String[] ops = args[0].split("\\s*[a-zA-Z()]+\\s*");
		LinkedHashMap<String,String> map = new LinkedHashMap<String,String>();
		ArrayList<String> list = new ArrayList<String>();
		ArrayList<String> bool = new ArrayList<String>();
		ArrayList<String> op = new ArrayList<String>();
		String newStr ="";
		
		for(int j=0;j<args.length-1;j++){					// ADD ARGUMENT VO BOOL
			bool.add(args[j+1]);
			System.out.println(bool.get(j));
		}
		for(int k=0;k<ops.length;k++){
			if(!ops[k].equals("")){
				op.add(ops[k]);
				System.out.println(ops[k]);
			}
		}
		for(int i=0;i<var.length;i++){						//HASHMAP TEN BIEN VA BOOL
				if(i==0){
					list.add(var[0]);
					map.put(list.get(i),bool.get(i));
				}else if(!var[i].equals("")){
					System.out.println(i);
					list.add(var[i]);
					map.put(list.get((list.size()-1)),bool.get(i));
					
				}
				System.out.println(map.entrySet()+"\t");

		}
		int size = list.size()+op.size();
   		for(int l=0;l<list.size();l++){
   			System.out.println("Running: "+l);
   			if(l==0){
   				
   				newStr = map.get(list.get(l))+op.get(l);
   				
   			}else if(l<list.size()-1){
   				newStr=newStr+map.get(list.get(l))+op.get(l);
   			}else{
   				newStr=newStr+map.get(list.get(l));
   			}
   			System.out.println(newStr);
   		}
		/*try{
			for(int i=0;i<args.length;i++){
				abool.add(Boolean.parseBoolean(args[i]));
			}
		}catch(IndexOutOfBoundsException e){
			System.err.println("NO INPUT!");
		}
		*/
		//variable.addAll(lex.readWord());
		//origin.addAll(variable);

		/*for(int j=0;j<variable.size();j++){								//Loai bo variable trung`
			for(int i=j+1;i<variable.size();i++){
				if(variable.get(j).equals(variable.get(i))){
					variable.remove(i);
				}	
			}
		}	
		*/
		

		Lexer lex = new Lexer(new ByteArrayInputStream(newStr.getBytes()));
		RecursiveParseRule parser = new RecursiveParseRule(lex);
		BooleanExp e = parser.buildExpression();
		System.out.println("Expression: "+e);
		System.out.println("Anser: "+e.interpret());
		
	}
}