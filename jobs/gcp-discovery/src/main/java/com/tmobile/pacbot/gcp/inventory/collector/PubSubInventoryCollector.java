package com.tmobile.pacbot.gcp.inventory.collector;

import com.google.cloud.pubsub.v1.TopicAdminClient;
import com.google.pubsub.v1.ProjectName;
import com.google.pubsub.v1.Topic;
import com.tmobile.pacbot.gcp.inventory.auth.GCPCredentialsProvider;

import com.tmobile.pacbot.gcp.inventory.vo.ProjectVH;
import com.tmobile.pacbot.gcp.inventory.vo.TopicVH;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.ArrayList;

import java.util.List;
@Component
public class PubSubInventoryCollector {
    @Autowired
    GCPCredentialsProvider gcpCredentialsProvider;

    private static final Logger logger = LoggerFactory.getLogger(PubSubInventoryCollector.class);
    public List<TopicVH> fetchPubSubInventory(ProjectVH project)  {
        logger.debug("PubSubInventoryCollector started");
        List<TopicVH> topicList = new ArrayList<>();
        logger.debug("Before getting TopicAdminClient");
        TopicAdminClient topicAdminClient = null;
        try {
            topicAdminClient = gcpCredentialsProvider.getTopicClient(project.getProjectId());
            logger.debug("After getting TopicAdminClient {}",topicAdminClient);
            TopicAdminClient.ListTopicsPagedResponse topics = topicAdminClient.listTopics(ProjectName.of(project.getProjectId()));
            logger.debug("Size of ListTopicsPagedResponse {}",topics);
            for (Topic topic : topics.iterateAll()) {
                logger.debug("Inside for loop of list topics");
                TopicVH topicVH=new TopicVH();
                topicVH.setKmsKeyName(topic.getKmsKeyName()==""?null:topic.getKmsKeyName());
                topicVH.setTags(topic.getLabelsMap());
                topicVH.setProjectName(project.getProjectName());
                topicVH.setRegion(project.getRegion());
                topicVH.setProjectId(project.getProjectId());
                topicVH.setId(topic.getName());
                topicList.add(topicVH);
            }
            logger.debug("After populating topicList");
            return topicList;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
