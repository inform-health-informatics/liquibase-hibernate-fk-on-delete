package liquibase.ext.hibernate.database;

import liquibase.CatalogAndSchema;
import liquibase.database.Database;
import liquibase.integration.commandline.CommandLineUtils;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.SnapshotControl;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.structure.core.ForeignKey;
import liquibase.structure.core.ForeignKeyConstraintType;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Table;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class ForeignKeyRuleTest {
    DatabaseSnapshot snapshot;

    @Before
    public void setup() throws Exception {
        // Using Liquibase-hibernate classes
        String url = "hibernate:ejb3:auction";
        Database database = CommandLineUtils.createDatabaseObject(this.getClass().getClassLoader(), url, null, null, null, null, null, false, false, null, null, null, null, null, null, null);
        snapshot = SnapshotGeneratorFactory.getInstance().createSnapshot(CatalogAndSchema.DEFAULT, database, new SnapshotControl(database));
    }

    /**
     * Given that the watcher table has a ManyToOne relationship with @OnDelete(action = OnDeleteAction.CASCADE)
     * Foreign key delete rule should be liquibase importedKeyCascade
     */
    @Test
    public void testForeignKeyOnDeleteCascade() {
        Table watcherTable = (Table) snapshot.get(new Table().setName("watcher").setSchema(new Schema()));

        // not sure why but contains duplicates for foreign keys
        List<ForeignKey> foreignKeys = watcherTable.getOutgoingForeignKeys().stream().distinct().collect(Collectors.toList());
        assertEquals(1, foreignKeys.size());
        assertEquals(ForeignKeyConstraintType.importedKeyCascade, foreignKeys.get(0).getDeleteRule());

    }

    /**
     * Given that the auctionitem table has two ManyToOne relationships with @OnDelete(action = OnDeleteAction.NO_ACTION)
     * Foreign key delete rule should be null
     */
    @Test
    public void testForeignKeyOnDeleteNoAction() {
        Table auctionItemTable = (Table) snapshot.get(new Table().setName("auctionitem").setSchema(new Schema()));
        List<ForeignKey> foreignKeys = auctionItemTable.getOutgoingForeignKeys().stream().distinct().collect(Collectors.toList());
        // not sure why but contains duplicates for foreign keys
        for (ForeignKey foreignKey : foreignKeys) {
            ForeignKeyConstraintType deleteRule = foreignKey.getDeleteRule();
            assertEquals(null, foreignKey.getDeleteRule());
        }
    }
}
