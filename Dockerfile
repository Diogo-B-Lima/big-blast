FROM openjdk:10-slim
COPY Blast-0.0.1-SNAPSHOT.jar /bin/bigBlast.jar

RUN apt-get -y update
RUN apt-get -y upgrade
RUN apt-get -y install ncbi-blast+

RUN apt-get install -y procps
RUN apt-get -y autoclean
RUN apt-get -y clean

CMD ["/bin/bash"]