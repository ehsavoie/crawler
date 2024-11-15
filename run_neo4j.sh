#!/bin/bash
# This script is used to run the graph database

# It reads the username, password, and database name from the environment variables.
export POD_CONTAINER="${POD_CONTAINER:-docker}"
export NEO4J_USERNAME="${NEO4J_USERNAME:-neo4j}"
export NEO4J_PASSWORD="${NEO4J_PASSWORD:-neo4jpassword}"
export NEO4J_DATABASE="${NEO4J_DATABASE:-neo4j}"
export NEO4J_VOLUME_NAME="${NEO4J_VOLUME_NAME:-neo4j_data}"
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

$POD_CONTAINER volume ls | grep $NEO4J_VOLUME_NAME
if [ $? -ne 0 ]; then
    echo "Creating volume " $NEO4J_VOLUME_NAME
    $POD_CONTAINER volume create $NEO4J_VOLUME_NAME
else
     echo "Volume " $NEO4J_VOLUME_NAME " already exists"
fi

export VOLUME_PATH="$($POD_CONTAINER volume inspect --format '{{ .Mountpoint }}' $NEO4J_VOLUME_NAME)"

if [ -d $NEO4J_IMPORT_DATA ]; then
	echo "Importing data from " $NEO4J_IMPORT_DATA
	$POD_CONTAINER  container create --name dummy -v $VOLUME_PATH:/root hello-world
	$POD_CONTAINER  cp $NEO4J_IMPORT_DATA/. dummy:/root/.
	$POD_CONTAINER rm dummy
fi

$POD_CONTAINER run --rm ${EXTRA_CONTAINER_OPTION} --name neo4j --publish=7474:7474 --publish=7687:7687 --volume $NEO4J_VOLUME_NAME:/data -e NEO4J_AUTH=$NEO4J_USERNAME/$NEO4J_PASSWORD -e NEO4J_PLUGINS=\[\"apoc\"\] neo4j:5.25.1
