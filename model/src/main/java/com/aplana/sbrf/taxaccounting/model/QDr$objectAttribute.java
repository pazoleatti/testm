package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QDr$objectAttribute is a Querydsl query type for QDr$objectAttribute
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QDr$objectAttribute extends com.querydsl.sql.RelationalPathBase<QDr$objectAttribute> {

    private static final long serialVersionUID = -2112224796;

    public static final QDr$objectAttribute dr$objectAttribute = new QDr$objectAttribute("DR$OBJECT_ATTRIBUTE");

    public final NumberPath<java.math.BigInteger> oatAttId = createNumber("oatAttId", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> oatClaId = createNumber("oatClaId", java.math.BigInteger.class);

    public final StringPath oatDatatype = createString("oatDatatype");

    public final StringPath oatDefault = createString("oatDefault");

    public final StringPath oatDesc = createString("oatDesc");

    public final NumberPath<java.math.BigInteger> oatId = createNumber("oatId", java.math.BigInteger.class);

    public final StringPath oatLov = createString("oatLov");

    public final StringPath oatName = createString("oatName");

    public final NumberPath<java.math.BigInteger> oatObjId = createNumber("oatObjId", java.math.BigInteger.class);

    public final StringPath oatRequired = createString("oatRequired");

    public final StringPath oatStatic = createString("oatStatic");

    public final StringPath oatSystem = createString("oatSystem");

    public final NumberPath<java.math.BigInteger> oatValMax = createNumber("oatValMax", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> oatValMin = createNumber("oatValMin", java.math.BigInteger.class);

    public final com.querydsl.sql.PrimaryKey<QDr$objectAttribute> drc$oatKey = createPrimaryKey(oatId);

    public QDr$objectAttribute(String variable) {
        super(QDr$objectAttribute.class, forVariable(variable), "CTXSYS", "DR$OBJECT_ATTRIBUTE");
        addMetadata();
    }

    public QDr$objectAttribute(String variable, String schema, String table) {
        super(QDr$objectAttribute.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QDr$objectAttribute(Path<? extends QDr$objectAttribute> path) {
        super(path.getType(), path.getMetadata(), "CTXSYS", "DR$OBJECT_ATTRIBUTE");
        addMetadata();
    }

    public QDr$objectAttribute(PathMetadata metadata) {
        super(QDr$objectAttribute.class, metadata, "CTXSYS", "DR$OBJECT_ATTRIBUTE");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(oatAttId, ColumnMetadata.named("OAT_ATT_ID").withIndex(4).ofType(Types.DECIMAL).withSize(22));
        addMetadata(oatClaId, ColumnMetadata.named("OAT_CLA_ID").withIndex(2).ofType(Types.DECIMAL).withSize(22));
        addMetadata(oatDatatype, ColumnMetadata.named("OAT_DATATYPE").withIndex(10).ofType(Types.CHAR).withSize(1));
        addMetadata(oatDefault, ColumnMetadata.named("OAT_DEFAULT").withIndex(11).ofType(Types.VARCHAR).withSize(500));
        addMetadata(oatDesc, ColumnMetadata.named("OAT_DESC").withIndex(6).ofType(Types.VARCHAR).withSize(80));
        addMetadata(oatId, ColumnMetadata.named("OAT_ID").withIndex(1).ofType(Types.DECIMAL).withSize(22).notNull());
        addMetadata(oatLov, ColumnMetadata.named("OAT_LOV").withIndex(14).ofType(Types.CHAR).withSize(1));
        addMetadata(oatName, ColumnMetadata.named("OAT_NAME").withIndex(5).ofType(Types.VARCHAR).withSize(30));
        addMetadata(oatObjId, ColumnMetadata.named("OAT_OBJ_ID").withIndex(3).ofType(Types.DECIMAL).withSize(22));
        addMetadata(oatRequired, ColumnMetadata.named("OAT_REQUIRED").withIndex(7).ofType(Types.CHAR).withSize(1));
        addMetadata(oatStatic, ColumnMetadata.named("OAT_STATIC").withIndex(9).ofType(Types.CHAR).withSize(1));
        addMetadata(oatSystem, ColumnMetadata.named("OAT_SYSTEM").withIndex(8).ofType(Types.CHAR).withSize(1));
        addMetadata(oatValMax, ColumnMetadata.named("OAT_VAL_MAX").withIndex(13).ofType(Types.DECIMAL).withSize(22));
        addMetadata(oatValMin, ColumnMetadata.named("OAT_VAL_MIN").withIndex(12).ofType(Types.DECIMAL).withSize(22));
    }

}

