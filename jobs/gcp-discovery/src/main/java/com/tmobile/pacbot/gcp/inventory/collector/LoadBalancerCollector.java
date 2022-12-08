package com.tmobile.pacbot.gcp.inventory.collector;

import com.google.cloud.compute.v1.TargetHttpProxy;
import com.google.cloud.compute.v1.UrlMap;
import com.tmobile.pacbot.gcp.inventory.auth.GCPCredentialsProvider;
import com.tmobile.pacbot.gcp.inventory.vo.LoadBalancerVH;
import com.tmobile.pacbot.gcp.inventory.vo.ProjectVH;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class LoadBalancerCollector {
    @Autowired
    GCPCredentialsProvider gcpCredentialsProvider;
    private static final Logger logger = LoggerFactory.getLogger(LoadBalancerCollector.class);
    public List<LoadBalancerVH> fetchLoadBalancerInventory(ProjectVH project) throws IOException {
       Iterable<UrlMap> urlmap= gcpCredentialsProvider.getURLMap().list(project.getProjectId()).iterateAll();
       List<LoadBalancerVH> loadBalancerVHList=new ArrayList<>();
       for(UrlMap u:urlmap) {
           LoadBalancerVH loadBalancerVH = new LoadBalancerVH();
           logger.debug("URL map name: {} URL id :{}", u.getName(), u.getId());
           loadBalancerVH.setUrlMap(u.getName());
           loadBalancerVH.setId(String.valueOf(u.getId()));
           Iterable<TargetHttpProxy> httpProxies = gcpCredentialsProvider.getTargetHttpProxiesClient().list(project.getProjectId()).iterateAll();
           List<String> targetHttpProxyVH = new ArrayList<>();
           for (TargetHttpProxy targetHttpProxy : httpProxies) {
               logger.debug("Target proxy :{} {}", targetHttpProxy.getName(), targetHttpProxy.getId());
               targetHttpProxyVH.add(targetHttpProxy.getName());
           }
           loadBalancerVH.setTargetHttpProxy(targetHttpProxyVH);
           loadBalancerVHList.add(loadBalancerVH);
       }

        return loadBalancerVHList;
    }
}
