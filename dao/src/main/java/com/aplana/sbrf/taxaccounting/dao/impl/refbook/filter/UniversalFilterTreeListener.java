// Generated from SqlCondition.g4 by ANTLR 4.1
package com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter;

import com.aplana.sbrf.taxaccounting.dao.refbook.filter.FilterTreeListener;
import com.aplana.sbrf.taxaccounting.model.PreparedStatementData;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Универсальная реализация лиснера для обхода дерева запроса
 * Для работы с внешними таблицами используется объект ForeignKeyResolver
 * Для проверки типов используется объект TypeVerifier
 *
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Qualifier("universalFilterTreeListener")
public class UniversalFilterTreeListener implements FilterTreeListener {
    // модель для preparedStatement
    PreparedStatementData ps;

    // справочник
    private RefBook refBook;

    @Autowired
    private ApplicationContext applicationContext;

    /**
     * объект-компаньен, реализует функционал
     * работы с внешними справочниками
     */
    private ForeignKeyResolver forignKeyResolver;

    private TypeVerifier typeVerifier;

    @PostConstruct
    public void init(){
        forignKeyResolver = applicationContext.getBean("foreignKeyResolver", ForeignKeyResolver.class);
        typeVerifier = applicationContext.getBean(TypeVerifier.class);
    }

    public PreparedStatementData getPs() {
        return ps;
    }

    public void setPs(PreparedStatementData ps) {
        this.ps = ps;
        forignKeyResolver.setPs(ps);
    }

    public RefBook getRefBook() {
        return refBook;
    }

