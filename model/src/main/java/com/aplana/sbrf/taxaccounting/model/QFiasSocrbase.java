package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QFiasSocrbase is a Querydsl query type for QFiasSocrbase
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QFiasSocrbase extends com.querydsl.sql.RelationalPathBase<QFiasSocrbase> {

    private static final long serialVersionUID = 1882397198;

    public static final QFiasSocrbase fiasSocrbase = new QFiasSocrbase("FIAS_SOCRBASE");

    public final StringPath kodTSt = createString("kodTSt");

    public final NumberPath<java.math.BigInteger> lev = createNumber("lev", java.math.BigInteger.class);

    public final StringPath scname = createString("scname");

    public final StringPath socrname = createString("socrname");

    public QFiasSocrbase(String variable) {
        super(QFiasSocrbase.class, forVariable(variable), "NDFL_UNSTABLE", "FIAS_SOCRBASE");
        addMetadata();
    }

    public QFiasSocrbase(String variable, String schema, String table) {
        super(QFiasSocrbase.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QFiasSocrbase(Path<? extends QFiasSocrbase> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "FIAS_SOCRBASE");
        addMetadata();
    }

    public QFiasSocrbase(PathMetadata metadata) {
        super(QFiasSocrbase.class, metadata, "NDFL_UNSTABLE", "FIAS_SOCRBASE");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(kodTSt, ColumnMetadata.named("KOD_T_ST").withIndex(3).ofType(Types.VARCHAR).withSize(4).notNull());
        addMetadata(lev, ColumnMetadata.named("LEV").withIndex(4).ofType(Types.DECIMAL).withSize(22));
        addMetadata(scname, ColumnMetadata.named("SCNAME").withIndex(1).ofType(Types.VARCHAR).withSize(10));
        addMetadata(socrname, ColumnMetadata.named("SOCRNAME").withIndex(2).ofType(Types.VARCHAR).withSize(50).notNull());
    }

}

