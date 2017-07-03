package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QSystemPrivilegeMap is a Querydsl query type for QSystemPrivilegeMap
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QSystemPrivilegeMap extends com.querydsl.sql.RelationalPathBase<QSystemPrivilegeMap> {

    private static final long serialVersionUID = 543696023;

    public static final QSystemPrivilegeMap systemPrivilegeMap = new QSystemPrivilegeMap("SYSTEM_PRIVILEGE_MAP");

    public final StringPath name = createString("name");

    public final NumberPath<java.math.BigInteger> privilege = createNumber("privilege", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> property = createNumber("property", java.math.BigInteger.class);

    public QSystemPrivilegeMap(String variable) {
        super(QSystemPrivilegeMap.class, forVariable(variable), "SYS", "SYSTEM_PRIVILEGE_MAP");
        addMetadata();
    }

    public QSystemPrivilegeMap(String variable, String schema, String table) {
        super(QSystemPrivilegeMap.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QSystemPrivilegeMap(Path<? extends QSystemPrivilegeMap> path) {
        super(path.getType(), path.getMetadata(), "SYS", "SYSTEM_PRIVILEGE_MAP");
        addMetadata();
    }

    public QSystemPrivilegeMap(PathMetadata metadata) {
        super(QSystemPrivilegeMap.class, metadata, "SYS", "SYSTEM_PRIVILEGE_MAP");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(name, ColumnMetadata.named("NAME").withIndex(2).ofType(Types.VARCHAR).withSize(40).notNull());
        addMetadata(privilege, ColumnMetadata.named("PRIVILEGE").withIndex(1).ofType(Types.DECIMAL).withSize(22).notNull());
        addMetadata(property, ColumnMetadata.named("PROPERTY").withIndex(3).ofType(Types.DECIMAL).withSize(22).notNull());
    }

}

