package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QStmtAuditOptionMap is a Querydsl query type for QStmtAuditOptionMap
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QStmtAuditOptionMap extends com.querydsl.sql.RelationalPathBase<QStmtAuditOptionMap> {

    private static final long serialVersionUID = -1060678447;

    public static final QStmtAuditOptionMap stmtAuditOptionMap = new QStmtAuditOptionMap("STMT_AUDIT_OPTION_MAP");

    public final StringPath name = createString("name");

    public final NumberPath<java.math.BigInteger> option_ = createNumber("option_", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> property = createNumber("property", java.math.BigInteger.class);

    public QStmtAuditOptionMap(String variable) {
        super(QStmtAuditOptionMap.class, forVariable(variable), "SYS", "STMT_AUDIT_OPTION_MAP");
        addMetadata();
    }

    public QStmtAuditOptionMap(String variable, String schema, String table) {
        super(QStmtAuditOptionMap.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QStmtAuditOptionMap(Path<? extends QStmtAuditOptionMap> path) {
        super(path.getType(), path.getMetadata(), "SYS", "STMT_AUDIT_OPTION_MAP");
        addMetadata();
    }

    public QStmtAuditOptionMap(PathMetadata metadata) {
        super(QStmtAuditOptionMap.class, metadata, "SYS", "STMT_AUDIT_OPTION_MAP");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(name, ColumnMetadata.named("NAME").withIndex(2).ofType(Types.VARCHAR).withSize(40).notNull());
        addMetadata(option_, ColumnMetadata.named("OPTION#").withIndex(1).ofType(Types.DECIMAL).withSize(22).notNull());
        addMetadata(property, ColumnMetadata.named("PROPERTY").withIndex(3).ofType(Types.DECIMAL).withSize(22).notNull());
    }

}

