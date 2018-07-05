<img src="http://wiki.dbpedia.org/sites/default/files/DBpediaLogoFull.png" width="200" height="100" align="right" alt="DBpedia Logo"/>

# Ontology driven REST-Api V1.0.0
 
 Software Engineering Project Uni Leipzig WS 2017/18
 
--- 

# Contents
1. [Description](#1-description)
2. [Requirements](#2-requirements)
3. [Installation](#3-installation)
4. [Usage](#4-usage)
5. [Configuration](#5-configuration)
 
---
 
### 1 Description
[DBpedia](http://wiki.dbpedia.org/) is one of the largest, open and freely accessible knowledge graphs about entities 
(such as movies, actors, athletes, politicians, places, ...) of our everyday life. Information contained in Wikipedia articles
are extracted into DBpedia as RDF, using mappings between Wikipedia infoboxes and the DBpedia Ontology to become accessible for complex requests.
Accessing DBpedias knowledge requires the use of SPARQL, the data query language for RDF databases.

This API provides a [REST](https://de.wikipedia.org/wiki/Representational_State_Transfer)-conform interface.
It is intended to be used by web developers who want easier DBpedia access.
The API uses the DBpedia Ontology and transforms HTTP requests into SPARQL queries, which are then sent to the [DBpedia endpoint](https://dbpedia.org/sparql).
The results can be returned in a range of formats, such as JSON, JSON-LD, TSV and styles (e.g. nested JSON).

Additional features of the API:
* Versioning (e.g. after an update of the ontology or if labels of DBpedia resources changed)
* Easy access control through API keys
* Caching
* Logging
* Windowing
* Documentation through Swagger UI


This README provides a brief overview and introduction for the administrator of the API to the software and its usage.

Further documentation:
* **Research report** for a basic grasp about discussed topics and technologies (DBpedia, RDF, Spring, ...)
* **Requirements specification** for a more precise specification of all features
* **Software design descriptin** for an understanding of the source code

### 2 Requirements
It is required that `maven` and at least`openjdk 8` are installed. Other Java versions, such as JDK 9, may work as well.
If the curl client is intended to be used for testing purposes, then `curl` and a shell are also required.
To install the projects straight from the [git repository](https://git.informatik.uni-leipzig.de/swp17/jf17a), you'll also need `git`.

**Arch Linux:**
```sh
pacman -S jdk8-openjdk maven curl git
```

### 3 Installation
The `jar` package is already included in the release package, so this step can be skipped and you can continue with step 4.
Otherwise, you'll need to clone the repo and build the project with maven. 

```sh
git clone https://github.com/dbpedia/ontology-driven-api dbpedia-rest-api
cd dbpedia-rest-api
mvn clean package
```

This listing creates a jar file named `target/dbpedia-api-1.0.0.jar`, which includes all dependencies needed for execution.
Therefore, it can be copied as you wish. But keep in mind to copy the folder `config`, where by default external resources and config files are saved.
(The path to those files can also be configured, skip to section [Configuration](#5-configuration) for more information.

### 4 Usage
To start the API, type 
```sh
java -jar target/dbpedia-api-1.0.0.jar
```
into a shell. The API will run on port 8080, which can be verified on the info page, which can be accessed on `http://localhost:8080/api/info`.

You can get an overview about the functionality with [Swagger-UI](https://swagger.io/) through `http://localhost:8080/swagger-ui.html`.
Swagger is also used as external documentation for users of the API.

A shell script is available at `dev/curl-em-all.sh` for more request examples or to generate and run all types of requests.
You will need a shell to run it.
To display an overview about the functionality, view the help page with `./curl-em-all.sh -h`.

```
 >>> Curl client for dbpedia rest api V1.0.0 <<< 
Usage:
 -f [TSV|RDF|JSONLDF] for format
 -p [PREFIX|SHORT|NESTED] for prettyfication
 -s to walk you through every api-call (s stands for slow)
 -v [version] for setting the API version
 -k [key] sets the API Key
```
For example, `./curl-em-all.sh -s` is suitable for a step by step presentation of all functionality specified in the requirements specification.

### 5 Configuration 
Configuration files and external resources (such as the DBpedia ontology) are placed in the folder `target/config` by default.
This folder contains the following files:
```
config
├── application.properties
├── dbpedia_2016-10.owl
├── keys.txt
├── mapped_properties_per_class.json
├── prefixes.json
└── versions
    └── 1_0_0.version.json
```
The file `target/config/application.properties` is used to configure various parameters from the API, including:
* **server.port**: Port to access the API
* **dbpedia.sparqlEndpoint**: SPARQL endpoint to send requests to
* **ontology.file**: Path to DBpedia ontology; this has to be replaced upon DBpedia update
* **window.maxWindowLimit**: Maximum value of the windowing query parameter
* **prefixes.file**: Path to the file containing namespace prefixes
* **versions.dir**: Directory with version files
* **uri.path** URI path to access the API `localhost:8080/[uri.path]/`
* **spring.cache.type=NONE** (optional) to switch off the cache 
* **keys.usingKeys** Toggle usage of API keys
* **keys.file**: Path to the file containing the API keys (see below)
* **keys.startQuotaDay**, **keys.startQuotaHour** and **keys.startQuotaMinute**: Sets usage quotas for all users

All file paths need to be given relative to the .jar file. Parameters can also be provided as command line argument:

```sh
java -jar target/dbpedia-api-1.0.0.jar --server.port=4200
```

##### API-Keys
Access to the API is restricted by API keys in order to limit the number of requests per user during specific time intervals (minute, hour, day). A list of all allowed keys are set up in `config/keys.txt`. For every key (= user), a statistic is created that keeps track of the quotas. This data is not persistent after a restart.

If a key ends with `_ADMIN`, a user gets unlimited (MAX_INT) quotas.

The key is provided in the URI, using the required parameter `&key=`. 

##### Prefixes.json

This file contains all supported prefixes and assigns namespaces to them. By default, prefixes from [prefix.cc](http://prefix.cc) are used.
Names of prefixes and namespaces can be changed, but it is recommended to create a new API version afterwards (see below).
**Important:** It is strongly recommended _not_ to change the prefixes *dbo*, *dbp*, *dbr*, *rdf* and *rdfs*.

##### mapped_properties_per_class.json
Contains important properties of various classes for a RML request. Classes are saved as an array, with all associated properties.
Properties have to be of format "prefix:propertyName". The identifier for class and property names within the JSON file is "@id".

##### Update of the ontology and versioning
If the DBpedia ontology changes, it is likely that an API update is needed, because entity names ("identifiers") might have changed.
To make requests from older versions possible, patch files are used. They define the replacement of old identifiers in requests.

First, the ontology from DBpedia has to be downloaded and the field `ontology.file` in `application.properties` must be updated.

It is optional to create a new patch file in `config/versions`. All files in this folder that end with `version.json` are loaded
on startup.

An example patch file `config/versions/v1_1_0.version.json` looks like this:

```
{
  "major": 1,
  "minor": 1,
  "patch": 0,
  "resourceReplacements": [
    {
      "prefixBefore": "dbp",
      "identifierBefore": "numOfEmployees",
      "prefixNow": "dbp",
      "identifierNow": "numberOfEmployees"
    },
    {
      "prefixBefore": "dbp",
      "identifierBefore": "some-property-that-does-not-work-anymore",
      "prefixNow": "foaf",
      "identifierNow": "surname"
    }
  ],
  "prefixReplacements": {
    "foaff": "foaf",
    "old-dbo": "dbo"
  }
}
```
Explanation:
* **major**, **minor** and **patch** define the version number
* **resourceReplacements**: An array, which contains replacements of resources within DBpedia. With the file above, *dbp:numOfEmployees* is replaced with *dbp:numberOfEmployees*
and *dbp:some-property-that-does-not-work-anymore* with *foaf:surname*.
* **prefixReplacements**: An object that contains prefix replacements which are applied to all resources with this prefix.
*foaff* is replaced with *foaf* and *old-dbo* with *dbo*.

The API follows the principle of [Semantic Versioning](https://semver.org/), i.e. the *Major* version should only be changed if there are incompatible changes in the ontology.
Requests to incompatible versions have to contain `&oldVerion=true` in the URI.
If not, error code 400 is returned.

##### Logging
The API logs every request to the directory `logs`. The logfile is a simple textfile that is archived daily. This may look like this:

```
logs
├── archive
│   ├── rollingfile.log.2018-03-22.gz
│   ├── rollingfile.log.2018-03-24.gz
│   ├── rollingfile.log.2018-03-25.gz
│   ├── rollingfile.log.2018-03-28.gz
│   ├── rollingfile.log.2018-04-03.gz
│   ├── rollingfile.log.2018-04-04.gz
│   └── rollingfile.log.2018-04-07.gz
└── logfile.log
```
The following information is logged in TSV:
* the SPARQL query of the request
* quotas of keys
* duration of response (number of characters)
* duration of response
* occurred errors



##### Caching
`ehcache2` is used to cache responses from the SPARQL endpoint. The cache is saved within the local swap, so it will be rebuilt after restart.
