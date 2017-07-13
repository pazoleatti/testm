package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QFormKind is a Querydsl query type for QFormKind
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QFormKind extends com.querydsl.sql.RelationalPathBase<QFormKind> {

    private static final long serialVersionUID = 493992597;

    public static final QFormKind formKind = new QFormKind("FORM_KIND");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath name = createString("name");

    public final com.querydsl.sql.PrimaryKey<QFormKind> formKindPk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QFormData> _formDataFkKind = createInvForeignKey(id, "KIND");

    public final com.querydsl.sql.ForeignKey<QDepartmentFormType> _deptFormTypeFkKind = createInvForeignKey(id, "KIND");

    public final com.querydsl.sql.ForeignKey<QLogSystem> _logSystemFkKind = createInvForeignKey(id, "FORM_KIND_ID");

    public QFormKind(String variable) {
        super(QFormKind.class, forVariable(variable), "NDFL_UNSTABLE", "FORM_KIND");
        addMetadata();
    }

    public QFormKind(String variable, String schema, String table) {
        super(QFormKind.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QFormKind(Path<? extends QFormKind> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "FORM_KIND");
        addMetadata();
    }

    public QFormKind(PathMetadata metadata) {
        super(QFormKind.class, metadata, "NDFL_UNSTABLE", "FORM_KIND");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(name, ColumnMetadata.named("NAME").withIndex(2).ofType(Types.VARCHAR).withSize(100).notNull());
    }

}

