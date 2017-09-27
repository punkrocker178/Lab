import java.io.ByteArrayInputStream;
import java.util.Scanner;

public class BooleanEvaluator {
	public static void main(String[] args) throws InterruptedException {
		Scanner sc = new Scanner((System.in));
		String expression = "";
		if(args.length > 0 && args[0].equals("-f")) {
			while(sc.hasNextLine()) 
				expression += sc.nextLine(); 
				System.out.println(expression);
		} else {
			System.out.println("Insert an expression:");
			expression = sc.nextLine();
		}

		Lexer lexer = new Lexer(new ByteArrayInputStream(expression.getBytes()));
		RecursiveDescentParser parser = new RecursiveDescentParser(lexer);
	    BooleanExpression ast = parser.build();
		System.out.println(String.format("AST: %s", ast));
		System.out.println(String.format("RES: %s", ast.interpret()));
	}
}
