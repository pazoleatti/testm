package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QXdb$schema is a Querydsl query type for QXdb$schema
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QXdb$schema extends com.querydsl.sql.RelationalPathBase<QXdb$schema> {

    private static final long serialVersionUID = 106399724;

    public static final QXdb$schema xdb$schema = new QXdb$schema("XDB$SCHEMA");

    public final SimplePath<Object> sysNcRowinfo$ = createSimple("sysNcRowinfo$", Object.class);

    public QXdb$schema(String variable) {
        super(QXdb$schema.class, forVariable(variable), "XDB", "XDB$SCHEMA");
        addMetadata();
    }

    public QXdb$schema(String variable, String schema, String table) {
        super(QXdb$schema.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QXdb$schema(Path<? extends QXdb$schema> path) {
        super(path.getType(), path.getMetadata(), "XDB", "XDB$SCHEMA");
        addMetadata();
    }

    public QXdb$schema(PathMetadata metadata) {
        super(QXdb$schema.class, metadata, "XDB", "XDB$SCHEMA");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(sysNcRowinfo$, ColumnMetadata.named("SYS_NC_ROWINFO$").withIndex(1).ofType(2007).withSize(2000));
    }

}

