

package runtime.tree;

import runtime.*;
import runtime.atn.ATN;
import runtime.misc.Interval;
import runtime.misc.Utils;

import java.util.*;


public class Trees {
	
	public static String toStringTree(Tree t) {
		return toStringTree(t, (List<String>)null);
	}

	
	public static String toStringTree(Tree t, Parser recog) {
		String[] ruleNames = recog != null ? recog.getRuleNames() : null;
		List<String> ruleNamesList = ruleNames != null ? Arrays.asList(ruleNames) : null;
		return toStringTree(t, ruleNamesList);
	}

	
	public static String toStringTree(final Tree t, final List<String> ruleNames) {
		String s = Utils.escapeWhitespace(getNodeText(t, ruleNames), false);
		if ( t.getChildCount()==0 ) return s;
		StringBuilder buf = new StringBuilder();
		buf.append("(");
		s = Utils.escapeWhitespace(getNodeText(t, ruleNames), false);
		buf.append(s);
		buf.append(' ');
		for (int i = 0; i<t.getChildCount(); i++) {
			if ( i>0 ) buf.append(' ');
			buf.append(toStringTree(t.getChild(i), ruleNames));
		}
		buf.append(")");
		return buf.toString();
	}

	public static String getNodeText(Tree t, Parser recog) {
		String[] ruleNames = recog != null ? recog.getRuleNames() : null;
		List<String> ruleNamesList = ruleNames != null ? Arrays.asList(ruleNames) : null;
		return getNodeText(t, ruleNamesList);
	}

	public static String getNodeText(Tree t, List<String> ruleNames) {
		if ( ruleNames!=null ) {
			if ( t instanceof RuleContext ) {
				int ruleIndex = ((RuleContext)t).getRuleContext().getRuleIndex();
				String ruleName = ruleNames.get(ruleIndex);
				int altNumber = ((RuleContext) t).getAltNumber();
				if ( altNumber!=ATN.INVALID_ALT_NUMBER ) {
					return ruleName+":"+altNumber;
				}
				return ruleName;
			}
			else if ( t instanceof ErrorNode) {
				return t.toString();
			}
			else if ( t instanceof TerminalNode) {
				Token symbol = ((TerminalNode)t).getSymbol();
				if (symbol != null) {
					String s = symbol.getText();
					return s;
				}
			}
		}
		// no recog for rule names
		Object payload = t.getPayload();
		if ( payload instanceof Token ) {
			return ((Token)payload).getText();
		}
		return t.getPayload().toString();
	}

	
	public static List<Tree> getChildren(Tree t) {
		List<Tree> kids = new ArrayList<Tree>();
		for (int i=0; i<t.getChildCount(); i++) {
			kids.add(t.getChild(i));
		}
		return kids;
	}

	
	public static List<? extends Tree> getAncestors(Tree t) {
		if ( t.getParent()==null ) return Collections.emptyList();
		List<Tree> ancestors = new ArrayList<Tree>();
		t = t.getParent();
		while ( t!=null ) {
			ancestors.add(0, t); // insert at start
			t = t.getParent();
		}
		return ancestors;
	}

	
	public static boolean isAncestorOf(Tree t, Tree u) {
		if ( t==null || u==null || t.getParent()==null ) return false;
		Tree p = u.getParent();
		while ( p!=null ) {
			if ( t==p ) return true;
			p = p.getParent();
		}
		return false;
	}

	public static Collection<ParseTree> findAllTokenNodes(ParseTree t, int ttype) {
		return findAllNodes(t, ttype, true);
	}

	public static Collection<ParseTree> findAllRuleNodes(ParseTree t, int ruleIndex) {
		return findAllNodes(t, ruleIndex, false);
	}

	public static List<ParseTree> findAllNodes(ParseTree t, int index, boolean findTokens) {
		List<ParseTree> nodes = new ArrayList<ParseTree>();
		_findAllNodes(t, index, findTokens, nodes);
		return nodes;
	}

	public static void _findAllNodes(ParseTree t, int index, boolean findTokens,
									 List<? super ParseTree> nodes)
	{
		// check this node (the root) first
		if ( findTokens && t instanceof TerminalNode ) {
			TerminalNode tnode = (TerminalNode)t;
			if ( tnode.getSymbol().getType()==index ) nodes.add(t);
		}
		else if ( !findTokens && t instanceof ParserRuleContext ) {
			ParserRuleContext ctx = (ParserRuleContext)t;
			if ( ctx.getRuleIndex() == index ) nodes.add(t);
		}
		// check children
		for (int i = 0; i < t.getChildCount(); i++){
			_findAllNodes(t.getChild(i), index, findTokens, nodes);
		}
	}

	
	public static List<ParseTree> getDescendants(ParseTree t) {
		List<ParseTree> nodes = new ArrayList<ParseTree>();
		nodes.add(t);

		int n = t.getChildCount();
		for (int i = 0 ; i < n ; i++){
			nodes.addAll(getDescendants(t.getChild(i)));
		}
		return nodes;
	}


	public static ParserRuleContext getRootOfSubtreeEnclosingRegion(ParseTree t,
																	int startTokenIndex, // inclusive
																	int stopTokenIndex)  // inclusive
	{
		int n = t.getChildCount();
		for (int i = 0; i<n; i++) {
			ParseTree child = t.getChild(i);
			ParserRuleContext r = getRootOfSubtreeEnclosingRegion(child, startTokenIndex, stopTokenIndex);
			if ( r!=null ) return r;
		}
		if ( t instanceof ParserRuleContext ) {
			ParserRuleContext r = (ParserRuleContext) t;
			if ( startTokenIndex>=r.getStart().getTokenIndex() && // is range fully contained in t?
				 (r.getStop()==null || stopTokenIndex<=r.getStop().getTokenIndex()) )
			{
				// note: r.getStop()==null likely implies that we bailed out of parser and there's nothing to the right
				return r;
			}
		}
		return null;
	}

	
	public static void stripChildrenOutOfRange(ParserRuleContext t,
											   ParserRuleContext root,
											   int startIndex,
											   int stopIndex)
	{
		if ( t==null ) return;
		for (int i = 0; i < t.getChildCount(); i++) {
			ParseTree child = t.getChild(i);
			Interval range = child.getSourceInterval();
			if ( child instanceof ParserRuleContext && (range.b < startIndex || range.a > stopIndex) ) {
				if ( isAncestorOf(child, root) ) { // replace only if subtree doesn't have displayed root
					CommonToken abbrev = new CommonToken(Token.INVALID_TYPE, "...");
					t.children.set(i, new TerminalNodeImpl(abbrev));
				}
			}
		}
	}


	private Trees() {
	}
}
