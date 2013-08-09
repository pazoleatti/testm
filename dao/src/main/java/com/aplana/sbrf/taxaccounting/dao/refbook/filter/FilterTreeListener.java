// Generated from FilterTree.g4 by ANTLR 4.1
package com.aplana.sbrf.taxaccounting.dao.refbook.filter;

import com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter.FilterTreeParser;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link FilterTreeParser}.
 */
public interface FilterTreeListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link FilterTreeParser#nobrakets}.
	 * @param ctx the parse tree
	 */
	void enterNobrakets(@NotNull FilterTreeParser.NobraketsContext ctx);
	/**
	 * Exit a parse tree produced by {@link FilterTreeParser#nobrakets}.
	 * @param ctx the parse tree
	 */
	void exitNobrakets(@NotNull FilterTreeParser.NobraketsContext ctx);

	/**
	 * Enter a parse tree produced by {@link FilterTreeParser#operand_type}.
	 * @param ctx the parse tree
	 */
	void enterOperand_type(@NotNull FilterTreeParser.Operand_typeContext ctx);
	/**
	 * Exit a parse tree produced by {@link FilterTreeParser#operand_type}.
	 * @param ctx the parse tree
	 */
	void exitOperand_type(@NotNull FilterTreeParser.Operand_typeContext ctx);

	/**
	 * Enter a parse tree produced by {@link FilterTreeParser#query}.
	 * @param ctx the parse tree
	 */
	void enterQuery(@NotNull FilterTreeParser.QueryContext ctx);
	/**
	 * Exit a parse tree produced by {@link FilterTreeParser#query}.
	 * @param ctx the parse tree
	 */
	void exitQuery(@NotNull FilterTreeParser.QueryContext ctx);

	/**
	 * Enter a parse tree produced by {@link FilterTreeParser#standartExpr}.
	 * @param ctx the parse tree
	 */
	void enterStandartExpr(@NotNull FilterTreeParser.StandartExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link FilterTreeParser#standartExpr}.
	 * @param ctx the parse tree
	 */
	void exitStandartExpr(@NotNull FilterTreeParser.StandartExprContext ctx);

	/**
	 * Enter a parse tree produced by {@link FilterTreeParser#withbrakets}.
	 * @param ctx the parse tree
	 */
	void enterWithbrakets(@NotNull FilterTreeParser.WithbraketsContext ctx);
	/**
	 * Exit a parse tree produced by {@link FilterTreeParser#withbrakets}.
	 * @param ctx the parse tree
	 */
	void exitWithbrakets(@NotNull FilterTreeParser.WithbraketsContext ctx);

	/**
	 * Enter a parse tree produced by {@link FilterTreeParser#operand}.
	 * @param ctx the parse tree
	 */
	void enterOperand(@NotNull FilterTreeParser.OperandContext ctx);
	/**
	 * Exit a parse tree produced by {@link FilterTreeParser#operand}.
	 * @param ctx the parse tree
	 */
	void exitOperand(@NotNull FilterTreeParser.OperandContext ctx);

	/**
	 * Enter a parse tree produced by {@link FilterTreeParser#isNullExpr}.
	 * @param ctx the parse tree
	 */
	void enterIsNullExpr(@NotNull FilterTreeParser.IsNullExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link FilterTreeParser#isNullExpr}.
	 * @param ctx the parse tree
	 */
	void exitIsNullExpr(@NotNull FilterTreeParser.IsNullExprContext ctx);

	/**
	 * Enter a parse tree produced by {@link FilterTreeParser#link_type}.
	 * @param ctx the parse tree
	 */
	void enterLink_type(@NotNull FilterTreeParser.Link_typeContext ctx);
	/**
	 * Exit a parse tree produced by {@link FilterTreeParser#link_type}.
	 * @param ctx the parse tree
	 */
	void exitLink_type(@NotNull FilterTreeParser.Link_typeContext ctx);
}