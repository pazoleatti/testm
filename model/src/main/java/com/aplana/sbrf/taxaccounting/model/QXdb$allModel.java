package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QXdb$allModel is a Querydsl query type for QXdb$allModel
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QXdb$allModel extends com.querydsl.sql.RelationalPathBase<QXdb$allModel> {

    private static final long serialVersionUID = 1650088339;

    public static final QXdb$allModel xdb$allModel = new QXdb$allModel("XDB$ALL_MODEL");

    public final SimplePath<Object> sysNcRowinfo$ = createSimple("sysNcRowinfo$", Object.class);

    public QXdb$allModel(String variable) {
        super(QXdb$allModel.class, forVariable(variable), "XDB", "XDB$ALL_MODEL");
        addMetadata();
    }

    public QXdb$allModel(String variable, String schema, String table) {
        super(QXdb$allModel.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QXdb$allModel(Path<? extends QXdb$allModel> path) {
        super(path.getType(), path.getMetadata(), "XDB", "XDB$ALL_MODEL");
        addMetadata();
    }

    public QXdb$allModel(PathMetadata metadata) {
        super(QXdb$allModel.class, metadata, "XDB", "XDB$ALL_MODEL");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(sysNcRowinfo$, ColumnMetadata.named("SYS_NC_ROWINFO$").withIndex(1).ofType(2007).withSize(2000));
    }

}

