#!/bin/sh
g++ -DavaPoint=$1 -DappNum_w=$2 -DAlpha=$3 -o random -DRANDOM -O2 -std=c++11 main.cpp
g++ -DavaPoint=$1 -DappNum_w=$2 -DAlpha=$3 -o topk -DTOPK -O2 -std=c++11 main.cpp
g++ -DavaPoint=$1 -DappNum_w=$2 -DAlpha=$3 -o a.out -O2 -std=c++11 main.cpp
