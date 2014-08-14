package com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter.components;

import com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter.FilterTreeParser;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.util.StringUtils;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
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
 * К примеру если в фильтре присутствуют внешний ключ user.city.name,
 * в этом случае он добавляет цепочку join'ов и условие.
 *
 * @author auldanov
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Qualifier("foreignKeyResolver")
public class ForeignKeyResolverComponent extends AbstractTreeListenerComponent implements TypeVerifierComponent.HasLastExternalRefBookAttribute{

    /**
     * Префикс который используется для составления алиасов для
     * join'а таблиц
     */
    private static final String FOREIGN_TABLE_PREFIX = "frb";

    class LinkModel{
        public Long recordId;
        public String alias;

        public LinkModel(Long recordId, String alias){
            this.alias = alias;
            this.recordId = recordId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof LinkModel)) return false;

            LinkModel linkModel = (LinkModel) o;

            if (alias != null ? !alias.equals(linkModel.alias) : linkModel.alias != null) return false;
            if (recordId != null ? !recordId.equals(linkModel.recordId) : linkModel.recordId != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = recordId != null ? recordId.hashCode() : 0;
            result = 31 * result + (alias != null ? alias.hashCode() : 0);
            return result;
        }
    }

    /**
     * Карта внешних зависимостей которые вставляются в нужном порядке
     */
    private List<LinkModel> foreignTables = new ArrayList();

    /** счетчик количества join'ов */
    private int ftCounter;

    /**
     * Значение содержит значение поля,
     * которая ссылается на другой справочник.
     */
    private RefBookAttribute lastRefBookAttribute;

    @Autowired
    private RefBookDao refBookDao;

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

        if (factory.getDataProvider(refBook.getId()) instanceof RefBookSimpla

        ps.appendQuery(buildAliasForUniversalRefBook());

        StringBuilder builder = new StringBuilder();
        // Генерация всех join'ов со списка
        for(int i=0; i < foreignTables.size(); i++){
            /**
             * порядковый номер внешней таблицы (берется с глобального счетчика всех join'ов в пределах работы с полным sql выражением),
             * используется для составляения уникальных алиасов "frb"+index при join'ах таблиц
             */
            int serialNumber = ftCounter - foreignTables.size() + i;

            // добавляем очередное join выражение в билдер строк
            builder.append(buildJoinQueryForUniversalRefBook(serialNumber, i));
        }
        foreignTables.clear();
        joinStatement.add(builder.toString());
    }

    /**
     * Метод возвращает строку с join запросом для универсального справочника
     *
     * @param serialNumber порядковый номер внешней таблицы (берется с глобального счетчика всех join'ов в пределах работы с полным sql выражением),
     *                     используется для составляения уникальных алиасов "frb"+index при join'ах таблиц
     * @param index порядковый номер в пределах текущего разименовывания (одной цепочки алиасов)
     * @return
     */
    private StringBuilder buildJoinQueryForUniversalRefBook(int serialNumber, int index){
        // алиас для join'а таблицы
        String tableAlias = buildAliasForForeignTable(serialNumber);

        // Модель данных для текущего Join'а
        LinkModel linkModel = foreignTables.get(index);

        // алиас аттрибута справочника который является ссылкой
        String linkAttributeAlias = linkModel.alias;

        // алиас аттрибута справочника который является ссылкой с учетом алиаса таблицы
        String linkAttributeAliasWithTableAlias = index == 0 ?
                "a" + linkAttributeAlias + ".reference_value" :
                tableAlias + "." + linkAttributeAlias;

        // составления join запроса
        return new StringBuilder()
            .append("left join ref_book_value ")
            .append(tableAlias)
            .append(" on ")
            .append(tableAlias)
            .append(".record_id = ")
            .append(linkAttributeAliasWithTableAlias)
            .append(" and ")
            .append(tableAlias)
            .append(".attribute_id = ")
            .append(linkModel.recordId)
            .append("\n");
    }

    private String buildAliasForUniversalRefBook(){
        return new StringBuilder()
            .append(buildAliasForForeignTable(ftCounter - 1))
            .append(".")
            .append(lastRefBookAttribute.getAttributeType().toString())
            .append("_value")
            .toString();
    }

    /**
     * Метод возвращает строку с join запросом для простого справочника
     *
     * @param serialNumber порядковый номер внешней таблицы (берется с глобального счетчика всех join'ов в пределах работы с полным sql выражением),
     *                     используется для составляения уникальных алиасов "frb"+index при join'ах таблиц
     * @param index порядковый номер в пределах текущего разименовывания (одной цепочки алиасов)
     * @return
     */
    private StringBuilder buildJoinQueryForSimpleRefBook(int serialNumber, int index){
        // алиас для join'а таблицы
        String tableAlias = buildAliasForForeignTable(serialNumber);

        // Модель данных для текущего Join'а
        LinkModel linkModel = foreignTables.get(index);

        // алиас аттрибута справочника который является ссылкой
        String linkAttributeAlias = linkModel.alias;

        // алиас аттрибута справочника который является ссылкой с учетом алиаса таблицы
        String linkAttributeAliasWithTableAlias = index == 0 ?
                "a" + linkAttributeAlias + ".reference_value" :
                tableAlias + "." + linkAttributeAlias;

        // составления join запроса
        return new StringBuilder()
                .append("left join название таблицы ")
                .append(tableAlias)
                .append(" on ")
                .append(tableAlias)
                .append(".id = ")
                .append(linkAttributeAliasWithTableAlias)
                .append("\n");
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
        addForeignTable(currentAttribute.getId(), lastRefBookAttribute.getAlias());

        // текущий атрибут как предыдущие данные
        lastRefBookAttribute = currentAttribute;
    }

    /**
     * Метод устанавливает в ps части sql запроса join для всех встретившихся
     * внешних справочников
     */
    @Override
    public void exitQuery(FilterTreeParser.QueryContext ctx) {
        if (joinStatement.size() > 0) {
            ps.setJoinPartsOfQuery(StringUtils.join(joinStatement.toArray(), '\n'));
        } else {
            ps.setJoinPartsOfQuery(new String());
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
    private void addForeignTable(Long id, String alias){
        foreignTables.add(new LinkModel(id, alias));
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
