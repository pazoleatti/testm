package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QReportPeriod is a Querydsl query type for QReportPeriod
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QReportPeriod extends com.querydsl.sql.RelationalPathBase<QReportPeriod> {

    private static final long serialVersionUID = -203970126;

    public static final QReportPeriod reportPeriod = new QReportPeriod("REPORT_PERIOD");

    public final DateTimePath<org.joda.time.LocalDateTime> calendarStartDate = createDateTime("calendarStartDate", org.joda.time.LocalDateTime.class);

    public final NumberPath<Long> dictTaxPeriodId = createNumber("dictTaxPeriodId", Long.class);

    public final DateTimePath<org.joda.time.LocalDateTime> endDate = createDateTime("endDate", org.joda.time.LocalDateTime.class);

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath name = createString("name");

    public final DateTimePath<org.joda.time.LocalDateTime> startDate = createDateTime("startDate", org.joda.time.LocalDateTime.class);

    public final NumberPath<Integer> taxPeriodId = createNumber("taxPeriodId", Integer.class);

    public final com.querydsl.sql.PrimaryKey<QReportPeriod> reportPeriodPk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QReportPeriodType> reportPeriodFkDtpId = createForeignKey(dictTaxPeriodId, "ID");

    public final com.querydsl.sql.ForeignKey<QTaxPeriod> reportPeriodFkTaxperiod = createForeignKey(taxPeriodId, "ID");

    public final com.querydsl.sql.ForeignKey<QDepartmentReportPeriod> _depRepPerFkRepPeriodId = createInvForeignKey(id, "REPORT_PERIOD_ID");

    public final com.querydsl.sql.ForeignKey<QNotification> _notificationFkReportPeriod = createInvForeignKey(id, "REPORT_PERIOD_ID");

    public QReportPeriod(String variable) {
        super(QReportPeriod.class, forVariable(variable), "NDFL_UNSTABLE", "REPORT_PERIOD");
        addMetadata();
    }

    public QReportPeriod(String variable, String schema, String table) {
        super(QReportPeriod.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QReportPeriod(Path<? extends QReportPeriod> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "REPORT_PERIOD");
        addMetadata();
    }

    public QReportPeriod(PathMetadata metadata) {
        super(QReportPeriod.class, metadata, "NDFL_UNSTABLE", "REPORT_PERIOD");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(calendarStartDate, ColumnMetadata.named("CALENDAR_START_DATE").withIndex(7).ofType(Types.TIMESTAMP).withSize(7).notNull());
        addMetadata(dictTaxPeriodId, ColumnMetadata.named("DICT_TAX_PERIOD_ID").withIndex(4).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(endDate, ColumnMetadata.named("END_DATE").withIndex(6).ofType(Types.TIMESTAMP).withSize(7).notNull());
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(9).notNull());
        addMetadata(name, ColumnMetadata.named("NAME").withIndex(2).ofType(Types.VARCHAR).withSize(510).notNull());
        addMetadata(startDate, ColumnMetadata.named("START_DATE").withIndex(5).ofType(Types.TIMESTAMP).withSize(7).notNull());
        addMetadata(taxPeriodId, ColumnMetadata.named("TAX_PERIOD_ID").withIndex(3).ofType(Types.DECIMAL).withSize(9).notNull());
    }

}

