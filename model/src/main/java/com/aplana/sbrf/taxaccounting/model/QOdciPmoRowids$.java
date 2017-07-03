package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QOdciPmoRowids$ is a Querydsl query type for QOdciPmoRowids$
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QOdciPmoRowids$ extends com.querydsl.sql.RelationalPathBase<QOdciPmoRowids$> {

    private static final long serialVersionUID = 172497036;

    public static final QOdciPmoRowids$ odciPmoRowids$ = new QOdciPmoRowids$("ODCI_PMO_ROWIDS$");

    public final StringPath newRowid = createString("newRowid");

    public final StringPath oldRowid = createString("oldRowid");

    public QOdciPmoRowids$(String variable) {
        super(QOdciPmoRowids$.class, forVariable(variable), "SYS", "ODCI_PMO_ROWIDS$");
        addMetadata();
    }

    public QOdciPmoRowids$(String variable, String schema, String table) {
        super(QOdciPmoRowids$.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QOdciPmoRowids$(Path<? extends QOdciPmoRowids$> path) {
        super(path.getType(), path.getMetadata(), "SYS", "ODCI_PMO_ROWIDS$");
        addMetadata();
    }

    public QOdciPmoRowids$(PathMetadata metadata) {
        super(QOdciPmoRowids$.class, metadata, "SYS", "ODCI_PMO_ROWIDS$");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(newRowid, ColumnMetadata.named("NEW_ROWID").withIndex(2).ofType(Types.VARCHAR).withSize(18));
        addMetadata(oldRowid, ColumnMetadata.named("OLD_ROWID").withIndex(1).ofType(Types.VARCHAR).withSize(18));
    }

}

