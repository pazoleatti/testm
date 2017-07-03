package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QXdb$choiceModel is a Querydsl query type for QXdb$choiceModel
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QXdb$choiceModel extends com.querydsl.sql.RelationalPathBase<QXdb$choiceModel> {

    private static final long serialVersionUID = 1741319837;

    public static final QXdb$choiceModel xdb$choiceModel = new QXdb$choiceModel("XDB$CHOICE_MODEL");

    public final SimplePath<Object> sysNcRowinfo$ = createSimple("sysNcRowinfo$", Object.class);

    public QXdb$choiceModel(String variable) {
        super(QXdb$choiceModel.class, forVariable(variable), "XDB", "XDB$CHOICE_MODEL");
        addMetadata();
    }

    public QXdb$choiceModel(String variable, String schema, String table) {
        super(QXdb$choiceModel.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QXdb$choiceModel(Path<? extends QXdb$choiceModel> path) {
        super(path.getType(), path.getMetadata(), "XDB", "XDB$CHOICE_MODEL");
        addMetadata();
    }

    public QXdb$choiceModel(PathMetadata metadata) {
        super(QXdb$choiceModel.class, metadata, "XDB", "XDB$CHOICE_MODEL");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(sysNcRowinfo$, ColumnMetadata.named("SYS_NC_ROWINFO$").withIndex(1).ofType(2007).withSize(2000));
    }

}

