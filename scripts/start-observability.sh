#!/usr/bin/env bash
set -euo pipefail

COMPOSE_FILE="$(cd "$(dirname "$0")/.." && pwd)/kafka-docker/docker-compose.yml"
REQUIRED_PORTS=(4317 4318 16686 9094 3100)
SERVICES=(otel-collector jaeger kafka-iwallet loki zookeeper-iwallet promtail)

red()   { printf '\033[31m%s\033[0m\n' "$*"; }
green() { printf '\033[32m%s\033[0m\n' "$*"; }
yellow(){ printf '\033[33m%s\033[0m\n' "$*"; }
bold()  { printf '\033[1m%s\033[0m\n' "$*"; }

# ── 1. Docker Desktop check ────────────────────────────────────────────────────
bold "==> Checking Docker Desktop..."
if ! docker info >/dev/null 2>&1; then
  yellow "Docker Desktop is not running. Attempting to start it..."
  open -a Docker
  DOCKER_DEADLINE=$(( $(date +%s) + 60 ))
  DOCKER_READY=0
  while [ "$(date +%s)" -lt "$DOCKER_DEADLINE" ]; do
    sleep 2
    if docker info >/dev/null 2>&1; then
      DOCKER_READY=1
      break
    fi
    printf '.'
  done
  echo ""
  if [ "$DOCKER_READY" -eq 0 ]; then
    red "Docker Desktop did not start within 60 seconds."
    echo ""
    echo "  Troubleshooting:"
    echo "    df -h /                                     # check disk space"
    echo "    ls ~/Library/Containers/com.docker.docker/Data/log/  # check logs"
    echo ""
    echo "  To suppress OTEL timeouts while Docker is down, add to your IDE run config:"
    echo "    OTEL_SDK_DISABLED=true"
    exit 1
  fi
fi
green "Docker Desktop is running."

# ── 2. Port conflict check ─────────────────────────────────────────────────────
bold ""
bold "==> Checking for port conflicts..."
CONFLICT=0
for port in "${REQUIRED_PORTS[@]}"; do
  pid=$(lsof -ti tcp:"$port" 2>/dev/null || true)
  if [ -n "$pid" ]; then
    proc=$(ps -p "$pid" -o comm= 2>/dev/null || echo "unknown")
    # Ignore if it's already our docker process
    if [[ "$proc" != "com.docker"* ]] && [[ "$proc" != "docker"* ]]; then
      yellow "  Port $port already in use by PID $pid ($proc)"
      CONFLICT=1
    fi
  fi
done
if [ "$CONFLICT" -eq 0 ]; then
  green "No port conflicts detected."
fi

# ── 3. Start / restart stack ───────────────────────────────────────────────────
bold ""
bold "==> Starting observability stack..."
docker compose -f "$COMPOSE_FILE" up -d --remove-orphans

# ── 4. Health check ────────────────────────────────────────────────────────────
bold ""
bold "==> Waiting for services to be healthy (up to 30s)..."
DEADLINE=$(( $(date +%s) + 30 ))
ALL_OK=0
while [ "$(date +%s)" -lt "$DEADLINE" ]; do
  ALL_OK=1
  for svc in "${SERVICES[@]}"; do
    status=$(docker inspect --format='{{.State.Status}}' "$svc" 2>/dev/null || echo "missing")
    if [ "$status" != "running" ]; then
      ALL_OK=0
      break
    fi
  done
  [ "$ALL_OK" -eq 1 ] && break
  sleep 2
done

bold ""
bold "==> Service status:"
for svc in "${SERVICES[@]}"; do
  status=$(docker inspect --format='{{.State.Status}}' "$svc" 2>/dev/null || echo "not found")
  if [ "$status" = "running" ]; then
    green "  [OK] $svc"
  else
    red "  [!!] $svc — $status"
  fi
done

# ── 5. OTLP endpoint smoke test ────────────────────────────────────────────────
bold ""
bold "==> Smoke-testing OTLP HTTP endpoint (localhost:4318)..."
if curl -sf --max-time 3 -o /dev/null -w "%{http_code}" \
     -X POST http://localhost:4318/v1/traces \
     -H "Content-Type: application/x-protobuf" \
     --data-binary "" 2>/dev/null | grep -qE "^(200|400|415)"; then
  green "OTLP HTTP endpoint is responding on :4318"
else
  yellow "OTLP endpoint did not respond — collector may still be starting."
  echo "  Wait a few seconds and retry, or check logs:"
  echo "    docker logs otel-collector"
fi

# ── 6. Summary ─────────────────────────────────────────────────────────────────
bold ""
bold "==> Endpoints:"
echo "  OTLP HTTP (Java agent) : http://localhost:4318"
echo "  OTLP gRPC (Java agent) : grpc://localhost:4317"
echo "  Jaeger UI               : http://localhost:16686"
echo "  Loki                    : http://localhost:3100"
echo "  Kafka                   : localhost:9094"
echo ""
bold "==> To stop the stack:"
echo "  docker compose -f kafka-docker/docker-compose.yml down"
bold ""
bold "==> To disable OTEL when this stack is not running:"
echo "  export OTEL_SDK_DISABLED=true   # in shell or IDE run config"
