package com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter;

import org.antlr.v4.runtime.misc.NotNull;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Класс - компаньен фильтра, отвечает за проверку типов
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
public class TypeVerifier {
    private boolean left;
    private boolean right;
    private OperandType letfType, rightType;

    /**
     * Сброс параметров, для повторного использования объекта
     */
    public void reset(){
        letfType = null;
        rightType = null;
        left = false;
        right = false;
    }

    /**
     * Переключения типа захвата типа
     * устанавливает работу с левым операндом
     */
    public void startCatchLeftType(){
        left = true;
        right = false;
    }

    /**
     * Метод устанавливает работу с правым операндом
     */
    public void startCatchRightType(){
        left = false;
        right = true;
    }

    /**
     * Установка типа, для текущего операнда
     * @param operandType
     */
    public void setType(OperandType operandType){
        if (left){
            letfType = operandType;
        } else if (right){
            rightType = operandType;
        } else {
            throw new RuntimeException("Implicit kind of operand. (Left of right)");
        }
    }

    /**
     * Проверка типа
     *
     * @param operandType
     * @param msg
     */
    public void checkType(OperandType operandType, String msg){
        if ((left && letfType != operandType) || (right && rightType != operandType)){
            throw new RuntimeException(msg);
        }
    }

    /**
     * Проверка типов левого и правого операнда
     * @param ctx
     */
    public void verifyTypes(@NotNull FilterTreeParser.StandartExprContext ctx){
        if (letfType != null && rightType != null){
            if (letfType != rightType){
                throw new RuntimeException("Not match types. (left: "+letfType+", right: "+rightType+") on expression <"+ctx.getText()+">");
            }
        } else {
            throw new RuntimeException("Not initiate one or both types.");
        }
    }
}

