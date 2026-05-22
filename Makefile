.PHONY: local docker down

# Runs the app locally using your local profile
local:
	./mvnw spring-boot:run -Dspring-boot.run.profiles=local

# Builds the jar and spins up the containerized cluster
docker:
	./mvnw clean package -DskipTests
	docker compose up --build

# Tears down the docker cluster
down:
	docker compose down