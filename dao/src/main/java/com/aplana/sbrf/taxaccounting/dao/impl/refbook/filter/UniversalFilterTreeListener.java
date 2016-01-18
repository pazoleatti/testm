// Generated from SqlCondition.g4 by ANTLR 4.1
package com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter;

import com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter.components.AbstractTreeListenerComponent;
import com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter.components.foreignkeyresolver.ForeignKeyResolverComponent;
import com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter.components.TypeVerifierComponent;
import com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter.components.UniversalQueryBuilderComponent;
import com.aplana.sbrf.taxaccounting.dao.refbook.filter.FilterTreeListener;
import com.aplana.sbrf.taxaccounting.model.PreparedStatementData;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * Универсальная реализация лиснера для обхода дерева запроса
 * Для работы с внешними таблицами используется объект ForeignKeyResolver
 * Для проверки типов используется объект TypeVerifier
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Qualifier("universalFilterTreeListener")
public class UniversalFilterTreeListener implements FilterTreeListener {

    /** Список компонентов */
    List<AbstractTreeListenerComponent> components;

    @Autowired
    private ApplicationContext applicationContext;

    @PostConstruct
    public void init() {
        components = new ArrayList<AbstractTreeListenerComponent>();

        // компонент отражающий простые лексемы в sql выражение
        components.add(applicationContext.getBean(UniversalQueryBuilderComponent.class));

        // компонент реализующий логику обработки параметров справочника которые являются ссылками
        ForeignKeyResolverComponent foreignKeyResolverComponent = applicationContext.getBean(ForeignKeyResolverComponent.class);
        components.add(foreignKeyResolverComponent);

        // компонент реализующий логику проверти типов
        TypeVerifierComponent verifierComponent = applicationContext.getBean(TypeVerifierComponent.class);
        verifierComponent.setHasLastExternalRefBookAttribute(foreignKeyResolverComponent);
        components.add(verifierComponent);
    }

    public void setPs(PreparedStatementData ps) {
        for (AbstractTreeListenerComponent c: components){
            c.setPreparedStatementData(ps);
        }
    }

    public void setRefBook(RefBook refBook) {
        for (AbstractTreeListenerComponent c: components){
            c.setRefBook(refBook);
        }
    }

    @Override
    public void enterNobrakets(FilterTreeParser.NobraketsContext ctx) {
        for (AbstractTreeListenerComponent c: components){
            c.enterNobrakets(ctx);
        }
    }

    @Override
    public void exitNobrakets(FilterTreeParser.NobraketsContext ctx) {
        for (AbstractTreeListenerComponent c: components){
            c.exitNobrakets(ctx);
        }
    }

    @Override
    public void enterAlias(FilterTreeParser.AliasContext ctx) {
        for (AbstractTreeListenerComponent c: components){
            c.enterAlias(ctx);
        }
    }

    @Override
    public void exitAlias(FilterTreeParser.AliasContext ctx) {
        for (AbstractTreeListenerComponent c: components){
            c.exitAlias(ctx);
        }
    }

    @Override
    public void enterFuncwrap(FilterTreeParser.FuncwrapContext ctx) {
        for (AbstractTreeListenerComponent c: components){
            c.enterFuncwrap(ctx);
        }
    }

    @Override
    public void exitFuncwrap(FilterTreeParser.FuncwrapContext ctx) {
        for (AbstractTreeListenerComponent c: components){
            c.exitFuncwrap(ctx);
        }
    }

    @Override
    public void enterOperand_type(FilterTreeParser.Operand_typeContext ctx) {
        for (AbstractTreeListenerComponent c: components){
            c.enterOperand_type(ctx);
        }
    }

    @Override
    public void exitOperand_type(FilterTreeParser.Operand_typeContext ctx) {
        for (AbstractTreeListenerComponent c: components){
            c.exitOperand_type(ctx);
        }
    }

    @Override
    public void enterString(FilterTreeParser.StringContext ctx) {
        for (AbstractTreeListenerComponent c: components){
            c.enterString(ctx);
        }
    }

    @Override
    public void exitString(FilterTreeParser.StringContext ctx) {
        for (AbstractTreeListenerComponent c: components){
            c.exitString(ctx);
        }
    }

    @Override
    public void enterQuery(FilterTreeParser.QueryContext ctx) {
        for (AbstractTreeListenerComponent c: components){
            c.enterQuery(ctx);
        }
    }

    @Override
    public void exitQuery(FilterTreeParser.QueryContext ctx) {
        for (AbstractTreeListenerComponent c: components){
            c.exitQuery(ctx);
        }
    }

    @Override
    public void enterInternlAlias(FilterTreeParser.InternlAliasContext ctx) {
        for (AbstractTreeListenerComponent c: components){
            c.enterInternlAlias(ctx);
        }
    }

    @Override
    public void exitInternlAlias(FilterTreeParser.InternlAliasContext ctx) {
        for (AbstractTreeListenerComponent c: components){
            c.exitInternlAlias(ctx);
        }
    }

    @Override
    public void enterNumber(FilterTreeParser.NumberContext ctx) {
        for (AbstractTreeListenerComponent c: components){
            c.enterNumber(ctx);
        }
    }

    @Override
    public void exitNumber(FilterTreeParser.NumberContext ctx) {
        for (AbstractTreeListenerComponent c: components){
            c.exitNumber(ctx);
        }
    }

