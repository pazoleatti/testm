package com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter.components.foreignkeyresolver;

import com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter.FilterTreeParser;
import com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter.components.AbstractTreeListenerComponent;
import com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter.components.TypeVerifierComponent;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.util.StringUtils;
import org.antlr.v4.runtime.misc.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Компонент ответственный за разименовывание параметров-ссылок
 * справочников.
 *
 * Принцип работы: К примеру если в фильтре присутствуют внешний ключ user.city.name,
 * в этом случае он добавляет цепочку join'ов и условие.
 *
 * Данный компонент оперирует тремя методами при обходе
 * синтаксического дерева (в примере для алиаса user.city.name):
 * 1. enterEAlias - выполняется для первого алиаса в цепочке,
 *                  т.е. для user, при этом в методе только сохраняется текущий атрибут в lastRefBookAttribute
 * 2. enterExternalAlias - срабатывает для каждого алиаса кроме первого т.е для city, name.
 *                      В методе инкрементируется глобальный счетчик ftCounter, а так же добавляются
 *                      новые данные в список joinPoints
 * 3. exitEAlias - выполняется после обработки всех алиасов.
 *
 * @author auldanov
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Qualifier("foreignKeyResolver")
public class ForeignKeyResolverComponent extends AbstractTreeListenerComponent implements TypeVerifierComponent.HasLastExternalRefBookAttribute {

    /**
     * Префикс который используется для составления алиасов для
     * join'а таблиц
     */
    private static final String FOREIGN_TABLE_PREFIX = "frb";

    /**
     * Список моделей содержащих информацию о точке соприкосновений справочников
     * содержит данные по одной цепочке связанных алиасов, и
     * очищается при каждой операции получения новой цепочки алиасов
     */
    private List<JoinPoint> joinPoints = new ArrayList();

    /** Счетчик количества join'ов */
    private int ftCounter;

    /**
     * Предыдущий атрибут справочника,
     * при каждой операции обработки нового алиаса он сохраняется
     * в текущую переменную
     */
    private RefBookAttribute lastRefBookAttribute;

    @Autowired
    private RefBookDao refBookDao;

    @Autowired
    @Qualifier("universal2UniversalJoinSqlPartBuilder")
    private JoinSqlPartBuilder u2UniversalJoinSqlPartBuilder;

    @Autowired
    @Qualifier("universal2SimpleJoinSqlPartBuilder")
    private JoinSqlPartBuilder u2SimpleJoinSqlPartBuilder;

    @Autowired
    @Qualifier("simple2UniversalJoinSqlPartBuilder")
    private JoinSqlPartBuilder simple2UniversalJoinSqlPartBuilder;

    @Autowired
    @Qualifier("simple2SimpleJoinSqlPartBuilder")
    private JoinSqlPartBuilder simple2SimpleJoinSqlPartBuilder;

    @Autowired
    @Qualifier("simpleRefBookAttributeAliasBuilder")
    private AttributeAliasBuilder simpleAttributeAliasBuilder;

    @Autowired
    @Qualifier("universalRefBookAttributeAliasBuilder")
    private AttributeAliasBuilder universalAttributeAliasBuilder;

    /**
     * Буфер куда складываются готовые составленные join запросы
     */
    private List<String> joinStatement = new ArrayList<String>();

    /**
     * Вход в узел содержащий поля внешних справочников eAlias
     */
    @Override
    public void enterEAlias(FilterTreeParser.EAliasContext ctx) {
        // Устанавливаем текущий атрибут как последний
        lastRefBookAttribute = refBook.getAttribute(ctx.ALIAS().getText());
    }

