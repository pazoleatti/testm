package com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter.components;

import com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter.FilterTreeParser;
import org.antlr.v4.runtime.misc.NotNull;

/**
 * Собирает sql выражение но при этом не учитывает
 * алиасы-параметры которые являются ссылками на другие справочники
 */
abstract class AbstractQueryBuilderComponent extends AbstractTreeListenerComponent {

    @Override public void enterNobrakets(@NotNull FilterTreeParser.NobraketsContext ctx) {
        if (ctx.link_type() != null){
            ps.appendQuery(" ");
            ps.appendQuery(ctx.link_type().getText());
            ps.appendQuery(" ");
        }
    }

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

    @Override
    public void enterString(@NotNull FilterTreeParser.StringContext ctx) {
        ps.appendQuery("?");
        // Строка по умолчанию содерижт символы кавычек. Пример " 'Текст' "
        ps.addParam(ctx.getText().substring(1, ctx.getText().length() - 1));
    }


    @Override
    public abstract void enterInternlAlias(@NotNull FilterTreeParser.InternlAliasContext ctx);

    @Override
    public void enterNumber(@NotNull FilterTreeParser.NumberContext ctx) {
        ps.appendQuery(ctx.getText());
    }

    @Override public void enterWithbrakets(@NotNull FilterTreeParser.WithbraketsContext ctx) {
        if (ctx.link_type() != null){
            ps.appendQuery(" ");
            ps.appendQuery(ctx.link_type().getText());
        }
        ps.appendQuery("(");
    }

    @Override public void exitWithbrakets(@NotNull FilterTreeParser.WithbraketsContext ctx) {
        ps.appendQuery(")");
    }

    @Override
    public void exitIsNullExpr(@NotNull FilterTreeParser.IsNullExprContext ctx) {
        ps.appendQuery(" is null");
    }
}
