package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QRaschsvVyplPrichina is a Querydsl query type for QRaschsvVyplPrichina
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QRaschsvVyplPrichina extends com.querydsl.sql.RelationalPathBase<QRaschsvVyplPrichina> {

    private static final long serialVersionUID = 1193855422;

    public static final QRaschsvVyplPrichina raschsvVyplPrichina = new QRaschsvVyplPrichina("RASCHSV_VYPL_PRICHINA");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath nodeName = createString("nodeName");

    public final NumberPath<Long> raschsvVyplFinFbId = createNumber("raschsvVyplFinFbId", Long.class);

    public final NumberPath<java.math.BigDecimal> svVnfUhodInv = createNumber("svVnfUhodInv", java.math.BigDecimal.class);

    public final com.querydsl.sql.PrimaryKey<QRaschsvVyplPrichina> raschsvVyplPrichinaPk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QRaschsvVyplFinFb> raschsvVyplFinFbFk = createForeignKey(raschsvVyplFinFbId, "ID");

    public final com.querydsl.sql.ForeignKey<QRaschsvRashVypl> _raschsvRashVyplPrichinaFk = createInvForeignKey(id, "RASCHSV_VYPL_PRICHINA_ID");

    public QRaschsvVyplPrichina(String variable) {
        super(QRaschsvVyplPrichina.class, forVariable(variable), "NDFL_UNSTABLE", "RASCHSV_VYPL_PRICHINA");
        addMetadata();
    }

    public QRaschsvVyplPrichina(String variable, String schema, String table) {
        super(QRaschsvVyplPrichina.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QRaschsvVyplPrichina(Path<? extends QRaschsvVyplPrichina> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "RASCHSV_VYPL_PRICHINA");
        addMetadata();
    }

    public QRaschsvVyplPrichina(PathMetadata metadata) {
        super(QRaschsvVyplPrichina.class, metadata, "NDFL_UNSTABLE", "RASCHSV_VYPL_PRICHINA");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(nodeName, ColumnMetadata.named("NODE_NAME").withIndex(3).ofType(Types.VARCHAR).withSize(20).notNull());
        addMetadata(raschsvVyplFinFbId, ColumnMetadata.named("RASCHSV_VYPL_FIN_FB_ID").withIndex(2).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(svVnfUhodInv, ColumnMetadata.named("SV_VNF_UHOD_INV").withIndex(4).ofType(Types.DECIMAL).withSize(19).withDigits(2));
    }

}

