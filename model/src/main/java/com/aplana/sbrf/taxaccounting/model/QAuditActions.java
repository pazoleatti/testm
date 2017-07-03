package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QAuditActions is a Querydsl query type for QAuditActions
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QAuditActions extends com.querydsl.sql.RelationalPathBase<QAuditActions> {

    private static final long serialVersionUID = 963310143;

    public static final QAuditActions auditActions = new QAuditActions("AUDIT_ACTIONS");

    public final NumberPath<java.math.BigInteger> action = createNumber("action", java.math.BigInteger.class);

    public final StringPath name = createString("name");

    public QAuditActions(String variable) {
        super(QAuditActions.class, forVariable(variable), "SYS", "AUDIT_ACTIONS");
        addMetadata();
    }

    public QAuditActions(String variable, String schema, String table) {
        super(QAuditActions.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QAuditActions(Path<? extends QAuditActions> path) {
        super(path.getType(), path.getMetadata(), "SYS", "AUDIT_ACTIONS");
        addMetadata();
    }

    public QAuditActions(PathMetadata metadata) {
        super(QAuditActions.class, metadata, "SYS", "AUDIT_ACTIONS");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(action, ColumnMetadata.named("ACTION").withIndex(1).ofType(Types.DECIMAL).withSize(22).notNull());
        addMetadata(name, ColumnMetadata.named("NAME").withIndex(2).ofType(Types.VARCHAR).withSize(28).notNull());
    }

}

