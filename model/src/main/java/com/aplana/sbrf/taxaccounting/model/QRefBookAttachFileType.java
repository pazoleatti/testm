package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QRefBookAttachFileType is a Querydsl query type for QRefBookAttachFileType
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QRefBookAttachFileType extends com.querydsl.sql.RelationalPathBase<QRefBookAttachFileType> {

    private static final long serialVersionUID = -1203453830;

    public static final QRefBookAttachFileType refBookAttachFileType = new QRefBookAttachFileType("REF_BOOK_ATTACH_FILE_TYPE");

    public final NumberPath<Byte> code = createNumber("code", Byte.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath name = createString("name");

    public final com.querydsl.sql.PrimaryKey<QRefBookAttachFileType> refBookAttachFileTypePk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QDeclarationDataFile> _declDataFileTypeIdFk = createInvForeignKey(id, "FILE_TYPE_ID");

    public QRefBookAttachFileType(String variable) {
        super(QRefBookAttachFileType.class, forVariable(variable), "NDFL_UNSTABLE", "REF_BOOK_ATTACH_FILE_TYPE");
        addMetadata();
    }

    public QRefBookAttachFileType(String variable, String schema, String table) {
        super(QRefBookAttachFileType.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QRefBookAttachFileType(Path<? extends QRefBookAttachFileType> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "REF_BOOK_ATTACH_FILE_TYPE");
        addMetadata();
    }

    public QRefBookAttachFileType(PathMetadata metadata) {
        super(QRefBookAttachFileType.class, metadata, "NDFL_UNSTABLE", "REF_BOOK_ATTACH_FILE_TYPE");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(code, ColumnMetadata.named("CODE").withIndex(2).ofType(Types.DECIMAL).withSize(1).notNull());
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(name, ColumnMetadata.named("NAME").withIndex(3).ofType(Types.VARCHAR).withSize(255).notNull());
    }

}

