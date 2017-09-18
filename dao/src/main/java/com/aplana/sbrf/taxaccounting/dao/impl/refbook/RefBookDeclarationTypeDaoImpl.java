package com.aplana.sbrf.taxaccounting.dao.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDeclarationTypeDao;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookDeclarationType;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.QBean;
import com.querydsl.sql.SQLQueryFactory;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.aplana.sbrf.taxaccounting.model.QDeclarationType.declarationType;
import static com.querydsl.core.types.Projections.bean;

/**
 * Реализация дао для работы со справочником Виды форм
 */
@Repository
public class RefBookDeclarationTypeDaoImpl implements RefBookDeclarationTypeDao {
    final private SQLQueryFactory sqlQueryFactory;

    public RefBookDeclarationTypeDaoImpl(SQLQueryFactory sqlQueryFactory) {
        this.sqlQueryFactory = sqlQueryFactory;
    }

    final private QBean<RefBookDeclarationType> refBookDeclarationTypeBean = bean(RefBookDeclarationType.class, declarationType.id,
            declarationType.ifrsName, declarationType.isIfrs, declarationType.name, declarationType.status.as("versionStatusId"));

    /**
     * Получение всех значений справочника
     *
     * @return Список значений справочника
     */
    @Override
    public List<RefBookDeclarationType> fetchAll() {
        BooleanBuilder where = new BooleanBuilder();
        where.and(declarationType.status.eq((byte) 0));
        return sqlQueryFactory
                .select(refBookDeclarationTypeBean)
                .from(declarationType)
                .where(where)
                .fetch();
    }
}
