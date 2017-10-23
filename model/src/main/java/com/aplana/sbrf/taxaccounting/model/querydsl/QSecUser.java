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
 * QSecUser is a Querydsl query type for QSecUser
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QSecUser extends com.querydsl.sql.RelationalPathBase<QSecUser> {

    private static final long serialVersionUID = 1688282288;

    public static final QSecUser secUser = new QSecUser("SEC_USER");

    public final NumberPath<Integer> departmentId = createNumber("departmentId", Integer.class);

    public final StringPath email = createString("email");

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final NumberPath<Byte> isActive = createNumber("isActive", Byte.class);

    public final StringPath login = createString("login");

    public final StringPath name = createString("name");

    public final com.querydsl.sql.PrimaryKey<QSecUser> secUserPk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QDepartment> secUserFkDepId = createForeignKey(departmentId, "ID");

    public final com.querydsl.sql.ForeignKey<QNotification> _notificationFkNotifyUser = createInvForeignKey(Arrays.asList(id, id, id, id, id, id), Arrays.asList("USER_ID", "USER_ID", "USER_ID", "USER_ID", "USER_ID", "USER_ID"));

    public final com.querydsl.sql.ForeignKey<QSecUserRole> _secUserRoleFkUserId = createInvForeignKey(id, "USER_ID");

    public final com.querydsl.sql.ForeignKey<QAsyncTaskSubscribers> _asyncTSubscrFkSecUser = createInvForeignKey(Arrays.asList(id, id, id), Arrays.asList("USER_ID", "USER_ID", "USER_ID"));

    public final com.querydsl.sql.ForeignKey<QLog> _logUserFk = createInvForeignKey(Arrays.asList(id, id, id, id, id), Arrays.asList("USER_ID", "USER_ID", "USER_ID", "USER_ID", "USER_ID"));

    public final com.querydsl.sql.ForeignKey<QLockData> _lockDataFkUserId = createInvForeignKey(Arrays.asList(id, id, id, id, id, id), Arrays.asList("USER_ID", "USER_ID", "USER_ID", "USER_ID", "USER_ID", "USER_ID"));

    public final com.querydsl.sql.ForeignKey<QTemplateChanges> _templateChangesFkUserId = createInvForeignKey(Arrays.asList(id, id, id, id, id, id), Arrays.asList("AUTHOR", "AUTHOR", "AUTHOR", "AUTHOR", "AUTHOR", "AUTHOR"));

    public QSecUser(String variable) {
        super(QSecUser.class, forVariable(variable), "TAX_1_5", "SEC_USER");
        addMetadata();
    }

    public QSecUser(String variable, String schema, String table) {
        super(QSecUser.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QSecUser(String variable, String schema) {
        super(QSecUser.class, forVariable(variable), schema, "SEC_USER");
        addMetadata();
    }

    public QSecUser(Path<? extends QSecUser> path) {
        super(path.getType(), path.getMetadata(), "TAX_1_5", "SEC_USER");
        addMetadata();
    }

    public QSecUser(PathMetadata metadata) {
        super(QSecUser.class, metadata, "TAX_1_5", "SEC_USER");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(departmentId, ColumnMetadata.named("DEPARTMENT_ID").withIndex(4).ofType(Types.DECIMAL).withSize(9).notNull());
        addMetadata(email, ColumnMetadata.named("EMAIL").withIndex(6).ofType(Types.VARCHAR).withSize(128));
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(9).notNull());
        addMetadata(isActive, ColumnMetadata.named("IS_ACTIVE").withIndex(5).ofType(Types.DECIMAL).withSize(1).notNull());
        addMetadata(login, ColumnMetadata.named("LOGIN").withIndex(2).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(name, ColumnMetadata.named("NAME").withIndex(3).ofType(Types.VARCHAR).withSize(512).notNull());
    }

}

