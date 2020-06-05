package liquibase.ext.hibernate;

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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasToString;

public class ForeignKeyOnDeleteDiffTest {
    private Database database;
    private CompareControl compareControl;
    private Database fkOnDeleteNoActionDatabase;
    private Database fkOnDeleteCascadeAction;


    @Before
    public void setUp() throws Exception {
        Class.forName("org.hsqldb.jdbc.JDBCDriver");
        Connection connection = DriverManager.getConnection("jdbc:hsqldb:mem:TESTDB" + System.currentTimeMillis(), "SA", "");
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

        fkOnDeleteNoActionDatabase = createHibernateDatabase("com.example.ejb3.auction");
        fkOnDeleteCascadeAction = createHibernateDatabase("com.example.ejb3.fk");
    }

    @After
    public void tearDown() throws Exception {
        database.close();
    }

    private static Database createHibernateDatabase(String modelsReference) throws DatabaseException {
        Database originalHibernateDatabase = new HibernateSpringPackageDatabase();
        originalHibernateDatabase.setDefaultSchemaName("PUBLIC");
        originalHibernateDatabase.setDefaultCatalogName("TESTDB");
        originalHibernateDatabase.setConnection(new JdbcConnection(new HibernateConnection("hibernate:spring:" + modelsReference + "?dialect=" + HSQLDialect.class.getName(), new ClassLoaderResourceAccessor())));
        return originalHibernateDatabase;
    }

    @Test
    public void foreignKeyOnDeleteAdded() throws Exception {
        Liquibase liquibase = new Liquibase((String) null, new ClassLoaderResourceAccessor(), database);
        DiffResult diffResult = liquibase.diff(fkOnDeleteNoActionDatabase, fkOnDeleteCascadeAction, compareControl);
        Map<DatabaseObject, ObjectDifferences> changes = diffResult.getChangedObjects();

        assertTrue(changes.size() > 0);
        ObjectDifferences differences = (ObjectDifferences) changes.values().toArray()[0];

        assertThat(differences,
                hasProperty("differences",
                        hasToString("[deleteRule changed from 'null' to 'importedKeyCascade']")));
    }

    @Test
    public void foreignKeyOnDeleteRemoved() throws Exception {
        Liquibase liquibase = new Liquibase((String) null, new ClassLoaderResourceAccessor(), database);
        DiffResult diffResult = liquibase.diff(fkOnDeleteCascadeAction, fkOnDeleteNoActionDatabase, compareControl);
        Map<DatabaseObject, ObjectDifferences> changes = diffResult.getChangedObjects();

        assertTrue(changes.size() > 0);
        ObjectDifferences differences = (ObjectDifferences) changes.values().toArray()[0];

        assertThat(differences,
                hasProperty("differences",
                        hasToString("[deleteRule changed from 'importedKeyCascade' to 'null']")));
    }
}