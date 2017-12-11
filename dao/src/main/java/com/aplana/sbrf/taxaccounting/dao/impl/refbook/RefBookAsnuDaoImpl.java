package com.aplana.sbrf.taxaccounting.dao.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookAsnuDao;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAsnu;
import com.querydsl.core.types.QBean;
import com.querydsl.sql.SQLQueryFactory;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.aplana.sbrf.taxaccounting.model.querydsl.QRefBookAsnu.refBookAsnu;
import static com.querydsl.core.types.Projections.bean;

/**
 * Реализация дао для работы со справочником АСНУ
 */
@Repository
public class RefBookAsnuDaoImpl implements RefBookAsnuDao {
    final private SQLQueryFactory sqlQueryFactory;

    public RefBookAsnuDaoImpl(SQLQueryFactory sqlQueryFactory) {
        this.sqlQueryFactory = sqlQueryFactory;
    }

    final private QBean<RefBookAsnu> refBookAsnuBean = bean(RefBookAsnu.class, refBookAsnu.all());

    /**
     * Получение всех значений справочника
     *
     * @return Список значений справочника
     */
    @Override
    public List<RefBookAsnu> fetchAll() {
        //Выбирются записи с положительным значением id, т.к. есть фиктивные записи с id=-1
        return sqlQueryFactory
                .select(refBookAsnuBean)
                .from(refBookAsnu)
                .where(refBookAsnu.id.gt(0))
                .fetch();
    }

    /**
     * Получение значений справочника по идентификаторам
     *
     * @param ids Идентификаторы
     * @return Список значений справочника
     */
    @Override
    public List<RefBookAsnu> fetchByIds(List<Long> ids) {
        //Выбирются записи с положительным значением id, т.к. есть фиктивные записи с id=-1
        return sqlQueryFactory
                .select(refBookAsnuBean)
                .from(refBookAsnu)
                .where(refBookAsnu.id.gt(0).and(refBookAsnu.id.in(ids)))
                .fetch();
    }

    @Override
    public RefBookAsnu fetchById(Long id) {
        return sqlQueryFactory.select(refBookAsnuBean)
                .from(refBookAsnu)
                .where(refBookAsnu.id.eq(id))
                .fetchOne();
    }
}