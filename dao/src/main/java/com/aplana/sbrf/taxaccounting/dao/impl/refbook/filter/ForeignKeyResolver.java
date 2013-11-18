package com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter;

import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.model.PreparedStatementData;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Класс помошник фильтра, берет на себя обязанности
 * разименовывания справочников если в фильтре присутствуют внешние ключи
 * к примеру user.city.name, в этом случае он добавляет цепочку join'ов
 * и условие.
 *
 * @author auldanov
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Qualifier("foreignKeyResolver")
public class ForeignKeyResolver {

    public PreparedStatementData getPs() {
        return ps;
    }

    public void setPs(PreparedStatementData ps) {
        this.ps = ps;
    }

    public RefBook getRefBook() {
        return refBook;
    }

    public void setRefBook(RefBook refBook) {
        this.refBook = refBook;
    }

    class LinkModel{
        public Long recordId;
        public String alias;

        public LinkModel(Long recordId, String alias){
            this.alias = alias;
            this.recordId = recordId;
        }
    }

    /**
     * Карта внешних зависимостей которые вставляются в нужном порядке
     * Параметры карты:
     *  - attribute id
     *  - record id
     */
    private List<LinkModel> foreignTables = new ArrayList();

    /**
     * Значение содержит значение поля,
     * которая ссылается на другой справочник.
     */
    private RefBookAttribute lastRefBookAttribute;

    /**
     * Корневой справочник
     */
    private RefBook refBook;

    /*
    * Модель содержащая данные для PreparedStatement
    */
    PreparedStatementData ps;

    @Autowired
    private RefBookDao refBookDao;

    /**
     * Вход в узел содержащий поля внешних справочников eAlias
     */
    public void enterEAliasNode(String alias){
        // Устанавливаем текущий атрибут как последний
        lastRefBookAttribute = refBook.getAttribute(alias);
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
    public void exitEAliasNode(){
        ps.appendQuery("frb"+(foreignTables.size() - 1));
        ps.appendQuery(".");
        ps.appendQuery(lastRefBookAttribute.getAttributeType().toString());
        ps.appendQuery("_value");
    }

    /**
     * Вход в узел содержащий алиас поля внешней таблицы
     */
    public void enterExternalAliasNode(String alias){
        // Получение текущего атрибута
        RefBook currentRefBook = refBookDao.get(lastRefBookAttribute.getRefBookId());
        RefBookAttribute currentAttribute = currentRefBook.getAttribute(alias); // getId

        // добавляем связанный справочник
        addForeignTable(currentAttribute.getId(), lastRefBookAttribute.getAlias());

        // текущий атрибут как предыдущие данные
        lastRefBookAttribute = currentAttribute;
    }

    /**
     * Метод устанавливает в ps части sql запроса join для всех встретившихся
     * внешних справочников
     */
    public void setSqlPartsOfJoin(){
        StringBuffer buffer = new StringBuffer();
        // Генерация всех join'ов со списка
        for(int i=0; i < foreignTables.size(); i++){
            // алиас лдя таблицы frb - foreign ref book
            String currentAlias = "frb"+i;
            // TODO завязка на r.id, r алиас главной таблицы
            String prevLinkCell = i == 0 ? "a"+foreignTables.get(i).alias+".reference_value" : "frb"+i+"."+foreignTables.get(i).alias;
            // составления join запроса
            buffer.append("left join ref_book_value ").append(currentAlias).append(" on ");
            buffer.append(currentAlias).append(".record_id = ");
            buffer.append(prevLinkCell);
            buffer.append(" and ").append(currentAlias).append(".attribute_id = ").append(foreignTables.get(i).recordId);
            buffer.append("\n");
        }

        ps.setJoinPartsOfQuery(buffer.toString());
    }

    /**
     * Функция добавления внешней таблицы в карту.
     * Отсутствие повторов гарантирует LinkedHashMap
     */
    private void addForeignTable(Long id, String alias){
        foreignTables.add(new LinkModel(id, alias));
    }
}
