package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QXdb$sequenceModel is a Querydsl query type for QXdb$sequenceModel
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QXdb$sequenceModel extends com.querydsl.sql.RelationalPathBase<QXdb$sequenceModel> {

    private static final long serialVersionUID = 1445713725;

    public static final QXdb$sequenceModel xdb$sequenceModel = new QXdb$sequenceModel("XDB$SEQUENCE_MODEL");

    public final SimplePath<Object> sysNcRowinfo$ = createSimple("sysNcRowinfo$", Object.class);

    public QXdb$sequenceModel(String variable) {
        super(QXdb$sequenceModel.class, forVariable(variable), "XDB", "XDB$SEQUENCE_MODEL");
        addMetadata();
    }

    public QXdb$sequenceModel(String variable, String schema, String table) {
        super(QXdb$sequenceModel.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QXdb$sequenceModel(Path<? extends QXdb$sequenceModel> path) {
        super(path.getType(), path.getMetadata(), "XDB", "XDB$SEQUENCE_MODEL");
        addMetadata();
    }

    public QXdb$sequenceModel(PathMetadata metadata) {
        super(QXdb$sequenceModel.class, metadata, "XDB", "XDB$SEQUENCE_MODEL");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(sysNcRowinfo$, ColumnMetadata.named("SYS_NC_ROWINFO$").withIndex(1).ofType(2007).withSize(2000));
    }

}

