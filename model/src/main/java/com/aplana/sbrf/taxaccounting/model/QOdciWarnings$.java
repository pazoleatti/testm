package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QOdciWarnings$ is a Querydsl query type for QOdciWarnings$
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QOdciWarnings$ extends com.querydsl.sql.RelationalPathBase<QOdciWarnings$> {

    private static final long serialVersionUID = 655483381;

    public static final QOdciWarnings$ odciWarnings$ = new QOdciWarnings$("ODCI_WARNINGS$");

    public final NumberPath<java.math.BigInteger> c1 = createNumber("c1", java.math.BigInteger.class);

    public final StringPath c2 = createString("c2");

    public QOdciWarnings$(String variable) {
        super(QOdciWarnings$.class, forVariable(variable), "SYS", "ODCI_WARNINGS$");
        addMetadata();
    }

    public QOdciWarnings$(String variable, String schema, String table) {
        super(QOdciWarnings$.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QOdciWarnings$(Path<? extends QOdciWarnings$> path) {
        super(path.getType(), path.getMetadata(), "SYS", "ODCI_WARNINGS$");
        addMetadata();
    }

    public QOdciWarnings$(PathMetadata metadata) {
        super(QOdciWarnings$.class, metadata, "SYS", "ODCI_WARNINGS$");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(c1, ColumnMetadata.named("C1").withIndex(1).ofType(Types.DECIMAL).withSize(22));
        addMetadata(c2, ColumnMetadata.named("C2").withIndex(2).ofType(Types.VARCHAR).withSize(2000));
    }

}

