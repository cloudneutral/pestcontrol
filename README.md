<p>	
	<a href="https://github.com/cloudneutral/pestcontrol/actions/workflows/maven.yml"><img src="https://github.com/cloudneutral/pestcontrol/actions/workflows/maven.yml/badge.svg?branch=main" alt="">
</p>

<!-- TOC -->
* [About](#about)
  * [Main features](#main-features)
  * [Compatibility](#compatibility)
  * [How it works](#how-it-works)
* [Terms of Use](#terms-of-use)
* [Prerequisites](#prerequisites)
  * [Install the JDK](#install-the-jdk)
* [Building](#building)
  * [Clone the project](#clone-the-project)
  * [Build the artifacts](#build-the-artifacts)
* [Installing](#installing)
* [Configuration](#configuration)
  * [Application](#application)
  * [Clusters](#clusters)
  * [DataSource Properties](#datasource-properties)
  * [Local Cluster Configuration](#local-cluster-configuration)
* [Running](#running)
  * [Local Cluster Management](#local-cluster-management)
    * [Remarks](#remarks)
    * [Shutdown](#shutdown)
* [Appendix: Configuration Files](#appendix-configuration-files)
<!-- TOC -->

# About

<img  align="left" src=".github/logo.png" alt="" width="64"/> 

[Pest Control](https://github.com/cloudneutral/pestcontrol) is a combined graphical and command-line tool for 
controlling and visualizing CockroachDB cluster failures, and it's 
impact on application workloads. It supports CockroachDB Cloud 
and local, self-hosted clusters for which it provides easy-to-use 
bash scripts.

## Main features

The main features include:

- Visualize cluster health in a web UI with disruption and recovery controls.
- Visualize impact on client workloads during steady state and node / zone / region disruptions.
- Easy-to-use bash scripts for local CockroachDB cluster deployment and management.
- Ad-hoc setup and testing of local CockroachDB clusters

The landing page showing the cluster layout and node status:

![ui1](.github/ui-1.png)

The workload page with some activity:

![ui2](.github/ui-2.png)

## Compatibility

This tool supports the following platforms and versions:

- CockroachDB Cloud v22.2+
  - Requires a feature flag enabled for the organization (file a support request) 
- CockroachDB Local Self-Hosted v22.2+
  - Secure or insecure mode
  - No license key needed
- MacOS (main platform)
- Linux

## How it works

Pest Control consists of two parts:

1. A front-end web app for the visuals along with a REST API for automation. 
2. Bash scripts for easy installing and management of local CockroachDB clusters.

The web app adapts to the logged in cluster type. It can be either CockroachDB Cloud 
or a local, self-hosted cluster. It mainly uses the Cockroach Cloud and Cluster APIs 
for interaction with the cluster, with the exception for local clusters that don't
provide any disruption API like Cockroach Cloud. For that, it uses bash scripts
to pass for disrupting and recovering node failures. 

Lastly, it uses JDBC to run optional user triggered workloads against the cluster.

# Terms of Use

This tool is not supported by Cockroach Labs. Use of this tool is entirely at your
own risk and Cockroach Labs makes no guarantees or warranties about its operation.

See [MIT](LICENSE.txt) for terms and conditions.

# Prerequisites

Things you need to run Pest Control locally.

- Java 17+ JDK
    - https://openjdk.org/projects/jdk/17/
    - https://www.oracle.com/java/technologies/downloads/#java17

## Install the JDK

MacOS (using sdkman):

    curl -s "https://get.sdkman.io" | bash
    sdk list java
    sdk install java 17.0 (pick version)  

Ubuntu:

    sudo apt-get install openjdk-17-jdk

# Building

Instruction for building the project locally, as an alternative to using the
packaged TAR.GZ assembly artifact.

## Clone the project

    git clone git@github.com:cloudneutral/pestcontrol.git && cd pestcontrol

## Build the artifacts

    chmod +x mvnw
    ./mvnw clean install

# Installing

If you prefer to use a packaged artifact (release or snapshot) rather than building, 
see [GitHub Packages](https://github.com/orgs/cloudneutral/packages?repo_name=pestcontrol). Scroll to the latest `TAR.GZ` file and copy+paste the download URL
as described:

    curl -o pestcontrol.tar.gz <paste-url-here>
    tar xvf pestcontrol.tar.gz && cd pestcontrol

# Configuration

Pest Control is configured through the files available in the [config](config) directory.
The main configuration properties are in the [config/application-default.yml](config/application-default.yml) file.
It includes a list of CockroachDB cluster definitions.

Example with three cloud clusters and two local:
```yaml
application:
  clusters:
    - cluster-id: "97a73235-fa45-4b4d-a229-f5efd52168ba"
      cluster-type: cloud_dedicated
      api-key: "..."
      admin-url: "https://admin-odin-qzx.cockroachlabs.cloud:8080"
      data-source-properties:
        url: "jdbc:postgresql://odin-qzx.aws-eu-north-1.cockroachlabs.cloud:26257/defaultdb?sslmode=require"
        username: "craig"
        password: "cockroach"
   
    - cluster-id: "bdeb3c96-4ab4-458a-86ac-927efd844294"
      cluster-type: cloud_dedicated
      api-key: "..."
      admin-url: "https://admin-hugin-qzx.cockroachlabs.cloud:8080"
      data-source-properties:
        url: "jdbc:postgresql://hugin-qzx.aws-eu-north-1.cockroachlabs.cloud:26257/defaultdb?sslmode=require"
        username: "craig"
        password: "cockroach"
   
    - cluster-id: "e3e17085-bbda-4d13-85a4-49e81dab04b0"
      cluster-type: cloud_dedicated
      api-key: "..."
      admin-url: "https://admin-munin-qzx.cockroachlabs.cloud:8080"
      data-source-properties:
        url: "jdbc:postgresql://munin-qzx.aws-eu-north-1.cockroachlabs.cloud:26257/defaultdb?sslmode=require"
        username: "craig"
        password: "cockroach"

    - cluster-id: "Local Secure Cluster"
      cluster-type: local_secure
      admin-url: "https://localhost:443"
      data-source-properties:
        url: "jdbc:postgresql://localhost:26257/defaultdb?sslmode=require"
        username: "craig"
        password: "cockroach"
      
    - cluster-id: "Local Insecure Cluster"
      cluster-type: local_insecure
      admin-url: "http://localhost:8080"
      data-source-properties:
        url: "jdbc:postgresql://localhost:26257/defaultdb?sslmode=disable"
        username: "craig"
        password: "cockroach"
```

## Application

Top-level entry in `application<-profile>.yml`.

```yaml
    application:
      clusters:
        ...
 ```

| Field Name  | Optional | Default | Description                         |
|-------------|----------|---------|-------------------------------------|
| clusters    | No       | -       | Collection of CockroachDB clusters. |

## Clusters

Collection of cluster definitions.

```yaml
    application:
      clusters:
        - cluster-id: "38e2ce4f-e9b6-43ae-a9ed-64d673e443cb"
          cluster-type: cloud_dedicated
          api-key: "..."
          admin-url: "https://admin-odin-qzx.cockroachlabs.cloud:8080"
          data-source-properties:
            ...
```

| Field Name             | Optional | Default         | Description                                                                                                                                    |
|------------------------|----------|-----------------|------------------------------------------------------------------------------------------------------------------------------------------------|
| cluster-id             | No       | -               | Either a CockroachDB Cloud cluster ID or a unique string for a local cluster                                                                   |
| cluster-type           | Yes      | cloud_dedicated | `cloud_dedicated`, `local_secure` or `local_insecure`                                                                                          |
| api-key                | Yes      | -               | Only required for `cloud_dedicated`, see [Create API Keys](https://www.cockroachlabs.com/docs/cockroachcloud/managing-access#create-api-keys). |
| admin-url              | No       | -               | Base URL for the Cluster API which is typically the regional/local cluster load balancer endpoint.                                             |
| data-source-properties | No       | -               | Data source connection parameters.                                                                                                             |

## DataSource Properties

The JDBC datasource configuration for querying node status and running workloads.

```yaml
    application:
      clusters:
          data-source-properties:
            url: "jdbc:postgresql://localhost:26257/defaultdb?sslmode=require"
            username: "craig"
            password: "cockroach"
```

| Field Name | Optional | Default   | Description                                |
|------------|----------|-----------|--------------------------------------------|
| url        | No       | -         | The JDBC connection URL.                   |
| username   | No       | craig     | The SQL user with ADMIN role.              |
| password   | Yes      | cockroach | The SQL user password for secure clusters. |


## Local Cluster Configuration

The default settings are usually sufficient unless you have conflicting network ports occupied.
Edit [config/settings-secure.cfg](config/settings-secure.cfg) or [config/settings-insecure.cfg](config/settings-insecure.cfg) and change the 
details, as necessary.

# Running

Start the app in the background with:
    
    ./cluster-admin start-service

Other alternative:

    ln -sf target/pestcontrol-<version>.jar pestcontrol.jar
    java -jar pestcontrol.jar

Now you can access the application via http://localhost:9090 and login to the cluster of choice.

## Local Cluster Management

This section only apply if you intend to install and operate a local, 
secure or insecure cluster. The default security mode is `insecure`.
Edit `config/settings.cfg` and change `security_mode` to either `secure|insecure`.
The secure mode will use self-signed CA certificates and keys in `.certs` including the PKCS12 truststore
used by the web app.  

Install CockroachDB, start some nodes, HAProxy and initialize the cluster:

    ./cluster-admin install  
    ./cluster-admin certs
    ./cluster-admin start-all (pick 1,2,3 to begin with)
    ./cluster-admin start-lb
    ./cluster-admin init
    ./cluster-admin open

Once the cluster is up, you can either use the web UI or `cluster-admin` to
kill and start nodes.

### Remarks

If you switch between the `secure` and `insecure` modes, re-run the `init` command to
set proper SQL user roles and secrets.

### Shutdown

To shut things down, run the inverse:

    ./cluster-admin stop-service
    ./cluster-admin stop-lb
    ./cluster-admin stop-all
    ./cluster-admin clean

# Appendix: Configuration Files

Pest Control can be configured through the files available in the `config` directory:

1. [settings.sh](config/settings.cfg) - Settings for creating and managing a local CockroachDB cluster.
1. [settings-insecure.sh](config/settings-insecure.cfg) - Settings for using a local CockroachDB self-hosted cluster in insecure mode.
1. [settings-secure.sh](config/settings-secure.cfg) - Settings for using a local CockroachDB self-hosted cluster in secure mode.
1. [init.sql](config/init.sql) - Init SQL statements (optional).
1. [haproxy.cfg](config/haproxy.cfg) - HAProxy configuration for local CockroachDB cluster.
1. [application-default.yml](config/application-default.yml) - Cluster connection settings.

---

That is all, carry on!
