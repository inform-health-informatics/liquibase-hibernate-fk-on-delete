# liquibase-hibernate-fk-on-delete
Allow foreign key on delete cascade to be generated using liquibase hibernate 

This is project takes liquibase-hibernate plugin and adds in functionality to allow foreign key rule: on delete = cascade
After installing the project using maven, it can be used as liquibase-hibernate and any on delete cascades will
be added to the changelogs.

Currently hibernate only has no action or cascade as OnDeleteActions. 
Because of this, only when adding a new foreign key with an `@OnDelete(action = OnDeleteAction.CASCADE)` 
annotation will result in a change to the foreign key in the changelog. Existing foreign keys will never have their
on delete rule altered, and changes to these should be created manually in the changelog.

## Installation

Currently this project should be downloaded and installed locally, and then added as a dependency to your maven `pom.xml` 

```shell script
git clone https://github.com/inform-health-informatics/liquibase-hibernate-fk-on-delete.git
cd liquibase-hibernate-fk-on-delete
mvn install
```

## Use within the liquibase-maven-plugin

This library has been set up to replace liquibase-hibernate5, 
and can be used as a dependency for the liquibase-maven-plugin.

If your project's `pom.xml` currently has the plugin and liquibase-hibernate5 (below)
```xml
...
    <build>
        <plugins>
            <plugin>
                <groupId>org.liquibase</groupId>
				<artifactId>liquibase-maven-plugin</artifactId>
				...
				<dependencies>
					<dependency>
						<groupId>org.liquibase.ext</groupId>
						<artifactId>liquibase-hibernate5</artifactId>
						<version>${liquibase-hibernate5.version}</version>
					</dependency>
                ...
```

Then replace liquibase-hibernate5 with liquibase-hibernate5-fk-ondelete, 

```xml
...
    <build>
        <plugins>
            <plugin>
                <groupId>org.liquibase</groupId>
				<artifactId>liquibase-maven-plugin</artifactId>
				...
				<dependencies>
                    <dependency>
                        <groupId>uk.ac.ucl.rits.inform</groupId>
                        <artifactId>liquibase-hibernate5-fk-ondelete</artifactId>
                        <version>${liquibase-hibernate5-fk-ondelete.version}</version>
					</dependency>
                ...
```

You can now use the maven goal as usual, after setting up your liquibase properties. 

```shell script
mvn liquibase:generateChangeLog
```