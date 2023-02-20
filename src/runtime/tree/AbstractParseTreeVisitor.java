

package runtime.tree;

public abstract class AbstractParseTreeVisitor<T> implements ParseTreeVisitor<T> {
	
	@Override
	public T visit(ParseTree tree) {
		return tree.accept(this);
	}

	
	@Override
	public T visitChildren(RuleNode node) {
		T result = defaultResult();
		int n = node.getChildCount();
		for (int i=0; i<n; i++) {
			if (!shouldVisitNextChild(node, result)) {
				break;
			}

			ParseTree c = node.getChild(i);
			T childResult = c.accept(this);
			result = aggregateResult(result, childResult);
		}

		return result;
	}

	
	@Override
	public T visitTerminal(TerminalNode node) {
		return defaultResult();
	}

	
	@Override
	public T visitErrorNode(ErrorNode node) {
		return defaultResult();
	}

	
	protected T defaultResult() {
		return null;
	}

	
	protected T aggregateResult(T aggregate, T nextResult) {
		return nextResult;
	}

	
	protected boolean shouldVisitNextChild(RuleNode node, T currentResult) {
		return true;
	}

}
