package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QRaschsvUplSvPrev is a Querydsl query type for QRaschsvUplSvPrev
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QRaschsvUplSvPrev extends com.querydsl.sql.RelationalPathBase<QRaschsvUplSvPrev> {

    private static final long serialVersionUID = 1422261400;

    public static final QRaschsvUplSvPrev raschsvUplSvPrev = new QRaschsvUplSvPrev("RASCHSV_UPL_SV_PREV");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath nodeName = createString("nodeName");

    public final StringPath priznak = createString("priznak");

    public final NumberPath<Long> raschsvOssVnmId = createNumber("raschsvOssVnmId", Long.class);

    public final NumberPath<java.math.BigDecimal> svSum = createNumber("svSum", java.math.BigDecimal.class);

    public final com.querydsl.sql.PrimaryKey<QRaschsvUplSvPrev> raschsvOssVnmUplPk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QRaschsvOssVnm> raschsvUplSvPrevOssFk = createForeignKey(raschsvOssVnmId, "ID");

    public QRaschsvUplSvPrev(String variable) {
        super(QRaschsvUplSvPrev.class, forVariable(variable), "NDFL_1_0", "RASCHSV_UPL_SV_PREV");
        addMetadata();
    }

    public QRaschsvUplSvPrev(String variable, String schema, String table) {
        super(QRaschsvUplSvPrev.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QRaschsvUplSvPrev(Path<? extends QRaschsvUplSvPrev> path) {
        super(path.getType(), path.getMetadata(), "NDFL_1_0", "RASCHSV_UPL_SV_PREV");
        addMetadata();
    }

    public QRaschsvUplSvPrev(PathMetadata metadata) {
        super(QRaschsvUplSvPrev.class, metadata, "NDFL_1_0", "RASCHSV_UPL_SV_PREV");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(nodeName, ColumnMetadata.named("NODE_NAME").withIndex(3).ofType(Types.VARCHAR).withSize(20).notNull());
        addMetadata(priznak, ColumnMetadata.named("PRIZNAK").withIndex(4).ofType(Types.VARCHAR).withSize(1));
        addMetadata(raschsvOssVnmId, ColumnMetadata.named("RASCHSV_OSS_VNM_ID").withIndex(2).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(svSum, ColumnMetadata.named("SV_SUM").withIndex(5).ofType(Types.DECIMAL).withSize(19).withDigits(2));
    }

}

