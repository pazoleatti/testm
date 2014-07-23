package com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter.components;

import com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter.FilterTreeParser;
import com.aplana.sbrf.taxaccounting.dao.refbook.filter.FilterTreeListener;
import com.aplana.sbrf.taxaccounting.model.PreparedStatementData;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.NotNull;
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
    PreparedStatementData ps;

    /**
     * Справочник, для которого составляется запрос
     */
    RefBook refBook;

    public void setRefBook(RefBook refBook){
        this.refBook = refBook;
    };

    public void setPreparedStatementData(PreparedStatementData ps){
        this.ps = ps;
    };

    @Override
    public void enterNobrakets(@NotNull FilterTreeParser.NobraketsContext ctx){};

    @Override
    public void exitNobrakets(@NotNull FilterTreeParser.NobraketsContext ctx){};

    @Override
    public void enterAlias(@NotNull FilterTreeParser.AliasContext ctx){};

    @Override
    public void exitAlias(@NotNull FilterTreeParser.AliasContext ctx){};

    @Override
    public void enterQuery(@NotNull FilterTreeParser.QueryContext ctx){};

    @Override
    public void exitQuery(@NotNull FilterTreeParser.QueryContext ctx){};

    @Override
    public void enterInternlAlias(@NotNull FilterTreeParser.InternlAliasContext ctx){};

    @Override
    public void exitInternlAlias(@NotNull FilterTreeParser.InternlAliasContext ctx){};

    @Override
    public void enterNumber(@NotNull FilterTreeParser.NumberContext ctx){};

    @Override
    public void exitNumber(@NotNull FilterTreeParser.NumberContext ctx){};

    @Override
    public void enterRoperand(@NotNull FilterTreeParser.RoperandContext ctx){};

    @Override
    public void exitRoperand(@NotNull FilterTreeParser.RoperandContext ctx){};

    @Override
    public void enterOperand(@NotNull FilterTreeParser.OperandContext ctx){};

    @Override
    public void exitOperand(@NotNull FilterTreeParser.OperandContext ctx){};

    @Override
    public void enterLoperand(@NotNull FilterTreeParser.LoperandContext ctx){};

    @Override
    public void exitLoperand(@NotNull FilterTreeParser.LoperandContext ctx){};

    @Override
    public void enterExternalAlias(@NotNull FilterTreeParser.ExternalAliasContext ctx){};

    @Override
    public void exitExternalAlias(@NotNull FilterTreeParser.ExternalAliasContext ctx){};

    @Override
    public void enterFuncwrap(@NotNull FilterTreeParser.FuncwrapContext ctx){};

    @Override
    public void exitFuncwrap(@NotNull FilterTreeParser.FuncwrapContext ctx){};

    @Override
    public void enterOperand_type(@NotNull FilterTreeParser.Operand_typeContext ctx){};

    @Override
    public void exitOperand_type(@NotNull FilterTreeParser.Operand_typeContext ctx){};

    @Override
    public void enterString(@NotNull FilterTreeParser.StringContext ctx){};

    @Override
    public void exitString(@NotNull FilterTreeParser.StringContext ctx){};

    @Override
    public void enterStandartExpr(@NotNull FilterTreeParser.StandartExprContext ctx){};

    @Override
    public void exitStandartExpr(@NotNull FilterTreeParser.StandartExprContext ctx){};

    @Override
    public void enterWithbrakets(@NotNull FilterTreeParser.WithbraketsContext ctx){};

    @Override
    public void exitWithbrakets(@NotNull FilterTreeParser.WithbraketsContext ctx){};

    @Override
    public void enterEAlias(@NotNull FilterTreeParser.EAliasContext ctx){};

    @Override
    public void exitEAlias(@NotNull FilterTreeParser.EAliasContext ctx){};

    @Override
    public void enterLink_type(@NotNull FilterTreeParser.Link_typeContext ctx){};

    @Override
    public void exitLink_type(@NotNull FilterTreeParser.Link_typeContext ctx){};

    @Override
    public void enterIsNullExpr(@NotNull FilterTreeParser.IsNullExprContext ctx){};

    @Override
    public void exitIsNullExpr(@NotNull FilterTreeParser.IsNullExprContext ctx){};

    @Override
    public void enterFunctype(@NotNull FilterTreeParser.FunctypeContext ctx){};

    @Override
    public void exitFunctype(@NotNull FilterTreeParser.FunctypeContext ctx){};

    @Override
    public void enterSimpleoperand(@NotNull FilterTreeParser.SimpleoperandContext ctx){};

    @Override
    public void exitSimpleoperand(@NotNull FilterTreeParser.SimpleoperandContext ctx){};

    @Override
    public void enterEveryRule(@NotNull ParserRuleContext ctx) {}

    @Override
    public void exitEveryRule(@NotNull ParserRuleContext ctx) {}

    @Override
    public void visitTerminal(@NotNull TerminalNode node) {}

    @Override
    public void visitErrorNode(@NotNull ErrorNode node) { }
}
