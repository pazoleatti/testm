package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QRaschsvSvOpsOms is a Querydsl query type for QRaschsvSvOpsOms
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QRaschsvSvOpsOms extends com.querydsl.sql.RelationalPathBase<QRaschsvSvOpsOms> {

    private static final long serialVersionUID = -1273577579;

    public static final QRaschsvSvOpsOms raschsvSvOpsOms = new QRaschsvSvOpsOms("RASCHSV_SV_OPS_OMS");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> raschsvObyazPlatSvId = createNumber("raschsvObyazPlatSvId", Long.class);

    public final StringPath tarifPlat = createString("tarifPlat");

    public final com.querydsl.sql.PrimaryKey<QRaschsvSvOpsOms> raschSvOpsOmsPk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QRaschsvObyazPlatSv> raschSvOpsOmsObPlatSvFk = createForeignKey(raschsvObyazPlatSvId, "ID");

    public final com.querydsl.sql.ForeignKey<QRaschsvSvOpsOmsRasch> _raschsvSvOpsOmsRaschFk = createInvForeignKey(id, "RASCHSV_SV_OPS_OMS_ID");

    public QRaschsvSvOpsOms(String variable) {
        super(QRaschsvSvOpsOms.class, forVariable(variable), "NDFL_UNSTABLE", "RASCHSV_SV_OPS_OMS");
        addMetadata();
    }

    public QRaschsvSvOpsOms(String variable, String schema, String table) {
        super(QRaschsvSvOpsOms.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QRaschsvSvOpsOms(Path<? extends QRaschsvSvOpsOms> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "RASCHSV_SV_OPS_OMS");
        addMetadata();
    }

    public QRaschsvSvOpsOms(PathMetadata metadata) {
        super(QRaschsvSvOpsOms.class, metadata, "NDFL_UNSTABLE", "RASCHSV_SV_OPS_OMS");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(raschsvObyazPlatSvId, ColumnMetadata.named("RASCHSV_OBYAZ_PLAT_SV_ID").withIndex(2).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(tarifPlat, ColumnMetadata.named("TARIF_PLAT").withIndex(3).ofType(Types.VARCHAR).withSize(2));
    }

}

