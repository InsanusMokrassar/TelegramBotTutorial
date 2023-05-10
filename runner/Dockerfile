FROM adoptopenjdk/openjdk11

USER 1000

ENTRYPOINT ["/runner/bin/runner", "/runner/config.json"]

ADD ./build/distributions/runner.tar /
