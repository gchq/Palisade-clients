package uk.gov.gchq.palisade.clients.simpleclient.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.openfeign.FeignClientBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import uk.gov.gchq.palisade.clients.simpleclient.config.ApplicationConfiguration.ClientConfiguration;

import java.util.Map;

@Component
@Profile("!eureka")
class UrlDataClient implements DynamicDataClient {

    private final FeignClientBuilder feignClientBuilder;
    private final Map<String, String> dataServices;

    UrlDataClient(@Autowired final ApplicationContext appContext, @Autowired final ClientConfiguration dataServices) {
        this.feignClientBuilder = new FeignClientBuilder(appContext);
        this.dataServices = dataServices.getClient();
    }

    public DataClient clientFor(final String serviceId) {
        return feignClientBuilder
                .forType(DataClient.class, serviceId)
                .url(dataServices.getOrDefault(serviceId, serviceId))
                .build();
    }
}
