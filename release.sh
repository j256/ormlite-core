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
#    exit 1
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

echo ""
echo -n "Enter the GPG pass-phrase: "
read gpgpass

tmp="/tmp/$0.$$.t"
touch $tmp 
gpg --passphrase $gpgpass -s -u D3412AC1 $tmp > /dev/null 2>&1 || exit 1 
rm -f $tmp*

#############################################################

echo ""
echo "------------------------------------------------------- "
echo "Releasing version '$release' with pass-phrase '$gpgpass'"
sleep 3

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
# making docs

cd $CORE_DIR/src/main/doc/
make install
cd ..
svn commit -m "checking in $release docs"

#############################################################
# releasing core to sonatype

echo ""
echo ""
echo -n "Releasing -core to sonatype: "
read cont
cd $CORE_DIR
svn -m cp delete https://ormlite.svn.sourceforge.net/svnroot/ormlite/ormlite-core/tags/ormlite-core-$release
mvn -P st release:clean || exit 1
mvn -Dgpg.passphrase=$gpgpass -P st release:prepare || exit 1
mvn -Dgpg.passphrase=$gpgpass -P st release:perform || exit 1

echo ""
echo ""
echo -n "Installing -core locally: "
read cont
cd target/checkout
mvn -Dgpg.passphrase=$gpgpass install || exit 1

#############################################################
# releasing jdbc to sonatype

echo ""
echo ""
echo -n "Releasing -jdbc to sonatype: "
cd $JDBC_DIR
svn -m cp delete https://ormlite.svn.sourceforge.net/svnroot/ormlite/ormlite-jdbc/tags/ormlite-jdbc-$release
mvn -P st release:clean || exit 1
mvn -Dgpg.passphrase=$gpgpass -Dormlite-version=$release -P st release:prepare || exit 1
mvn -Dgpg.passphrase=$gpgpass -P st release:perform || exit 1

#############################################################
# releasing android to sonatype

echo ""
echo ""
echo -n "Releasing -android to sonatype: "
cd $ANDROID_DIR
svn -m cp delete https://ormlite.svn.sourceforge.net/svnroot/ormlite/ormlite-jdbc/tags/ormlite-android-$release
mvn -P st release:clean || exit 1
mvn -Dgpg.passphrase=$gpgpass -Dormlite-version=$release -P st release:prepare || exit 1
mvn -Dgpg.passphrase=$gpgpass -P st release:perform || exit 1

#############################################################
# releasing all to sourceforge

echo ""
echo ""
echo -n "Releasing -core to sourceforge: "
cd $CORE_DIR/target/checkout
mvn -Dgpg.passphrase=$gpgpass -P sf deploy

echo ""
echo ""
echo -n "Releasing -jdbc to sourceforge: "
cd $JDBC_DIR/target/checkout
mvn -Dgpg.passphrase=$gpgpass -P sf deploy

echo ""
echo ""
echo -n "Releasing -android to sourceforge: "
cd $ANDROID_DIR/target/checkout
mvn -Dgpg.passphrase=$gpgpass -P sf deploy

#############################################################
# run mvn eclipse/eclipse in local

cd $LOCAL_DIR
mvn -DdownloadSources=true -DdownloadJavadocs=true eclipse:eclipse
