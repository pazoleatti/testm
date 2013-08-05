package com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter;

import com.aplana.sbrf.taxaccounting.dao.refbook.filter.FilterTreeListener;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;

/**
 * User: ekuvshinov
 */
public class DepartmentFilterTreeListener implements FilterTreeListener {
    @Override
    public void enterNobrakets(@NotNull FilterTreeParser.NobraketsContext ctx) {
        //To change body of implemented methods use File | Settings | File Templates.
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
        //To change body of implemented methods use File | Settings | File Templates.
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
    public void enterExpr(@NotNull FilterTreeParser.ExprContext ctx) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void exitExpr(@NotNull FilterTreeParser.ExprContext ctx) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void enterWithbrakets(@NotNull FilterTreeParser.WithbraketsContext ctx) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void exitWithbrakets(@NotNull FilterTreeParser.WithbraketsContext ctx) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void enterOperand(@NotNull FilterTreeParser.OperandContext ctx) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void exitOperand(@NotNull FilterTreeParser.OperandContext ctx) {
        //To change body of implemented methods use File | Settings | File Templates.
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
