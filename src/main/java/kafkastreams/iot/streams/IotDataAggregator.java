package kafkastreams.iot.streams;

import com.fasterxml.jackson.annotation.JsonProperty;
import kafkastreams.iot.model.IotDataMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class IotDataAggregator {
    @JsonProperty("id")
    String id;
    @JsonProperty("currentAgg")
    Map<String, Integer[]> currentAggBag = new HashMap<>();

    public IotDataAggregator agg(String id, IotDataMessage dataMessage) {
        this.id = id;

        for (Map.Entry<String, Integer> metric : dataMessage.getMetrics().entrySet()) {
            if (currentAggBag.containsKey(metric.getKey())) {
                Integer[] currentAgg = currentAggBag.get(metric.getKey());
                currentAgg[0] += metric.getValue();
                currentAgg[1] += 1;
                currentAggBag.put(metric.getKey(), currentAgg);
            } else {
                currentAggBag.put(metric.getKey(), new Integer[]{metric.getValue(), 1});
            }
        }

        return this;
    }

    public Map<String, Integer> generateAggMetrics() {
        return currentAggBag.entrySet().stream()
                .collect(
                        Collectors.toMap(
                                entry -> entry.getKey(),
                                entry -> {
                                    Integer[] currentAgg = entry.getValue();
                                    return currentAgg[0] / currentAgg[1];
                                }
                        )
                );
    }
}
