package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QKu$noexpTab is a Querydsl query type for QKu$noexpTab
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QKu$noexpTab extends com.querydsl.sql.RelationalPathBase<QKu$noexpTab> {

    private static final long serialVersionUID = -1122510346;

    public static final QKu$noexpTab ku$noexpTab = new QKu$noexpTab("KU$NOEXP_TAB");

    public final StringPath name = createString("name");

    public final StringPath objType = createString("objType");

    public final StringPath schema = createString("schema");

    public QKu$noexpTab(String variable) {
        super(QKu$noexpTab.class, forVariable(variable), "SYS", "KU$NOEXP_TAB");
        addMetadata();
    }

    public QKu$noexpTab(String variable, String schema, String table) {
        super(QKu$noexpTab.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QKu$noexpTab(Path<? extends QKu$noexpTab> path) {
        super(path.getType(), path.getMetadata(), "SYS", "KU$NOEXP_TAB");
        addMetadata();
    }

    public QKu$noexpTab(PathMetadata metadata) {
        super(QKu$noexpTab.class, metadata, "SYS", "KU$NOEXP_TAB");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(name, ColumnMetadata.named("NAME").withIndex(3).ofType(Types.VARCHAR).withSize(30));
        addMetadata(objType, ColumnMetadata.named("OBJ_TYPE").withIndex(1).ofType(Types.VARCHAR).withSize(30));
        addMetadata(schema, ColumnMetadata.named("SCHEMA").withIndex(2).ofType(Types.VARCHAR).withSize(30));
    }

}

