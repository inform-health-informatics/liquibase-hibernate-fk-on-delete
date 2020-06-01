package liquibase.ext.hibernate.snapshot;

import liquibase.diff.compare.DatabaseObjectComparatorFactory;
import liquibase.exception.DatabaseException;
import liquibase.ext.hibernate.database.HibernateDatabase;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.InvalidExampleException;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.ForeignKey;
import liquibase.structure.core.ForeignKeyConstraintType;
import liquibase.structure.core.Table;
import org.hibernate.boot.spi.MetadataImplementor;

import java.util.Collection;
import java.util.Iterator;

/**
 * Original ForeignKey Snapshot Generator, enabling onDelete Cascade.
 */
public class OnDeleteEnabledForeignKeySnapshotGenerator extends ForeignKeySnapshotGenerator {

    @Override
    protected void addTo(DatabaseObject foundObject, DatabaseSnapshot snapshot) throws DatabaseException, InvalidExampleException {
        // original implementation
        if (!snapshot.getSnapshotControl().shouldInclude(ForeignKey.class)) {
            return;
        }
        if (foundObject instanceof Table) {
            Table table = (Table) foundObject;
            HibernateDatabase database = (HibernateDatabase) snapshot.getDatabase();
            MetadataImplementor metadata = (MetadataImplementor) database.getMetadata();

            Collection<org.hibernate.mapping.Table> tmapp = metadata.collectTableMappings();
            Iterator<org.hibernate.mapping.Table> tableMappings = tmapp.iterator();
            while (tableMappings.hasNext()) {
                org.hibernate.mapping.Table hibernateTable = (org.hibernate.mapping.Table) tableMappings.next();
                Iterator fkIterator = hibernateTable.getForeignKeyIterator();
                while (fkIterator.hasNext()) {
                    org.hibernate.mapping.ForeignKey hibernateForeignKey = (org.hibernate.mapping.ForeignKey) fkIterator.next();
                    Table currentTable = new Table().setName(hibernateTable.getName());
                    currentTable.setSchema(hibernateTable.getCatalog(), hibernateTable.getSchema());

                    org.hibernate.mapping.Table hibernateReferencedTable = hibernateForeignKey.getReferencedTable();
                    Table referencedTable = new Table().setName(hibernateReferencedTable.getName());
                    referencedTable.setSchema(hibernateReferencedTable.getCatalog(), hibernateReferencedTable.getSchema());

                    if (hibernateForeignKey.isPhysicalConstraint()) {
                        ForeignKey fk = new ForeignKey();
                        fk.setName(hibernateForeignKey.getName());
                        fk.setPrimaryKeyTable(referencedTable);
                        fk.setForeignKeyTable(currentTable);
                        for (Object column : hibernateForeignKey.getColumns()) {
                            fk.addForeignKeyColumn(new liquibase.structure.core.Column(((org.hibernate.mapping.Column) column).getName()));
                        }
                        for (Object column : hibernateForeignKey.getReferencedColumns()) {
                            fk.addPrimaryKeyColumn(new liquibase.structure.core.Column(((org.hibernate.mapping.Column) column).getName()));
                        }
                        if (fk.getPrimaryKeyColumns() == null || fk.getPrimaryKeyColumns().isEmpty()) {
                            for (Object column : hibernateReferencedTable.getPrimaryKey().getColumns()) {
                                fk.addPrimaryKeyColumn(new liquibase.structure.core.Column(((org.hibernate.mapping.Column) column).getName()));
                            }
                        }

                        // Add in cascade delete
                        // TODO: also add in all other variants of onDelete or not worth it?
                        // TODO: also add in on update?
                        if (hibernateForeignKey.isCascadeDeleteEnabled()) {
                            fk.setDeleteRule(ForeignKeyConstraintType.importedKeyCascade);
                        }

                        fk.setDeferrable(false);
                        fk.setInitiallyDeferred(false);


                        if (DatabaseObjectComparatorFactory.getInstance().isSameObject(currentTable, table, null, database)) {
                            table.getOutgoingForeignKeys().add(fk);
                            table.getSchema().addDatabaseObject(fk);
                        }
                    }
                }
            }
        }
    }

}
