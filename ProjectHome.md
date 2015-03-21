# Introduction #

This is the first release of an installable package to allow google android phones to connect to Cisco like VPN concentrators.

# Details #

The installing phone will require:
  * Cyanogen, JesusFreke or ROM with root and tun support.
  * A cisco, nortel or freeswan IPSEC server to connect to.

# How To #

## Installing this version ##

This is available in the [android market](http://www.cyrket.com/package/org.codeandroid.vpnc_frontend) as a free application as the name VPN Connections.

The latest version of the application can also be downloaded from [here](http://code.google.com/p/get-a-robot-vpnc/downloads/list?can=3&q=&colspec=Filename+Summary+Uploaded+Size+DownloadCount) and [installed using adb](http://developer.android.com/guide/developing/tools/adb.html#move).

The 0.4 release (and later) attempts to load the tun module automatically, although some roms do not have the tun.ko in the standard location, or compiled into the kernel.  To load the kernel module manually check out [this document](LoadingTheTunModule.md).


You should do this before the first launch after boot.  You will not need to create special device files as they should exist on your phone.

If you need to do this, please see the troubleshooting section below to report this issue so it can be loaded for this version of the ROM in future releases.

## Reporting bugs ##

Please inform us which ROM and version that you are using, if the /dev/tun exists, its permissions, and the output of logcat from a boot, also the version of this application that you are running.

If your ROM and version are already reported in the [issue list](http://code.google.com/p/get-a-robot-vpnc/issues/list), please comment in the thread as there has been some inconsistencies in reports.

Please be very careful when lodging a bug as it may contain company confidential information.  Be sure to strip any secrets or passwords from the configuration files or debugging information.


http://code.google.com/p/get-a-robot-vpnc/issues/list

## Known issues ##

Usernames can not contain the @ symbol.
Does not currently work with one time pass devices (Rsa keys).

# Screenshots #

<img src='http://i56.photobucket.com/albums/g161/caillen/wades/wadesapp-1.png' alt='Photobucket' border='0'>

<img src='http://i56.photobucket.com/albums/g161/caillen/wades/updated-now.png' border='0'>