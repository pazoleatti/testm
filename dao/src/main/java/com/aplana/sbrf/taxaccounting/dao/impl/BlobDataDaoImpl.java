package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.BlobDataDao;
import com.aplana.sbrf.taxaccounting.model.BlobData;
import com.aplana.sbrf.taxaccounting.model.querydsl.*;
import com.querydsl.core.types.QBean;
import com.querydsl.sql.SQLExpressions;
import com.querydsl.sql.SQLQueryFactory;
import org.joda.time.LocalDateTime;
import org.springframework.stereotype.Repository;

import java.io.InputStream;
import java.util.Date;
import java.util.List;

import static com.aplana.sbrf.taxaccounting.model.querydsl.QBlobData.blobData;
import static com.querydsl.core.types.Projections.bean;
import static com.querydsl.sql.oracle.OracleGrammar.sysdate;

/**
 * Реализация доступа к базе данных для {@link BlobData}.
 */
@Repository
public class BlobDataDaoImpl implements BlobDataDao {

    final private SQLQueryFactory sqlQueryFactory;

    public BlobDataDaoImpl(SQLQueryFactory sqlQueryFactory) {
        this.sqlQueryFactory = sqlQueryFactory;
    }

    final private QBean<BlobData> blobDataBean = bean(BlobData.class,
            blobData.id.as("uuid"),
            blobData.name,
            blobData.data.as("inputStream"),
            blobData.creationDate);

    @Override
    public String createWithSysdate(final BlobData newBlobData) {
        sqlQueryFactory.insert(blobData)
                .columns(blobData.id, blobData.name, blobData.data, blobData.creationDate)
                .values(newBlobData.getUuid(), newBlobData.getName(), newBlobData.getInputStream(), sysdate)
                .execute();
        return newBlobData.getUuid();
    }

    @Override
    public String create(final BlobData newBlobData) {
        sqlQueryFactory.insert(blobData)
                .columns(blobData.id, blobData.name, blobData.data, blobData.creationDate)
                .values(newBlobData.getUuid(), newBlobData.getName(), newBlobData.getInputStream(), newBlobData.getCreationDate())
                .execute();
        return newBlobData.getUuid();
    }

    @Override
    public void delete(String uuid) {
        sqlQueryFactory.delete(blobData)
                .where(blobData.id.eq(uuid))
                .execute();
    }

    @Override
    public void delete(List<String> uuidStrings) {
        sqlQueryFactory.delete(blobData)
                .where(blobData.id.in(uuidStrings))
                .execute();
    }

    @Override
    public void updateDataByUUID(final String uuid, final InputStream inputStream) {
        sqlQueryFactory.update(blobData)
                .where(blobData.id.eq(uuid))
                .set(blobData.data, inputStream)
                .execute();
    }

    @Override
    public BlobData fetch(String uuid) {
        return sqlQueryFactory.select(blobDataBean)
                .from(blobData)
                .where(blobData.id.eq(uuid))
                .fetchOne();
    }

    @Override
    public long fetchLength(String uuid) {
        return sqlQueryFactory.select(SQLExpressions.relationalFunctionCall(Long.class, "dbms_lob.getlength", blobData.data))
                .from(blobData)
                .where(blobData.id.eq(uuid))
                .fetchOne();
    }

    @Override
    public long clean() {
        List<String> blobIDS = sqlQueryFactory.select().unionAll(
                SQLExpressions.select(QRefBook.refBook.scriptId)
                        .from(QRefBook.refBook)
                        .where(QRefBook.refBook.scriptId.isNotNull()),
                SQLExpressions.select(QDeclarationTemplate.declarationTemplate.xsd)
                        .from(QDeclarationTemplate.declarationTemplate)
                        .where(QDeclarationTemplate.declarationTemplate.xsd.isNotNull()),
                SQLExpressions.select(QDeclarationTemplate.declarationTemplate.jrxml)
                        .from(QDeclarationTemplate.declarationTemplate)
                        .where(QDeclarationTemplate.declarationTemplate.jrxml.isNotNull()),
                SQLExpressions.select(QDeclarationReport.declarationReport.blobDataId)
                        .from(QDeclarationReport.declarationReport)
                        .where(QDeclarationReport.declarationReport.blobDataId.isNotNull()),
                SQLExpressions.select(QDeclarationDataFile.declarationDataFile.blobDataId)
                        .from(QDeclarationDataFile.declarationDataFile)
                        .where(QDeclarationDataFile.declarationDataFile.blobDataId.isNotNull()),
                SQLExpressions.select(QDeclarationTemplateFile.declarationTemplateFile.blobDataId)
                        .from(QDeclarationTemplateFile.declarationTemplateFile)
                        .where(QDeclarationTemplateFile.declarationTemplateFile.blobDataId.isNotNull()),
                SQLExpressions.select(QDeclarationSubreport.declarationSubreport.blobDataId)
                        .from(QDeclarationSubreport.declarationSubreport)
                        .where(QDeclarationSubreport.declarationSubreport.blobDataId.isNotNull())
        ).fetch();

        Date oneDayBack = sqlQueryFactory.select(SQLExpressions.addDays(sysdate, -1)).fetchOne();

        return sqlQueryFactory.delete(blobData)
                .where(blobData.id.notIn(blobIDS).and(blobData.creationDate.lt(new LocalDateTime(oneDayBack))))
                .execute();
    }
}