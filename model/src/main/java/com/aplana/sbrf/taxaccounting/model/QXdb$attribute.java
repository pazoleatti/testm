package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QXdb$attribute is a Querydsl query type for QXdb$attribute
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QXdb$attribute extends com.querydsl.sql.RelationalPathBase<QXdb$attribute> {

    private static final long serialVersionUID = 234500209;

    public static final QXdb$attribute xdb$attribute = new QXdb$attribute("XDB$ATTRIBUTE");

    public final SimplePath<Object> sysNcRowinfo$ = createSimple("sysNcRowinfo$", Object.class);

    public QXdb$attribute(String variable) {
        super(QXdb$attribute.class, forVariable(variable), "XDB", "XDB$ATTRIBUTE");
        addMetadata();
    }

    public QXdb$attribute(String variable, String schema, String table) {
        super(QXdb$attribute.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QXdb$attribute(Path<? extends QXdb$attribute> path) {
        super(path.getType(), path.getMetadata(), "XDB", "XDB$ATTRIBUTE");
        addMetadata();
    }

    public QXdb$attribute(PathMetadata metadata) {
        super(QXdb$attribute.class, metadata, "XDB", "XDB$ATTRIBUTE");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(sysNcRowinfo$, ColumnMetadata.named("SYS_NC_ROWINFO$").withIndex(1).ofType(2007).withSize(2000));
    }

}

