package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QXdb$acl is a Querydsl query type for QXdb$acl
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QXdb$acl extends com.querydsl.sql.RelationalPathBase<QXdb$acl> {

    private static final long serialVersionUID = -273792481;

    public static final QXdb$acl xdb$acl = new QXdb$acl("XDB$ACL");

    public final SimplePath<Object> sysNcRowinfo$ = createSimple("sysNcRowinfo$", Object.class);

    public QXdb$acl(String variable) {
        super(QXdb$acl.class, forVariable(variable), "XDB", "XDB$ACL");
        addMetadata();
    }

    public QXdb$acl(String variable, String schema, String table) {
        super(QXdb$acl.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QXdb$acl(Path<? extends QXdb$acl> path) {
        super(path.getType(), path.getMetadata(), "XDB", "XDB$ACL");
        addMetadata();
    }

    public QXdb$acl(PathMetadata metadata) {
        super(QXdb$acl.class, metadata, "XDB", "XDB$ACL");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(sysNcRowinfo$, ColumnMetadata.named("SYS_NC_ROWINFO$").withIndex(1).ofType(2007).withSize(2000));
    }

}

