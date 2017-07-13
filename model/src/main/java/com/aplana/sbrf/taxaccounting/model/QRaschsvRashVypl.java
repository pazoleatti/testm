package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QRaschsvRashVypl is a Querydsl query type for QRaschsvRashVypl
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QRaschsvRashVypl extends com.querydsl.sql.RelationalPathBase<QRaschsvRashVypl> {

    private static final long serialVersionUID = 843309042;

    public static final QRaschsvRashVypl raschsvRashVypl = new QRaschsvRashVypl("RASCHSV_RASH_VYPL");

    public final NumberPath<Integer> chislPoluch = createNumber("chislPoluch", Integer.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Integer> kolVypl = createNumber("kolVypl", Integer.class);

    public final StringPath nodeName = createString("nodeName");

    public final NumberPath<Long> raschsvVyplPrichinaId = createNumber("raschsvVyplPrichinaId", Long.class);

    public final NumberPath<java.math.BigDecimal> rashod = createNumber("rashod", java.math.BigDecimal.class);

    public final com.querydsl.sql.PrimaryKey<QRaschsvRashVypl> raschsvRashVyplPk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QRaschsvVyplPrichina> raschsvRashVyplPrichinaFk = createForeignKey(raschsvVyplPrichinaId, "ID");

    public QRaschsvRashVypl(String variable) {
        super(QRaschsvRashVypl.class, forVariable(variable), "NDFL_UNSTABLE", "RASCHSV_RASH_VYPL");
        addMetadata();
    }

    public QRaschsvRashVypl(String variable, String schema, String table) {
        super(QRaschsvRashVypl.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QRaschsvRashVypl(Path<? extends QRaschsvRashVypl> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "RASCHSV_RASH_VYPL");
        addMetadata();
    }

    public QRaschsvRashVypl(PathMetadata metadata) {
        super(QRaschsvRashVypl.class, metadata, "NDFL_UNSTABLE", "RASCHSV_RASH_VYPL");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(chislPoluch, ColumnMetadata.named("CHISL_POLUCH").withIndex(4).ofType(Types.DECIMAL).withSize(7));
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(kolVypl, ColumnMetadata.named("KOL_VYPL").withIndex(5).ofType(Types.DECIMAL).withSize(7));
        addMetadata(nodeName, ColumnMetadata.named("NODE_NAME").withIndex(3).ofType(Types.VARCHAR).withSize(20).notNull());
        addMetadata(raschsvVyplPrichinaId, ColumnMetadata.named("RASCHSV_VYPL_PRICHINA_ID").withIndex(2).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(rashod, ColumnMetadata.named("RASHOD").withIndex(6).ofType(Types.DECIMAL).withSize(19).withDigits(2));
    }

}

