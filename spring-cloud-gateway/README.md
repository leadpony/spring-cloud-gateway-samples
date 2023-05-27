# spring-cloud-gateway

## How to build

```
mvn clean package
docker compose build
```

## Run and stop the containers

```
docker compose up
docker compose down
```

## Test

Calling the API direct.
```
curl http://localhost:8081/orders
```

Calling the API via the gateway.
```
curl -H 'Host: order.example.org' http://localhost:8080/orders
```
