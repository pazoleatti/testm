package com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter;

import com.aplana.sbrf.taxaccounting.dao.refbook.filter.FilterTreeListener;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;

/**
 * User: ekuvshinov
 */
public class Income101FilterTreeListener implements FilterTreeListener {
    private RefBook refBook;
    private StringBuffer query;

    public Income101FilterTreeListener(RefBook refBook, StringBuffer query){
        this.refBook = refBook;
        this.query = query;
    }

    @Override
    public void enterNobrakets(@NotNull FilterTreeParser.NobraketsContext ctx) {
        if (ctx.link_type() != null){
            query.append(" ").append(ctx.link_type().getText());
        }
    }

    @Override
    public void exitNobrakets(@NotNull FilterTreeParser.NobraketsContext ctx) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void enterOperand_type(@NotNull FilterTreeParser.Operand_typeContext ctx) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void exitOperand_type(@NotNull FilterTreeParser.Operand_typeContext ctx) {
        query.append(" ").append(ctx.getText());
    }

    @Override
    public void enterQuery(@NotNull FilterTreeParser.QueryContext ctx) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void exitQuery(@NotNull FilterTreeParser.QueryContext ctx) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void enterStandartExpr(@NotNull FilterTreeParser.StandartExprContext ctx) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void exitStandartExpr(@NotNull FilterTreeParser.StandartExprContext ctx) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void enterWithbrakets(@NotNull FilterTreeParser.WithbraketsContext ctx) {
        if (ctx.link_type() != null){
            query.append(" ").append(ctx.link_type().getText());
        }
        query.append("(");
    }

    @Override
    public void exitWithbrakets(@NotNull FilterTreeParser.WithbraketsContext ctx) {
        query.append(")");
    }

    @Override
    public void enterOperand(@NotNull FilterTreeParser.OperandContext ctx) {
        query.append(" ").append(ctx.getText());
    }

    @Override
    public void exitOperand(@NotNull FilterTreeParser.OperandContext ctx) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void enterIsNullExpr(@NotNull FilterTreeParser.IsNullExprContext ctx) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void exitIsNullExpr(@NotNull FilterTreeParser.IsNullExprContext ctx) {
        query.append(" is null");
    }

    @Override
    public void enterLink_type(@NotNull FilterTreeParser.Link_typeContext ctx) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void exitLink_type(@NotNull FilterTreeParser.Link_typeContext ctx) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void visitTerminal(@NotNull TerminalNode terminalNode) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void visitErrorNode(@NotNull ErrorNode errorNode) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void enterEveryRule(@NotNull ParserRuleContext parserRuleContext) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void exitEveryRule(@NotNull ParserRuleContext parserRuleContext) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
