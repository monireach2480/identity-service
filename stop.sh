#!/bin/bash

echo "Stopping Digital Identity Service..."

# Stop Spring Boot if running
pkill -f "identity-service"

# Stop Docker containers
echo "Stopping Docker containers..."
docker-compose down

echo "All services stopped!"
```

Make it executable:
```bash
chmod +x stop.sh