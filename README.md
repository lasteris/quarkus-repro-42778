## Description
This project is reproducing issue https://github.com/quarkusio/quarkus/issues/42778

## Structure

### OmsResource
This resource contains 2 endpoints:
- api/slow1000


    imitates slow work (respond after 1000ms

- api/slow

    
    calls slow1000 via preconfigured rest-client. Look into resources/application.properties. 
    Client configured with connectTimeout 500ms and connectionPoolSize 150.
    But it works like connectTimeout 500ms and connectionPoolSize still equals to 5.
    I've set connectTimeout=500ms to prove, that if 6+call has not enough time (less 1s in case of 1000ms endpoint response time),
    so, it just failes, despite  connectionPoolSize > 5.   

Please run quarkusDev
and exec:
```
curl --location --request PATCH 'http://127.0.0.1:8080/reproducer/42778/api/slow'
```
Look into logs, you will see IllegalStateException sourced by only one of 6 Threads.

### ConnectionPoolSizeViaBuilderTest

This test suite proves, that none of methods lets set connectionPoolSize properly.

