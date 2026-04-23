FROM golang:1.21-alpine AS builder
WORKDIR /app
COPY go.mod go.sum ./
RUN go mod download
COPY . .
RUN CGO_ENABLED=0 GOOS=linux go build -a -ldflags="-s -w" -o ci-defenderx-agent ./cmd/agent

FROM alpine:3.19
RUN apk --no-cache add ca-certificates tzdata
WORKDIR /app
COPY --from=builder /app/ci-defenderx-agent .
COPY agent.yaml /etc/ci-defenderx/agent.yaml
ENTRYPOINT ["./ci-defenderx-agent"]
