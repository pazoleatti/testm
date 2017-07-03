package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QAuditFormType is a Querydsl query type for QAuditFormType
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QAuditFormType extends com.querydsl.sql.RelationalPathBase<QAuditFormType> {

    private static final long serialVersionUID = 1927712028;

    public static final QAuditFormType auditFormType = new QAuditFormType("AUDIT_FORM_TYPE");

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath name = createString("name");

    public final com.querydsl.sql.PrimaryKey<QAuditFormType> auditFormTypePk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QLogSystem> _logSystemFkAuditFormType = createInvForeignKey(id, "AUDIT_FORM_TYPE_ID");

    public QAuditFormType(String variable) {
        super(QAuditFormType.class, forVariable(variable), "NDFL_1_0", "AUDIT_FORM_TYPE");
        addMetadata();
    }

    public QAuditFormType(String variable, String schema, String table) {
        super(QAuditFormType.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QAuditFormType(Path<? extends QAuditFormType> path) {
        super(path.getType(), path.getMetadata(), "NDFL_1_0", "AUDIT_FORM_TYPE");
        addMetadata();
    }

    public QAuditFormType(PathMetadata metadata) {
        super(QAuditFormType.class, metadata, "NDFL_1_0", "AUDIT_FORM_TYPE");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(9).notNull());
        addMetadata(name, ColumnMetadata.named("NAME").withIndex(2).ofType(Types.VARCHAR).withSize(1000).notNull());
    }

}

