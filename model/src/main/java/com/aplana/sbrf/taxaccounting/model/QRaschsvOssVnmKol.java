package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QRaschsvOssVnmKol is a Querydsl query type for QRaschsvOssVnmKol
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QRaschsvOssVnmKol extends com.querydsl.sql.RelationalPathBase<QRaschsvOssVnmKol> {

    private static final long serialVersionUID = -1982532781;

    public static final QRaschsvOssVnmKol raschsvOssVnmKol = new QRaschsvOssVnmKol("RASCHSV_OSS_VNM_KOL");

    public final StringPath nodeName = createString("nodeName");

    public final NumberPath<Long> raschsvKolLicTipId = createNumber("raschsvKolLicTipId", Long.class);

    public final NumberPath<Long> raschsvOssVnmId = createNumber("raschsvOssVnmId", Long.class);

    public final com.querydsl.sql.PrimaryKey<QRaschsvOssVnmKol> raschsvOssVnmKolPk = createPrimaryKey(raschsvKolLicTipId, raschsvOssVnmId);

    public final com.querydsl.sql.ForeignKey<QRaschsvKolLicTip> raschsvOssVnmKolTipFk = createForeignKey(raschsvKolLicTipId, "ID");

    public final com.querydsl.sql.ForeignKey<QRaschsvOssVnm> raschsvOssVnmKolOssFk = createForeignKey(raschsvOssVnmId, "ID");

    public QRaschsvOssVnmKol(String variable) {
        super(QRaschsvOssVnmKol.class, forVariable(variable), "NDFL_UNSTABLE", "RASCHSV_OSS_VNM_KOL");
        addMetadata();
    }

    public QRaschsvOssVnmKol(String variable, String schema, String table) {
        super(QRaschsvOssVnmKol.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QRaschsvOssVnmKol(Path<? extends QRaschsvOssVnmKol> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "RASCHSV_OSS_VNM_KOL");
        addMetadata();
    }

    public QRaschsvOssVnmKol(PathMetadata metadata) {
        super(QRaschsvOssVnmKol.class, metadata, "NDFL_UNSTABLE", "RASCHSV_OSS_VNM_KOL");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(nodeName, ColumnMetadata.named("NODE_NAME").withIndex(3).ofType(Types.VARCHAR).withSize(20).notNull());
        addMetadata(raschsvKolLicTipId, ColumnMetadata.named("RASCHSV_KOL_LIC_TIP_ID").withIndex(2).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(raschsvOssVnmId, ColumnMetadata.named("RASCHSV_OSS_VNM_ID").withIndex(1).ofType(Types.DECIMAL).withSize(18).notNull());
    }

}

