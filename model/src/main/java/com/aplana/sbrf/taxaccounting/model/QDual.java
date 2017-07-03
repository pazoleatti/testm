package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QDual is a Querydsl query type for QDual
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QDual extends com.querydsl.sql.RelationalPathBase<QDual> {

    private static final long serialVersionUID = -1627402471;

    public static final QDual dual = new QDual("DUAL");

    public final StringPath dummy = createString("dummy");

    public QDual(String variable) {
        super(QDual.class, forVariable(variable), "SYS", "DUAL");
        addMetadata();
    }

    public QDual(String variable, String schema, String table) {
        super(QDual.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QDual(Path<? extends QDual> path) {
        super(path.getType(), path.getMetadata(), "SYS", "DUAL");
        addMetadata();
    }

    public QDual(PathMetadata metadata) {
        super(QDual.class, metadata, "SYS", "DUAL");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(dummy, ColumnMetadata.named("DUMMY").withIndex(1).ofType(Types.VARCHAR).withSize(1));
    }

}

