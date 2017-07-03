package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QRaschsvRashOssZakRash is a Querydsl query type for QRaschsvRashOssZakRash
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QRaschsvRashOssZakRash extends com.querydsl.sql.RelationalPathBase<QRaschsvRashOssZakRash> {

    private static final long serialVersionUID = -961809236;

    public static final QRaschsvRashOssZakRash raschsvRashOssZakRash = new QRaschsvRashOssZakRash("RASCHSV_RASH_OSS_ZAK_RASH");

    public final NumberPath<Integer> chislSluch = createNumber("chislSluch", Integer.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Integer> kolVypl = createNumber("kolVypl", Integer.class);

    public final StringPath nodeName = createString("nodeName");

    public final NumberPath<Long> raschsvRashOssZakId = createNumber("raschsvRashOssZakId", Long.class);

    public final NumberPath<java.math.BigDecimal> rashFinFb = createNumber("rashFinFb", java.math.BigDecimal.class);

    public final NumberPath<java.math.BigDecimal> rashVsego = createNumber("rashVsego", java.math.BigDecimal.class);

    public final com.querydsl.sql.PrimaryKey<QRaschsvRashOssZakRash> raschsvRashOssZakRashPk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QRaschsvRashOssZak> raschsvRashOssZakRashFk = createForeignKey(raschsvRashOssZakId, "ID");

    public QRaschsvRashOssZakRash(String variable) {
        super(QRaschsvRashOssZakRash.class, forVariable(variable), "NDFL_1_0", "RASCHSV_RASH_OSS_ZAK_RASH");
        addMetadata();
    }

    public QRaschsvRashOssZakRash(String variable, String schema, String table) {
        super(QRaschsvRashOssZakRash.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QRaschsvRashOssZakRash(Path<? extends QRaschsvRashOssZakRash> path) {
        super(path.getType(), path.getMetadata(), "NDFL_1_0", "RASCHSV_RASH_OSS_ZAK_RASH");
        addMetadata();
    }

    public QRaschsvRashOssZakRash(PathMetadata metadata) {
        super(QRaschsvRashOssZakRash.class, metadata, "NDFL_1_0", "RASCHSV_RASH_OSS_ZAK_RASH");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(chislSluch, ColumnMetadata.named("CHISL_SLUCH").withIndex(4).ofType(Types.DECIMAL).withSize(7));
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(kolVypl, ColumnMetadata.named("KOL_VYPL").withIndex(5).ofType(Types.DECIMAL).withSize(7));
        addMetadata(nodeName, ColumnMetadata.named("NODE_NAME").withIndex(3).ofType(Types.VARCHAR).withSize(20).notNull());
        addMetadata(raschsvRashOssZakId, ColumnMetadata.named("RASCHSV_RASH_OSS_ZAK_ID").withIndex(2).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(rashFinFb, ColumnMetadata.named("RASH_FIN_FB").withIndex(7).ofType(Types.DECIMAL).withSize(19).withDigits(2));
        addMetadata(rashVsego, ColumnMetadata.named("RASH_VSEGO").withIndex(6).ofType(Types.DECIMAL).withSize(19).withDigits(2));
    }

}

