package com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter.components;

import com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter.FilterTreeParser;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Собирает sql выражение но при этом не учитывает
 * алиасы-параметры которые являются ссылками на другие справочники
 */
abstract class AbstractQueryBuilderComponent extends AbstractTreeListenerComponent {

    /**
     * Максимальный размер строки
     */
    private static final int MAX_STRING_LENGTH = 2000;

    @Override public void enterNobrakets(FilterTreeParser.NobraketsContext ctx) {
        if (ctx.link_type() != null){
            ps.appendQuery(" ");
            ps.appendQuery(ctx.link_type().getText());
            ps.appendQuery(" ");
        }
    }

    @Override
    public void enterFuncwrap(FilterTreeParser.FuncwrapContext ctx) {
        FilterTreeParser.FunctypeContext functype = ctx.functype();
        if (functype != null) {
            ps.appendQuery(functype.getText());
            ps.appendQuery("(");
        }
    }

    @Override
    public void exitFuncwrap(FilterTreeParser.FuncwrapContext ctx) {
        ps.appendQuery(")");
    }

    @Override public void enterOperand_type(FilterTreeParser.Operand_typeContext ctx) {
        ps.appendQuery(" ");
        ps.appendQuery(ctx.getText());
        ps.appendQuery(" ");
    }

    @Override
    public void enterString(FilterTreeParser.StringContext ctx) {
        ps.appendQuery("?");
        // Строка по умолчанию содерижт символы кавычек. Пример " 'Текст' "
        String originalText = ctx.getText().substring(1, ctx.getText().length() - 1).replaceAll("\\\\\'", "\'");
        ps.addParam(trimString(originalText));
    }


    @Override
    public abstract void enterInternlAlias(FilterTreeParser.InternlAliasContext ctx);

    @Override
    public void enterNumber(FilterTreeParser.NumberContext ctx) {
        ps.appendQuery(ctx.getText());
    }

    @Override public void enterWithbrakets(FilterTreeParser.WithbraketsContext ctx) {
        if (ctx.link_type() != null){
            ps.appendQuery(" ");
            ps.appendQuery(ctx.link_type().getText());
        }
        ps.appendQuery("(");
    }

    @Override public void exitWithbrakets(FilterTreeParser.WithbraketsContext ctx) {
        ps.appendQuery(")");
    }

    @Override
    public void exitIsNullExpr(FilterTreeParser.IsNullExprContext ctx) {
        ps.appendQuery(" is null");
    }

    @Override
    public void enterTo_date(FilterTreeParser.To_dateContext ctx) {
        ps.appendQuery(" TO_DATE(");
    }

    @Override
    public void exitTo_date(FilterTreeParser.To_dateContext ctx) {
        String s = checkDateValue(ctx);
        List<Object> params = ps.getParams();
        params.remove(params.size() - 1);
        ps.addParam(s);
    }

    private String checkDateValue(FilterTreeParser.To_dateContext ctx) {
        String search = ctx.getChild(2).getText().replace("\'", "");
        Pattern ddmmyyyy = Pattern.compile("(0[1-9]|[12][0-9]|3[01])\\.(0[1-9]|1[012])\\.(19|20)\\d\\d");
        Matcher matcher = ddmmyyyy.matcher(search);
        if (matcher.matches()) {
            return search;
        }
        Calendar c = Calendar.getInstance();
        c.add(Calendar.YEAR, 1);
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
        return format.format(c.getTime());
    }

    /**
     * Обрезает строку до {@link AbstractQueryBuilderComponent#MAX_STRING_LENGTH}
     * У строки "%VERY_LONG_STR_2000%" обрезается только внутренняя часть:"%VERY_LONG_STR_1998%"
     *
     * @param originalText строка, которую надо обрезать
     */
    private String trimString(String originalText) {
        if (originalText.length() <= MAX_STRING_LENGTH) {
            return originalText;
        } else {
            boolean isLikeStart = originalText.startsWith("%");
            boolean isLikeEnd = originalText.endsWith("%");

            String text = originalText;
            text = text.substring(isLikeStart ? 1 : 0, isLikeEnd ? (text.length() - 1) : text.length());
            text = text.substring(0, MAX_STRING_LENGTH - (isLikeStart ? 1 : 0) - (isLikeEnd ? 1 : 0));
            text = (isLikeStart ? "%" : "") + text + (isLikeEnd ? "%" : "");

            return text;
        }
    }
}