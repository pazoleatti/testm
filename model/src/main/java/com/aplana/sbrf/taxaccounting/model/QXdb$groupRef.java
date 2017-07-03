package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QXdb$groupRef is a Querydsl query type for QXdb$groupRef
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QXdb$groupRef extends com.querydsl.sql.RelationalPathBase<QXdb$groupRef> {

    private static final long serialVersionUID = 374944031;

    public static final QXdb$groupRef xdb$groupRef = new QXdb$groupRef("XDB$GROUP_REF");

    public final SimplePath<Object> sysNcRowinfo$ = createSimple("sysNcRowinfo$", Object.class);

    public QXdb$groupRef(String variable) {
        super(QXdb$groupRef.class, forVariable(variable), "XDB", "XDB$GROUP_REF");
        addMetadata();
    }

    public QXdb$groupRef(String variable, String schema, String table) {
        super(QXdb$groupRef.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QXdb$groupRef(Path<? extends QXdb$groupRef> path) {
        super(path.getType(), path.getMetadata(), "XDB", "XDB$GROUP_REF");
        addMetadata();
    }

    public QXdb$groupRef(PathMetadata metadata) {
        super(QXdb$groupRef.class, metadata, "XDB", "XDB$GROUP_REF");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(sysNcRowinfo$, ColumnMetadata.named("SYS_NC_ROWINFO$").withIndex(1).ofType(2007).withSize(2000));
    }

}

