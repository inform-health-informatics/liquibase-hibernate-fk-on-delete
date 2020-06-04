package liquibase.ext.hibernate.diff;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.core.HsqlDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.diff.DiffResult;
import liquibase.diff.ObjectDifferences;
import liquibase.diff.compare.CompareControl;
import liquibase.exception.DatabaseException;
import liquibase.ext.hibernate.database.HibernateSpringPackageDatabase;
import liquibase.ext.hibernate.database.connection.HibernateConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Column;
import liquibase.structure.core.ForeignKey;
import liquibase.structure.core.Index;
import liquibase.structure.core.PrimaryKey;
import liquibase.structure.core.Sequence;
import liquibase.structure.core.Table;
import liquibase.structure.core.UniqueConstraint;
import org.hibernate.dialect.HSQLDialect;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static junit.framework.TestCase.assertTrue;

public class ForeignKeyDiffOnDeleteTest {
    private Database database;
    private Connection connection;
    private CompareControl compareControl;


    @Before
    public void setUp() throws Exception {
        Class.forName("org.hsqldb.jdbc.JDBCDriver");
        connection = DriverManager.getConnection("jdbc:hsqldb:mem:TESTDB" + System.currentTimeMillis(), "SA", "");
        database = new HsqlDatabase();
        database.setConnection(new JdbcConnection(connection));

        Set<Class<? extends DatabaseObject>> typesToInclude = new HashSet<Class<? extends DatabaseObject>>();
        typesToInclude.add(Table.class);
        typesToInclude.add(Column.class);
        typesToInclude.add(PrimaryKey.class);
        typesToInclude.add(ForeignKey.class);
        typesToInclude.add(UniqueConstraint.class);
        typesToInclude.add(Sequence.class);
        compareControl = new CompareControl(typesToInclude);
        compareControl.addSuppressedField(Table.class, "remarks");
        compareControl.addSuppressedField(Column.class, "remarks");
        compareControl.addSuppressedField(Column.class, "certainDataType");
        compareControl.addSuppressedField(Column.class, "autoIncrementInformation");
        compareControl.addSuppressedField(ForeignKey.class, "deleteRule");
        compareControl.addSuppressedField(ForeignKey.class, "updateRule");
        compareControl.addSuppressedField(Index.class, "unique");
    }

    @After
    public void tearDown() throws Exception {
        database.close();
        connection = null;
        database = null;
        compareControl = null;
    }

    private static Database createHibernateDatabase(String modelsReference) throws DatabaseException {
        Database originalHibernateDatabase = new HibernateSpringPackageDatabase();
        originalHibernateDatabase.setDefaultSchemaName("PUBLIC");
        originalHibernateDatabase.setDefaultCatalogName("TESTDB");
        originalHibernateDatabase.setConnection(new JdbcConnection(new HibernateConnection("hibernate:spring:" + modelsReference + "?dialect=" + HSQLDialect.class.getName(), new ClassLoaderResourceAccessor())));
        return originalHibernateDatabase;
    }

    @Test
    public void foreignKeyOnDeleteDifferenceFound() throws Exception {
        Database originalHibernateDatabase = createHibernateDatabase("com.example.ejb3.auction");
        Database fkHibernateDatabase = createHibernateDatabase("com.example.ejb3.fk");

        Liquibase liquibase = new Liquibase((String) null, new ClassLoaderResourceAccessor(), database);
        DiffResult diff = liquibase.diff(originalHibernateDatabase, fkHibernateDatabase, compareControl);
        Map<DatabaseObject, ObjectDifferences> changes = diff.getChangedObjects();

        assertTrue(changes.size() > 0);
    }
}