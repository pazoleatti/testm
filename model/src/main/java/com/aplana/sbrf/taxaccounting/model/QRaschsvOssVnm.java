package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QRaschsvOssVnm is a Querydsl query type for QRaschsvOssVnm
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QRaschsvOssVnm extends com.querydsl.sql.RelationalPathBase<QRaschsvOssVnm> {

    private static final long serialVersionUID = 901428213;

    public static final QRaschsvOssVnm raschsvOssVnm = new QRaschsvOssVnm("RASCHSV_OSS_VNM");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath prizVypl = createString("prizVypl");

    public final NumberPath<Long> raschsvObyazPlatSvId = createNumber("raschsvObyazPlatSvId", Long.class);

    public final com.querydsl.sql.PrimaryKey<QRaschsvOssVnm> raschsvOssVnmPk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QRaschsvObyazPlatSv> raschsvOssVnmObPlatSvFk = createForeignKey(raschsvObyazPlatSvId, "ID");

    public final com.querydsl.sql.ForeignKey<QRaschsvOssVnmKol> _raschsvOssVnmKolOssFk = createInvForeignKey(id, "RASCHSV_OSS_VNM_ID");

    public final com.querydsl.sql.ForeignKey<QRaschsvOssVnmSum> _raschsvOssVnmSumOssFk = createInvForeignKey(id, "RASCHSV_OSS_VNM_ID");

    public final com.querydsl.sql.ForeignKey<QRaschsvUplSvPrev> _raschsvUplSvPrevOssFk = createInvForeignKey(id, "RASCHSV_OSS_VNM_ID");

    public QRaschsvOssVnm(String variable) {
        super(QRaschsvOssVnm.class, forVariable(variable), "NDFL_UNSTABLE", "RASCHSV_OSS_VNM");
        addMetadata();
    }

    public QRaschsvOssVnm(String variable, String schema, String table) {
        super(QRaschsvOssVnm.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QRaschsvOssVnm(Path<? extends QRaschsvOssVnm> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "RASCHSV_OSS_VNM");
        addMetadata();
    }

    public QRaschsvOssVnm(PathMetadata metadata) {
        super(QRaschsvOssVnm.class, metadata, "NDFL_UNSTABLE", "RASCHSV_OSS_VNM");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(prizVypl, ColumnMetadata.named("PRIZ_VYPL").withIndex(3).ofType(Types.VARCHAR).withSize(1));
        addMetadata(raschsvObyazPlatSvId, ColumnMetadata.named("RASCHSV_OBYAZ_PLAT_SV_ID").withIndex(2).ofType(Types.DECIMAL).withSize(18).notNull());
    }

}

