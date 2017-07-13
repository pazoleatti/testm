package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QRaschsvVyplSvDopMt is a Querydsl query type for QRaschsvVyplSvDopMt
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QRaschsvVyplSvDopMt extends com.querydsl.sql.RelationalPathBase<QRaschsvVyplSvDopMt> {

    private static final long serialVersionUID = -543402821;

    public static final QRaschsvVyplSvDopMt raschsvVyplSvDopMt = new QRaschsvVyplSvDopMt("RASCHSV_VYPL_SV_DOP_MT");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath mesyac = createString("mesyac");

    public final NumberPath<java.math.BigDecimal> nachislSv = createNumber("nachislSv", java.math.BigDecimal.class);

    public final NumberPath<Long> raschsvVyplSvDopId = createNumber("raschsvVyplSvDopId", Long.class);

    public final StringPath tarif = createString("tarif");

    public final NumberPath<java.math.BigDecimal> vyplSv = createNumber("vyplSv", java.math.BigDecimal.class);

    public final com.querydsl.sql.PrimaryKey<QRaschsvVyplSvDopMt> raschvVyplSvDopMtPk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QRaschsvVyplSvDop> raschsvVsvDopMtVsvDopFk = createForeignKey(raschsvVyplSvDopId, "ID");

    public QRaschsvVyplSvDopMt(String variable) {
        super(QRaschsvVyplSvDopMt.class, forVariable(variable), "NDFL_UNSTABLE", "RASCHSV_VYPL_SV_DOP_MT");
        addMetadata();
    }

    public QRaschsvVyplSvDopMt(String variable, String schema, String table) {
        super(QRaschsvVyplSvDopMt.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QRaschsvVyplSvDopMt(Path<? extends QRaschsvVyplSvDopMt> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "RASCHSV_VYPL_SV_DOP_MT");
        addMetadata();
    }

    public QRaschsvVyplSvDopMt(PathMetadata metadata) {
        super(QRaschsvVyplSvDopMt.class, metadata, "NDFL_UNSTABLE", "RASCHSV_VYPL_SV_DOP_MT");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(mesyac, ColumnMetadata.named("MESYAC").withIndex(3).ofType(Types.VARCHAR).withSize(2));
        addMetadata(nachislSv, ColumnMetadata.named("NACHISL_SV").withIndex(6).ofType(Types.DECIMAL).withSize(19).withDigits(2));
        addMetadata(raschsvVyplSvDopId, ColumnMetadata.named("RASCHSV_VYPL_SV_DOP_ID").withIndex(2).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(tarif, ColumnMetadata.named("TARIF").withIndex(4).ofType(Types.VARCHAR).withSize(2));
        addMetadata(vyplSv, ColumnMetadata.named("VYPL_SV").withIndex(5).ofType(Types.DECIMAL).withSize(19).withDigits(2));
    }

}

