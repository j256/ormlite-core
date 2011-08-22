#!/bin/sh
#
# Release script for ORMLite
#

LOCAL_DIR="$HOME/svn/local/ormlite"
CORE_DIR=$LOCAL_DIR/ormlite-core
JDBC_DIR=$LOCAL_DIR/ormlite-jdbc
ANDROID_DIR=$LOCAL_DIR/ormlite-android

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

release=`grep version pom.xml | grep SNAPSHOT | head -1 | cut -f2 -d\> | cut -f1 -d\-`

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

GPG_ARGS="-Darguments=-Dgpg.passphrase=$gpgpass -Dgpg.passphrase=$gpgpass"

tmp="/tmp/release.sh.$$.t"
touch $tmp
gpg --passphrase $gpgpass -s -u D3412AC1 $tmp > /dev/null 2>&1
if [ $? -ne 0 ]; then
    echo "Passphrase incorrect"
    exit 1
fi
rm -f $tmp*

#############################################################

echo ""
echo "------------------------------------------------------- "
echo "Releasing version '$release'"
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
echo -n "Should we release -core to sonatype [y]: "
read cont
if [ "$cont" = "" ]; then
    cd $CORE_DIR
    svn -m cp delete https://ormlite.svn.sourceforge.net/svnroot/ormlite/ormlite-core/tags/ormlite-core-$release
    mvn -P st release:clean || exit 1
    mvn $GPG_ARGS -P st release:prepare || exit 1
    mvn $GPG_ARGS -P st release:perform || exit 1

    echo ""
    echo ""
fi

echo -n "Should we install -core locally [y]: "
read cont
if [ "$cont" = "" ]; then
    cd target/checkout
    mvn $GPG_ARGS install || exit 1
fi

#############################################################
# releasing jdbc to sonatype

echo ""
echo ""
echo -n "Should we release -jdbc to sonatype [y]: "
read cont
if [ "$cont" = "" ]; then
    cd $JDBC_DIR
    svn -m cp delete https://ormlite.svn.sourceforge.net/svnroot/ormlite/ormlite-jdbc/tags/ormlite-jdbc-$release
    mvn -P st release:clean || exit 1
    mvn $GPG_ARGS -Dormlite-version=$release -P st release:prepare || exit 1
    mvn $GPG_ARGS -P st release:perform || exit 1
fi

#############################################################
# releasing android to sonatype

echo ""
echo ""
echo -n "Should we release -android to sonatype [y]: "
read cont
if [ "$cont" = "" ]; then
    cd $ANDROID_DIR
    svn -m cp delete https://ormlite.svn.sourceforge.net/svnroot/ormlite/ormlite-jdbc/tags/ormlite-android-$release
    mvn -P st release:clean || exit 1
    mvn $GPG_ARGS -Dormlite-version=$release -P st release:prepare || exit 1
    mvn $GPG_ARGS -P st release:perform || exit 1
fi

#############################################################
# releasing all to sourceforge

echo ""
echo ""
echo -n "Should we release -core to sourceforge [y]: "
read cont
if [ "$cont" = "" ]; then
    cd $CORE_DIR/target/checkout
    mvn $GPG_ARGS -P sf deploy
fi

echo ""
echo ""
echo -n "Should we release -jdbc to sourceforge [y]: "
read cont
if [ "$cont" = "" ]; then
    cd $JDBC_DIR/target/checkout
    mvn $GPG_ARGS -P sf deploy
fi

echo ""
echo ""
echo -n "Should we release -android to sourceforge [y]: "
read cont
if [ "$cont" = "" ]; then
    cd $ANDROID_DIR/target/checkout
    mvn $GPG_ARGS -P sf deploy
fi

#############################################################
# run mvn eclipse/eclipse in local

cd $LOCAL_DIR
mvn -DdownloadSources=true -DdownloadJavadocs=true eclipse:eclipse
