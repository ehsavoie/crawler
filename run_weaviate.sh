#!/bin/bash
# This script is used to run the graph database

# It reads the username, password, and database name from the environment variables.
export POD_CONTAINER="${POD_CONTAINER:-docker}"
export NEO4J_USERNAME="${NEO4J_USERNAME:-neo4j}"
export NEO4J_PASSWORD="${NEO4J_PASSWORD:-neo4jpassword}"
export NEO4J_DATABASE="${NEO4J_DATABASE:-neo4j}"
export WEAVIATE_VOLUME_NAME="${WEAVIATE_VOLUME_NAME:-weaviate_data}"
export NEO4J_IMPORT_DATA="${NEO4J_IMPORT_DATA}"

# If a .env file is present, it will read the environment variables from there
# The .env file should be in the same directory as the script
# Rename the .env.example file to .env and set the environment variables
WORKING_DIR="$(dirname "${BASH_SOURCE[0]}")"
if [ -f $WORKING_DIR/../.env ]; then
	echo "Executing " $WORKING_DIR/../.env
	. $WORKING_DIR/../.env
fi
if [ -f $WORKING_DIR/.env ]; then
	echo "Executing " $WORKING_DIR/.env
	. $WORKING_DIR/.env
fi

echo "Neo4j Username: $NEO4J_USERNAME"
echo "Neo4j Password: $NEO4J_PASSWORD"
echo "Neo4j Database: $NEO4J_DATABASE"
echo "Pod container: $POD_CONTAINER"

$POD_CONTAINER volume ls | grep $WEAVIATE_VOLUME_NAME
if [ $? -ne 0 ]; then
    echo "Creating volume " $WEAVIATE_VOLUME_NAME
    $POD_CONTAINER volume create $WEAVIATE_VOLUME_NAME
else
     echo "Volume " $WEAVIATE_VOLUME_NAME " already exists"
fi

$POD_CONTAINER run --rm ${EXTRA_CONTAINER_OPTION} --name weaviate --publish=8090:8080 --publish=50051:50051 --volume $WEAVIATE_VOLUME_NAME:/var/lib/weaviate -e PERSISTENCE_DATA_PATH"="/var/lib/weaviate" -e AUTHENTICATION_ANONYMOUS_ACCESS_ENABLED="true" cr.weaviate.io/semitechnologies/weaviate:1.27.3
