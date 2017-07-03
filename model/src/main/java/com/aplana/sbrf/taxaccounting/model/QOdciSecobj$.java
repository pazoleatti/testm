package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QOdciSecobj$ is a Querydsl query type for QOdciSecobj$
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QOdciSecobj$ extends com.querydsl.sql.RelationalPathBase<QOdciSecobj$> {

    private static final long serialVersionUID = -2098926810;

    public static final QOdciSecobj$ odciSecobj$ = new QOdciSecobj$("ODCI_SECOBJ$");

    public final StringPath idxname = createString("idxname");

    public final StringPath idxschema = createString("idxschema");

    public final StringPath secobjname = createString("secobjname");

    public final StringPath secobjschema = createString("secobjschema");

    public QOdciSecobj$(String variable) {
        super(QOdciSecobj$.class, forVariable(variable), "SYS", "ODCI_SECOBJ$");
        addMetadata();
    }

    public QOdciSecobj$(String variable, String schema, String table) {
        super(QOdciSecobj$.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QOdciSecobj$(Path<? extends QOdciSecobj$> path) {
        super(path.getType(), path.getMetadata(), "SYS", "ODCI_SECOBJ$");
        addMetadata();
    }

    public QOdciSecobj$(PathMetadata metadata) {
        super(QOdciSecobj$.class, metadata, "SYS", "ODCI_SECOBJ$");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(idxname, ColumnMetadata.named("IDXNAME").withIndex(2).ofType(Types.VARCHAR).withSize(30));
        addMetadata(idxschema, ColumnMetadata.named("IDXSCHEMA").withIndex(1).ofType(Types.VARCHAR).withSize(30));
        addMetadata(secobjname, ColumnMetadata.named("SECOBJNAME").withIndex(4).ofType(Types.VARCHAR).withSize(30));
        addMetadata(secobjschema, ColumnMetadata.named("SECOBJSCHEMA").withIndex(3).ofType(Types.VARCHAR).withSize(30));
    }

}

