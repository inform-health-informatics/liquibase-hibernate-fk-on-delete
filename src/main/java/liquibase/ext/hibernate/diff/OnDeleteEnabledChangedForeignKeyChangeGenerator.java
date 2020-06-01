package liquibase.ext.hibernate.diff;


import liquibase.change.Change;
import liquibase.change.core.AddForeignKeyConstraintChange;
import liquibase.database.Database;
import liquibase.diff.Difference;
import liquibase.diff.ObjectDifferences;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.ChangeGeneratorChain;
import liquibase.ext.hibernate.database.HibernateDatabase;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.ForeignKeyConstraintType;


/**
 * Hibernate doesn't know about all the variations that occur with foreign keys but just whether the FK exists or not.
 * To prevent changing customized foreign keys, all foreign key changes from hibernate are suppressed, except for on delete.
 */
public class OnDeleteEnabledChangedForeignKeyChangeGenerator extends ChangedForeignKeyChangeGenerator {

    @Override
    public Change[] fixChanged(DatabaseObject changedObject, ObjectDifferences differences, DiffOutputControl control,
                               Database referenceDatabase, Database comparisonDatabase, ChangeGeneratorChain chain) {
        if (referenceDatabase instanceof HibernateDatabase || comparisonDatabase instanceof HibernateDatabase) {

            // original implementation returns changes without dealing with deleteRule or UpdateRole
            Change[] changes = super.fixChanged(changedObject, differences, control, referenceDatabase, comparisonDatabase, chain);

            // if there are changes, first would need to be create fk, second could be delete or update rule
            if (changes != null && changes.length == 2) {
                AddForeignKeyConstraintChange addFkChange = (AddForeignKeyConstraintChange) changes[1];
                updateForeinKeyRule("deleteRule", differences, addFkChange);
                updateForeinKeyRule("updateRule", differences, addFkChange);
            }

            if (!differences.hasDifferences()) {
                return null;
            }
            return changes;
        }

        return super.fixChanged(changedObject, differences, control, referenceDatabase, comparisonDatabase, chain);
    }

    /**
     * Update the foreign key rule for deleteRule or updateRule.
     * @param rule deleteRule or updateRule
     * @param differences object differences
     * @param addFkChange foreign key constraint change
     */
    private void updateForeinKeyRule(String rule, ObjectDifferences differences, AddForeignKeyConstraintChange addFkChange) {
        if (differences.isDifferent(rule)) {
            Difference deleteRule = differences.getDifference(rule);
            // compare rules
            ForeignKeyConstraintType hibernateFkAction = (ForeignKeyConstraintType) deleteRule.getReferenceValue();
            ForeignKeyConstraintType actualFkAction = (ForeignKeyConstraintType) deleteRule.getComparedValue();
            if (hibernateFkAction == null || hibernateFkAction == ForeignKeyConstraintType.importedKeyNoAction) {
                if (actualFkAction != null && actualFkAction != ForeignKeyConstraintType.importedKeyNoAction) {
                    // changed from onDelete to doNothing, most likely added ondelete by hand
                    differences.removeDifference(rule);
                } else {
                    // no real change -> remove it
                    differences.removeDifference(rule);
                }
            } else {
                addFkChange.setOnDelete(hibernateFkAction);
            }
        }
    }
}
