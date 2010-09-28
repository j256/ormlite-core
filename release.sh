#!/bin/sh

LOCAL_DIR="$HOME/svn/local"
CORE_DIR=$LOCAL_DIR/ormlite-core
JDBC_DIR=$LOCAL_DIR/ormlite-jdbc
ANDROID_DIR=$LOCAL_DIR/ormlite-android

release=`grep version pom.xml | grep SNAPSHOT | head -1 | cut -f2 -d\> | cut -f1 -d\-`

#############################################################
# check for not commited files:

cd $CORE_DIR
if [ "`svn stat`" != "" ]; then
    echo "Files not checked-in inside -core"
    svn stat
    exit 1
fi
cd $JDBC_DIR
if [ "`svn stat`" != "" ]; then
    echo "Files not checked-in inside -jdbc"
    svn stat
    exit 1
fi
cd $ANDROID_DIR
if [ "`svn stat`" != "" ]; then
    echo "Files not checked-in inside -android"
    svn stat
    exit 1
fi

#############################################################
# run tests

cd $CORE_DIR
mvn test || exit 1
#cd $JDBC_DIR
#mvn test || exit 1
#cd $ANDROID_DIR
#mvn test || exit 1

#############################################################

echo ""
echo ""
echo ""
echo "------------------------------------------------------- "
echo -n "Enter release number [$release]: "
read rel
if [ "$rel" != "" ]; then
    release=$rel
fi

#############################################################

echo ""
echo "Releasing version '$release'"
sleep 1

#############################################################
# check docs:

cd $CORE_DIR
ver=`head -1 src/main/javadoc/doc-files/changelog.txt | cut -f1 -d:`
if [ "$release" != "$ver" ]; then
    echo "Change log top line version seems wrong:"
    head -1 src/main/javadoc/doc-files/changelog.txt
    exit 1
fi

ver=`grep '^@set ormlite_version' src/main/doc/ormlite.texi | cut -f3 -d' '`
if [ "$release" != "$ver" ]; then
    echo "ormlite.texi version seems wrong:"
    grep '^@set ormlite_version' src/main/doc/ormlite.texi
    exit 1
fi

#############################################################

cd $CORE_DIR
svn -m cp delete https://ormlite.svn.sourceforge.net/svnroot/ormlite/ormlite-core/tags/ormlite-core-$release
mvn -P st release:clean || exit 1
mvn -P st release:prepare || exit 1
mvn -P st release:perform || exit 1

cd target/checkout
mvn install || exit 1

#############################################################

cd $JDBC_DIR
svn -m cp delete https://ormlite.svn.sourceforge.net/svnroot/ormlite/ormlite-jdbc/tags/ormlite-jdbc-$release
mvn -P st release:clean || exit 1
mvn -Dormlite-version=$release -P st release:prepare || exit 1
mvn -P st release:perform || exit 1

#############################################################

cd $ANDROID_DIR
svn -m cp delete https://ormlite.svn.sourceforge.net/svnroot/ormlite/ormlite-jdbc/tags/ormlite-android-$release
mvn -P st release:clean || exit 1
mvn -Dormlite-version=$release -P st release:prepare || exit 1
mvn -P st release:perform || exit 1

#############################################################

cd $CORE_DIR/target/checkout
mvn -P sf deploy
cd $JDBC_DIR/target/checkout
mvn -P sf deploy
cd $ANDROID_DIR/target/checkout
mvn -P sf deploy
