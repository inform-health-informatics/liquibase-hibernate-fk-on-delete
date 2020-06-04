# liquibase-hibernate-fk-on-delete
Allow foreign key on delete cascade to be generated using liquibase hibernate 

This is project takes liquibase-hibernate plugin and adds in functionality to allow foreign key rule: on delete = cascade
After installing the project using maven, it can be used as liquibase-hibernate and any on delete cascades will
be added to the changelogs.

Currently, 