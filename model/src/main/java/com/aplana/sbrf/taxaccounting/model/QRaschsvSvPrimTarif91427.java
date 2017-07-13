package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QRaschsvSvPrimTarif91427 is a Querydsl query type for QRaschsvSvPrimTarif91427
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QRaschsvSvPrimTarif91427 extends com.querydsl.sql.RelationalPathBase<QRaschsvSvPrimTarif91427> {

    private static final long serialVersionUID = -612517961;

    public static final QRaschsvSvPrimTarif91427 raschsvSvPrimTarif91427 = new QRaschsvSvPrimTarif91427("RASCHSV_SV_PRIM_TARIF9_1_427");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> raschsvObyazPlatSvId = createNumber("raschsvObyazPlatSvId", Long.class);

    public final com.querydsl.sql.PrimaryKey<QRaschsvSvPrimTarif91427> raschsvPrimTarif91427Pk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QRaschsvObyazPlatSv> raschsvTarif9427Fk = createForeignKey(raschsvObyazPlatSvId, "ID");

    public final com.querydsl.sql.ForeignKey<QRaschsvVyplatIt427> _raschsvVyplatTarif9427Fk = createInvForeignKey(id, "RASCHSV_SV_PRIM_TARIF9_427_ID");

    public final com.querydsl.sql.ForeignKey<QRaschsvSvedPatent> _raschsvSvedPTarif9427Fk = createInvForeignKey(id, "RASCHSV_SV_PRIM_TARIF9_427_ID");

    public QRaschsvSvPrimTarif91427(String variable) {
        super(QRaschsvSvPrimTarif91427.class, forVariable(variable), "NDFL_UNSTABLE", "RASCHSV_SV_PRIM_TARIF9_1_427");
        addMetadata();
    }

    public QRaschsvSvPrimTarif91427(String variable, String schema, String table) {
        super(QRaschsvSvPrimTarif91427.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QRaschsvSvPrimTarif91427(Path<? extends QRaschsvSvPrimTarif91427> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "RASCHSV_SV_PRIM_TARIF9_1_427");
        addMetadata();
    }

    public QRaschsvSvPrimTarif91427(PathMetadata metadata) {
        super(QRaschsvSvPrimTarif91427.class, metadata, "NDFL_UNSTABLE", "RASCHSV_SV_PRIM_TARIF9_1_427");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(raschsvObyazPlatSvId, ColumnMetadata.named("RASCHSV_OBYAZ_PLAT_SV_ID").withIndex(2).ofType(Types.DECIMAL).withSize(18).notNull());
    }

}

