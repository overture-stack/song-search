# song-search
Song Search - GQL microservice for searching maestro generated song indexes

## Graphql

* Graphql Endpoint:
 
    `POST /graphql`

* With `secure` enabled

    Include Authorization header with token: `{ Authorization: Bearer $JWT }`

* full schema can be found here: `./src/main/resources/schema.graphql`

#### Apollo Federation support
This service has support for Apollo Federation which extends some gql types found in `workflow-search`.
 
With a service like `rdpc-gateway` the schemas from these two services can be federated into a larger schema that joins the entities.  

## Configuration

Configuration is setup in `./src/main/resources/application.yaml`

#### Elasticsearch
This service requires two elastic search index, `analysisCentricIndex` and `fileCentricIndex`. 

The mappings for these indices can be found from Maestro's mapping: 

analysisCentric: https://github.com/overture-stack/maestro/blob/master/maestro-app/src/main/resources/analysis_centric.json

fileCentric: https://github.com/overture-stack/maestro/blob/master/maestro-app/src/main/resources/file_centric.json

Configure other es properties as requried.

#### Secure profile
 There is a `secure` profile that enables Oauth2 scope based authorization on requests. 
 
 Configure `jwtPublicKeyUrl` (or `jwtPublicKeyStr` for dev setup) in conjunction with the JWTs issuer. Also configure the expected scopes if needed.

## Test
```bash
mvn clean test
```

## Build
With maven:
```bash
mvn clean package
```

With docker:
```bash 
docker build . -t icgcargo/song-search:test
```

## Run
Maven with app default and secure profile:
```bash
mvn spring-boot:run
```
```bash
mvn -Dspring-boot.run.profiles=secure spring-boot:run
```

Docker with app default and secure profile:
```bash
docker run icgcargo/song-search:test
```
```bash
docker run -e "SPRING_PROFILES_ACTIVE=secure" icgcargo/song-search:test
```
