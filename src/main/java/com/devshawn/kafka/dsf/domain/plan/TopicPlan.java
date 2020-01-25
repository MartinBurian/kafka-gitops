package com.devshawn.kafka.dsf.domain.plan;

import com.devshawn.kafka.dsf.domain.state.TopicDetails;
import com.devshawn.kafka.dsf.enums.PlanAction;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.inferred.freebuilder.FreeBuilder;

import java.util.List;
import java.util.Optional;

@FreeBuilder
@JsonDeserialize(builder = TopicPlan.Builder.class)
public interface TopicPlan {

    String getName();

    PlanAction getAction();

    Optional<TopicDetails> getTopicDetails();

    List<TopicConfigPlan> getTopicConfigPlans();

    class Builder extends TopicPlan_Builder {
    }
}
