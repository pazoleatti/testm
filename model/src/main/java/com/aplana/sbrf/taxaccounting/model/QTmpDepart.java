package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QTmpDepart is a Querydsl query type for QTmpDepart
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QTmpDepart extends com.querydsl.sql.RelationalPathBase<QTmpDepart> {

    private static final long serialVersionUID = 779062126;

    public static final QTmpDepart tmpDepart = new QTmpDepart("TMP_DEPART");

    public final StringPath code = createString("code");

    public final NumberPath<java.math.BigInteger> depId = createNumber("depId", java.math.BigInteger.class);

    public final StringPath name = createString("name");

    public QTmpDepart(String variable) {
        super(QTmpDepart.class, forVariable(variable), "NDFL_1_0", "TMP_DEPART");
        addMetadata();
    }

    public QTmpDepart(String variable, String schema, String table) {
        super(QTmpDepart.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QTmpDepart(Path<? extends QTmpDepart> path) {
        super(path.getType(), path.getMetadata(), "NDFL_1_0", "TMP_DEPART");
        addMetadata();
    }

    public QTmpDepart(PathMetadata metadata) {
        super(QTmpDepart.class, metadata, "NDFL_1_0", "TMP_DEPART");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(code, ColumnMetadata.named("CODE").withIndex(1).ofType(Types.VARCHAR).withSize(4));
        addMetadata(depId, ColumnMetadata.named("DEP_ID").withIndex(3).ofType(Types.DECIMAL).withSize(22));
        addMetadata(name, ColumnMetadata.named("NAME").withIndex(2).ofType(Types.VARCHAR).withSize(100));
    }

}

