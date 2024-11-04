# Toxiproxy Notes

Internal notes for manual setup of toxiproxy.

First start the Toxiproxy server.

    toxiproxy-server

Add a proxy for each node intercepting the advertise-addr (the address it tells other nodes):

    toxiproxy-cli create --listen localhost:35258 --upstream localhost:25258 crdb1
    toxiproxy-cli create --listen localhost:35259 --upstream localhost:25259 crdb2
    toxiproxy-cli create --listen localhost:35260 --upstream localhost:25260 crdb3
    toxiproxy-cli list

Start CRDB nodes:

    .binaries/cockroach start --locality=region=eu-north-1,zone=eu-north-1a --listen-addr=localhost:25258 --advertise-addr=localhost:35258 --sql-addr=localhost:26258 --http-addr=localhost:8081 --join=localhost:25258,localhost:25259,localhost:25260 --store=.datafiles/n1  --cache=10% --max-sql-memory=10% --background --insecure
    .binaries/cockroach start --locality=region=eu-north-1,zone=eu-north-1b --listen-addr=localhost:25259 --advertise-addr=localhost:35259 --sql-addr=localhost:26259 --http-addr=localhost:8082 --join=localhost:25258,localhost:25259,localhost:25260 --store=.datafiles/n2 --cache=10% --max-sql-memory=10% --background --insecure
    .binaries/cockroach start --locality=region=eu-north-1,zone=eu-north-1c --listen-addr=localhost:25260 --advertise-addr=localhost:35260 --sql-addr=localhost:26260 --http-addr=localhost:8083 --join=localhost:25258,localhost:25259,localhost:25260 --store=.datafiles/n3 --cache=10% --max-sql-memory=10% --background --insecure

Add upstream and downstream toxics to each proxy:

    toxiproxy-cli toxic add --type latency --attribute latency=500 --upstream crdb1
    toxiproxy-cli inspect crdb1

    toxiproxy-cli toxic add --type latency --attribute latency=500 --upstream crdb2
    toxiproxy-cli inspect crdb2

    toxiproxy-cli toxic add --type latency --attribute latency=500 --upstream crdb2
    toxiproxy-cli inspect crdb3
    
Remove toxics:

    toxiproxy-cli toxic delete --toxicName latency_upstream crdb1
    toxiproxy-cli toxic delete --toxicName latency_upstream crdb2
    toxiproxy-cli toxic delete --toxicName latency_upstream crdb3

    toxiproxy-cli toxic delete --toxicName latency_downstream crdb1
    toxiproxy-cli toxic delete --toxicName latency_downstream crdb2
    toxiproxy-cli toxic delete --toxicName latency_downstream crdb3
