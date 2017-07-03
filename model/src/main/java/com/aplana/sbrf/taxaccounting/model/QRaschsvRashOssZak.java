package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QRaschsvRashOssZak is a Querydsl query type for QRaschsvRashOssZak
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QRaschsvRashOssZak extends com.querydsl.sql.RelationalPathBase<QRaschsvRashOssZak> {

    private static final long serialVersionUID = -1534699576;

    public static final QRaschsvRashOssZak raschsvRashOssZak = new QRaschsvRashOssZak("RASCHSV_RASH_OSS_ZAK");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> raschsvObyazPlatSvId = createNumber("raschsvObyazPlatSvId", Long.class);

    public final com.querydsl.sql.PrimaryKey<QRaschsvRashOssZak> raschsvRashOssZakPk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QRaschsvObyazPlatSv> raschsvOssZakObPlatSvFk = createForeignKey(raschsvObyazPlatSvId, "ID");

    public final com.querydsl.sql.ForeignKey<QRaschsvRashOssZakRash> _raschsvRashOssZakRashFk = createInvForeignKey(id, "RASCHSV_RASH_OSS_ZAK_ID");

    public QRaschsvRashOssZak(String variable) {
        super(QRaschsvRashOssZak.class, forVariable(variable), "NDFL_1_0", "RASCHSV_RASH_OSS_ZAK");
        addMetadata();
    }

    public QRaschsvRashOssZak(String variable, String schema, String table) {
        super(QRaschsvRashOssZak.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QRaschsvRashOssZak(Path<? extends QRaschsvRashOssZak> path) {
        super(path.getType(), path.getMetadata(), "NDFL_1_0", "RASCHSV_RASH_OSS_ZAK");
        addMetadata();
    }

    public QRaschsvRashOssZak(PathMetadata metadata) {
        super(QRaschsvRashOssZak.class, metadata, "NDFL_1_0", "RASCHSV_RASH_OSS_ZAK");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(raschsvObyazPlatSvId, ColumnMetadata.named("RASCHSV_OBYAZ_PLAT_SV_ID").withIndex(2).ofType(Types.DECIMAL).withSize(18).notNull());
    }

}

