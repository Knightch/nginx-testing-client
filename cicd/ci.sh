docker build -t ss-eefa/client .
docker tag ss-eefa/client local.eefa.io/ss-eefa/client
docker push local.eefa.io/ss-eefa/client