package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QRaschsvUplPer is a Querydsl query type for QRaschsvUplPer
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QRaschsvUplPer extends com.querydsl.sql.RelationalPathBase<QRaschsvUplPer> {

    private static final long serialVersionUID = 1070217979;

    public static final QRaschsvUplPer raschsvUplPer = new QRaschsvUplPer("RASCHSV_UPL_PER");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath kbk = createString("kbk");

    public final StringPath nodeName = createString("nodeName");

    public final NumberPath<Long> raschsvObyazPlatSvId = createNumber("raschsvObyazPlatSvId", Long.class);

    public final NumberPath<java.math.BigDecimal> sumSbUpl1m = createNumber("sumSbUpl1m", java.math.BigDecimal.class);

    public final NumberPath<java.math.BigDecimal> sumSbUpl2m = createNumber("sumSbUpl2m", java.math.BigDecimal.class);

    public final NumberPath<java.math.BigDecimal> sumSbUpl3m = createNumber("sumSbUpl3m", java.math.BigDecimal.class);

    public final NumberPath<java.math.BigDecimal> sumSbUplPer = createNumber("sumSbUplPer", java.math.BigDecimal.class);

    public final com.querydsl.sql.PrimaryKey<QRaschsvUplPer> raschsvUplPerPk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QRaschsvObyazPlatSv> raschsvUplPerObPlatSvFk = createForeignKey(raschsvObyazPlatSvId, "ID");

    public QRaschsvUplPer(String variable) {
        super(QRaschsvUplPer.class, forVariable(variable), "NDFL_1_0", "RASCHSV_UPL_PER");
        addMetadata();
    }

    public QRaschsvUplPer(String variable, String schema, String table) {
        super(QRaschsvUplPer.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QRaschsvUplPer(Path<? extends QRaschsvUplPer> path) {
        super(path.getType(), path.getMetadata(), "NDFL_1_0", "RASCHSV_UPL_PER");
        addMetadata();
    }

    public QRaschsvUplPer(PathMetadata metadata) {
        super(QRaschsvUplPer.class, metadata, "NDFL_1_0", "RASCHSV_UPL_PER");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(kbk, ColumnMetadata.named("KBK").withIndex(4).ofType(Types.VARCHAR).withSize(20));
        addMetadata(nodeName, ColumnMetadata.named("NODE_NAME").withIndex(3).ofType(Types.VARCHAR).withSize(20).notNull());
        addMetadata(raschsvObyazPlatSvId, ColumnMetadata.named("RASCHSV_OBYAZ_PLAT_SV_ID").withIndex(2).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(sumSbUpl1m, ColumnMetadata.named("SUM_SB_UPL_1M").withIndex(6).ofType(Types.DECIMAL).withSize(19).withDigits(2));
        addMetadata(sumSbUpl2m, ColumnMetadata.named("SUM_SB_UPL_2M").withIndex(7).ofType(Types.DECIMAL).withSize(19).withDigits(2));
        addMetadata(sumSbUpl3m, ColumnMetadata.named("SUM_SB_UPL_3M").withIndex(8).ofType(Types.DECIMAL).withSize(19).withDigits(2));
        addMetadata(sumSbUplPer, ColumnMetadata.named("SUM_SB_UPL_PER").withIndex(5).ofType(Types.DECIMAL).withSize(19).withDigits(2));
    }

}

