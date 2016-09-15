# SkyCave (Group css-14)
#
# Version 0.2

FROM henrikbaerbak/cloudarch:e16.1
MAINTAINER css-14
LABEL Description="This image is used to start SkyCave"

ENV skycave /root/cave/
WORKDIR ${skycave}

RUN apt-get update

# Copy the code base inside the container
ADD . ${skycave}

# Build and resolve
RUN ant build.src

ENTRYPOINT ["/bin/bash", "./entry-point.sh"]
