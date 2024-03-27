#!/usr/bin/env sh

set -e

find . -name pom.xml -print0 | xargs -0 sed -i -E \
-e "s:<revision>dev-SNAPSHOT</revision>:<revision>${REVISION}</revision>:g" \
