# tpm-workorder-service

Breakdown reports for the TPM system. Owns the work order lifecycle, the roles allowed to move
it, and the rules that guard each transition.

Part of a set of four repositories — **start with
[tpm-platform](../tpm-platform/README.md)**, which explains the architecture and runs
everything together.

## The state machine

This is the core of the service. Every transition checks the current state first, then the
actor's role, then the supplied data — in that order, so the error always names the first real
obstacle rather than whichever check happened to run first.

| From | Action | To | Role | Condition |
|---|---|---|---|---|
| `REPORTED` | `assign` | `ASSIGNED` | manager or technician | sets the assignee |
| `ASSIGNED` | `start` | `IN_PROGRESS` | **only the assigned technician** | records the start time |
| `IN_PROGRESS` | `hold` | `ON_HOLD` | technician | requires a reason |
| `ON_HOLD` | `resume` | `IN_PROGRESS` | technician | |
| `IN_PROGRESS` | `resolve` | `RESOLVED` | technician | requires a description, records the end time |
| `RESOLVED` | `close` | `CLOSED` | manager | terminal |

Illegal transitions are rejected and covered by tests: `REPORTED → CLOSED`,
`REPORTED → IN_PROGRESS`, anything out of `CLOSED`, closing as a technician, starting as a
technician the work order was not assigned to.

Permissions live in the aggregate, not in annotations. The framework establishes *who* the
caller is; the domain decides *whether they may*.

## Endpoints

All require a `Bearer` token signed by the auth service. The actor is taken from the token's
`sub` and `role` claims.

| Method | Path | |
|---|---|---|
| `POST` | `/work-orders` | report a breakdown, returns `201` |
| `POST` | `/work-orders/{id}/assign` | body: `{"technicianId": "..."}` |
| `POST` | `/work-orders/{id}/start` | |
| `POST` | `/work-orders/{id}/hold` | body: `{"reason": "..."}` |
| `POST` | `/work-orders/{id}/resume` | |
| `POST` | `/work-orders/{id}/resolve` | body: `{"resolution": "..."}` |
| `POST` | `/work-orders/{id}/close` | |
| `GET` | `/work-orders/{id}` | |

Status codes carry meaning: `401` no valid token, `403` the role may not perform this
transition, `422` the transition is illegal from the current state or required data is missing,
`400` the request itself is malformed.

## Events

Publishes `WorkOrderStarted` and `WorkOrderResolved`. The other four transitions publish
nothing — events are emitted where a consumer exists, not on speculation.

Consumes `MachineRegistered` and keeps a minimal local copy of the machine registry, so
reporting a breakdown never requires calling another service.

## Trying it by hand

With the platform running (`cd ../tpm-platform && make up`). Copy-paste in order — the whole
lifecycle plus every refusal worth seeing.

```bash
# Three tokens, three roles
OP=$(curl -s -X POST localhost:8080/token -H 'Content-Type: application/json' \
  -d '{"username":"operator","password":"operator"}' | grep -o '"token":"[^"]*' | cut -d'"' -f4)
TECH=$(curl -s -X POST localhost:8080/token -H 'Content-Type: application/json' \
  -d '{"username":"technik","password":"technik"}' | grep -o '"token":"[^"]*' | cut -d'"' -f4)
MGR=$(curl -s -X POST localhost:8080/token -H 'Content-Type: application/json' \
  -d '{"username":"kierownik","password":"kierownik"}' | grep -o '"token":"[^"]*' | cut -d'"' -f4)

# A machine to report against
MACHINE=$(curl -s -X POST localhost:8081/machines \
  -H "Authorization: Bearer $MGR" -H 'Content-Type: application/json' \
  -d '{"name":"Hydraulic press"}' | grep -o '"id":"[^"]*' | cut -d'"' -f4)
sleep 1

# This service already knows about it - it arrived as an event, not as a query
docker compose -f ../tpm-platform/docker-compose.yml exec -T db \
  psql -U workorder_user -d workorder_db -c 'SELECT id, name FROM known_machines;'
```

### The happy path

