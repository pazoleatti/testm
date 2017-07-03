package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QRaschsvVyplFinFb is a Querydsl query type for QRaschsvVyplFinFb
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QRaschsvVyplFinFb extends com.querydsl.sql.RelationalPathBase<QRaschsvVyplFinFb> {

    private static final long serialVersionUID = -1952582087;

    public static final QRaschsvVyplFinFb raschsvVyplFinFb = new QRaschsvVyplFinFb("RASCHSV_VYPL_FIN_FB");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> raschsvObyazPlatSvId = createNumber("raschsvObyazPlatSvId", Long.class);

    public final com.querydsl.sql.PrimaryKey<QRaschsvVyplFinFb> raschsvVyplFinFbPk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QRaschsvObyazPlatSv> raschsvVyplFinFbObPlFk = createForeignKey(raschsvObyazPlatSvId, "ID");

    public final com.querydsl.sql.ForeignKey<QRaschsvVyplPrichina> _raschsvVyplFinFbFk = createInvForeignKey(id, "RASCHSV_VYPL_FIN_FB_ID");

    public QRaschsvVyplFinFb(String variable) {
        super(QRaschsvVyplFinFb.class, forVariable(variable), "NDFL_1_0", "RASCHSV_VYPL_FIN_FB");
        addMetadata();
    }

    public QRaschsvVyplFinFb(String variable, String schema, String table) {
        super(QRaschsvVyplFinFb.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QRaschsvVyplFinFb(Path<? extends QRaschsvVyplFinFb> path) {
        super(path.getType(), path.getMetadata(), "NDFL_1_0", "RASCHSV_VYPL_FIN_FB");
        addMetadata();
    }

    public QRaschsvVyplFinFb(PathMetadata metadata) {
        super(QRaschsvVyplFinFb.class, metadata, "NDFL_1_0", "RASCHSV_VYPL_FIN_FB");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(raschsvObyazPlatSvId, ColumnMetadata.named("RASCHSV_OBYAZ_PLAT_SV_ID").withIndex(2).ofType(Types.DECIMAL).withSize(18).notNull());
    }

}

