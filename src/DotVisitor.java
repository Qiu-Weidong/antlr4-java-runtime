// Generated from java-escape by ANTLR 4.11.1
import runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link DotParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface DotVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link DotParser#graph_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGraph_list(DotParser.Graph_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link DotParser#graph}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGraph(DotParser.GraphContext ctx);
	/**
	 * Visit a parse tree produced by {@link DotParser#stmt_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStmt_list(DotParser.Stmt_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link DotParser#stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStmt(DotParser.StmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link DotParser#attr_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAttr_stmt(DotParser.Attr_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link DotParser#attr_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAttr_list(DotParser.Attr_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link DotParser#a_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitA_list(DotParser.A_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link DotParser#assign_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssign_stmt(DotParser.Assign_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link DotParser#edge_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEdge_stmt(DotParser.Edge_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link DotParser#edgeRHS}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEdgeRHS(DotParser.EdgeRHSContext ctx);
	/**
	 * Visit a parse tree produced by {@link DotParser#edgeop}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEdgeop(DotParser.EdgeopContext ctx);
	/**
	 * Visit a parse tree produced by {@link DotParser#node_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNode_stmt(DotParser.Node_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link DotParser#node_id}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNode_id(DotParser.Node_idContext ctx);
	/**
	 * Visit a parse tree produced by {@link DotParser#compass_pt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCompass_pt(DotParser.Compass_ptContext ctx);
	/**
	 * Visit a parse tree produced by {@link DotParser#subgraph}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSubgraph(DotParser.SubgraphContext ctx);
	/**
	 * Visit a parse tree produced by {@link DotParser#id}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitId(DotParser.IdContext ctx);
	/**
	 * Visit a parse tree produced by {@link DotParser#lexpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLexpr(DotParser.LexprContext ctx);
	/**
	 * Visit a parse tree produced by {@link DotParser#rexpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRexpr(DotParser.RexprContext ctx);
}