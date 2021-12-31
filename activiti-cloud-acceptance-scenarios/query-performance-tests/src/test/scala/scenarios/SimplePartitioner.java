package scenarios;

import java.util.Map;
import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.clients.producer.internals.StickyPartitionCache;
import org.apache.kafka.common.Cluster;
import org.apache.kafka.common.utils.Utils;

public class SimplePartitioner implements Partitioner {
    private final StickyPartitionCache stickyPartitionCache = new StickyPartitionCache();

    public SimplePartitioner() {
    }

    public void configure(Map<String, ?> configs) {
    }

    public int partition(String topic, Object key, byte[] keyBytes, Object value, byte[] valueBytes, Cluster cluster) {
        return this.partition(topic, key, keyBytes, value, valueBytes, cluster, cluster.partitionsForTopic(topic).size());
    }

    public int partition(String topic, Object key, byte[] keyBytes, Object value, byte[] valueBytes, Cluster cluster, int numPartitions) {
        System.out.println("Key=" + key);
        return keyBytes == null ? this.stickyPartitionCache.partition(topic, cluster) : key.hashCode();
    }

    public void close() {
    }

    public void onNewBatch(String topic, Cluster cluster, int prevPartition) {
        this.stickyPartitionCache.nextPartition(topic, cluster, prevPartition);
    }
}
