package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QSdoCsContextInformation is a Querydsl query type for QSdoCsContextInformation
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QSdoCsContextInformation extends com.querydsl.sql.RelationalPathBase<QSdoCsContextInformation> {

    private static final long serialVersionUID = 1507977262;

    public static final QSdoCsContextInformation sdoCsContextInformation = new QSdoCsContextInformation("SDO_CS_CONTEXT_INFORMATION");

    public final SimplePath<byte[]> context = createSimple("context", byte[].class);

    public final NumberPath<java.math.BigInteger> fromSrid = createNumber("fromSrid", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> toSrid = createNumber("toSrid", java.math.BigInteger.class);

    public QSdoCsContextInformation(String variable) {
        super(QSdoCsContextInformation.class, forVariable(variable), "MDSYS", "SDO_CS_CONTEXT_INFORMATION");
        addMetadata();
    }

    public QSdoCsContextInformation(String variable, String schema, String table) {
        super(QSdoCsContextInformation.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QSdoCsContextInformation(Path<? extends QSdoCsContextInformation> path) {
        super(path.getType(), path.getMetadata(), "MDSYS", "SDO_CS_CONTEXT_INFORMATION");
        addMetadata();
    }

    public QSdoCsContextInformation(PathMetadata metadata) {
        super(QSdoCsContextInformation.class, metadata, "MDSYS", "SDO_CS_CONTEXT_INFORMATION");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(context, ColumnMetadata.named("CONTEXT").withIndex(3).ofType(Types.VARBINARY).withSize(4));
        addMetadata(fromSrid, ColumnMetadata.named("FROM_SRID").withIndex(1).ofType(Types.DECIMAL).withSize(22));
        addMetadata(toSrid, ColumnMetadata.named("TO_SRID").withIndex(2).ofType(Types.DECIMAL).withSize(22));
    }

}

