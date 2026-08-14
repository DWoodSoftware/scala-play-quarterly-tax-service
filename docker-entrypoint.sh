#!/usr/bin/env sh

set -eu

if [ -z "${APPLICATION_SECRET:-}" ]; then
  if [ "${APP_ENV:-local}" = "production" ]; then
    echo "ERROR: APPLICATION_SECRET must be provided when APP_ENV=production." >&2
    exit 1
  fi

  APPLICATION_SECRET="$(
    head -c 32 /dev/urandom |
      base64 |
      tr -d '\n'
  )"

  export APPLICATION_SECRET

  echo "Generated ephemeral APPLICATION_SECRET for local container execution."
fi

exec bin/scala-play-quarterly-tax-service "$@"