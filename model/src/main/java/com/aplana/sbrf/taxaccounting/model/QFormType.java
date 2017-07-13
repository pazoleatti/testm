package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QFormType is a Querydsl query type for QFormType
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QFormType extends com.querydsl.sql.RelationalPathBase<QFormType> {

    private static final long serialVersionUID = 494276155;

    public static final QFormType formType = new QFormType("FORM_TYPE");

    public final StringPath code = createString("code");

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath ifrsName = createString("ifrsName");

    public final NumberPath<Byte> isIfrs = createNumber("isIfrs", Byte.class);

    public final StringPath name = createString("name");

    public final NumberPath<Byte> status = createNumber("status", Byte.class);

    public final StringPath taxType = createString("taxType");

    public final com.querydsl.sql.PrimaryKey<QFormType> formTypePk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QTaxType> formTypeFkTaxtype = createForeignKey(taxType, "ID");

    public final com.querydsl.sql.ForeignKey<QDepartmentFormType> _deptFormTypeFkTypeId = createInvForeignKey(id, "FORM_TYPE_ID");

    public final com.querydsl.sql.ForeignKey<QFormTemplate> _formTemplateFkTypeId = createInvForeignKey(id, "TYPE_ID");

    public QFormType(String variable) {
        super(QFormType.class, forVariable(variable), "NDFL_UNSTABLE", "FORM_TYPE");
        addMetadata();
    }

    public QFormType(String variable, String schema, String table) {
        super(QFormType.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QFormType(Path<? extends QFormType> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "FORM_TYPE");
        addMetadata();
    }

    public QFormType(PathMetadata metadata) {
        super(QFormType.class, metadata, "NDFL_UNSTABLE", "FORM_TYPE");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(code, ColumnMetadata.named("CODE").withIndex(5).ofType(Types.VARCHAR).withSize(9));
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(9).notNull());
        addMetadata(ifrsName, ColumnMetadata.named("IFRS_NAME").withIndex(7).ofType(Types.VARCHAR).withSize(200));
        addMetadata(isIfrs, ColumnMetadata.named("IS_IFRS").withIndex(6).ofType(Types.DECIMAL).withSize(1).notNull());
        addMetadata(name, ColumnMetadata.named("NAME").withIndex(2).ofType(Types.VARCHAR).withSize(1000).notNull());
        addMetadata(status, ColumnMetadata.named("STATUS").withIndex(4).ofType(Types.DECIMAL).withSize(1).notNull());
        addMetadata(taxType, ColumnMetadata.named("TAX_TYPE").withIndex(3).ofType(Types.CHAR).withSize(1).notNull());
    }

}