    /**
     * Выход из узла eAlias
     *
     * При выходе из узла прописывается условие связанное
     * с последним алиасом
     *
     * Пример: для условия фильтра - user.city.name = 'Уфа'
     * в sql выражение пропишется последний алиас name = 'Уфа'
     */
    @Override
    public void exitEAlias(@NotNull FilterTreeParser.EAliasContext ctx) {
        String foreignTableAlias = buildAliasForForeignTable(ftCounter - 1);
        ps.appendQuery(getAttributeAliasBuilder().buildAlias(foreignTableAlias, lastRefBookAttribute));

        StringBuilder builder = new StringBuilder();
        // Генерация всех join'ов со списка
        for(int i=0; i < joinPoints.size(); i++){
            /**
             * порядковый номер внешней таблицы (берется с глобального счетчика всех join'ов в пределах работы с полным sql выражением),
             * используется для составляения уникальных алиасов "frb"+index при join'ах таблиц
             */
            int serialNumber = ftCounter - joinPoints.size() + i;
            JoinPoint joinPoint = joinPoints.get(i);
            JoinSqlPartBuilder joinSqlPartBuilder = getJoinSqlPartBuilder(joinPoint);
            builder.append(joinSqlPartBuilder.build(buildAliasForForeignTable(serialNumber), joinPoint, i == 0));
        }
        joinPoints.clear();
        joinStatement.add(builder.toString());
    }

    /**
     * Фабрика компонентов по созданию алиасов
     */
    private AttributeAliasBuilder getAttributeAliasBuilder() {
        RefBook lastRefBook = refBookDao.getByAttribute(lastRefBookAttribute.getId());

        return lastRefBook.getTableName() != null ? simpleAttributeAliasBuilder : universalAttributeAliasBuilder;
    }

    /**
     * Фабрика компонентов по созданию join части sql выражения
     *
     * @param joinPoint
     * @return
     */
    private JoinSqlPartBuilder getJoinSqlPartBuilder(JoinPoint joinPoint) {
        RefBook destinationRefBook = refBookDao.getByAttribute(joinPoint.getDestinationAttribute().getId());
        RefBook referenceRefBook = refBookDao.getByAttribute(joinPoint.getReferenceAttribute().getId());

        if (!destinationRefBook.isSimple() && !referenceRefBook.isSimple()){
            return u2UniversalJoinSqlPartBuilder;
        } else if (destinationRefBook.isSimple() && !referenceRefBook.isSimple()){
            return u2SimpleJoinSqlPartBuilder;
        } else if (destinationRefBook.isSimple() && referenceRefBook.isSimple()){
            return simple2SimpleJoinSqlPartBuilder;
        } else if (!destinationRefBook.isSimple() && referenceRefBook.isSimple()){
            return simple2UniversalJoinSqlPartBuilder;
        } else {
            throw new RuntimeException("Не найден соответствующий компонент.");
        }
    }

    /**
     * Вход в узел содержащий алиас поля внешней таблицы
     */
    @Override
    public void enterExternalAlias(FilterTreeParser.ExternalAliasContext ctx) {
        // увеличим счетчик внешних таблиц
        ftCounter++;

        // Получение текущего атрибута
        RefBook currentRefBook = refBookDao.get(lastRefBookAttribute.getRefBookId());
        RefBookAttribute currentAttribute = currentRefBook.getAttribute(ctx.ALIAS().getText()); // getId

        // добавляем связанный справочник
        addForeignTable(lastRefBookAttribute, currentAttribute);

        // текущий атрибут как предыдущие данные
        lastRefBookAttribute = currentAttribute;
    }

    /**
     * Метод устанавливает в ps части sql запроса join для всех встретившихся
     * внешних справочников
     */
    @Override
    public void exitQuery(FilterTreeParser.QueryContext ctx) {
        if (!joinStatement.isEmpty()) {
            ps.setJoinPartsOfQuery(StringUtils.join(joinStatement.toArray(), '\n'));
        } else {
            ps.setJoinPartsOfQuery("");
        }
    }

    @Override
    public RefBookAttribute getLastExternalRefBookAttribute() {
        return lastRefBookAttribute;
    }

    /**
     * Функция добавления внешней таблицы в карту.
     * Отсутствие повторов гарантирует LinkedHashMap
     */
    private void addForeignTable(RefBookAttribute referenceAttribute, RefBookAttribute destinationAttribute){
        joinPoints.add(new JoinPoint(referenceAttribute, destinationAttribute));
    }

    /**
     * Получение алиаса для таблицы
     * @param serialNumber порядковый номер таблицы
     * @return
     */
    private String buildAliasForForeignTable(int serialNumber){
        return FOREIGN_TABLE_PREFIX + serialNumber;
    }
}
