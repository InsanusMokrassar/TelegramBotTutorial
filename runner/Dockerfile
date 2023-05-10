FROM adoptopenjdk/openjdk11

USER 1000

ENTRYPOINT ["/runner/bin/runner", "/runner/local.config.json"]

ADD ./build/distributions/runner.tar /
ADD ./local.config.json /runner/
