package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QFormDataSigner is a Querydsl query type for QFormDataSigner
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QFormDataSigner extends com.querydsl.sql.RelationalPathBase<QFormDataSigner> {

    private static final long serialVersionUID = -1446519371;

    public static final QFormDataSigner formDataSigner = new QFormDataSigner("FORM_DATA_SIGNER");

    public final NumberPath<Long> formDataId = createNumber("formDataId", Long.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath name = createString("name");

    public final NumberPath<Short> ord = createNumber("ord", Short.class);

    public final StringPath position = createString("position");

    public final com.querydsl.sql.PrimaryKey<QFormDataSigner> formDataSignerPk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QFormData> formDataSignerFkFormdata = createForeignKey(formDataId, "ID");

    public QFormDataSigner(String variable) {
        super(QFormDataSigner.class, forVariable(variable), "NDFL_UNSTABLE", "FORM_DATA_SIGNER");
        addMetadata();
    }

    public QFormDataSigner(String variable, String schema, String table) {
        super(QFormDataSigner.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QFormDataSigner(Path<? extends QFormDataSigner> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "FORM_DATA_SIGNER");
        addMetadata();
    }

    public QFormDataSigner(PathMetadata metadata) {
        super(QFormDataSigner.class, metadata, "NDFL_UNSTABLE", "FORM_DATA_SIGNER");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(formDataId, ColumnMetadata.named("FORM_DATA_ID").withIndex(2).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(name, ColumnMetadata.named("NAME").withIndex(3).ofType(Types.VARCHAR).withSize(200).notNull());
        addMetadata(ord, ColumnMetadata.named("ORD").withIndex(5).ofType(Types.DECIMAL).withSize(3).notNull());
        addMetadata(position, ColumnMetadata.named("POSITION").withIndex(4).ofType(Types.VARCHAR).withSize(200).notNull());
    }

}

