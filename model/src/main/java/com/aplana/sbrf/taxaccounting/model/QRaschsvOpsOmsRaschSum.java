package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QRaschsvOpsOmsRaschSum is a Querydsl query type for QRaschsvOpsOmsRaschSum
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QRaschsvOpsOmsRaschSum extends com.querydsl.sql.RelationalPathBase<QRaschsvOpsOmsRaschSum> {

    private static final long serialVersionUID = 1570432820;

    public static final QRaschsvOpsOmsRaschSum raschsvOpsOmsRaschSum = new QRaschsvOpsOmsRaschSum("RASCHSV_OPS_OMS_RASCH_SUM");

    public final StringPath nodeName = createString("nodeName");

    public final NumberPath<Long> raschsvOpsOmsRaschSumId = createNumber("raschsvOpsOmsRaschSumId", Long.class);

    public final NumberPath<Long> raschsvSvSum1TipId = createNumber("raschsvSvSum1TipId", Long.class);

    public final com.querydsl.sql.PrimaryKey<QRaschsvOpsOmsRaschSum> raschsvOpsOmsRaschSumPk = createPrimaryKey(raschsvOpsOmsRaschSumId, raschsvSvSum1TipId);

    public final com.querydsl.sql.ForeignKey<QRaschsvSvSum1tip> raschsvOpsOmsRSumTipFk = createForeignKey(raschsvSvSum1TipId, "ID");

    public final com.querydsl.sql.ForeignKey<QRaschsvSvOpsOmsRasch> raschsvSvOpsOmsSumFk = createForeignKey(raschsvOpsOmsRaschSumId, "ID");

    public QRaschsvOpsOmsRaschSum(String variable) {
        super(QRaschsvOpsOmsRaschSum.class, forVariable(variable), "NDFL_UNSTABLE", "RASCHSV_OPS_OMS_RASCH_SUM");
        addMetadata();
    }

    public QRaschsvOpsOmsRaschSum(String variable, String schema, String table) {
        super(QRaschsvOpsOmsRaschSum.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QRaschsvOpsOmsRaschSum(Path<? extends QRaschsvOpsOmsRaschSum> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "RASCHSV_OPS_OMS_RASCH_SUM");
        addMetadata();
    }

    public QRaschsvOpsOmsRaschSum(PathMetadata metadata) {
        super(QRaschsvOpsOmsRaschSum.class, metadata, "NDFL_UNSTABLE", "RASCHSV_OPS_OMS_RASCH_SUM");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(nodeName, ColumnMetadata.named("NODE_NAME").withIndex(3).ofType(Types.VARCHAR).withSize(20).notNull());
        addMetadata(raschsvOpsOmsRaschSumId, ColumnMetadata.named("RASCHSV_OPS_OMS_RASCH_SUM_ID").withIndex(1).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(raschsvSvSum1TipId, ColumnMetadata.named("RASCHSV_SV_SUM1_TIP_ID").withIndex(2).ofType(Types.DECIMAL).withSize(18).notNull());
    }

}

