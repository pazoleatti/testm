package com.aplana.sbrf.taxaccounting.dao.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookAttachFileTypeDao;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttachFileType;
import com.querydsl.core.types.QBean;
import com.querydsl.sql.SQLQueryFactory;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.aplana.sbrf.taxaccounting.model.querydsl.QRefBookAttachFileType.refBookAttachFileType;
import static com.querydsl.core.types.Projections.bean;

/**
 * Реализация дао для работы со справочником Категории прикрепленных файлов
 */
@Repository
public class RefBookAttachFileTypeDaoImpl implements RefBookAttachFileTypeDao {

    final private SQLQueryFactory sqlQueryFactory;

    public RefBookAttachFileTypeDaoImpl(SQLQueryFactory sqlQueryFactory) {
        this.sqlQueryFactory = sqlQueryFactory;
    }

    final private QBean<RefBookAttachFileType> refBookAttachFileTypeBean = bean(RefBookAttachFileType.class, refBookAttachFileType.all());

    /**
     * Получение всех значений справочника
     *
     * @return Список значений справочника
     */
    @Override
    public List<RefBookAttachFileType> fetchAll() {
        return sqlQueryFactory
                .select(refBookAttachFileTypeBean)
                .from(refBookAttachFileType)
                .where(refBookAttachFileType.code.gt(0))
                .fetch();
    }
}