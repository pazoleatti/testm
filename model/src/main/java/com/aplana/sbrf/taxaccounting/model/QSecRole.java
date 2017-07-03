package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QSecRole is a Querydsl query type for QSecRole
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QSecRole extends com.querydsl.sql.RelationalPathBase<QSecRole> {

    private static final long serialVersionUID = -385406806;

    public static final QSecRole secRole = new QSecRole("SEC_ROLE");

    public final StringPath alias = createString("alias");

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath name = createString("name");

    public final StringPath taxType = createString("taxType");

    public final com.querydsl.sql.PrimaryKey<QSecRole> secRolePk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QNotification> _notificationFkNotifyRole = createInvForeignKey(id, "ROLE_ID");

    public final com.querydsl.sql.ForeignKey<QRoleEvent> _roleEventFkRoleId = createInvForeignKey(id, "ROLE_ID");

    public final com.querydsl.sql.ForeignKey<QSecUserRole> _secUserRoleFkRoleId = createInvForeignKey(id, "ROLE_ID");

    public QSecRole(String variable) {
        super(QSecRole.class, forVariable(variable), "NDFL_1_0", "SEC_ROLE");
        addMetadata();
    }

    public QSecRole(String variable, String schema, String table) {
        super(QSecRole.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QSecRole(Path<? extends QSecRole> path) {
        super(path.getType(), path.getMetadata(), "NDFL_1_0", "SEC_ROLE");
        addMetadata();
    }

    public QSecRole(PathMetadata metadata) {
        super(QSecRole.class, metadata, "NDFL_1_0", "SEC_ROLE");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(alias, ColumnMetadata.named("ALIAS").withIndex(2).ofType(Types.VARCHAR).withSize(20).notNull());
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(9).notNull());
        addMetadata(name, ColumnMetadata.named("NAME").withIndex(3).ofType(Types.VARCHAR).withSize(50).notNull());
        addMetadata(taxType, ColumnMetadata.named("TAX_TYPE").withIndex(4).ofType(Types.VARCHAR).withSize(1).notNull());
    }

}

