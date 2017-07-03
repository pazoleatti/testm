package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QDr$numberSequence is a Querydsl query type for QDr$numberSequence
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QDr$numberSequence extends com.querydsl.sql.RelationalPathBase<QDr$numberSequence> {

    private static final long serialVersionUID = 362718595;

    public static final QDr$numberSequence dr$numberSequence = new QDr$numberSequence("DR$NUMBER_SEQUENCE");

    public final NumberPath<java.math.BigInteger> num = createNumber("num", java.math.BigInteger.class);

    public QDr$numberSequence(String variable) {
        super(QDr$numberSequence.class, forVariable(variable), "CTXSYS", "DR$NUMBER_SEQUENCE");
        addMetadata();
    }

    public QDr$numberSequence(String variable, String schema, String table) {
        super(QDr$numberSequence.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QDr$numberSequence(Path<? extends QDr$numberSequence> path) {
        super(path.getType(), path.getMetadata(), "CTXSYS", "DR$NUMBER_SEQUENCE");
        addMetadata();
    }

    public QDr$numberSequence(PathMetadata metadata) {
        super(QDr$numberSequence.class, metadata, "CTXSYS", "DR$NUMBER_SEQUENCE");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(num, ColumnMetadata.named("NUM").withIndex(1).ofType(Types.DECIMAL).withSize(22));
    }

}