    @Override
    public void enterRoperand(FilterTreeParser.RoperandContext ctx) {
        for (AbstractTreeListenerComponent c: components){
            c.enterRoperand(ctx);
        }
    }

    @Override
    public void exitRoperand(FilterTreeParser.RoperandContext ctx) {
        for (AbstractTreeListenerComponent c: components){
            c.exitRoperand(ctx);
        }
    }

    @Override
    public void enterStandartExpr(FilterTreeParser.StandartExprContext ctx) {
        for (AbstractTreeListenerComponent c: components){
            c.enterStandartExpr(ctx);
        }
    }

    @Override
    public void exitStandartExpr(FilterTreeParser.StandartExprContext ctx) {
        for (AbstractTreeListenerComponent c: components){
            c.exitStandartExpr(ctx);
        }
    }

    @Override
    public void enterWithbrakets(FilterTreeParser.WithbraketsContext ctx) {
        for (AbstractTreeListenerComponent c: components){
            c.enterWithbrakets(ctx);
        }
    }

    @Override
    public void exitWithbrakets(FilterTreeParser.WithbraketsContext ctx) {
        for (AbstractTreeListenerComponent c: components){
            c.exitWithbrakets(ctx);
        }
    }

    @Override
    public void enterEAlias(FilterTreeParser.EAliasContext ctx) {
        for (AbstractTreeListenerComponent c: components){
            c.enterEAlias(ctx);
        }
    }

    @Override
    public void exitEAlias(FilterTreeParser.EAliasContext ctx) {
        for (AbstractTreeListenerComponent c: components){
            c.exitEAlias(ctx);
        }
    }

    @Override
    public void enterOperand(FilterTreeParser.OperandContext ctx) {
        for (AbstractTreeListenerComponent c: components){
            c.enterOperand(ctx);
        }
    }

    @Override
    public void exitOperand(FilterTreeParser.OperandContext ctx) {
        for (AbstractTreeListenerComponent c: components){
            c.exitOperand(ctx);
        }
    }

    @Override
    public void enterLoperand(FilterTreeParser.LoperandContext ctx) {
        for (AbstractTreeListenerComponent c: components){
            c.enterLoperand(ctx);
        }
    }

    @Override
    public void exitLoperand(FilterTreeParser.LoperandContext ctx) {
        for (AbstractTreeListenerComponent c: components){
            c.exitLoperand(ctx);
        }
    }

    @Override
    public void enterExternalAlias(FilterTreeParser.ExternalAliasContext ctx) {
        for (AbstractTreeListenerComponent c: components){
            c.enterExternalAlias(ctx);
        }
    }

    @Override
    public void exitExternalAlias(FilterTreeParser.ExternalAliasContext ctx) {
        for (AbstractTreeListenerComponent c: components){
            c.exitExternalAlias(ctx);
        }
    }

    @Override
    public void enterFunctype(FilterTreeParser.FunctypeContext ctx) {
        for (AbstractTreeListenerComponent c: components){
            c.enterFunctype(ctx);
        }
    }

    @Override
    public void exitFunctype(FilterTreeParser.FunctypeContext ctx) {
        for (AbstractTreeListenerComponent c: components){
            c.exitFunctype(ctx);
        }
    }

    @Override
    public void enterIsNullExpr(FilterTreeParser.IsNullExprContext ctx) {
        for (AbstractTreeListenerComponent c: components){
            c.enterIsNullExpr(ctx);
        }
    }

    @Override
    public void exitIsNullExpr(FilterTreeParser.IsNullExprContext ctx) {
        for (AbstractTreeListenerComponent c: components){
            c.exitIsNullExpr(ctx);
        }
    }

    @Override
    public void enterLink_type(FilterTreeParser.Link_typeContext ctx) {
        for (AbstractTreeListenerComponent c: components){
            c.enterLink_type(ctx);
        }
    }

    @Override
    public void exitLink_type(FilterTreeParser.Link_typeContext ctx) {
        for (AbstractTreeListenerComponent c: components){
            c.exitLink_type(ctx);
        }
    }

    @Override
    public void enterSimpleoperand(FilterTreeParser.SimpleoperandContext ctx) {
        for (AbstractTreeListenerComponent c: components){
            c.enterSimpleoperand(ctx);
        }
    }

    @Override
    public void exitSimpleoperand(FilterTreeParser.SimpleoperandContext ctx) {
        for (AbstractTreeListenerComponent c: components){
            c.exitSimpleoperand(ctx);
        }
    }

    @Override
    public void enterEveryRule(ParserRuleContext ctx) {
        for (AbstractTreeListenerComponent c: components){
            c.enterEveryRule(ctx);
        }
    }

    @Override
    public void exitEveryRule(ParserRuleContext ctx) {
        for (AbstractTreeListenerComponent c: components){
            c.exitEveryRule(ctx);
        }
    }

    @Override
    public void visitTerminal(TerminalNode node) {
        for (AbstractTreeListenerComponent c: components){
            c.visitTerminal(node);
        }
    }

    @Override
    public void visitErrorNode(ErrorNode node) {
        for (AbstractTreeListenerComponent c: components){
            c.visitErrorNode(node);
        }
    }

    @Override
    public void enterTo_date(FilterTreeParser.To_dateContext ctx) {
        for (AbstractTreeListenerComponent c : components) {
            c.enterTo_date(ctx);
        }
    }

    @Override
    public void exitTo_date(FilterTreeParser.To_dateContext ctx) {
        for (AbstractTreeListenerComponent c : components) {
            c.exitTo_date(ctx);
        }
    }
}