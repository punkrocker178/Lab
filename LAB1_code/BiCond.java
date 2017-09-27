public class BiCond extends NonTerminal{
	public boolean interpret(){
		return (!left.interpret() || right.interpret())&&(!right.interpret()||left.interpret());
	}
	public String toString(){
		return String.format("(%s <-> %s)",left,right);
	}
}