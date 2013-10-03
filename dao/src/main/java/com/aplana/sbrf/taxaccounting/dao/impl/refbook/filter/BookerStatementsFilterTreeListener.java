package com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter;

import com.aplana.sbrf.taxaccounting.dao.refbook.filter.FilterTreeListener;
import com.aplana.sbrf.taxaccounting.model.PreparedStatementData;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;

/**
 * User: ekuvshinov
 */
public class BookerStatementsFilterTreeListener implements FilterTreeListener {
    private RefBook refBook;
    // модель для preparedStatement
    PreparedStatementData ps;

    public BookerStatementsFilterTreeListener(RefBook refBook, PreparedStatementData preparedStatementData){
        this.refBook = refBook;
        this.ps = preparedStatementData;
    }

    @Override
    public void enterNobrakets(@NotNull FilterTreeParser.NobraketsContext ctx) {
        if (ctx.link_type() != null){
            ps.appendQuery(" ");
            ps.appendQuery(ctx.link_type().getText());
            ps.appendQuery(" ");
        }
    }

    @Override
    public void exitNobrakets(@NotNull FilterTreeParser.NobraketsContext ctx) {}

    @Override
    public void enterStrtype(@NotNull FilterTreeParser.StrtypeContext ctx) {
        if(ctx.STRING() != null){
            ps.appendQuery("?");
            ps.addParam(ctx.getText().substring(1, ctx.getText().length() - 1));
        } else  {
            ps.appendQuery(ctx.getText());
        }
    }

    @Override
    public void exitStrtype(@NotNull FilterTreeParser.StrtypeContext ctx) {}

    @Override
    public void enterFuncwrap(@NotNull FilterTreeParser.FuncwrapContext ctx) {
        ps.appendQuery(ctx.functype().getText());
        ps.appendQuery("(");
    }

    @Override
    public void exitFuncwrap(@NotNull FilterTreeParser.FuncwrapContext ctx) {
        ps.appendQuery(")");
    }

    @Override
    public void enterOperand_type(@NotNull FilterTreeParser.Operand_typeContext ctx) {
        ps.appendQuery(" ");
        ps.appendQuery(ctx.getText());
        ps.appendQuery(" ");
    }

    @Override
    public void exitOperand_type(@NotNull FilterTreeParser.Operand_typeContext ctx) {
    }

    @Override
    public void enterQuery(@NotNull FilterTreeParser.QueryContext ctx) {}

    @Override
    public void exitQuery(@NotNull FilterTreeParser.QueryContext ctx) {}

    @Override
    public void enterStandartExpr(@NotNull FilterTreeParser.StandartExprContext ctx) {}

    @Override
    public void exitStandartExpr(@NotNull FilterTreeParser.StandartExprContext ctx) {}

    @Override
    public void enterWithbrakets(@NotNull FilterTreeParser.WithbraketsContext ctx) {
        if (ctx.link_type() != null){
            ps.appendQuery(" ");
            ps.appendQuery(ctx.link_type().getText());
        }
        ps.appendQuery("(");
    }

    @Override
    public void exitWithbrakets(@NotNull FilterTreeParser.WithbraketsContext ctx) {
        ps.appendQuery(")");
    }

    @Override
    public void enterOperand(@NotNull FilterTreeParser.OperandContext ctx) {}

    @Override
    public void exitOperand(@NotNull FilterTreeParser.OperandContext ctx) {}

    @Override
    public void enterFunctype(@NotNull FilterTreeParser.FunctypeContext ctx) {}

    @Override
    public void exitFunctype(@NotNull FilterTreeParser.FunctypeContext ctx) {}

    @Override
    public void enterIsNullExpr(@NotNull FilterTreeParser.IsNullExprContext ctx) {}

    @Override
    public void exitIsNullExpr(@NotNull FilterTreeParser.IsNullExprContext ctx) {
        ps.appendQuery(" is null");
    }

    @Override
    public void enterLink_type(@NotNull FilterTreeParser.Link_typeContext ctx) {}

    @Override
    public void exitLink_type(@NotNull FilterTreeParser.Link_typeContext ctx) {}

    @Override
    public void enterSimpleoperand(@NotNull FilterTreeParser.SimpleoperandContext ctx) {
        if(ctx.STRING() != null){
            ps.appendQuery("?");
            ps.addParam(ctx.getText().substring(1, ctx.getText().length() - 1));
        } else  {
            ps.appendQuery(ctx.getText());
        }
    }

    @Override
    public void exitSimpleoperand(@NotNull FilterTreeParser.SimpleoperandContext ctx) {}

    @Override
    public void visitTerminal(@NotNull TerminalNode terminalNode) {}

    @Override
    public void visitErrorNode(@NotNull ErrorNode errorNode) {}

    @Override
    public void enterEveryRule(@NotNull ParserRuleContext parserRuleContext) {}

    @Override
    public void exitEveryRule(@NotNull ParserRuleContext parserRuleContext) {}
}
