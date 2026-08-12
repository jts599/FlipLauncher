# Android command-line development image used by the repository dev container.
FROM mcr.microsoft.com/devcontainers/base:ubuntu-24.04

ENV DEBIAN_FRONTEND=noninteractive
ENV ANDROID_HOME=/opt/android-sdk
ENV ANDROID_SDK_ROOT=/opt/android-sdk
ENV JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
ENV PATH=${PATH}:${ANDROID_HOME}/cmdline-tools/latest/bin:${ANDROID_HOME}/platform-tools

ARG ANDROID_CMDLINE_TOOLS_ZIP=commandlinetools-linux-13114758_latest.zip
ARG ANDROID_CMDLINE_TOOLS_URL=https://dl.google.com/android/repository/${ANDROID_CMDLINE_TOOLS_ZIP}
ARG ANDROID_BUILD_TOOLS_VERSION=35.0.0
ARG ANDROID_PLATFORM=android-35

RUN apt-get update \
    && apt-get install -y --no-install-recommends ca-certificates curl git npm nodejs openjdk-21-jdk unzip wget zip \
    && rm -rf /var/lib/apt/lists/*

# Install Codex in the image so rebuilding the container preserves CLI availability.
RUN npm install --global @openai/codex

# Seed the mounted Codex-home volume with the remote user's ownership on first creation.
RUN mkdir -p /home/vscode/.codex \
    && chown -R vscode:vscode /home/vscode/.codex

RUN mkdir -p ${ANDROID_HOME}/cmdline-tools /tmp/android-sdk \
    && curl -fsSL "${ANDROID_CMDLINE_TOOLS_URL}" -o /tmp/android-sdk/${ANDROID_CMDLINE_TOOLS_ZIP} \
    && unzip -q /tmp/android-sdk/${ANDROID_CMDLINE_TOOLS_ZIP} -d /tmp/android-sdk \
    && mv /tmp/android-sdk/cmdline-tools ${ANDROID_HOME}/cmdline-tools/latest \
    && rm -rf /tmp/android-sdk

RUN yes | sdkmanager --licenses > /dev/null \
    && sdkmanager "platform-tools" "platforms;${ANDROID_PLATFORM}" "build-tools;${ANDROID_BUILD_TOOLS_VERSION}" \
    && chown -R vscode:vscode ${ANDROID_HOME}
