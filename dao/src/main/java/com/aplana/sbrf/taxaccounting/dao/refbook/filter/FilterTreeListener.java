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
	 * Enter a parse tree produced by {@link FilterTreeParser#string}.
	 * @param ctx the parse tree
	 */
	void enterString(@NotNull FilterTreeParser.StringContext ctx);
	/**
	 * Exit a parse tree produced by {@link FilterTreeParser#string}.
	 * @param ctx the parse tree
	 */
	void exitString(@NotNull FilterTreeParser.StringContext ctx);

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
	 * Enter a parse tree produced by {@link FilterTreeParser#loperand}.
	 * @param ctx the parse tree
	 */
	void enterLoperand(@NotNull FilterTreeParser.LoperandContext ctx);
	/**
	 * Exit a parse tree produced by {@link FilterTreeParser#loperand}.
	 * @param ctx the parse tree
	 */
	void exitLoperand(@NotNull FilterTreeParser.LoperandContext ctx);

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
	 * Enter a parse tree produced by {@link FilterTreeParser#roperand}.
	 * @param ctx the parse tree
	 */
	void enterRoperand(@NotNull FilterTreeParser.RoperandContext ctx);
	/**
	 * Exit a parse tree produced by {@link FilterTreeParser#roperand}.
	 * @param ctx the parse tree
	 */
	void exitRoperand(@NotNull FilterTreeParser.RoperandContext ctx);

	/**
	 * Enter a parse tree produced by {@link FilterTreeParser#eAlias}.
	 * @param ctx the parse tree
	 */
	void enterEAlias(@NotNull FilterTreeParser.EAliasContext ctx);
	/**
	 * Exit a parse tree produced by {@link FilterTreeParser#eAlias}.
	 * @param ctx the parse tree
	 */
	void exitEAlias(@NotNull FilterTreeParser.EAliasContext ctx);

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
	 * Enter a parse tree produced by {@link FilterTreeParser#link_type}.
	 * @param ctx the parse tree
	 */
	void enterLink_type(@NotNull FilterTreeParser.Link_typeContext ctx);
	/**
	 * Exit a parse tree produced by {@link FilterTreeParser#link_type}.
	 * @param ctx the parse tree
	 */
	void exitLink_type(@NotNull FilterTreeParser.Link_typeContext ctx);

	/**
	 * Enter a parse tree produced by {@link FilterTreeParser#simpleoperand}.
	 * @param ctx the parse tree
	 */
	void enterSimpleoperand(@NotNull FilterTreeParser.SimpleoperandContext ctx);
	/**
	 * Exit a parse tree produced by {@link FilterTreeParser#simpleoperand}.
	 * @param ctx the parse tree
	 */
	void exitSimpleoperand(@NotNull FilterTreeParser.SimpleoperandContext ctx);

	/**
	 * Enter a parse tree produced by {@link FilterTreeParser#functype}.
	 * @param ctx the parse tree
	 */
	void enterFunctype(@NotNull FilterTreeParser.FunctypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link FilterTreeParser#functype}.
	 * @param ctx the parse tree
	 */
	void exitFunctype(@NotNull FilterTreeParser.FunctypeContext ctx);

	/**
	 * Enter a parse tree produced by {@link FilterTreeParser#number}.
	 * @param ctx the parse tree
	 */
	void enterNumber(@NotNull FilterTreeParser.NumberContext ctx);
	/**
	 * Exit a parse tree produced by {@link FilterTreeParser#number}.
	 * @param ctx the parse tree
	 */
	void exitNumber(@NotNull FilterTreeParser.NumberContext ctx);

	/**
	 * Enter a parse tree produced by {@link FilterTreeParser#to_date}.
	 * @param ctx the parse tree
	 */
	void enterTo_date(@NotNull FilterTreeParser.To_dateContext ctx);
	/**
	 * Exit a parse tree produced by {@link FilterTreeParser#to_date}.
	 * @param ctx the parse tree
	 */
	void exitTo_date(@NotNull FilterTreeParser.To_dateContext ctx);

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
	 * Enter a parse tree produced by {@link FilterTreeParser#internlAlias}.
	 * @param ctx the parse tree
	 */
	void enterInternlAlias(@NotNull FilterTreeParser.InternlAliasContext ctx);
	/**
	 * Exit a parse tree produced by {@link FilterTreeParser#internlAlias}.
	 * @param ctx the parse tree
	 */
	void exitInternlAlias(@NotNull FilterTreeParser.InternlAliasContext ctx);

	/**
	 * Enter a parse tree produced by {@link FilterTreeParser#alias}.
	 * @param ctx the parse tree
	 */
	void enterAlias(@NotNull FilterTreeParser.AliasContext ctx);
	/**
	 * Exit a parse tree produced by {@link FilterTreeParser#alias}.
	 * @param ctx the parse tree
	 */
	void exitAlias(@NotNull FilterTreeParser.AliasContext ctx);

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
	 * Enter a parse tree produced by {@link FilterTreeParser#funcwrap}.
	 * @param ctx the parse tree
	 */
	void enterFuncwrap(@NotNull FilterTreeParser.FuncwrapContext ctx);
	/**
	 * Exit a parse tree produced by {@link FilterTreeParser#funcwrap}.
	 * @param ctx the parse tree
	 */
	void exitFuncwrap(@NotNull FilterTreeParser.FuncwrapContext ctx);

	/**
	 * Enter a parse tree produced by {@link FilterTreeParser#externalAlias}.
	 * @param ctx the parse tree
	 */
	void enterExternalAlias(@NotNull FilterTreeParser.ExternalAliasContext ctx);
	/**
	 * Exit a parse tree produced by {@link FilterTreeParser#externalAlias}.
	 * @param ctx the parse tree
	 */
	void exitExternalAlias(@NotNull FilterTreeParser.ExternalAliasContext ctx);
}