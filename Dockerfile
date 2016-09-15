# SkyCave (Group css-14)
#
# Version 0.2

FROM henrikbaerbak/cloudarch:e16.1
MAINTAINER css-14
LABEL Description="This image is used to start SkyCave"

ENV skycave /root/cave/
WORKDIR ${skycave}

# Copy the code base inside the container
ADD . ${skycave}

# Build src, avoiding test files
# OBS! the test files are always created when starting the container
RUN ant build.src

ENTRYPOINT ["/bin/bash", "./entry-point.sh"]
