package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QXdb$groupDef is a Querydsl query type for QXdb$groupDef
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QXdb$groupDef extends com.querydsl.sql.RelationalPathBase<QXdb$groupDef> {

    private static final long serialVersionUID = 374930577;

    public static final QXdb$groupDef xdb$groupDef = new QXdb$groupDef("XDB$GROUP_DEF");

    public final SimplePath<Object> sysNcRowinfo$ = createSimple("sysNcRowinfo$", Object.class);

    public QXdb$groupDef(String variable) {
        super(QXdb$groupDef.class, forVariable(variable), "XDB", "XDB$GROUP_DEF");
        addMetadata();
    }

    public QXdb$groupDef(String variable, String schema, String table) {
        super(QXdb$groupDef.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QXdb$groupDef(Path<? extends QXdb$groupDef> path) {
        super(path.getType(), path.getMetadata(), "XDB", "XDB$GROUP_DEF");
        addMetadata();
    }

    public QXdb$groupDef(PathMetadata metadata) {
        super(QXdb$groupDef.class, metadata, "XDB", "XDB$GROUP_DEF");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(sysNcRowinfo$, ColumnMetadata.named("SYS_NC_ROWINFO$").withIndex(1).ofType(2007).withSize(2000));
    }

}

