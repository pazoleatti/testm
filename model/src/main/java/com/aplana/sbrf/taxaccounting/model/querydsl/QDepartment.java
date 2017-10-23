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
 * QDepartment is a Querydsl query type for QDepartment
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QDepartment extends com.querydsl.sql.RelationalPathBase<QDepartment> {

    private static final long serialVersionUID = -864643618;

    public static final QDepartment department = new QDepartment("DEPARTMENT");

    public final NumberPath<Long> code = createNumber("code", Long.class);

    public final StringPath destinationParent = createString("destinationParent");

    public final NumberPath<Byte> garantUse = createNumber("garantUse", Byte.class);

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final NumberPath<Byte> isActive = createNumber("isActive", Byte.class);

    public final StringPath name = createString("name");

    public final NumberPath<Integer> parentId = createNumber("parentId", Integer.class);

    public final NumberPath<Long> regionId = createNumber("regionId", Long.class);

    public final StringPath sbrfCode = createString("sbrfCode");

    public final StringPath shortname = createString("shortname");

    public final NumberPath<Byte> sunrUse = createNumber("sunrUse", Byte.class);

    public final StringPath tbIndex = createString("tbIndex");

    public final EnumPath<com.aplana.sbrf.taxaccounting.model.DepartmentType> type = createEnum("type", com.aplana.sbrf.taxaccounting.model.DepartmentType.class);

    public final com.querydsl.sql.PrimaryKey<QDepartment> departmentPk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QDepartmentType> departmentFkType = createForeignKey(type, "ID");

    public final com.querydsl.sql.ForeignKey<QDepartment> deptFkParentId = createForeignKey(parentId, "ID");

    public final com.querydsl.sql.ForeignKey<QConfiguration> _configurationFk = createInvForeignKey(Arrays.asList(id, id, id, id, id, id), Arrays.asList("DEPARTMENT_ID", "DEPARTMENT_ID", "DEPARTMENT_ID", "DEPARTMENT_ID", "DEPARTMENT_ID", "DEPARTMENT_ID"));

    public final com.querydsl.sql.ForeignKey<QRefBookNdfl> _refBookNdflDepartFk = createInvForeignKey(Arrays.asList(id, id, id, id, id), Arrays.asList("DEPARTMENT_ID", "DEPARTMENT_ID", "DEPARTMENT_ID", "DEPARTMENT_ID", "DEPARTMENT_ID"));

    public final com.querydsl.sql.ForeignKey<QDepartmentReportPeriod> _depRepPerFkDepartmentId = createInvForeignKey(Arrays.asList(id, id, id, id, id, id), Arrays.asList("DEPARTMENT_ID", "DEPARTMENT_ID", "DEPARTMENT_ID", "DEPARTMENT_ID", "DEPARTMENT_ID", "DEPARTMENT_ID"));

    public final com.querydsl.sql.ForeignKey<QNotification> _notificationFkReceiver = createInvForeignKey(Arrays.asList(id, id, id, id, id, id), Arrays.asList("RECEIVER_DEPARTMENT_ID", "RECEIVER_DEPARTMENT_ID", "RECEIVER_DEPARTMENT_ID", "RECEIVER_DEPARTMENT_ID", "RECEIVER_DEPARTMENT_ID", "RECEIVER_DEPARTMENT_ID"));

    public final com.querydsl.sql.ForeignKey<QDepartment> _deptFkParentId = createInvForeignKey(id, "PARENT_ID");

    public final com.querydsl.sql.ForeignKey<QSecUser> _secUserFkDepId = createInvForeignKey(id, "DEPARTMENT_ID");

    public final com.querydsl.sql.ForeignKey<QNotification> _notificationFkSender = createInvForeignKey(Arrays.asList(id, id, id, id, id, id), Arrays.asList("SENDER_DEPARTMENT_ID", "SENDER_DEPARTMENT_ID", "SENDER_DEPARTMENT_ID", "SENDER_DEPARTMENT_ID", "SENDER_DEPARTMENT_ID", "SENDER_DEPARTMENT_ID"));

    public final com.querydsl.sql.ForeignKey<QDepartmentDeclarationType> _deptDeclTypeFkDept = createInvForeignKey(Arrays.asList(id, id, id, id, id, id), Arrays.asList("DEPARTMENT_ID", "DEPARTMENT_ID", "DEPARTMENT_ID", "DEPARTMENT_ID", "DEPARTMENT_ID", "DEPARTMENT_ID"));

    public final com.querydsl.sql.ForeignKey<QDepartmentDeclTypePerformer> _deptDeclTypePerfPerfFk = createInvForeignKey(Arrays.asList(id, id, id, id, id), Arrays.asList("PERFORMER_DEP_ID", "PERFORMER_DEP_ID", "PERFORMER_DEP_ID", "PERFORMER_DEP_ID", "PERFORMER_DEP_ID"));

    public final com.querydsl.sql.ForeignKey<QRefBookNdflDetail> _refBookNdflDetDepartFk = createInvForeignKey(Arrays.asList(id, id, id, id, id), Arrays.asList("DEPARTMENT_ID", "DEPARTMENT_ID", "DEPARTMENT_ID", "DEPARTMENT_ID", "DEPARTMENT_ID"));

    public QDepartment(String variable) {
        super(QDepartment.class, forVariable(variable), "TAX_1_5", "DEPARTMENT");
        addMetadata();
    }

    public QDepartment(String variable, String schema, String table) {
        super(QDepartment.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QDepartment(String variable, String schema) {
        super(QDepartment.class, forVariable(variable), schema, "DEPARTMENT");
        addMetadata();
    }

    public QDepartment(Path<? extends QDepartment> path) {
        super(path.getType(), path.getMetadata(), "TAX_1_5", "DEPARTMENT");
        addMetadata();
    }

    public QDepartment(PathMetadata metadata) {
        super(QDepartment.class, metadata, "TAX_1_5", "DEPARTMENT");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(code, ColumnMetadata.named("CODE").withIndex(10).ofType(Types.DECIMAL).withSize(15).notNull());
        addMetadata(destinationParent, ColumnMetadata.named("DESTINATION_PARENT").withIndex(13).ofType(Types.VARCHAR).withSize(4000));
        addMetadata(garantUse, ColumnMetadata.named("GARANT_USE").withIndex(11).ofType(Types.DECIMAL).withSize(1).notNull());
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(9).notNull());
        addMetadata(isActive, ColumnMetadata.named("IS_ACTIVE").withIndex(9).ofType(Types.DECIMAL).withSize(1).notNull());
        addMetadata(name, ColumnMetadata.named("NAME").withIndex(2).ofType(Types.VARCHAR).withSize(510).notNull());
        addMetadata(parentId, ColumnMetadata.named("PARENT_ID").withIndex(3).ofType(Types.DECIMAL).withSize(9));
        addMetadata(regionId, ColumnMetadata.named("REGION_ID").withIndex(8).ofType(Types.DECIMAL).withSize(18));
        addMetadata(sbrfCode, ColumnMetadata.named("SBRF_CODE").withIndex(7).ofType(Types.VARCHAR).withSize(255));
        addMetadata(shortname, ColumnMetadata.named("SHORTNAME").withIndex(5).ofType(Types.VARCHAR).withSize(510));
        addMetadata(sunrUse, ColumnMetadata.named("SUNR_USE").withIndex(12).ofType(Types.DECIMAL).withSize(1).notNull());
        addMetadata(tbIndex, ColumnMetadata.named("TB_INDEX").withIndex(6).ofType(Types.VARCHAR).withSize(3));
        addMetadata(type, ColumnMetadata.named("TYPE").withIndex(4).ofType(Types.DECIMAL).withSize(9).notNull());
    }

}

