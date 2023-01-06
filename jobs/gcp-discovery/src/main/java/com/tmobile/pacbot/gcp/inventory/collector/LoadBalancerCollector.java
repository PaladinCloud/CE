package com.tmobile.pacbot.gcp.inventory.collector;

import com.google.cloud.compute.v1.*;
import com.tmobile.pacbot.gcp.inventory.auth.GCPCredentialsProvider;
import com.tmobile.pacbot.gcp.inventory.vo.LoadBalancerVH;
import com.tmobile.pacbot.gcp.inventory.vo.ProjectVH;
import com.tmobile.pacbot.gcp.inventory.vo.SslPolicyVH;

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

           Iterable<TargetHttpsProxy> httpsProxies = gcpCredentialsProvider.getTargetHttpsProxiesClient().list(project.getProjectId()).iterateAll();
           List<String> targetHttpsProxyVH = new ArrayList<>();
           List<String> sslPolicyList=new ArrayList<>();
           List<Boolean> quicEnabledList = new ArrayList<>();
           for (TargetHttpsProxy targetHttpsProxy : httpsProxies) {
               logger.debug("Target proxy :{} {}", targetHttpsProxy.getName(), targetHttpsProxy.getId());
               sslPolicyList.add(targetHttpsProxy.getSslPolicy());
               targetHttpsProxyVH.add(targetHttpsProxy.getName());
               quicEnabledList.add(targetHttpsProxy.hasQuicOverride());
           }
           loadBalancerVH.setTargetHttpsProxy(targetHttpsProxyVH);
           loadBalancerVH.setQuicNegotiation(quicEnabledList);

           Iterable<TargetSslProxy> sslProxies=gcpCredentialsProvider.getTargetSslProxiesClient().list(project.getProjectId()).iterateAll();
           for(TargetSslProxy targetSslProxy:sslProxies){
               sslPolicyList.add(targetSslProxy.getSslPolicy());
               targetSslProxy.getName();
           }

           List<SslPolicyVH>sslPolicyVHList=new ArrayList<>();

           for(String ssl_Policy:sslPolicyList) {

               SslPolicyVH sslPolicyVH = new SslPolicyVH();

               Iterable<SslPolicy> sslPolicies = gcpCredentialsProvider.getSslPoliciesClient().list(ssl_Policy).iterateAll();
               for (SslPolicy sslPolicy : sslPolicies) {
                   sslPolicyVH.setMinTlsVersion(sslPolicy.getMinTlsVersion());
                   sslPolicyVH.setProfile(sslPolicy.getProfile());
                   sslPolicyVH.setEnabledFeatures(sslPolicy.getEnabledFeaturesList());

                   sslPolicyVHList.add(sslPolicyVH);
               }
           }

           loadBalancerVH.setSslPolicyList(sslPolicyVHList);

           String backendServiceName=u.getDefaultService().substring(u.getDefaultService().lastIndexOf('/')+1);
           try {
               BackendServiceLogConfig backendServiceLogConfig = gcpCredentialsProvider.getBackendServiceClient().get(project.getProjectId(), backendServiceName).getLogConfig();
               loadBalancerVH.setLogConfigEnabled(backendServiceLogConfig.getEnable());
           }catch (Exception e)
           {
               loadBalancerVH.setLogConfigEnabled(true);
           }
           loadBalancerVHList.add(loadBalancerVH);
       }

        return loadBalancerVHList;
    }
}