```bash
# Report a breakdown -> 201
WO=$(curl -s -X POST localhost:8082/work-orders \
  -H "Authorization: Bearer $OP" -H 'Content-Type: application/json' \
  -d "{\"machineId\":\"$MACHINE\",\"reason\":\"BREAKDOWN\",\"reportedBy\":\"op-1\"}" \
  | grep -o '"id":"[^"]*' | cut -d'"' -f4)
echo "$WO"

# Manager assigns a technician -> ASSIGNED
curl -s -X POST localhost:8082/work-orders/"$WO"/assign \
  -H "Authorization: Bearer $MGR" -H 'Content-Type: application/json' \
  -d '{"technicianId":"tech-1"}'

# The assigned technician starts -> IN_PROGRESS
curl -s -X POST localhost:8082/work-orders/"$WO"/start -H "Authorization: Bearer $TECH"

# The machine moved itself into maintenance, with no call made against it
sleep 1 && curl -s localhost:8081/machines/"$MACHINE" -H "Authorization: Bearer $MGR"

# Waiting for a spare part -> ON_HOLD
curl -s -X POST localhost:8082/work-orders/"$WO"/hold \
  -H "Authorization: Bearer $TECH" -H 'Content-Type: application/json' \
  -d '{"reason":"Spare part unavailable"}'

# Part arrived -> IN_PROGRESS
curl -s -X POST localhost:8082/work-orders/"$WO"/resume -H "Authorization: Bearer $TECH"

# Repair done -> RESOLVED
curl -s -X POST localhost:8082/work-orders/"$WO"/resolve \
  -H "Authorization: Bearer $TECH" -H 'Content-Type: application/json' \
  -d '{"resolution":"Bearing replaced"}'

# The machine is back in service, again without being called
sleep 1 && curl -s localhost:8081/machines/"$MACHINE" -H "Authorization: Bearer $MGR"

# Manager closes it -> CLOSED, terminal
curl -s -X POST localhost:8082/work-orders/"$WO"/close -H "Authorization: Bearer $MGR"

# Read it back
curl -s localhost:8082/work-orders/"$WO" -H "Authorization: Bearer $MGR"
```

### The refusals

Each returns a different code for a different reason. This is the point of the service.

```bash
NEW=$(curl -s -X POST localhost:8082/work-orders \
  -H "Authorization: Bearer $OP" -H 'Content-Type: application/json' \
  -d "{\"machineId\":\"$MACHINE\",\"reason\":\"BREAKDOWN\",\"reportedBy\":\"op-1\"}" \
  | grep -o '"id":"[^"]*' | cut -d'"' -f4)

# No token -> 401, "I do not know who you are" (Spring Security)
curl -s -i -X POST localhost:8082/work-orders/"$NEW"/start | head -1

# Operator assigning -> 403, "I know, and that role may not" (the aggregate)
curl -s -i -X POST localhost:8082/work-orders/"$NEW"/assign \
  -H "Authorization: Bearer $OP" -H 'Content-Type: application/json' \
  -d '{"technicianId":"tech-1"}' | head -1

# Starting a work order that is still REPORTED -> 422, illegal transition
curl -s -i -X POST localhost:8082/work-orders/"$NEW"/start \
  -H "Authorization: Bearer $TECH" | head -1

# Assign to tech-1, then have a different technician try to start -> 403
curl -s -o /dev/null -X POST localhost:8082/work-orders/"$NEW"/assign \
  -H "Authorization: Bearer $MGR" -H 'Content-Type: application/json' \
  -d '{"technicianId":"tech-2"}'
curl -s -i -X POST localhost:8082/work-orders/"$NEW"/start \
  -H "Authorization: Bearer $TECH" | head -1

# Hold a work order that is not in progress -> 422, illegal transition
curl -s -i -X POST localhost:8082/work-orders/"$NEW"/hold \
  -H "Authorization: Bearer $TECH" -H 'Content-Type: application/json' \
  -d '{"reason":"Spare part unavailable"}' | head -1

# Hold with a blank reason -> 400, stopped by request validation before the domain.
# The aggregate enforces the same rule independently, because it is also reachable
# from the event consumer, where no request validation exists.
curl -s -i -X POST localhost:8082/work-orders/"$NEW"/hold \
  -H "Authorization: Bearer $TECH" -H 'Content-Type: application/json' \
  -d '{"reason":"  "}' | head -1

# Unknown work order -> 404
curl -s -i localhost:8082/work-orders/no-such-id -H "Authorization: Bearer $MGR" | head -1

# Malformed request -> 400
curl -s -i -X POST localhost:8082/work-orders \
  -H "Authorization: Bearer $OP" -H 'Content-Type: application/json' \
  -d '{"machineId":"","reason":"BREAKDOWN"}' | head -1
```

### Following one action across services

```bash
curl -s -X POST localhost:8082/work-orders \
  -H "Authorization: Bearer $OP" -H 'Content-Type: application/json' \
  -H 'X-Correlation-Id: my-trace-1' \
  -d "{\"machineId\":\"$MACHINE\",\"reason\":\"BREAKDOWN\",\"reportedBy\":\"op-1\"}" > /dev/null

docker compose -f ../tpm-platform/docker-compose.yml logs machine workorder | grep my-trace-1
```

## Running it on its own

```bash
docker compose up --build -d
```

For development, `./mvnw spring-boot:test-run` brings up PostgreSQL and RabbitMQ automatically
through Testcontainers.

## Tests

```bash
./mvnw test
```
