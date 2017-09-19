package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QReportPeriodType is a Querydsl query type for QReportPeriodType
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QReportPeriodType extends com.querydsl.sql.RelationalPathBase<QReportPeriodType> {

    private static final long serialVersionUID = -2016443380;

    public static final QReportPeriodType reportPeriodType = new QReportPeriodType("REPORT_PERIOD_TYPE");

    public final DateTimePath<org.joda.time.LocalDateTime> calendarStartDate = createDateTime("calendarStartDate", org.joda.time.LocalDateTime.class);

    public final StringPath code = createString("code");

    public final DateTimePath<org.joda.time.LocalDateTime> endDate = createDateTime("endDate", org.joda.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath name = createString("name");

    public final DateTimePath<org.joda.time.LocalDateTime> startDate = createDateTime("startDate", org.joda.time.LocalDateTime.class);

    public final com.querydsl.sql.PrimaryKey<QReportPeriodType> reportPeriodTypePk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QReportPeriod> _reportPeriodFkDtpId = createInvForeignKey(id, "DICT_TAX_PERIOD_ID");

    public QReportPeriodType(String variable) {
        super(QReportPeriodType.class, forVariable(variable), "NDFL_UNSTABLE", "REPORT_PERIOD_TYPE");
        addMetadata();
    }

    public QReportPeriodType(String variable, String schema, String table) {
        super(QReportPeriodType.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QReportPeriodType(Path<? extends QReportPeriodType> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "REPORT_PERIOD_TYPE");
        addMetadata();
    }

    public QReportPeriodType(PathMetadata metadata) {
        super(QReportPeriodType.class, metadata, "NDFL_UNSTABLE", "REPORT_PERIOD_TYPE");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(calendarStartDate, ColumnMetadata.named("CALENDAR_START_DATE").withIndex(6).ofType(Types.TIMESTAMP).withSize(7));
        addMetadata(code, ColumnMetadata.named("CODE").withIndex(2).ofType(Types.VARCHAR).withSize(2).notNull());
        addMetadata(endDate, ColumnMetadata.named("END_DATE").withIndex(5).ofType(Types.TIMESTAMP).withSize(7));
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(name, ColumnMetadata.named("NAME").withIndex(3).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(startDate, ColumnMetadata.named("START_DATE").withIndex(4).ofType(Types.TIMESTAMP).withSize(7));
    }

}

