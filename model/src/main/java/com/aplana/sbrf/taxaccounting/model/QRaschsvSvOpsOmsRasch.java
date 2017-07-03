package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QRaschsvSvOpsOmsRasch is a Querydsl query type for QRaschsvSvOpsOmsRasch
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QRaschsvSvOpsOmsRasch extends com.querydsl.sql.RelationalPathBase<QRaschsvSvOpsOmsRasch> {

    private static final long serialVersionUID = 1513885940;

    public static final QRaschsvSvOpsOmsRasch raschsvSvOpsOmsRasch = new QRaschsvSvOpsOmsRasch("RASCHSV_SV_OPS_OMS_RASCH");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath klasUslTrud = createString("klasUslTrud");

    public final StringPath kodOsnov = createString("kodOsnov");

    public final StringPath nodeName = createString("nodeName");

    public final StringPath osnovZap = createString("osnovZap");

    public final StringPath prOsnSvDop = createString("prOsnSvDop");

    public final StringPath prRaschSum = createString("prRaschSum");

    public final NumberPath<Long> raschsvSvOpsOmsId = createNumber("raschsvSvOpsOmsId", Long.class);

    public final com.querydsl.sql.PrimaryKey<QRaschsvSvOpsOmsRasch> raschsvSvOpsOmsRaschPk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QRaschsvSvOpsOms> raschsvSvOpsOmsRaschFk = createForeignKey(raschsvSvOpsOmsId, "ID");

    public final com.querydsl.sql.ForeignKey<QRaschsvOpsOmsRaschKol> _raschsvSvOpsOmsKolFk = createInvForeignKey(id, "RASCHSV_OPS_OMS_RASCH_KOL_ID");

    public final com.querydsl.sql.ForeignKey<QRaschsvOpsOmsRaschSum> _raschsvSvOpsOmsSumFk = createInvForeignKey(id, "RASCHSV_OPS_OMS_RASCH_SUM_ID");

    public QRaschsvSvOpsOmsRasch(String variable) {
        super(QRaschsvSvOpsOmsRasch.class, forVariable(variable), "NDFL_1_0", "RASCHSV_SV_OPS_OMS_RASCH");
        addMetadata();
    }

    public QRaschsvSvOpsOmsRasch(String variable, String schema, String table) {
        super(QRaschsvSvOpsOmsRasch.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QRaschsvSvOpsOmsRasch(Path<? extends QRaschsvSvOpsOmsRasch> path) {
        super(path.getType(), path.getMetadata(), "NDFL_1_0", "RASCHSV_SV_OPS_OMS_RASCH");
        addMetadata();
    }

    public QRaschsvSvOpsOmsRasch(PathMetadata metadata) {
        super(QRaschsvSvOpsOmsRasch.class, metadata, "NDFL_1_0", "RASCHSV_SV_OPS_OMS_RASCH");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(klasUslTrud, ColumnMetadata.named("KLAS_USL_TRUD").withIndex(7).ofType(Types.VARCHAR).withSize(1));
        addMetadata(kodOsnov, ColumnMetadata.named("KOD_OSNOV").withIndex(5).ofType(Types.VARCHAR).withSize(1));
        addMetadata(nodeName, ColumnMetadata.named("NODE_NAME").withIndex(3).ofType(Types.VARCHAR).withSize(20).notNull());
        addMetadata(osnovZap, ColumnMetadata.named("OSNOV_ZAP").withIndex(6).ofType(Types.VARCHAR).withSize(1));
        addMetadata(prOsnSvDop, ColumnMetadata.named("PR_OSN_SV_DOP").withIndex(4).ofType(Types.VARCHAR).withSize(1));
        addMetadata(prRaschSum, ColumnMetadata.named("PR_RASCH_SUM").withIndex(8).ofType(Types.VARCHAR).withSize(1));
        addMetadata(raschsvSvOpsOmsId, ColumnMetadata.named("RASCHSV_SV_OPS_OMS_ID").withIndex(2).ofType(Types.DECIMAL).withSize(18).notNull());
    }

}

