package com.aplana.sbrf.taxaccounting.dao.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookAsnuDao;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAsnu;
import com.querydsl.core.types.QBean;
import com.querydsl.sql.SQLQueryFactory;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.aplana.sbrf.taxaccounting.model.QRefBookAsnu.refBookAsnu;
import static com.querydsl.core.types.Projections.bean;

/**
 * Created by aokunev on 09.08.2017.
 */
@Repository
public class RefBookAsnuDaoImpl implements RefBookAsnuDao {
    final private SQLQueryFactory sqlQueryFactory;

    public RefBookAsnuDaoImpl(SQLQueryFactory sqlQueryFactory) {
        this.sqlQueryFactory = sqlQueryFactory;
    }

    final private QBean<RefBookAsnu> refBookAsnuBean = bean(RefBookAsnu.class, refBookAsnu.all());

    @Override
    public List<RefBookAsnu> fetchAll() {
        return sqlQueryFactory
                .select(refBookAsnuBean)
                .from(refBookAsnu)
                .where(refBookAsnu.id.gt(0))
                .fetch();
    }
}
