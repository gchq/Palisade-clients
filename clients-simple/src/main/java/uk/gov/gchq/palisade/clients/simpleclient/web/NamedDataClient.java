package uk.gov.gchq.palisade.clients.simpleclient.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.openfeign.FeignClientBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("eureka")
class NamedDataClient implements DynamicDataClient {

    private final FeignClientBuilder feignClientBuilder;

    NamedDataClient(@Autowired final ApplicationContext appContext) {
        this.feignClientBuilder = new FeignClientBuilder(appContext);
    }

    public DataClient clientFor(final String serviceId) {
        return feignClientBuilder
                .forType(DataClient.class, serviceId)
                .build();
    }
}
