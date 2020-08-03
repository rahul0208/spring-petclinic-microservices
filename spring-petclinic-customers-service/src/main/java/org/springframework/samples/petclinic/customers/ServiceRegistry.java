package org.springframework.samples.petclinic.customers;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.context.ServletWebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PreDestroy;
import java.util.List;

import static java.text.MessageFormat.format;

@Configuration
public class ServiceRegistry implements ApplicationListener<ServletWebServerInitializedEvent> {

    @Autowired
    private ConsulClient consulClient;

    @Value("${spring.application.name}")
    private String applicationName;

    private int index = -1;

    final String serviceKey = "/traefik/http/services/{0}/loadBalancer/servers/";
    final String serverKey = "/traefik/http/services/{0}/loadBalancer/servers/{1}/";
    final String urlKey = "/traefik/http/services/{0}/loadBalancer/servers/{1}/url";

    @PreDestroy
    void removerServerMapping() {
        if(index > -1) {
            consulClient.deleteKVValues(format(serverKey, applicationName, index));
        }
    }

    @Override
    public void onApplicationEvent(ServletWebServerInitializedEvent servletWebServerInitializedEvent) {
        int serverPort=servletWebServerInitializedEvent.getWebServer().getPort();
        addServerMapping(serverPort);

    }
    void addServerMapping(int port)  {
        Response<List<String>> keys = consulClient.getKVKeysOnly(format(serviceKey, applicationName));
        index = keys.getValue()!=null ? keys.getValue().size() : 0;
        consulClient.setKVValue(format(urlKey, applicationName,index), format("http://{0}:{1,number,#}/","127.0.0.1",port));
    }
}
