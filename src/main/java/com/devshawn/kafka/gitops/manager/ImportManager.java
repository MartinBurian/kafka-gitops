package com.devshawn.kafka.gitops.manager;

import com.devshawn.kafka.gitops.domain.state.DesiredStateFile;
import com.devshawn.kafka.gitops.domain.state.TopicDetails;
import com.devshawn.kafka.gitops.service.KafkaService;
import org.apache.kafka.clients.admin.Config;
import org.apache.kafka.clients.admin.ConfigEntry;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.common.config.ConfigResource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ImportManager {
    private final KafkaService kafkaService;

    public ImportManager(KafkaService kafkaService) {
        this.kafkaService = kafkaService;
    }

    public DesiredStateFile importState() {
        Map<String, TopicDescription> topics = kafkaService.getTopics();
        List<String> topicNames = topics.entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toList());
        Map<String, List<ConfigEntry>> topicConfigs = fetchTopicConfigurations(topicNames);

        DesiredStateFile.Builder stateBuilder = new DesiredStateFile.Builder();

        topics.forEach((name, description) -> {
            stateBuilder.putTopics(name, new TopicDetails.Builder()
                    .setPartitions(description.partitions().size())
                    .setReplication(description.partitions().get(0).replicas().size())
                    .putAllConfigs(topicConfigs.get(name).stream()
                            .filter((entry) -> !entry.isDefault())
                            .collect(Collectors.toMap(ConfigEntry::name, ConfigEntry::value)))
                    .build());
        });

        return stateBuilder.build();
    }

    private Map<String, List<ConfigEntry>> fetchTopicConfigurations(List<String> topicNames) {
        Map<String, List<ConfigEntry>> map = new HashMap<>();
        Map<ConfigResource, Config> configs = kafkaService.describeConfigsForTopics(topicNames);
        configs.forEach((key, value) -> map.put(key.name(), new ArrayList<ConfigEntry>(value.entries())));
        return map;
    }
}
