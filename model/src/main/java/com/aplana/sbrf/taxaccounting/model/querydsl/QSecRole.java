package com.aplana.sbrf.taxaccounting.model.querydsl;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import java.util.*;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QSecRole is a Querydsl query type for QSecRole
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QSecRole extends com.querydsl.sql.RelationalPathBase<QSecRole> {

    private static final long serialVersionUID = 1688189275;

    public static final QSecRole secRole = new QSecRole("SEC_ROLE");

    public final StringPath alias = createString("alias");

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final NumberPath<Byte> isActive = createNumber("isActive", Byte.class);

    public final StringPath name = createString("name");

    public final com.querydsl.sql.PrimaryKey<QSecRole> secRolePk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QRefBookAsnu> _refBookAsnuRoleAliasFk = createInvForeignKey(Arrays.asList(id, id, id, id, id), Arrays.asList("ROLE_ALIAS", "ROLE_ALIAS", "ROLE_ALIAS", "ROLE_ALIAS", "ROLE_ALIAS"));

    public final com.querydsl.sql.ForeignKey<QNotification> _notificationFkNotifyRole = createInvForeignKey(Arrays.asList(id, id, id, id, id, id), Arrays.asList("ROLE_ID", "ROLE_ID", "ROLE_ID", "ROLE_ID", "ROLE_ID", "ROLE_ID"));

    public final com.querydsl.sql.ForeignKey<QRefBookAsnu> _refBookAsnuRoleNameFk = createInvForeignKey(Arrays.asList(id, id, id, id, id), Arrays.asList("ROLE_NAME", "ROLE_NAME", "ROLE_NAME", "ROLE_NAME", "ROLE_NAME"));

    public final com.querydsl.sql.ForeignKey<QSecUserRole> _secUserRoleFkRoleId = createInvForeignKey(id, "ROLE_ID");

    public QSecRole(String variable) {
        super(QSecRole.class, forVariable(variable), "TAX_1_5", "SEC_ROLE");
        addMetadata();
    }

    public QSecRole(String variable, String schema, String table) {
        super(QSecRole.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QSecRole(String variable, String schema) {
        super(QSecRole.class, forVariable(variable), schema, "SEC_ROLE");
        addMetadata();
    }

    public QSecRole(Path<? extends QSecRole> path) {
        super(path.getType(), path.getMetadata(), "TAX_1_5", "SEC_ROLE");
        addMetadata();
    }

    public QSecRole(PathMetadata metadata) {
        super(QSecRole.class, metadata, "TAX_1_5", "SEC_ROLE");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(alias, ColumnMetadata.named("ALIAS").withIndex(2).ofType(Types.VARCHAR).withSize(120).notNull());
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(9).notNull());
        addMetadata(isActive, ColumnMetadata.named("IS_ACTIVE").withIndex(4).ofType(Types.DECIMAL).withSize(1).notNull());
        addMetadata(name, ColumnMetadata.named("NAME").withIndex(3).ofType(Types.VARCHAR).withSize(120).notNull());
    }

}

