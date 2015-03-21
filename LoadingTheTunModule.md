# Introduction #

Recent ROMS released by the community have the tun module already enabled.


To find the tun.ko module..

> su

> cd /

> find -iname tun.ko

> /system/modules/somethingelse/something/tun.ko


This directory is the location of the tun kernel module.  A plan later is to have the software search for the tun kernel module and download it for the rom that is missing.

> insmod /system/modules/somethingelse/something/tun.ko