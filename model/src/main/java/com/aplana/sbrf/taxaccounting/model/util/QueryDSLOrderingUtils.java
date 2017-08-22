package com.aplana.sbrf.taxaccounting.model.util;

import com.google.common.collect.ImmutableMap;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.QBean;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.lang.reflect.Field;

/**
 * Утилитарный класс для выявления порядка сортировки QueryDSL
 */

public final class QueryDSLOrderingUtils {

    private QueryDSLOrderingUtils() {
    }

    /**
     * Получение выражения/пути в бд для QueryDSL связанного с именем атрибута в классе
     *
     * @param bean     объект QBean для класса типа Data Transfer Object
     * @param property имя поля в классе типа Data Transfer Object
     * @return выражение/путь в бд для QueryDSL
     * Если по имени атрибута не будет найден путь - вернётся null
     */

    @SuppressWarnings("unchecked")
    public static Expression<?> getOrderExpressionByProperty(QBean<?> bean, String property) {

        Field bindingField = FieldUtils.getField(bean.getClass(), "bindings", true);
        ImmutableMap<String, Expression<?>> bindingMap;
        try {
            bindingMap = (ImmutableMap<String, Expression<?>>) bindingField.get(bean);
        } catch (IllegalAccessException e) {
            return null;
        }

        return bindingMap.get(property);
    }

    /**
     * Получение описания порядка сортировки по имени свойства/атрибута связанного с ним
     *
     * @param bean     объект QBean для класса типа Data Transfer Object
     * @param property имя поля в классе типа Data Transfer Object
     * @param order    прямой или обратный порядок
     * @return описание порядка сортировки
     * Если по имени атрибута не будет найден путь - вернётся null
     */

    @SuppressWarnings("unchecked")
    public static OrderSpecifier getOrderSpecifierByPropertyAndOrder(QBean<?> bean, String property, Order order) {
        Expression<?> expression = getOrderExpressionByProperty(bean, property);

        return expression != null ? new OrderSpecifier(order, expression) : null;
    }

    /**
     * Получение описания порядка сортировки по имени свойства/атрибута связанного с ним
     *
     * @param bean     объект QBean для класса типа Data Transfer Object
     * @param property имя поля в классе типа Data Transfer Object
     * @param order    прямой или обратный порядок
     * @param alter    альтернативное описание порядка сортировки.
     *                 Будет возвращено, если по имени атрибута не будет найден путь
     * @return описание порядка сортировки
     */

    public static OrderSpecifier getOrderSpecifierByPropertyAndOrder(QBean<?> bean, String property, Order order, OrderSpecifier alter) {
        OrderSpecifier orderSpecifier = getOrderSpecifierByPropertyAndOrder(bean, property, order);

        return orderSpecifier != null ? orderSpecifier : alter;
    }

}