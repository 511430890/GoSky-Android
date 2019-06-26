#!/bin/sh

echo removing build
rm -rf build

echo removing app/build
rm -rf app/build

echo removing models/build
rm -rf models/build

echo removing ijkplayer-arm64 build
rm -rf ijkplayer-arm64/build
rm -rf ijkplayer-arm64/src/main/obj/local

echo removing ijkplayer-armv5 build
rm -rf ijkplayer-armv5/build
rm -rf ijkplayer-armv5/src/main/obj/local

echo removing ijkplayer-armv7a build
rm -rf ijkplayer-armv7a/build
rm -rf ijkplayer-armv7a/src/main/obj/local

echo removing ijkplayer-x86 build
rm -rf ijkplayer-x86/build
rm -rf ijkplayer-x86/src/main/obj/local

echo removing ijkplayer-x86_64 build
rm -rf ijkplayer-x86_64/build
rm -rf ijkplayer-x86_64/src/main/obj/local

echo removing ijkplayer-java build
rm -rf ijkplayer-java/build

echo removing ijkplayer-media build
rm -rf ijkplayer-media/build

echo removing .gradle
rm -rf .gradle

echo done
