# Group css-14
FROM henrikbaerbak/cloudarch:e16.1
RUN mkdir /root/cave
ENV skycave /root/cave/
WORKDIR ${skycave}

RUN apt-get update

# Create dir for project and copy the code base inside the container
ADD . ${skycave}
RUN cd ${skycave}

# Build and resolve
RUN ant build.all

ENTRYPOINT ["/bin/bash", "./entry-point.sh"]