    public void setRefBook(RefBook refBook) {
        this.refBook = refBook;
        forignKeyResolver.setRefBook(refBook);
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
    public void enterStrtype(@NotNull FilterTreeParser.StrtypeContext ctx) {}

    @Override
    public void exitStrtype(@NotNull FilterTreeParser.StrtypeContext ctx) {}

    @Override
    public void enterAlias(@NotNull FilterTreeParser.AliasContext ctx) {
    }

    @Override
    public void exitAlias(@NotNull FilterTreeParser.AliasContext ctx) {}

    @Override
    public void enterFuncwrap(@NotNull FilterTreeParser.FuncwrapContext ctx) {
        ps.appendQuery(ctx.functype().getText());
        ps.appendQuery("(");
    }

    @Override
    public void exitFuncwrap(@NotNull FilterTreeParser.FuncwrapContext ctx) {
        ps.appendQuery(")");
        // текущие поддерживаемые функции LOWER и LENGTH, они работают со строковыми параметрами
        typeVerifier.checkType(OperandType.STRING, "Function require a string param");
        // установка типа
        if (ctx.functype().LOWER() != null){
            typeVerifier.setType(OperandType.STRING);
        } else if (ctx.functype().LENGTH() != null){
            typeVerifier.setType(OperandType.NUMBER);
        }
    }

	@Override public void enterOperand_type(@NotNull FilterTreeParser.Operand_typeContext ctx) {
        ps.appendQuery(" ");
        ps.appendQuery(ctx.getText());
        ps.appendQuery(" ");
    }

	@Override public void exitOperand_type(@NotNull FilterTreeParser.Operand_typeContext ctx) { }

    @Override
    public void enterString(@NotNull FilterTreeParser.StringContext ctx) {
        ps.appendQuery("?");
        // Строка по умолчанию содерижт символы кавычек. Пример " 'Текст' "
        ps.addParam(ctx.getText().substring(1, ctx.getText().length() - 1));
    }

    @Override
    public void exitString(@NotNull FilterTreeParser.StringContext ctx) {
        typeVerifier.setType(OperandType.STRING);
    }

    @Override public void enterQuery(@NotNull FilterTreeParser.QueryContext ctx) {

    }

	@Override
    public void exitQuery(@NotNull FilterTreeParser.QueryContext ctx) {
        forignKeyResolver.setSqlPartsOfJoin();
    }

    @Override
    public void enterInternlAlias(@NotNull FilterTreeParser.InternlAliasContext ctx) {
        ps.appendQuery(buildAliasStr(ctx.getText()));
    }

    @Override
    public void exitInternlAlias(@NotNull FilterTreeParser.InternlAliasContext ctx) {
        setAliasType(refBook.getAttribute(ctx.getText()));
    }

    @Override
    public void enterNumber(@NotNull FilterTreeParser.NumberContext ctx) {
        ps.appendQuery(ctx.getText());
    }

    @Override
    public void exitNumber(@NotNull FilterTreeParser.NumberContext ctx) {
        typeVerifier.setType(OperandType.NUMBER);
    }

    @Override
    public void enterRoperand(@NotNull FilterTreeParser.RoperandContext ctx) {
        typeVerifier.startCatchRightType();
    }

    @Override
    public void exitRoperand(@NotNull FilterTreeParser.RoperandContext ctx) {}

    @Override
    public void enterStandartExpr(@NotNull FilterTreeParser.StandartExprContext ctx) {
        typeVerifier.reset();
    }

    @Override
    public void exitStandartExpr(@NotNull FilterTreeParser.StandartExprContext ctx) {
        typeVerifier.verifyTypes(ctx);
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
    public void enterEAlias(@NotNull FilterTreeParser.EAliasContext ctx) {
        forignKeyResolver.enterEAliasNode(ctx.ALIAS().getText());
    }

    @Override
    public void exitEAlias(@NotNull FilterTreeParser.EAliasContext ctx) {
        forignKeyResolver.exitEAliasNode();
    }

    @Override
    public void enterOperand(@NotNull FilterTreeParser.OperandContext ctx) { }

    @Override
    public void exitOperand(@NotNull FilterTreeParser.OperandContext ctx) { }

    @Override
    public void enterLoperand(@NotNull FilterTreeParser.LoperandContext ctx) {
        typeVerifier.startCatchLeftType();
    }

    @Override
    public void exitLoperand(@NotNull FilterTreeParser.LoperandContext ctx) {}

    @Override
    public void enterExternalAlias(@NotNull FilterTreeParser.ExternalAliasContext ctx) {
        forignKeyResolver.enterExternalAliasNode(ctx.ALIAS().getText());
    }

    @Override
    public void exitExternalAlias(@NotNull FilterTreeParser.ExternalAliasContext ctx) {
        setAliasType(forignKeyResolver.getLastRefBookAttribute());
    }

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
    public void enterSimpleoperand(@NotNull FilterTreeParser.SimpleoperandContext ctx) {}

    @Override
    public void exitSimpleoperand(@NotNull FilterTreeParser.SimpleoperandContext ctx) {}

	@Override public void enterEveryRule(@NotNull ParserRuleContext ctx) {}

	@Override public void exitEveryRule(@NotNull ParserRuleContext ctx) {}

	@Override public void visitTerminal(@NotNull TerminalNode node) {}

	@Override public void visitErrorNode(@NotNull ErrorNode node) { }

    private String buildAliasStr(String alias){
        StringBuffer sb = new StringBuffer();
        sb.append("a");
        sb.append(alias);
        sb.append(".");
        sb.append(refBook.getAttribute(alias).getAttributeType().toString());
        sb.append("_value");

        return sb.toString();
    }

    private void setAliasType(RefBookAttribute refBookAttribute){
        switch (refBookAttribute.getAttributeType()){
            case STRING: typeVerifier.setType(OperandType.STRING); break;
            case NUMBER: typeVerifier.setType(OperandType.NUMBER); break;
            case DATE: typeVerifier.setType(OperandType.DATE); break;
            case REFERENCE: typeVerifier.setType(OperandType.NUMBER); break;
            default: throw new RuntimeException("Unexpected internal alias type");
        }
    }
}