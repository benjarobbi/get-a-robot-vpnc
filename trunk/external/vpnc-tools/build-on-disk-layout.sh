#!/bin/bash

# this is only run on the buildhost.
# probably wont be used later.

export TOP=/home/android/mydroid/

FILENAME=get-a-robot-vpnc.`date '+%d-%m-%y'`
TMPDIR=`mktemp -d`
BUILDDIR=`pwd`

mkdir -p $TMPDIR/etc/vpnc
mkdir -p $TMPDIR/system/bin/

cp        $TOP/out/target/product/generic/system/bin/vpnc \
  	  $TOP/out/target/product/generic/system/bin/make-tun-device \
	  $TMPDIR/system/bin

cp 	  $TOP/external/vpnc/vpnc.conf $TMPDIR/etc/vpnc
cp 	  $TOP/external/vpnc-tools/vpnc-script $TMPDIR/etc/vpnc

# ifconfig/route are from busybox.. if somone can build iproute2 i'll be happy
cp 	  $TOP/external/vpnc-tools/bb $TMPDIR/system/bin/
ln -s	  $TOP/external/vpnc-tools/bb $TMPDIR/system/bin/ifconfig
ln -s 	  $TOP/external/vpnc-tools/bb $TMPDIR/system/bin/route 

sync 
cd $TMPDIR
tar  -jcvf "$BUILDDIR/$FILENAME.tar.bz2" .



