FROM quay.io/vital/scala:jdk-master

# Download app dependencies
ADD project /app/project
ADD build.sbt /app

RUN sbt update

# Add sources
ADD . /app
