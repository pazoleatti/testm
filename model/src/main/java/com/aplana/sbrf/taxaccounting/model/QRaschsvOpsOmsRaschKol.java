package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QRaschsvOpsOmsRaschKol is a Querydsl query type for QRaschsvOpsOmsRaschKol
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QRaschsvOpsOmsRaschKol extends com.querydsl.sql.RelationalPathBase<QRaschsvOpsOmsRaschKol> {

    private static final long serialVersionUID = 1570424945;

    public static final QRaschsvOpsOmsRaschKol raschsvOpsOmsRaschKol = new QRaschsvOpsOmsRaschKol("RASCHSV_OPS_OMS_RASCH_KOL");

    public final StringPath nodeName = createString("nodeName");

    public final NumberPath<Long> raschsvKolLicTipId = createNumber("raschsvKolLicTipId", Long.class);

    public final NumberPath<Long> raschsvOpsOmsRaschKolId = createNumber("raschsvOpsOmsRaschKolId", Long.class);

    public final com.querydsl.sql.PrimaryKey<QRaschsvOpsOmsRaschKol> raschsvOpsOmsRaschKolPk = createPrimaryKey(raschsvKolLicTipId, raschsvOpsOmsRaschKolId);

    public final com.querydsl.sql.ForeignKey<QRaschsvSvOpsOmsRasch> raschsvSvOpsOmsKolFk = createForeignKey(raschsvOpsOmsRaschKolId, "ID");

    public final com.querydsl.sql.ForeignKey<QRaschsvKolLicTip> raschsvSvPMKolTipFk = createForeignKey(raschsvKolLicTipId, "ID");

    public QRaschsvOpsOmsRaschKol(String variable) {
        super(QRaschsvOpsOmsRaschKol.class, forVariable(variable), "NDFL_UNSTABLE", "RASCHSV_OPS_OMS_RASCH_KOL");
        addMetadata();
    }

    public QRaschsvOpsOmsRaschKol(String variable, String schema, String table) {
        super(QRaschsvOpsOmsRaschKol.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QRaschsvOpsOmsRaschKol(Path<? extends QRaschsvOpsOmsRaschKol> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "RASCHSV_OPS_OMS_RASCH_KOL");
        addMetadata();
    }

    public QRaschsvOpsOmsRaschKol(PathMetadata metadata) {
        super(QRaschsvOpsOmsRaschKol.class, metadata, "NDFL_UNSTABLE", "RASCHSV_OPS_OMS_RASCH_KOL");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(nodeName, ColumnMetadata.named("NODE_NAME").withIndex(3).ofType(Types.VARCHAR).withSize(20).notNull());
        addMetadata(raschsvKolLicTipId, ColumnMetadata.named("RASCHSV_KOL_LIC_TIP_ID").withIndex(2).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(raschsvOpsOmsRaschKolId, ColumnMetadata.named("RASCHSV_OPS_OMS_RASCH_KOL_ID").withIndex(1).ofType(Types.DECIMAL).withSize(18).notNull());
    }

}

