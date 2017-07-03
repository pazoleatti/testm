package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QRaschsvUplPrevOss is a Querydsl query type for QRaschsvUplPrevOss
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QRaschsvUplPrevOss extends com.querydsl.sql.RelationalPathBase<QRaschsvUplPrevOss> {

    private static final long serialVersionUID = -1617105222;

    public static final QRaschsvUplPrevOss raschsvUplPrevOss = new QRaschsvUplPrevOss("RASCHSV_UPL_PREV_OSS");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath kbk = createString("kbk");

    public final NumberPath<java.math.BigDecimal> prevRashSv1m = createNumber("prevRashSv1m", java.math.BigDecimal.class);

    public final NumberPath<java.math.BigDecimal> prevRashSv2m = createNumber("prevRashSv2m", java.math.BigDecimal.class);

    public final NumberPath<java.math.BigDecimal> prevRashSv3m = createNumber("prevRashSv3m", java.math.BigDecimal.class);

    public final NumberPath<java.math.BigDecimal> prevRashSvPer = createNumber("prevRashSvPer", java.math.BigDecimal.class);

    public final NumberPath<Long> raschsvObyazPlatSvId = createNumber("raschsvObyazPlatSvId", Long.class);

    public final NumberPath<java.math.BigDecimal> sumSbUpl1m = createNumber("sumSbUpl1m", java.math.BigDecimal.class);

    public final NumberPath<java.math.BigDecimal> sumSbUpl2m = createNumber("sumSbUpl2m", java.math.BigDecimal.class);

    public final NumberPath<java.math.BigDecimal> sumSbUpl3m = createNumber("sumSbUpl3m", java.math.BigDecimal.class);

    public final NumberPath<java.math.BigDecimal> sumSbUplPer = createNumber("sumSbUplPer", java.math.BigDecimal.class);

    public final com.querydsl.sql.PrimaryKey<QRaschsvUplPrevOss> raschsvUplPrevOssPk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QRaschsvObyazPlatSv> raschsvUplPrevObPlatFk = createForeignKey(raschsvObyazPlatSvId, "ID");

    public QRaschsvUplPrevOss(String variable) {
        super(QRaschsvUplPrevOss.class, forVariable(variable), "NDFL_1_0", "RASCHSV_UPL_PREV_OSS");
        addMetadata();
    }

    public QRaschsvUplPrevOss(String variable, String schema, String table) {
        super(QRaschsvUplPrevOss.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QRaschsvUplPrevOss(Path<? extends QRaschsvUplPrevOss> path) {
        super(path.getType(), path.getMetadata(), "NDFL_1_0", "RASCHSV_UPL_PREV_OSS");
        addMetadata();
    }

    public QRaschsvUplPrevOss(PathMetadata metadata) {
        super(QRaschsvUplPrevOss.class, metadata, "NDFL_1_0", "RASCHSV_UPL_PREV_OSS");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(kbk, ColumnMetadata.named("KBK").withIndex(3).ofType(Types.VARCHAR).withSize(20));
        addMetadata(prevRashSv1m, ColumnMetadata.named("PREV_RASH_SV_1M").withIndex(9).ofType(Types.DECIMAL).withSize(19).withDigits(2));
        addMetadata(prevRashSv2m, ColumnMetadata.named("PREV_RASH_SV_2M").withIndex(10).ofType(Types.DECIMAL).withSize(19).withDigits(2));
        addMetadata(prevRashSv3m, ColumnMetadata.named("PREV_RASH_SV_3M").withIndex(11).ofType(Types.DECIMAL).withSize(19).withDigits(2));
        addMetadata(prevRashSvPer, ColumnMetadata.named("PREV_RASH_SV_PER").withIndex(8).ofType(Types.DECIMAL).withSize(19).withDigits(2));
        addMetadata(raschsvObyazPlatSvId, ColumnMetadata.named("RASCHSV_OBYAZ_PLAT_SV_ID").withIndex(2).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(sumSbUpl1m, ColumnMetadata.named("SUM_SB_UPL_1M").withIndex(5).ofType(Types.DECIMAL).withSize(19).withDigits(2));
        addMetadata(sumSbUpl2m, ColumnMetadata.named("SUM_SB_UPL_2M").withIndex(6).ofType(Types.DECIMAL).withSize(19).withDigits(2));
        addMetadata(sumSbUpl3m, ColumnMetadata.named("SUM_SB_UPL_3M").withIndex(7).ofType(Types.DECIMAL).withSize(19).withDigits(2));
        addMetadata(sumSbUplPer, ColumnMetadata.named("SUM_SB_UPL_PER").withIndex(4).ofType(Types.DECIMAL).withSize(19).withDigits(2));
    }

}

