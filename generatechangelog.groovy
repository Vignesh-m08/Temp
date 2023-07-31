import groovy.xml.StreamingMarkupBuilder

def generateChangesetSql(sqlFilePath) {
    def sqlContent = new File(sqlFilePath).text

    def changeset = new StreamingMarkupBuilder().bind {
        changeset(id: 'unique_id') {
            comment("SQL migration from file: ${sqlFilePath}")
            sql(sqlContent)
        }
    }

    return changeset
}

def updateChangelog(sqlDirectory, changelogFile) {
    def root = new StreamingMarkupBuilder().bind {
        mkp.declareNamespace('': 'http://www.liquibase.org/xml/ns/dbchangelog')
        mkp.yieldUnescaped('databaseChangeLog') {
            def changelogFileLastModified = new File(changelogFile).lastModified()

            for (filename in new File(sqlDirectory).list()) {
                if (filename.endsWith('.sql')) {
                    def sqlFilePath = "${sqlDirectory}/${filename}"
                    def sqlFileLastModified = new File(sqlFilePath).lastModified()

                    if (sqlFileLastModified > changelogFileLastModified) {
                        def changeset = generateChangesetSql(sqlFilePath)
                        mkp.yieldUnescaped(changeset)
                    }
                }
            }
        }
    }

    new File(changelogFile).text = root
}

// Call the function to update the changelog.xml
def sqlDirectoryPath = '/path/to/sql/files'
def changelogFilePath = '/path/to/changelog.xml'

updateChangelog(sqlDirectoryPath, changelogFilePath)
