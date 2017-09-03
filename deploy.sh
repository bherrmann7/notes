lein clean
lein pom
mvn dependency:copy-dependencies -DoutputDirectory=target/war/WEB-INF/lib
mkdir -p target/war/WEB-INF/classes
cp -R resources/public target/war/WEB-INF/classes/
cp -R src/* config/* target/war/WEB-INF/classes
cp web.xml target/war/WEB-INF/
jar cf notes.war -C target/war .
scp notes.war jadn.com:/tmp/notes.war
ssh jadn.com mv /tmp/notes.war /jadn/notes.war
