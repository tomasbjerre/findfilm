#!/bin/sh

mvn clean

find -name .classpath -exec rm {} \;
find -name .project -exec rm {} \;
