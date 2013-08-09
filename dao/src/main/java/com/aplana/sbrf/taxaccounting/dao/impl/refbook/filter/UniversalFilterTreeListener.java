// Generated from SqlCondition.g4 by ANTLR 4.1
package com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter;

import com.aplana.sbrf.taxaccounting.dao.refbook.filter.FilterTreeListener;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;

/**
 * This class provides an empty implementation of {@link com.aplana.sbrf.taxaccounting.dao.refbook.filter.FilterTreeListener},
 * which can be extended to create a listener which only needs to handle a subset
 * of the available methods.
 */
public class UniversalFilterTreeListener implements FilterTreeListener {
    // объект для создания строки запроса
    private StringBuffer query;
    // справочник
    private RefBook refBook;

    public UniversalFilterTreeListener(RefBook refBook, StringBuffer stringBuffer){
        this.query = stringBuffer;
        this.refBook = refBook;
    }

    /**
	 * {@inheritDoc}
	 * <p/>
	 * The default implementation does nothing.
	 */
	@Override public void enterNobrakets(@NotNull FilterTreeParser.NobraketsContext ctx) {
        if (ctx.link_type() != null){
            appendToQuery(ctx.link_type().getText());
        }
    }
	/**
	 * {@inheritDoc}
	 * <p/>
	 * The default implementation does nothing.
	 */
	@Override public void exitNobrakets(@NotNull FilterTreeParser.NobraketsContext ctx) { }

	/**
	 * {@inheritDoc}
	 * <p/>
	 * The default implementation does nothing.
	 */
	@Override public void enterOperand_type(@NotNull FilterTreeParser.Operand_typeContext ctx) {
        appendToQuery(ctx.getText());
    }
	/**
	 * {@inheritDoc}
	 * <p/>
	 * The default implementation does nothing.
	 */
	@Override public void exitOperand_type(@NotNull FilterTreeParser.Operand_typeContext ctx) { }

	/**
	 * {@inheritDoc}
	 * <p/>
	 * The default implementation does nothing.
	 */
	@Override public void enterQuery(@NotNull FilterTreeParser.QueryContext ctx) { }
	/**
	 * {@inheritDoc}
	 * <p/>
	 * The default implementation does nothing.
	 */
	@Override public void exitQuery(@NotNull FilterTreeParser.QueryContext ctx) { }

    @Override
    public void enterStandartExpr(@NotNull FilterTreeParser.StandartExprContext ctx) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void exitStandartExpr(@NotNull FilterTreeParser.StandartExprContext ctx) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
	 * {@inheritDoc}
	 * <p/>
	 * The default implementation does nothing.
	 */
	@Override public void enterWithbrakets(@NotNull FilterTreeParser.WithbraketsContext ctx) {
        if (ctx.link_type() != null){
            appendToQuery(ctx.link_type().getText());
        }
        query.append("(");
    }
	/**
	 * {@inheritDoc}
	 * <p/>
	 * The default implementation does nothing.
	 */
	@Override public void exitWithbrakets(@NotNull FilterTreeParser.WithbraketsContext ctx) {
        query.append(")");
    }

	/**
	 * {@inheritDoc}
	 * <p/>
	 * The default implementation does nothing.
	 */
	@Override public void enterOperand(@NotNull FilterTreeParser.OperandContext ctx) {
        if (ctx.ALIAS() != null){
            appendToQuery("a");
            query.append(ctx.getText());
            query.append(".");
            query.append(refBook.getAttribute(ctx.getText()).getAttributeType().toString());
            query.append("_value");
        } else {
            appendToQuery(ctx.getText());
        }
    }
	/**
	 * {@inheritDoc}
	 * <p/>
	 * The default implementation does nothing.
	 */
	@Override public void exitOperand(@NotNull FilterTreeParser.OperandContext ctx) { }

    @Override
    public void enterIsNullExpr(@NotNull FilterTreeParser.IsNullExprContext ctx) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void exitIsNullExpr(@NotNull FilterTreeParser.IsNullExprContext ctx) {
        query.append(" is null");
    }

    /**
	 * {@inheritDoc}
	 * <p/>
	 * The default implementation does nothing.
	 */
	@Override public void enterLink_type(@NotNull FilterTreeParser.Link_typeContext ctx) { }
	/**
	 * {@inheritDoc}
	 * <p/>
	 * The default implementation does nothing.
	 */
	@Override public void exitLink_type(@NotNull FilterTreeParser.Link_typeContext ctx) { }

	/**
	 * {@inheritDoc}
	 * <p/>
	 * The default implementation does nothing.
	 */
	@Override public void enterEveryRule(@NotNull ParserRuleContext ctx) { }
	/**
	 * {@inheritDoc}
	 * <p/>
	 * The default implementation does nothing.
	 */
	@Override public void exitEveryRule(@NotNull ParserRuleContext ctx) { }
	/**
	 * {@inheritDoc}
	 * <p/>
	 * The default implementation does nothing.
	 */
	@Override public void visitTerminal(@NotNull TerminalNode node) { }
	/**
	 * {@inheritDoc}
	 * <p/>
	 * The default implementation does nothing.
	 */
	@Override public void visitErrorNode(@NotNull ErrorNode node) { }

    private void appendToQuery(String queryPart){
        query.append(" ").append(queryPart);
    }
}