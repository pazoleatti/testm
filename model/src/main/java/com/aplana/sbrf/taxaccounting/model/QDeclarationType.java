package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QDeclarationType is a Querydsl query type for QDeclarationType
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QDeclarationType extends com.querydsl.sql.RelationalPathBase<QDeclarationType> {

    private static final long serialVersionUID = 1765556503;

    public static final QDeclarationType declarationType = new QDeclarationType("DECLARATION_TYPE");

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath ifrsName = createString("ifrsName");

    public final NumberPath<Byte> isIfrs = createNumber("isIfrs", Byte.class);

    public final StringPath name = createString("name");

    public final NumberPath<Byte> status = createNumber("status", Byte.class);

    public final StringPath taxType = createString("taxType");

    public final com.querydsl.sql.PrimaryKey<QDeclarationType> declarationTypePk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QTaxType> declarationTypeFkTaxtype = createForeignKey(taxType, "ID");

    public final com.querydsl.sql.ForeignKey<QDeclarationTemplate> _declarationTemplateFkDtype = createInvForeignKey(id, "DECLARATION_TYPE_ID");

    public final com.querydsl.sql.ForeignKey<QDepartmentDeclarationType> _deptDeclTypeFkDeclType = createInvForeignKey(id, "DECLARATION_TYPE_ID");

    public QDeclarationType(String variable) {
        super(QDeclarationType.class, forVariable(variable), "NDFL_UNSTABLE", "DECLARATION_TYPE");
        addMetadata();
    }

    public QDeclarationType(String variable, String schema, String table) {
        super(QDeclarationType.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QDeclarationType(Path<? extends QDeclarationType> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "DECLARATION_TYPE");
        addMetadata();
    }

    public QDeclarationType(PathMetadata metadata) {
        super(QDeclarationType.class, metadata, "NDFL_UNSTABLE", "DECLARATION_TYPE");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(9).notNull());
        addMetadata(ifrsName, ColumnMetadata.named("IFRS_NAME").withIndex(6).ofType(Types.VARCHAR).withSize(200));
        addMetadata(isIfrs, ColumnMetadata.named("IS_IFRS").withIndex(5).ofType(Types.DECIMAL).withSize(1).notNull());
        addMetadata(name, ColumnMetadata.named("NAME").withIndex(3).ofType(Types.VARCHAR).withSize(1000).notNull());
        addMetadata(status, ColumnMetadata.named("STATUS").withIndex(4).ofType(Types.DECIMAL).withSize(1).notNull());
        addMetadata(taxType, ColumnMetadata.named("TAX_TYPE").withIndex(2).ofType(Types.CHAR).withSize(1).notNull());
    }

}

