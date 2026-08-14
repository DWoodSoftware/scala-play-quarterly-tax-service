FROM eclipse-temurin:17-jdk AS builder

ARG SBT_VERSION=1.10.1

WORKDIR /app

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl ca-certificates \
    && curl -fsSL "https://github.com/sbt/sbt/releases/download/v${SBT_VERSION}/sbt-${SBT_VERSION}.tgz" \
      | tar -xz -C /opt \
    && ln -s /opt/sbt/bin/sbt /usr/local/bin/sbt \
    && rm -rf /var/lib/apt/lists/*

COPY project ./project
COPY build.sbt .
COPY app ./app
COPY conf ./conf

RUN sbt -batch clean stage


FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=builder /app/target/universal/stage ./

COPY docker-entrypoint.sh /usr/local/bin/docker-entrypoint.sh

RUN chmod +x /usr/local/bin/docker-entrypoint.sh

EXPOSE 9000

ENTRYPOINT ["docker-entrypoint.sh"]