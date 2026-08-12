#!/usr/bin/env bash
# Syncs shared host configuration without replacing container-local Codex authentication or state.
set -euo pipefail

settings_source=/mnt/d/codex-settings
settings_destination=/home/vscode/.codex

if [[ ! -d "$settings_source" ]]; then
  exit 0
fi

mkdir -p "$settings_destination"

copy_shared_file() {
  local relative_path=$1
  local source_path="$settings_source/$relative_path"

  if [[ -f "$source_path" ]]; then
    cp "$source_path" "$settings_destination/$relative_path"
  fi
}

copy_shared_directory() {
  local relative_path=$1
  local source_path="$settings_source/$relative_path"

  if [[ -d "$source_path" ]]; then
    mkdir -p "$settings_destination/$relative_path"
    cp -R "$source_path"/. "$settings_destination/$relative_path"/
  fi
}

copy_shared_file "config.toml"
copy_shared_file "hooks.json"
copy_shared_directory "hooks"
copy_shared_directory "instructions"
copy_shared_directory "rules"
copy_shared_directory "skills"
