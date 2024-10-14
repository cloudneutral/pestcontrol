#################################
# CockroachDB Cloud Configuration
#################################

# Hints:
# - You can find all settings below in the CC console (https://cockroachlabs.cloud/cluster/<CLUSTER_ID>).
# - Don't forget to add your network to the IP Allowlist

# Your CockroachCloud cluster UUID, pick from URL: https://cockroachlabs.cloud/cluster/<CLUSTER_ID>
CC_CLUSTERID=YOUR_CLUSTER_ID
# Database host and port used to build URLs (pick closest region), for ex:
CC_HOST=hugin-qmg.aws-eu-north-1.cockroachlabs.cloud:26257
# Path to local SSL root cert (download from https://cockroachlabs.cloud/clusters/<CLUSTER_ID>/cert), for ex:
#   curl --create-dirs -o $HOME/Library/CockroachCloud/certs/00000000-0000-0000-0000-000000000000/hugin-ca.crt 'https://cockroachlabs.cloud/clusters/<YOUR_CLUSTER_ID>/cert'
CC_SSL_ROOT_CERT=$HOME/Library/CockroachCloud/certs/YOUR_CC_UUID/hugin-ca.crt
# CockroachCloud API key, see: https://www.cockroachlabs.com/docs/cockroachcloud/managing-access#create-api-keys
CC_API_KEY=YOUR_API_KEY
# Admin endpoint URL (pick same region as for CC_HOST), for ex:
ADMIN_URL="https://admin-hugin-qmg.cockroachlabs.cloud:8080"

#####################################################
# Modify below ONLY if needed (local port collisions)
#####################################################

##################
# Connect settings
##################

# User name with ADMIN privilege
DB_USER=craig
# User secret
DB_PASSWORD=cockroach
# Connection URL
DB_URL="postgresql://${DB_USER}:${DB_PASSWORD}@${CC_HOST}?sslmode=require&sslrootcert=${CC_SSL_ROOT_CERT}"
# (DO NOT MODIFY)
DEPLOY_OPTION=cloud

##################
# Service settings
##################

# Service frontend URL
APP_SERVICE_URL=http://localhost:9090
# JDBC datasource url
APP_JDBC_URL="jdbc:postgresql://${CC_HOST}/defaultdb?sslmode=verify-full&sslrootcert=${CC_SSL_ROOT_CERT}"
# Application parameters
APP_PARAMS="\
--spring.server.port=9090 \
--application.adminUrl=${ADMIN_URL} \
--spring.profiles.active=cloud \
--spring.datasource.url=${APP_JDBC_URL} \
--spring.datasource.username=${DB_USER} \
--spring.datasource.password=${DB_PASSWORD}"
# Executable jar
APP_JARFILE=${rootdir}/whackanode.jar
