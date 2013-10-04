// Generated from SqlCondition.g4 by ANTLR 4.1
package com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter;

import com.aplana.sbrf.taxaccounting.dao.refbook.filter.FilterTreeListener;
import com.aplana.sbrf.taxaccounting.model.PreparedStatementData;
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
    // модель для preparedStatement
    PreparedStatementData ps;
    // справочник
    private RefBook refBook;

    public UniversalFilterTreeListener(RefBook refBook, PreparedStatementData preparedStatementData){
        ps = preparedStatementData;
        this.refBook = refBook;
    }

	@Override public void enterNobrakets(@NotNull FilterTreeParser.NobraketsContext ctx) {
        if (ctx.link_type() != null){
            ps.appendQuery(" ");
            ps.appendQuery(ctx.link_type().getText());
            ps.appendQuery(" ");
        }
    }

	@Override public void exitNobrakets(@NotNull FilterTreeParser.NobraketsContext ctx) { }

    @Override
    public void enterStrtype(@NotNull FilterTreeParser.StrtypeContext ctx) {
        if (ctx.ALIAS() != null){
            ps.appendQuery(buildAliasStr(ctx.getText()));
        } else if(ctx.STRING() != null){
            ps.appendQuery("?");
            ps.addParam(ctx.getText().substring(1, ctx.getText().length() - 1));
        } else  {
            appendToQuery(ctx.getText());
        }
    }

    @Override
    public void exitStrtype(@NotNull FilterTreeParser.StrtypeContext ctx) { }

    @Override
    public void enterFuncwrap(@NotNull FilterTreeParser.FuncwrapContext ctx) {
        ps.appendQuery(ctx.functype().getText());
        ps.appendQuery("(");
    }

    @Override
    public void exitFuncwrap(@NotNull FilterTreeParser.FuncwrapContext ctx) {
        ps.appendQuery(")");
    }

	@Override public void enterOperand_type(@NotNull FilterTreeParser.Operand_typeContext ctx) {
        ps.appendQuery(" ");
        ps.appendQuery(ctx.getText());
        ps.appendQuery(" ");
    }

	@Override public void exitOperand_type(@NotNull FilterTreeParser.Operand_typeContext ctx) { }

	@Override public void enterQuery(@NotNull FilterTreeParser.QueryContext ctx) { }

	@Override
    public void exitQuery(@NotNull FilterTreeParser.QueryContext ctx) { }

    @Override
    public void enterStandartExpr(@NotNull FilterTreeParser.StandartExprContext ctx) {}

    @Override
    public void exitStandartExpr(@NotNull FilterTreeParser.StandartExprContext ctx) { }

	@Override public void enterWithbrakets(@NotNull FilterTreeParser.WithbraketsContext ctx) {
        if (ctx.link_type() != null){
            appendToQuery(ctx.link_type().getText());
        }
        ps.appendQuery("(");
    }

	@Override public void exitWithbrakets(@NotNull FilterTreeParser.WithbraketsContext ctx) {
        ps.appendQuery(")");
    }

    @Override
    public void enterOperand(@NotNull FilterTreeParser.OperandContext ctx) { }

    @Override
    public void exitOperand(@NotNull FilterTreeParser.OperandContext ctx) { }

    @Override
    public void enterFunctype(@NotNull FilterTreeParser.FunctypeContext ctx) { }

    @Override
    public void exitFunctype(@NotNull FilterTreeParser.FunctypeContext ctx) { }

    @Override
    public void enterIsNullExpr(@NotNull FilterTreeParser.IsNullExprContext ctx) { }

    @Override
    public void exitIsNullExpr(@NotNull FilterTreeParser.IsNullExprContext ctx) {
        ps.appendQuery(" is null");
    }

	@Override public void enterLink_type(@NotNull FilterTreeParser.Link_typeContext ctx) { }

	@Override public void exitLink_type(@NotNull FilterTreeParser.Link_typeContext ctx) { }

    @Override
    public void enterSimpleoperand(@NotNull FilterTreeParser.SimpleoperandContext ctx) {
        if (ctx.ALIAS() != null){
            ps.appendQuery(buildAliasStr(ctx.getText()));
        } else if(ctx.STRING() != null){
            ps.appendQuery("?");
            ps.addParam(ctx.getText().substring(1, ctx.getText().length() - 1));
        } else {
            ps.appendQuery(ctx.getText());
        }
    }

    @Override
    public void exitSimpleoperand(@NotNull FilterTreeParser.SimpleoperandContext ctx) {}

	@Override public void enterEveryRule(@NotNull ParserRuleContext ctx) {}

	@Override public void exitEveryRule(@NotNull ParserRuleContext ctx) {}

	@Override public void visitTerminal(@NotNull TerminalNode node) {}

	@Override public void visitErrorNode(@NotNull ErrorNode node) { }

    private void appendToQuery(String queryPart){
        ps.appendQuery(" ");
        ps.appendQuery(queryPart);
    }

    private String buildAliasStr(String alias){
        StringBuffer sb = new StringBuffer();
        sb.append("a");
        sb.append(alias);
        sb.append(".");
        sb.append(refBook.getAttribute(alias).getAttributeType().toString());
        sb.append("_value");

        return sb.toString();
    }
}