package com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter.components;

import com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter.FilterTreeParser;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Компонент для проверку типов
 * пример при вызове фильтра с "length(name) > city.name"
 * фильтр выкинет исключение RuntimeException "Not match types. (left: NUMBER, right: STRING) on expression <length(name) > city.name>"
 *
 * Объект содержит значения типов правого и левого операнда в выражении, а
 * так же флаг который устанавливает с каким из операндов идет текущая работа.
 *
 * Работа с типами организована в виде всплытия типов, каждый узел выдает свой тип
 * (тип алиаса, тип строки, тип чисел) высокоуровневые функции проверяют тип входящих данных,
 * и переопределяют тип, в конечном счете в момен выхода из узла выражения (expression) проверяется
 * типы правого и левого операнда.
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class TypeVerifierComponent extends AbstractTreeListenerComponent {

    public interface HasLastExternalRefBookAttribute{
        RefBookAttribute getLastExternalRefBookAttribute();
    }

    private boolean left;
    private boolean right;
    private OperandType letfType, rightType;
    private HasLastExternalRefBookAttribute hasLastExternalRefBookAttribute;

    @Override
    public void exitFuncwrap(FilterTreeParser.FuncwrapContext ctx) {
        // проверка типа данных для функции
        checkFunctionType(ctx);

        // установка типа
        if (ctx.functype() != null) {
            if (ctx.functype().LOWER() != null){
                setType(OperandType.STRING);
            } else if (ctx.functype().LENGTH() != null){
                setType(OperandType.NUMBER);
            } else if (ctx.functype().TO_CHAR() != null){
                setType(OperandType.STRING);
            }
        }
    }

    @Override
    public void exitString(FilterTreeParser.StringContext ctx) {
        setType(OperandType.STRING);
    }

    @Override
    public void exitNumber(FilterTreeParser.NumberContext ctx) {
        setType(OperandType.NUMBER);
    }

    @Override
    public void enterRoperand(FilterTreeParser.RoperandContext ctx) {
        /**
         * Метод устанавливает работу с правым операндом
         */
        left = false;
        right = true;
    }

    @Override
    public void enterStandartExpr(FilterTreeParser.StandartExprContext ctx) {
        /**
         * Сброс параметров, для повторного использования объекта
         */
        letfType = null;
        rightType = null;
        left = false;
        right = false;
    }

    @Override
    public void exitStandartExpr(FilterTreeParser.StandartExprContext ctx) {
        /**
         * Проверка типов левого и правого операнда
         */
        if (letfType != null && rightType != null){
            if (letfType != rightType){
                throw new RuntimeException("Not match types. (left: "+letfType+", right: "+rightType+") on expression <"+ctx.getText()+">");
            }
        } else {
            throw new RuntimeException("Not initiate one or both types.");
        }
    }

    @Override
    public void enterLoperand(FilterTreeParser.LoperandContext ctx) {
        startCatchLeftType();
    }

    @Override
    public void enterIsNullExpr(FilterTreeParser.IsNullExprContext ctx) {
        // хотя это совсем не играет роли, чтоб исключить исплючение
        startCatchLeftType();
    }

    @Override
    public void exitExternalAlias(FilterTreeParser.ExternalAliasContext ctx) {
        setAliasType(hasLastExternalRefBookAttribute.getLastExternalRefBookAttribute());
    }

    @Override
    public void exitInternlAlias(FilterTreeParser.InternlAliasContext ctx) {
        setAliasType(ctx.getText());
    }

    private void setAliasType(String alias){
        if (alias.equalsIgnoreCase(RefBook.RECORD_ID_ALIAS)){
            setType(OperandType.NUMBER);
        } else{
            setAliasType(refBook.getAttribute(alias));
        }
    }

    private void setAliasType(RefBookAttribute refBookAttribute){
        switch (refBookAttribute.getAttributeType()){
            case STRING: setType(OperandType.STRING); break;
            case NUMBER: setType(OperandType.NUMBER); break;
            // в oracle с датой можно работать как со строкой
            case DATE: setType(OperandType.STRING); break;
            case REFERENCE: setType(OperandType.NUMBER); break;
            default: throw new RuntimeException("Unexpected internal alias type");
        }
    }

    /**
     * Переключения типа захвата типа
     * устанавливает работу с левым операндом
     */
    private void startCatchLeftType(){
        left = true;
        right = false;
    }

    /**
     * Установка типа, для текущего операнда
     * @param operandType
     */
    private void setType(OperandType operandType){
        if (operandType == OperandType.DATE){
            operandType = OperandType.STRING;
        }
        if (left){
            letfType = operandType;
        } else if (right){
            rightType = operandType;
        } else {
            throw new RuntimeException("Implicit kind of operand. (Left of right)");
        }
    }

    /**
     * Проверка типа переменной для функции
     *
     * @param ctx
     */
    private void checkFunctionType(FilterTreeParser.FuncwrapContext ctx){
        // текущие поддерживаемые функции LOWER и LENGTH, они работают со строковыми и date параметрами
        OperandType internalType = left ? letfType: rightType;

        // для LOWER и LENGTH мы должны передавать строку либо дату
        if (ctx.functype() != null) {
            if (ctx.functype().LOWER() != null || ctx.functype().LENGTH() != null){
                if (internalType != OperandType.DATE && internalType != OperandType.STRING){
                    throw new RuntimeException("Function require a string or date param");
                }
            } else if (ctx.functype().TO_CHAR() != null){
                // функция to_char
                if (internalType != OperandType.NUMBER){
                    throw new RuntimeException("Function require a number type as param");
                }
            }
        }
    }

    public void setHasLastExternalRefBookAttribute(HasLastExternalRefBookAttribute hasLastExternalRefBookAttribute) {
        this.hasLastExternalRefBookAttribute = hasLastExternalRefBookAttribute;
    }
}

