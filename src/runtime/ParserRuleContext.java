
package runtime;

import runtime.misc.Interval;
import runtime.tree.ErrorNode;
import runtime.tree.ErrorNodeImpl;
import runtime.tree.ParseTree;
import runtime.tree.ParseTreeListener;
import runtime.tree.TerminalNode;
import runtime.tree.TerminalNodeImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class ParserRuleContext extends RuleContext {
	public static final ParserRuleContext EMPTY = new ParserRuleContext();

	
	public List<ParseTree> children;


	public Token start, stop;

	
	public RecognitionException exception;

	public ParserRuleContext() { }


	public ParserRuleContext(ParserRuleContext parent, int invokingStateNumber) {
		super(parent, invokingStateNumber);
	}

	// Double dispatch methods for listeners

	public void enterRule(ParseTreeListener listener) { }
	public void exitRule(ParseTreeListener listener) { }

	
	public <T extends ParseTree> T addAnyChild(T t) {
		if ( children==null ) children = new ArrayList<>();
		children.add(t);
		return t;
	}

	public RuleContext addChild(RuleContext ruleInvocation) {
		return addAnyChild(ruleInvocation);
	}

	
	public TerminalNode addChild(TerminalNode t) {
		t.setParent(this);
		return addAnyChild(t);
	}

	
	public ErrorNode addErrorNode(ErrorNode errorNode) {
		errorNode.setParent(this);
		return addAnyChild(errorNode);
	}


	
	public void removeLastChild() {
		if ( children!=null ) {
			children.remove(children.size()-1);
		}
	}

	@Override
	
	public ParserRuleContext getParent() {
		return (ParserRuleContext)super.getParent();
	}

	@Override
	public ParseTree getChild(int i) {
		return children!=null && i>=0 && i<children.size() ? children.get(i) : null;
	}

	public <T extends ParseTree> T getChild(Class<? extends T> ctxType, int i) {
		if ( children==null || i < 0 || i >= children.size() ) {
			return null;
		}

		int j = -1; // what element have we found with ctxType?
		for (ParseTree o : children) {
			if ( ctxType.isInstance(o) ) {
				j++;
				if ( j == i ) {
					return ctxType.cast(o);
				}
			}
		}
		return null;
	}

	public TerminalNode getToken(int ttype, int i) {
		if ( children==null || i < 0 || i >= children.size() ) {
			return null;
		}

		int j = -1; // what token with ttype have we found?
		for (ParseTree o : children) {
			if ( o instanceof TerminalNode ) {
				TerminalNode tnode = (TerminalNode)o;
				Token symbol = tnode.getSymbol();
				if ( symbol.getType()==ttype ) {
					j++;
					if ( j == i ) {
						return tnode;
					}
				}
			}
		}

		return null;
	}

	public List<TerminalNode> getTokens(int ttype) {
		if ( children==null ) {
			return Collections.emptyList();
		}

		List<TerminalNode> tokens = null;
		for (ParseTree o : children) {
			if ( o instanceof TerminalNode ) {
				TerminalNode tnode = (TerminalNode)o;
				Token symbol = tnode.getSymbol();
				if ( symbol.getType()==ttype ) {
					if ( tokens==null ) {
						tokens = new ArrayList<TerminalNode>();
					}
					tokens.add(tnode);
				}
			}
		}

		if ( tokens==null ) {
			return Collections.emptyList();
		}

		return tokens;
	}

	public <T extends ParserRuleContext> T getRuleContext(Class<? extends T> ctxType, int i) {
		return getChild(ctxType, i);
	}

	public <T extends ParserRuleContext> List<T> getRuleContexts(Class<? extends T> ctxType) {
		if ( children==null ) {
			return Collections.emptyList();
		}

		List<T> contexts = null;
		for (ParseTree o : children) {
			if ( ctxType.isInstance(o) ) {
				if ( contexts==null ) {
					contexts = new ArrayList<T>();
				}

				contexts.add(ctxType.cast(o));
			}
		}

		if ( contexts==null ) {
			return Collections.emptyList();
		}

		return contexts;
	}

	@Override
	public int getChildCount() { return children!=null ? children.size() : 0; }

	@Override
	public Interval getSourceInterval() {
		if ( start == null ) {
			return Interval.INVALID;
		}
		if ( stop==null || stop.getTokenIndex()<start.getTokenIndex() ) {
			return Interval.of(start.getTokenIndex(), start.getTokenIndex()-1); // empty
		}
		return Interval.of(start.getTokenIndex(), stop.getTokenIndex());
	}

	
	public Token getStart() { return start; }
	
	public Token getStop() { return stop; }

	
	public String toInfoString(Parser recognizer) {
		List<String> rules = recognizer.getRuleInvocationStack(this);
		Collections.reverse(rules);
		return "ParserRuleContext"+rules+"{" +
			"start=" + start +
			", stop=" + stop +
			'}';
	}
}

