package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QNdflPersonPrepayment is a Querydsl query type for QNdflPersonPrepayment
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QNdflPersonPrepayment extends com.querydsl.sql.RelationalPathBase<QNdflPersonPrepayment> {

    private static final long serialVersionUID = 1550119153;

    public static final QNdflPersonPrepayment ndflPersonPrepayment = new QNdflPersonPrepayment("NDFL_PERSON_PREPAYMENT");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> ndflPersonId = createNumber("ndflPersonId", Long.class);

    public final DateTimePath<java.sql.Timestamp> notifDate = createDateTime("notifDate", java.sql.Timestamp.class);

    public final StringPath notifNum = createString("notifNum");

    public final StringPath notifSource = createString("notifSource");

    public final StringPath operationId = createString("operationId");

    public final NumberPath<java.math.BigDecimal> rowNum = createNumber("rowNum", java.math.BigDecimal.class);

    public final NumberPath<Long> sourceId = createNumber("sourceId", Long.class);

    public final NumberPath<java.math.BigDecimal> summ = createNumber("summ", java.math.BigDecimal.class);

    public final com.querydsl.sql.PrimaryKey<QNdflPersonPrepayment> ndflPpPk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QNdflPersonPrepayment> ndflPpFkS = createForeignKey(sourceId, "ID");

    public final com.querydsl.sql.ForeignKey<QNdflPerson> ndflPpFkNp = createForeignKey(ndflPersonId, "ID");

    public final com.querydsl.sql.ForeignKey<QNdflPersonPrepayment> _ndflPpFkS = createInvForeignKey(id, "SOURCE_ID");

    public QNdflPersonPrepayment(String variable) {
        super(QNdflPersonPrepayment.class, forVariable(variable), "NDFL_UNSTABLE", "NDFL_PERSON_PREPAYMENT");
        addMetadata();
    }

    public QNdflPersonPrepayment(String variable, String schema, String table) {
        super(QNdflPersonPrepayment.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QNdflPersonPrepayment(Path<? extends QNdflPersonPrepayment> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "NDFL_PERSON_PREPAYMENT");
        addMetadata();
    }

    public QNdflPersonPrepayment(PathMetadata metadata) {
        super(QNdflPersonPrepayment.class, metadata, "NDFL_UNSTABLE", "NDFL_PERSON_PREPAYMENT");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(ndflPersonId, ColumnMetadata.named("NDFL_PERSON_ID").withIndex(2).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(notifDate, ColumnMetadata.named("NOTIF_DATE").withIndex(8).ofType(Types.TIMESTAMP).withSize(7));
        addMetadata(notifNum, ColumnMetadata.named("NOTIF_NUM").withIndex(7).ofType(Types.VARCHAR).withSize(20));
        addMetadata(notifSource, ColumnMetadata.named("NOTIF_SOURCE").withIndex(9).ofType(Types.VARCHAR).withSize(20));
        addMetadata(operationId, ColumnMetadata.named("OPERATION_ID").withIndex(5).ofType(Types.VARCHAR).withSize(100));
        addMetadata(rowNum, ColumnMetadata.named("ROW_NUM").withIndex(4).ofType(Types.DECIMAL).withSize(20));
        addMetadata(sourceId, ColumnMetadata.named("SOURCE_ID").withIndex(3).ofType(Types.DECIMAL).withSize(18));
        addMetadata(summ, ColumnMetadata.named("SUMM").withIndex(6).ofType(Types.DECIMAL).withSize(20));
    }

}

