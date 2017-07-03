package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QXdb$element is a Querydsl query type for QXdb$element
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QXdb$element extends com.querydsl.sql.RelationalPathBase<QXdb$element> {

    private static final long serialVersionUID = -281602543;

    public static final QXdb$element xdb$element = new QXdb$element("XDB$ELEMENT");

    public final SimplePath<Object> sysNcRowinfo$ = createSimple("sysNcRowinfo$", Object.class);

    public QXdb$element(String variable) {
        super(QXdb$element.class, forVariable(variable), "XDB", "XDB$ELEMENT");
        addMetadata();
    }

    public QXdb$element(String variable, String schema, String table) {
        super(QXdb$element.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QXdb$element(Path<? extends QXdb$element> path) {
        super(path.getType(), path.getMetadata(), "XDB", "XDB$ELEMENT");
        addMetadata();
    }

    public QXdb$element(PathMetadata metadata) {
        super(QXdb$element.class, metadata, "XDB", "XDB$ELEMENT");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(sysNcRowinfo$, ColumnMetadata.named("SYS_NC_ROWINFO$").withIndex(1).ofType(2007).withSize(2000));
    }

}

