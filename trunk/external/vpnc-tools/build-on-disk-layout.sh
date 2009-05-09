#!/bin/bash

# this is only run on the buildhost.
# probably wont be used later.

export TOP=/home/android/mydroid/

ORG="org.codeandroid.vpnc"
FILENAME=get-a-robot-vpnc.`date '+%d-%m-%y'`
TMPDIR=`mktemp -d`
BUILDDIR=`pwd`

ROOT="$TMPDIR/data/data/$ORG"
BINDIR="$ROOT/bin"
CONFDIR="$ROOT/etc/vpnc"

mkdir -p $BINDIR 
mkdir -p $CONFDIR 

cp        $TOP/out/target/product/generic/system/bin/vpnc \
  	  $TOP/out/target/product/generic/system/bin/make-tun-device \
	  $BINDIR

cp 	  $TOP/external/vpnc/vpnc.conf \
	  $TOP/external/vpnc-tools/vpnc-script \
	  $CONFDIR 

cp 	  $TOP/external/vpnc-tools/bb $BINDIR
ln -s	  $BINDIR/bb $BINDIR/ifconfig
ln -s 	  $BINDIR/bb $BINDIR/route 

sync 
cd $TMPDIR
tar  -jcvf "$BUILDDIR/$FILENAME.tar.bz2" .

