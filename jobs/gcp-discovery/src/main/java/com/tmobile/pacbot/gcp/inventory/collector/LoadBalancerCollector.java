package com.tmobile.pacbot.gcp.inventory.collector;

import com.amazonaws.util.StringUtils;
import com.google.cloud.compute.v1.*;
import com.tmobile.pacbot.gcp.inventory.auth.GCPCredentialsProvider;
import com.tmobile.pacbot.gcp.inventory.vo.HttpsProxyVH;
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
       Iterable<UrlMap> urlmap= gcpCredentialsProvider.getURLMap(project.getProjectId()).list(project.getProjectId()).iterateAll();
       List<LoadBalancerVH> loadBalancerVHList=new ArrayList<>();
       for(UrlMap u:urlmap) {
           LoadBalancerVH loadBalancerVH = new LoadBalancerVH();
           logger.debug("URL map name: {} URL id :{}", u.getName(), u.getId());
           loadBalancerVH.setUrlMap(u.getName());
           loadBalancerVH.setId(String.valueOf(u.getId()));
           loadBalancerVH.setProjectId(project.getProjectId());
           loadBalancerVH.setProjectName(project.getProjectName());
           String region=u.getSelfLink().substring(u.getSelfLink().indexOf(project.getProjectId()+"/")+project.getProjectId().length()+1,u.getSelfLink().indexOf("/urlMaps"));
           loadBalancerVH.setRegion(region);
           Iterable<TargetHttpsProxy> httpsProxies = gcpCredentialsProvider.getTargetHttpsProxiesClient(project.getProjectId()).list(project.getProjectId()).iterateAll();
           List<String> targetHttpsProxyVH = new ArrayList<>();
           List<String> sslPolicyList=new ArrayList<>();
           List<HttpsProxyVH> httpProxyDetailList = new ArrayList<>();
           List<Boolean> quicEnabledList = new ArrayList<>();

           for (TargetHttpsProxy targetHttpsProxy : httpsProxies) {
               logger.debug("Target proxy :{} {}", targetHttpsProxy.getName(), targetHttpsProxy.getId());
               HttpsProxyVH httpsProxyVH = new HttpsProxyVH();
               httpsProxyVH.setName(targetHttpsProxy.getName());
               httpsProxyVH.setHasCustomPolicy(!StringUtils.isNullOrEmpty(targetHttpsProxy.getSslPolicy()));
               sslPolicyList.add(targetHttpsProxy.getSslPolicy());
               targetHttpsProxyVH.add(targetHttpsProxy.getName());
               httpProxyDetailList.add(httpsProxyVH);
               if(targetHttpsProxy.hasQuicOverride()){
                   quicEnabledList.add(targetHttpsProxy.getQuicOverride().equals("ENABLE"));
               }
           }
           loadBalancerVH.setHttpProxyDetailList(httpProxyDetailList);
           loadBalancerVH.setTargetHttpsProxy(targetHttpsProxyVH);
           loadBalancerVH.setQuicNegotiation(quicEnabledList);

           Iterable<TargetSslProxy> sslProxies=gcpCredentialsProvider.getTargetSslProxiesClient(project.getProjectId()).list(project.getProjectId()).iterateAll();
           for(TargetSslProxy targetSslProxy:sslProxies){
               sslPolicyList.add(targetSslProxy.getSslPolicy());
               targetSslProxy.getName();
           }

           List<SslPolicyVH>sslPolicyVHList=new ArrayList<>();
               Iterable<SslPolicy> sslPolicies = gcpCredentialsProvider.getSslPoliciesClient(project.getProjectId()).list(project.getProjectId()).iterateAll();
               for (SslPolicy sslPolicy : sslPolicies) {
                   SslPolicyVH sslPolicyVH = new SslPolicyVH();
                   sslPolicyVH.setMinTlsVersion(sslPolicy.getMinTlsVersion());
                   sslPolicyVH.setProfile(sslPolicy.getProfile());
                   sslPolicyVH.setEnabledFeatures(sslPolicy.getEnabledFeaturesList());

                   sslPolicyVHList.add(sslPolicyVH);
               }

           loadBalancerVH.setSslPolicyList(sslPolicyVHList);

           String backendServiceName=u.getDefaultService().substring(u.getDefaultService().lastIndexOf('/')+1);
           try {
               BackendServiceLogConfig backendServiceLogConfig = gcpCredentialsProvider.getBackendServiceClient(project.getProjectId()).get(project.getProjectId(), backendServiceName).getLogConfig();
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
