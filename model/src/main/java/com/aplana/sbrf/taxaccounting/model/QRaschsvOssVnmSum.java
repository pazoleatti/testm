package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QRaschsvOssVnmSum is a Querydsl query type for QRaschsvOssVnmSum
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QRaschsvOssVnmSum extends com.querydsl.sql.RelationalPathBase<QRaschsvOssVnmSum> {

    private static final long serialVersionUID = -1982524906;

    public static final QRaschsvOssVnmSum raschsvOssVnmSum = new QRaschsvOssVnmSum("RASCHSV_OSS_VNM_SUM");

    public final StringPath nodeName = createString("nodeName");

    public final NumberPath<Long> raschsvOssVnmId = createNumber("raschsvOssVnmId", Long.class);

    public final NumberPath<Long> raschsvSvSum1TipId = createNumber("raschsvSvSum1TipId", Long.class);

    public final com.querydsl.sql.PrimaryKey<QRaschsvOssVnmSum> raschsvOssVnmSumPk = createPrimaryKey(raschsvOssVnmId, raschsvSvSum1TipId);

    public final com.querydsl.sql.ForeignKey<QRaschsvSvSum1tip> raschsvOssVnmSumTipFk = createForeignKey(raschsvSvSum1TipId, "ID");

    public final com.querydsl.sql.ForeignKey<QRaschsvOssVnm> raschsvOssVnmSumOssFk = createForeignKey(raschsvOssVnmId, "ID");

    public QRaschsvOssVnmSum(String variable) {
        super(QRaschsvOssVnmSum.class, forVariable(variable), "NDFL_UNSTABLE", "RASCHSV_OSS_VNM_SUM");
        addMetadata();
    }

    public QRaschsvOssVnmSum(String variable, String schema, String table) {
        super(QRaschsvOssVnmSum.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QRaschsvOssVnmSum(Path<? extends QRaschsvOssVnmSum> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "RASCHSV_OSS_VNM_SUM");
        addMetadata();
    }

    public QRaschsvOssVnmSum(PathMetadata metadata) {
        super(QRaschsvOssVnmSum.class, metadata, "NDFL_UNSTABLE", "RASCHSV_OSS_VNM_SUM");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(nodeName, ColumnMetadata.named("NODE_NAME").withIndex(3).ofType(Types.VARCHAR).withSize(20).notNull());
        addMetadata(raschsvOssVnmId, ColumnMetadata.named("RASCHSV_OSS_VNM_ID").withIndex(1).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(raschsvSvSum1TipId, ColumnMetadata.named("RASCHSV_SV_SUM1_TIP_ID").withIndex(2).ofType(Types.DECIMAL).withSize(18).notNull());
    }

}

