package com.tmobile.pacbot.gcp.inventory.collector;

import com.google.cloud.pubsub.v1.TopicAdminClient;
import com.google.pubsub.v1.ProjectName;
import com.google.pubsub.v1.Topic;
import com.tmobile.pacbot.gcp.inventory.auth.GCPCredentialsProvider;

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
    public List<TopicVH> fetchPubSubInventory(String project)  {
        logger.debug("PubSubInventoryCollector started");
        List<TopicVH> topicList = new ArrayList<>();
        logger.debug("Before getting TopicAdminClient");
        TopicAdminClient topicAdminClient = null;
        try {
            topicAdminClient = gcpCredentialsProvider.getTopicClient();
            logger.debug("After getting TopicAdminClient {}",topicAdminClient);
            TopicAdminClient.ListTopicsPagedResponse topics = topicAdminClient.listTopics(ProjectName.of(project));
            logger.debug("Size of ListTopicsPagedResponse {}",topics);
            for (Topic topic : topics.iterateAll()) {
                logger.debug("Inside for loop of list topics");
                TopicVH topicVH=new TopicVH();
                topicVH.setKmsKeyName(topic.getKmsKeyName()==""?null:topic.getKmsKeyName());
                topicVH.setProjectName(project);
                topicVH.setId(topic.getName());
                topicList.add(topicVH);
            }
            logger.debug("After populating topicList");
            return topicList;
        } catch (Exception e) {
            logger.error("Exception has occurred {}",e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }
}
