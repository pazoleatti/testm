package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QNdflPersonDeduction is a Querydsl query type for QNdflPersonDeduction
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QNdflPersonDeduction extends com.querydsl.sql.RelationalPathBase<QNdflPersonDeduction> {

    private static final long serialVersionUID = 1665395639;

    public static final QNdflPersonDeduction ndflPersonDeduction = new QNdflPersonDeduction("NDFL_PERSON_DEDUCTION");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final DateTimePath<org.joda.time.LocalDateTime> incomeAccrued = createDateTime("incomeAccrued", org.joda.time.LocalDateTime.class);

    public final StringPath incomeCode = createString("incomeCode");

    public final NumberPath<java.math.BigDecimal> incomeSumm = createNumber("incomeSumm", java.math.BigDecimal.class);

    public final NumberPath<Long> ndflPersonId = createNumber("ndflPersonId", Long.class);

    public final DateTimePath<org.joda.time.LocalDateTime> notifDate = createDateTime("notifDate", org.joda.time.LocalDateTime.class);

    public final StringPath notifNum = createString("notifNum");

    public final StringPath notifSource = createString("notifSource");

    public final NumberPath<java.math.BigDecimal> notifSumm = createNumber("notifSumm", java.math.BigDecimal.class);

    public final StringPath notifType = createString("notifType");

    public final StringPath operationId = createString("operationId");

    public final DateTimePath<org.joda.time.LocalDateTime> periodCurrDate = createDateTime("periodCurrDate", org.joda.time.LocalDateTime.class);

    public final NumberPath<java.math.BigDecimal> periodCurrSumm = createNumber("periodCurrSumm", java.math.BigDecimal.class);

    public final DateTimePath<org.joda.time.LocalDateTime> periodPrevDate = createDateTime("periodPrevDate", org.joda.time.LocalDateTime.class);

    public final NumberPath<java.math.BigDecimal> periodPrevSumm = createNumber("periodPrevSumm", java.math.BigDecimal.class);

    public final NumberPath<java.math.BigDecimal> rowNum = createNumber("rowNum", java.math.BigDecimal.class);

    public final NumberPath<Long> sourceId = createNumber("sourceId", Long.class);

    public final StringPath typeCode = createString("typeCode");

    public final com.querydsl.sql.PrimaryKey<QNdflPersonDeduction> ndflPdPk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QNdflPerson> ndflPdFkNp = createForeignKey(ndflPersonId, "ID");

    public final com.querydsl.sql.ForeignKey<QNdflPersonDeduction> ndflPdFkS = createForeignKey(sourceId, "ID");

    public final com.querydsl.sql.ForeignKey<QNdflPersonDeduction> _ndflPdFkS = createInvForeignKey(id, "SOURCE_ID");

    public QNdflPersonDeduction(String variable) {
        super(QNdflPersonDeduction.class, forVariable(variable), "NDFL_UNSTABLE", "NDFL_PERSON_DEDUCTION");
        addMetadata();
    }

    public QNdflPersonDeduction(String variable, String schema, String table) {
        super(QNdflPersonDeduction.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QNdflPersonDeduction(Path<? extends QNdflPersonDeduction> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "NDFL_PERSON_DEDUCTION");
        addMetadata();
    }

    public QNdflPersonDeduction(PathMetadata metadata) {
        super(QNdflPersonDeduction.class, metadata, "NDFL_UNSTABLE", "NDFL_PERSON_DEDUCTION");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(incomeAccrued, ColumnMetadata.named("INCOME_ACCRUED").withIndex(12).ofType(Types.TIMESTAMP).withSize(7));
        addMetadata(incomeCode, ColumnMetadata.named("INCOME_CODE").withIndex(13).ofType(Types.VARCHAR).withSize(4));
        addMetadata(incomeSumm, ColumnMetadata.named("INCOME_SUMM").withIndex(14).ofType(Types.DECIMAL).withSize(22).withDigits(2));
        addMetadata(ndflPersonId, ColumnMetadata.named("NDFL_PERSON_ID").withIndex(2).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(notifDate, ColumnMetadata.named("NOTIF_DATE").withIndex(8).ofType(Types.TIMESTAMP).withSize(7));
        addMetadata(notifNum, ColumnMetadata.named("NOTIF_NUM").withIndex(9).ofType(Types.VARCHAR).withSize(20));
        addMetadata(notifSource, ColumnMetadata.named("NOTIF_SOURCE").withIndex(10).ofType(Types.VARCHAR).withSize(20));
        addMetadata(notifSumm, ColumnMetadata.named("NOTIF_SUMM").withIndex(11).ofType(Types.DECIMAL).withSize(22).withDigits(2));
        addMetadata(notifType, ColumnMetadata.named("NOTIF_TYPE").withIndex(7).ofType(Types.VARCHAR).withSize(2));
        addMetadata(operationId, ColumnMetadata.named("OPERATION_ID").withIndex(5).ofType(Types.VARCHAR).withSize(100));
        addMetadata(periodCurrDate, ColumnMetadata.named("PERIOD_CURR_DATE").withIndex(17).ofType(Types.TIMESTAMP).withSize(7));
        addMetadata(periodCurrSumm, ColumnMetadata.named("PERIOD_CURR_SUMM").withIndex(18).ofType(Types.DECIMAL).withSize(22).withDigits(2));
        addMetadata(periodPrevDate, ColumnMetadata.named("PERIOD_PREV_DATE").withIndex(15).ofType(Types.TIMESTAMP).withSize(7));
        addMetadata(periodPrevSumm, ColumnMetadata.named("PERIOD_PREV_SUMM").withIndex(16).ofType(Types.DECIMAL).withSize(22).withDigits(2));
        addMetadata(rowNum, ColumnMetadata.named("ROW_NUM").withIndex(4).ofType(Types.DECIMAL).withSize(20));
        addMetadata(sourceId, ColumnMetadata.named("SOURCE_ID").withIndex(3).ofType(Types.DECIMAL).withSize(18));
        addMetadata(typeCode, ColumnMetadata.named("TYPE_CODE").withIndex(6).ofType(Types.VARCHAR).withSize(3));
    }

}

