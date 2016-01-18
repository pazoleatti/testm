package com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter.components;

import com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter.FilterTreeParser;
import com.aplana.sbrf.taxaccounting.dao.refbook.filter.FilterTreeListener;
import com.aplana.sbrf.taxaccounting.model.PreparedStatementData;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;

/**
 * Интерфес компонента для обхода дерева
 *
 * Каждый компонент содежит параметры:
 * 1. PreparedStatementData - объект содержащий свойства и методы для подготовки результирующего sql запроса
 * 2. RefBook - экземпляр текущего справочника для которого формируется запрос
 *
 * Каждый компонент в результате своей работы может воздействовать
 * на результат конечного sql запроса
 */
public abstract class AbstractTreeListenerComponent implements FilterTreeListener {
    /*
    * Модель содержащая данные для PreparedStatement,
    * который используется для составления полного sql запроса
    */
    protected PreparedStatementData ps;

    /**
     * Справочник, для которого составляется запрос
     */
    protected RefBook refBook;

    public void setRefBook(RefBook refBook){
        this.refBook = refBook;
    };

    public void setPreparedStatementData(PreparedStatementData ps){
        this.ps = ps;
    };

    @Override
    public void enterNobrakets(FilterTreeParser.NobraketsContext ctx){};

    @Override
    public void exitNobrakets(FilterTreeParser.NobraketsContext ctx){};

    @Override
    public void enterAlias(FilterTreeParser.AliasContext ctx){};

    @Override
    public void exitAlias(FilterTreeParser.AliasContext ctx){};

    @Override
    public void enterQuery(FilterTreeParser.QueryContext ctx){};

    @Override
    public void exitQuery(FilterTreeParser.QueryContext ctx){};

    @Override
    public void enterInternlAlias(FilterTreeParser.InternlAliasContext ctx){};

    @Override
    public void exitInternlAlias(FilterTreeParser.InternlAliasContext ctx){};

    @Override
    public void enterNumber(FilterTreeParser.NumberContext ctx){};

    @Override
    public void exitNumber(FilterTreeParser.NumberContext ctx){};

    @Override
    public void enterRoperand(FilterTreeParser.RoperandContext ctx){};

    @Override
    public void exitRoperand(FilterTreeParser.RoperandContext ctx){};

    @Override
    public void enterOperand(FilterTreeParser.OperandContext ctx){};

    @Override
    public void exitOperand(FilterTreeParser.OperandContext ctx){};

    @Override
    public void enterLoperand(FilterTreeParser.LoperandContext ctx){};

    @Override
    public void exitLoperand(FilterTreeParser.LoperandContext ctx){};

    @Override
    public void enterExternalAlias(FilterTreeParser.ExternalAliasContext ctx){};

    @Override
    public void exitExternalAlias(FilterTreeParser.ExternalAliasContext ctx){};

    @Override
    public void enterFuncwrap(FilterTreeParser.FuncwrapContext ctx){};

    @Override
    public void exitFuncwrap(FilterTreeParser.FuncwrapContext ctx){};

    @Override
    public void enterOperand_type(FilterTreeParser.Operand_typeContext ctx){};

    @Override
    public void exitOperand_type(FilterTreeParser.Operand_typeContext ctx){};

    @Override
    public void enterString(FilterTreeParser.StringContext ctx){};

    @Override
    public void exitString(FilterTreeParser.StringContext ctx){};

    @Override
    public void enterStandartExpr(FilterTreeParser.StandartExprContext ctx){};

    @Override
    public void exitStandartExpr(FilterTreeParser.StandartExprContext ctx){};

    @Override
    public void enterWithbrakets(FilterTreeParser.WithbraketsContext ctx){};

    @Override
    public void exitWithbrakets(FilterTreeParser.WithbraketsContext ctx){};

    @Override
    public void enterEAlias(FilterTreeParser.EAliasContext ctx){};

    @Override
    public void exitEAlias(FilterTreeParser.EAliasContext ctx){};

    @Override
    public void enterLink_type(FilterTreeParser.Link_typeContext ctx){};

    @Override
    public void exitLink_type(FilterTreeParser.Link_typeContext ctx){};

    @Override
    public void enterIsNullExpr(FilterTreeParser.IsNullExprContext ctx){};

    @Override
    public void exitIsNullExpr(FilterTreeParser.IsNullExprContext ctx){};

    @Override
    public void enterFunctype(FilterTreeParser.FunctypeContext ctx){};

    @Override
    public void exitFunctype(FilterTreeParser.FunctypeContext ctx){};

    @Override
    public void enterSimpleoperand(FilterTreeParser.SimpleoperandContext ctx){};

    @Override
    public void exitSimpleoperand(FilterTreeParser.SimpleoperandContext ctx){};

    @Override
    public void enterEveryRule(ParserRuleContext ctx) {}

    @Override
    public void exitEveryRule(ParserRuleContext ctx) {}

    @Override
    public void visitTerminal(TerminalNode node) {}

    @Override
    public void visitErrorNode(ErrorNode node) { }

    @Override
    public void enterTo_date(FilterTreeParser.To_dateContext ctx) { }

    @Override
    public void exitTo_date(FilterTreeParser.To_dateContext ctx) { }
}
