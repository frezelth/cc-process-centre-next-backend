# Base Docker Image shared by all backends to ensure consistency in runtime
#
# Each Package is responsible for it's Docker file, the template should be followed and adapted for special needs
# Best practices should be spread to all packages.
FROM eclipse-temurin:21-jre-alpine

# Expose ports to enable running the service
# Ports should be standardized to make it easier to debug
# Exposing two services in the same port can create conflicts

ENV PORT 8080
EXPOSE 8080

# List of ARGS input from Kaniko Build
ARG IMAGE_DATE
ARG VCS_REVISION
ARG VCS_SEMVER
ARG PKG_WORKDIR

# Labeling based on https://github.com/opencontainers/image-spec/blob/master/annotations.md /
LABEL org.opencontainers.image.created="${IMAGE_DATE}"              \
      org.opencontainers.image.revision="${VCS_REVISION}"           \
      org.opencontainers.image.version="${VCS_SEMVER}"              \
      org.opencontainers.image.title="cc-process-centre-next-backend"                      \
      org.opencontainers.image.description="Process Centre service next"          \
      org.opencontainers.image.authors="Thomas Frezel"                  \
      org.opencontainers.image.vendor="EC"                    \
      org.opencontainers.image.url="https://webgate.ec.europa.eu/CITnet/confluence/display/PROCESS"                          \
      org.opencontainers.image.documentation="https://webgate.ec.europa.eu/CITnet/confluence/display/PROCESS"  \
      org.opencontainers.image.source="https://webgate.ec.europa.eu/CITnet/stash/scm/csdr/cc-process-centre-next-backend.git"

# Copy of distribution/target folder artifacts
# In case additional Artifacts are required

# All containers should run in least privileged mode, meaning not ROOT.
# NOTE: On OpenShift there is a warning when you try to run as ROOT
RUN addgroup -g 1001 -S cc && \
    adduser -u 1001 -S -G cc cc && \
    chown -R 1001:0 /home/cc && \
    chmod -R g=u /home/cc && \
    apk add --no-cache git=2.47.2-r0

COPY --chown=1001:0 ${PKG_WORKDIR}/target/process-centre-service.jar /home/cc/service.jar

USER 1001
# Command to initialize the service
CMD ["java", "-jar", "home/cc/service.jar"]
