package com.aplana.sbrf.taxaccounting.model.querydsl;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QSecUserRole is a Querydsl query type for QSecUserRole
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QSecUserRole extends com.querydsl.sql.RelationalPathBase<QSecUserRole> {

    private static final long serialVersionUID = 826687814;

    public static final QSecUserRole secUserRole = new QSecUserRole("SEC_USER_ROLE");

    public final NumberPath<Integer> roleId = createNumber("roleId", Integer.class);

    public final NumberPath<Integer> userId = createNumber("userId", Integer.class);

    public final com.querydsl.sql.PrimaryKey<QSecUserRole> secUserRolePk = createPrimaryKey(roleId, userId);

    public final com.querydsl.sql.ForeignKey<QSecUser> secUserRoleFkUserId = createForeignKey(userId, "ID");

    public final com.querydsl.sql.ForeignKey<QSecRole> secUserRoleFkRoleId = createForeignKey(roleId, "ID");

    public QSecUserRole(String variable) {
        super(QSecUserRole.class, forVariable(variable), "TAX_1_5", "SEC_USER_ROLE");
        addMetadata();
    }

    public QSecUserRole(String variable, String schema, String table) {
        super(QSecUserRole.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QSecUserRole(String variable, String schema) {
        super(QSecUserRole.class, forVariable(variable), schema, "SEC_USER_ROLE");
        addMetadata();
    }

    public QSecUserRole(Path<? extends QSecUserRole> path) {
        super(path.getType(), path.getMetadata(), "TAX_1_5", "SEC_USER_ROLE");
        addMetadata();
    }

    public QSecUserRole(PathMetadata metadata) {
        super(QSecUserRole.class, metadata, "TAX_1_5", "SEC_USER_ROLE");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(roleId, ColumnMetadata.named("ROLE_ID").withIndex(2).ofType(Types.DECIMAL).withSize(9).notNull());
        addMetadata(userId, ColumnMetadata.named("USER_ID").withIndex(1).ofType(Types.DECIMAL).withSize(9).notNull());
    }

}

