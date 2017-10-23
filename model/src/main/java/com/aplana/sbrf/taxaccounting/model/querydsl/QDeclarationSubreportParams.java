package com.aplana.sbrf.taxaccounting.model.querydsl;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QDeclarationSubreportParams is a Querydsl query type for QDeclarationSubreportParams
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QDeclarationSubreportParams extends com.querydsl.sql.RelationalPathBase<QDeclarationSubreportParams> {

    private static final long serialVersionUID = -900531252;

    public static final QDeclarationSubreportParams declarationSubreportParams = new QDeclarationSubreportParams("DECLARATION_SUBREPORT_PARAMS");

    public final StringPath alias = createString("alias");

    public final NumberPath<Long> attributeId = createNumber("attributeId", Long.class);

    public final NumberPath<Integer> declarationSubreportId = createNumber("declarationSubreportId", Integer.class);

    public final StringPath filter = createString("filter");

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath name = createString("name");

    public final NumberPath<Integer> ord = createNumber("ord", Integer.class);

    public final NumberPath<Byte> required = createNumber("required", Byte.class);

    public final StringPath type = createString("type");

    public final com.querydsl.sql.PrimaryKey<QDeclarationSubreportParams> declSubrepParamsPk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QRefBookAttribute> declSubrepParsAttribIdFk = createForeignKey(attributeId, "ID");

    public final com.querydsl.sql.ForeignKey<QDeclarationSubreport> declSubrepParsSubrepIdFk = createForeignKey(declarationSubreportId, "ID");

    public QDeclarationSubreportParams(String variable) {
        super(QDeclarationSubreportParams.class, forVariable(variable), "NDFL_UNSTABLE", "DECLARATION_SUBREPORT_PARAMS");
        addMetadata();
    }

    public QDeclarationSubreportParams(String variable, String schema, String table) {
        super(QDeclarationSubreportParams.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QDeclarationSubreportParams(String variable, String schema) {
        super(QDeclarationSubreportParams.class, forVariable(variable), schema, "DECLARATION_SUBREPORT_PARAMS");
        addMetadata();
    }

    public QDeclarationSubreportParams(Path<? extends QDeclarationSubreportParams> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "DECLARATION_SUBREPORT_PARAMS");
        addMetadata();
    }

    public QDeclarationSubreportParams(PathMetadata metadata) {
        super(QDeclarationSubreportParams.class, metadata, "NDFL_UNSTABLE", "DECLARATION_SUBREPORT_PARAMS");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(alias, ColumnMetadata.named("ALIAS").withIndex(4).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(attributeId, ColumnMetadata.named("ATTRIBUTE_ID").withIndex(8).ofType(Types.DECIMAL).withSize(18));
        addMetadata(declarationSubreportId, ColumnMetadata.named("DECLARATION_SUBREPORT_ID").withIndex(2).ofType(Types.DECIMAL).withSize(9).notNull());
        addMetadata(filter, ColumnMetadata.named("FILTER").withIndex(7).ofType(Types.VARCHAR).withSize(1000));
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(9).notNull());
        addMetadata(name, ColumnMetadata.named("NAME").withIndex(3).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(ord, ColumnMetadata.named("ORD").withIndex(5).ofType(Types.DECIMAL).withSize(9).notNull());
        addMetadata(required, ColumnMetadata.named("REQUIRED").withIndex(9).ofType(Types.DECIMAL).withSize(1).notNull());
        addMetadata(type, ColumnMetadata.named("TYPE").withIndex(6).ofType(Types.CHAR).withSize(1).notNull());
    }

}

