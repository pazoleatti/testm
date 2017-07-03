package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QSdoFeatureUsage is a Querydsl query type for QSdoFeatureUsage
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QSdoFeatureUsage extends com.querydsl.sql.RelationalPathBase<QSdoFeatureUsage> {

    private static final long serialVersionUID = 576468204;

    public static final QSdoFeatureUsage sdoFeatureUsage = new QSdoFeatureUsage("SDO_FEATURE_USAGE");

    public final StringPath featureName = createString("featureName");

    public final StringPath used = createString("used");

    public final com.querydsl.sql.PrimaryKey<QSdoFeatureUsage> sysC005576 = createPrimaryKey(featureName);

    public QSdoFeatureUsage(String variable) {
        super(QSdoFeatureUsage.class, forVariable(variable), "MDSYS", "SDO_FEATURE_USAGE");
        addMetadata();
    }

    public QSdoFeatureUsage(String variable, String schema, String table) {
        super(QSdoFeatureUsage.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QSdoFeatureUsage(Path<? extends QSdoFeatureUsage> path) {
        super(path.getType(), path.getMetadata(), "MDSYS", "SDO_FEATURE_USAGE");
        addMetadata();
    }

    public QSdoFeatureUsage(PathMetadata metadata) {
        super(QSdoFeatureUsage.class, metadata, "MDSYS", "SDO_FEATURE_USAGE");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(featureName, ColumnMetadata.named("FEATURE_NAME").withIndex(1).ofType(Types.VARCHAR).withSize(24).notNull());
        addMetadata(used, ColumnMetadata.named("USED").withIndex(2).ofType(Types.VARCHAR).withSize(1));
    }

}

