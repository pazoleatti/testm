package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QRaschsvSvPrimTarif13422 is a Querydsl query type for QRaschsvSvPrimTarif13422
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QRaschsvSvPrimTarif13422 extends com.querydsl.sql.RelationalPathBase<QRaschsvSvPrimTarif13422> {

    private static final long serialVersionUID = -619846552;

    public static final QRaschsvSvPrimTarif13422 raschsvSvPrimTarif13422 = new QRaschsvSvPrimTarif13422("RASCHSV_SV_PRIM_TARIF1_3_422");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> raschsvObyazPlatSvId = createNumber("raschsvObyazPlatSvId", Long.class);

    public final com.querydsl.sql.PrimaryKey<QRaschsvSvPrimTarif13422> raschsvPrimTarif13422Pk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QRaschsvObyazPlatSv> raschsvTarif13422ObPlFk = createForeignKey(raschsvObyazPlatSvId, "ID");

    public final com.querydsl.sql.ForeignKey<QRaschsvVyplatIt422> _raschsvVyplatTarif3422Fk = createInvForeignKey(id, "RASCHSV_SV_PRIM_TARIF1_422_ID");

    public final com.querydsl.sql.ForeignKey<QRaschsvSvedObuch> _raschsvSvedObTarif1422Fk = createInvForeignKey(id, "RASCHSV_SV_PRIM_TARIF1_422_ID");

    public QRaschsvSvPrimTarif13422(String variable) {
        super(QRaschsvSvPrimTarif13422.class, forVariable(variable), "NDFL_1_0", "RASCHSV_SV_PRIM_TARIF1_3_422");
        addMetadata();
    }

    public QRaschsvSvPrimTarif13422(String variable, String schema, String table) {
        super(QRaschsvSvPrimTarif13422.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QRaschsvSvPrimTarif13422(Path<? extends QRaschsvSvPrimTarif13422> path) {
        super(path.getType(), path.getMetadata(), "NDFL_1_0", "RASCHSV_SV_PRIM_TARIF1_3_422");
        addMetadata();
    }

    public QRaschsvSvPrimTarif13422(PathMetadata metadata) {
        super(QRaschsvSvPrimTarif13422.class, metadata, "NDFL_1_0", "RASCHSV_SV_PRIM_TARIF1_3_422");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(raschsvObyazPlatSvId, ColumnMetadata.named("RASCHSV_OBYAZ_PLAT_SV_ID").withIndex(2).ofType(Types.DECIMAL).withSize(18).notNull());
    }

}

