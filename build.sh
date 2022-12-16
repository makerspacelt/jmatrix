mvn clean install
mkdir target/jar
mv target/jmatrix.jar target/jar/jmatrix.jar
jpackage --type app-image \
  --app-version 1.0.0 \
  --name jmatrix \
  --dest target/ \
  --add-modules java.base,java.desktop,java.net.http \
  --input target/jar/ \
  --main-class lt.makerspace.jmatrix.Main \
  --main-jar jmatrix.jar

