package org.springframework.samples.petclinic.vets.system;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;

import static java.text.MessageFormat.format;

@Configuration
public class ServiceRegistry {

    @Autowired
    private ConsulClient consulClient;

    @Autowired
    Environment environment;

    @Value("${spring.application.name}")
    private String applicationName;

    private int index = -1;

    final String serviceKey = "/traefik/http/services/{0}/loadBalancer/servers/";
    final String serverKey = "/traefik/http/services/{0}/loadBalancer/servers/{1}/";
    final String urlKey = "/traefik/http/services/{0}/loadBalancer/servers/{1}/url";


    @PostConstruct
    void addServerMapping() throws Exception {
        String port = environment.getProperty("local.server.port");
        Response<List<String>> keys = consulClient.getKVKeysOnly(format(serviceKey, applicationName));
        index = keys.getValue()!=null ? keys.getValue().size() : 0;
        consulClient.setKVValue(format(urlKey, applicationName,index), String.format("http://%s:%s/","127.0.0.1","8000"));
    }

    @PreDestroy
    void removerServerMapping() throws Exception {
        consulClient.deleteKVValues(format(serverKey, applicationName,index));
    }

}
