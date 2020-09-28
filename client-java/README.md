# Palisade Client (Java)

Sequence:

```
title Client Processing

client-java->+Palisade-service: Request
Palisade-service->-client-java: Response
client-java->filtered-resource-service: open web socket
loop Process Resources
    filtered-resource-service->client-java: Resource
    client-java->+data-service: Resource
    data-service->-client-java: Records
    client-java->+Deserializer: Records
    Deserializer->-client-java: Objects
    client-java->Receiver: output
end
filtered-resource-service->client-java: Finished
client-java->filtered-resource-service: Close
```
