package com.aplana.sbrf.taxaccounting.model.querydsl;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QRefBookAsnu is a Querydsl query type for QRefBookAsnu
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QRefBookAsnu extends com.querydsl.sql.RelationalPathBase<QRefBookAsnu> {

    private static final long serialVersionUID = -528105303;

    public static final QRefBookAsnu refBookAsnu = new QRefBookAsnu("REF_BOOK_ASNU");

    public final StringPath code = createString("code");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath name = createString("name");

    public final NumberPath<Integer> roleAlias = createNumber("roleAlias", Integer.class);

    public final NumberPath<Integer> roleName = createNumber("roleName", Integer.class);

    public final StringPath type = createString("type");

    public final com.querydsl.sql.PrimaryKey<QRefBookAsnu> refBookAsnuPk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QSecRole> refBookAsnuRoleNameFk = createForeignKey(roleName, "ID");

    public final com.querydsl.sql.ForeignKey<QSecRole> refBookAsnuRoleAliasFk = createForeignKey(roleAlias, "ID");

    public final com.querydsl.sql.ForeignKey<QRefBookIdTaxPayer> _refBookIdTaxPayerAsNuFk = createInvForeignKey(id, "AS_NU");

    public final com.querydsl.sql.ForeignKey<QRefBookPerson> _refBookPersonSourceFk = createInvForeignKey(id, "SOURCE_ID");

    public final com.querydsl.sql.ForeignKey<QDeclarationData> _declarationDataFkAsnuId = createInvForeignKey(id, "ASNU_ID");

    public QRefBookAsnu(String variable) {
        super(QRefBookAsnu.class, forVariable(variable), "NDFL_UNSTABLE", "REF_BOOK_ASNU");
        addMetadata();
    }

    public QRefBookAsnu(String variable, String schema, String table) {
        super(QRefBookAsnu.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QRefBookAsnu(String variable, String schema) {
        super(QRefBookAsnu.class, forVariable(variable), schema, "REF_BOOK_ASNU");
        addMetadata();
    }

    public QRefBookAsnu(Path<? extends QRefBookAsnu> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "REF_BOOK_ASNU");
        addMetadata();
    }

    public QRefBookAsnu(PathMetadata metadata) {
        super(QRefBookAsnu.class, metadata, "NDFL_UNSTABLE", "REF_BOOK_ASNU");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(code, ColumnMetadata.named("CODE").withIndex(2).ofType(Types.VARCHAR).withSize(4).notNull());
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(name, ColumnMetadata.named("NAME").withIndex(3).ofType(Types.VARCHAR).withSize(100).notNull());
        addMetadata(roleAlias, ColumnMetadata.named("ROLE_ALIAS").withIndex(5).ofType(Types.DECIMAL).withSize(9));
        addMetadata(roleName, ColumnMetadata.named("ROLE_NAME").withIndex(6).ofType(Types.DECIMAL).withSize(9));
        addMetadata(type, ColumnMetadata.named("TYPE").withIndex(4).ofType(Types.VARCHAR).withSize(255).notNull());
    }

}

