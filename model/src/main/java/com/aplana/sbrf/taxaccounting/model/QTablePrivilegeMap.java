package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QTablePrivilegeMap is a Querydsl query type for QTablePrivilegeMap
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QTablePrivilegeMap extends com.querydsl.sql.RelationalPathBase<QTablePrivilegeMap> {

    private static final long serialVersionUID = 411895900;

    public static final QTablePrivilegeMap tablePrivilegeMap = new QTablePrivilegeMap("TABLE_PRIVILEGE_MAP");

    public final StringPath name = createString("name");

    public final NumberPath<java.math.BigInteger> privilege = createNumber("privilege", java.math.BigInteger.class);

    public QTablePrivilegeMap(String variable) {
        super(QTablePrivilegeMap.class, forVariable(variable), "SYS", "TABLE_PRIVILEGE_MAP");
        addMetadata();
    }

    public QTablePrivilegeMap(String variable, String schema, String table) {
        super(QTablePrivilegeMap.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QTablePrivilegeMap(Path<? extends QTablePrivilegeMap> path) {
        super(path.getType(), path.getMetadata(), "SYS", "TABLE_PRIVILEGE_MAP");
        addMetadata();
    }

    public QTablePrivilegeMap(PathMetadata metadata) {
        super(QTablePrivilegeMap.class, metadata, "SYS", "TABLE_PRIVILEGE_MAP");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(name, ColumnMetadata.named("NAME").withIndex(2).ofType(Types.VARCHAR).withSize(40).notNull());
        addMetadata(privilege, ColumnMetadata.named("PRIVILEGE").withIndex(1).ofType(Types.DECIMAL).withSize(22).notNull());
    }

}

