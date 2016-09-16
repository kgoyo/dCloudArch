#!/bin/sh
# Find path for entry-point.sh
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# If no args is provided, default to shell
# else run build.xml with provided commands
if [ $# -eq 0 ]; then
  echo "No args provided, starting shell."
  exec /bin/bash
else
  cd $DIR
  ant $@
fi
