package cn.ubiwifi.test;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;

import java.util.*;

public class Main {
	public static final int PARTITION = 15;
	public static final String TOPIC = "probe_status_280";
	public static final String GROUP_ID = "testCon";
	public static final String BROKERS = "kafka3.1.ubiwifi:9092,kafka3.2.ubiwifi:9092,kafka3.3.ubiwifi:9092";
	public static final long OFFSET = 3000000L;
	public static final int POLL_TIME = 100;

    private static Properties properties;
    private static Map<TopicPartition, Long> topicPartitionOffset;

    static {
    	properties = new Properties();
    	properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BROKERS);
    	properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    	properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
    	properties.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
    	properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
				"org.apache.kafka.common.serialization.StringDeserializer");
    	properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
				"org.apache.kafka.common.serialization.StringDeserializer");
    	topicPartitionOffset = new HashMap<>();
    	for (int i = 0; i < PARTITION; i++) {
    		topicPartitionOffset.put(new TopicPartition(TOPIC, i), OFFSET);
		}
	}

	public static boolean persistOffset(ConsumerRecord<String, String> record) {
    	System.out.println(String.format("topic: [%s], partition: [%s], offset: [%s]",
				record.topic(), record.partition(), record.offset()));
    	return true;
	}

	public static void main(String[] args) {
		KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(properties);
		consumer.assign(topicPartitionOffset.keySet());
		for (Map.Entry<TopicPartition, Long> entry: topicPartitionOffset.entrySet()) {
	        consumer.seek(entry.getKey(), entry.getValue());
		}
//        consumer.subscribe(Arrays.asList(new String[]{TOPIC}));
		try {
			while (true) {
				ConsumerRecords<String, String> consumerRecords = consumer.poll(POLL_TIME);
				System.out.println(consumerRecords.isEmpty());
				System.out.println(consumerRecords.count());
				for (ConsumerRecord<String, String> record : consumerRecords) {
					System.out.println(String.format("topic: [%s], partition: [%s], offset: [%s]",
							record.topic(), record.partition(), record.offset()));
				}
				consumer.commitAsync();
			}
		} finally {
			consumer.commitSync();
			consumer.close();
			System.out.println("unconsume successfully!");
		}

	}
}
