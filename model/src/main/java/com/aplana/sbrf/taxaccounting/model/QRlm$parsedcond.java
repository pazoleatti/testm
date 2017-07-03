package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QRlm$parsedcond is a Querydsl query type for QRlm$parsedcond
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QRlm$parsedcond extends com.querydsl.sql.RelationalPathBase<QRlm$parsedcond> {

    private static final long serialVersionUID = -596556575;

    public static final QRlm$parsedcond rlm$parsedcond = new QRlm$parsedcond("RLM$PARSEDCOND");

    public final NumberPath<java.math.BigInteger> peseqpos = createNumber("peseqpos", java.math.BigInteger.class);

    public final StringPath tagname = createString("tagname");

    public final StringPath tagvalue = createString("tagvalue");

    public QRlm$parsedcond(String variable) {
        super(QRlm$parsedcond.class, forVariable(variable), "EXFSYS", "RLM$PARSEDCOND");
        addMetadata();
    }

    public QRlm$parsedcond(String variable, String schema, String table) {
        super(QRlm$parsedcond.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QRlm$parsedcond(Path<? extends QRlm$parsedcond> path) {
        super(path.getType(), path.getMetadata(), "EXFSYS", "RLM$PARSEDCOND");
        addMetadata();
    }

    public QRlm$parsedcond(PathMetadata metadata) {
        super(QRlm$parsedcond.class, metadata, "EXFSYS", "RLM$PARSEDCOND");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(peseqpos, ColumnMetadata.named("PESEQPOS").withIndex(2).ofType(Types.DECIMAL).withSize(22));
        addMetadata(tagname, ColumnMetadata.named("TAGNAME").withIndex(1).ofType(Types.VARCHAR).withSize(32));
        addMetadata(tagvalue, ColumnMetadata.named("TAGVALUE").withIndex(3).ofType(Types.VARCHAR).withSize(4000));
    }

}

