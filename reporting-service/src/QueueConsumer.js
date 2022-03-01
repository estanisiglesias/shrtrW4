const {KAFKA_BROKERS, KAFKA_CLIENT_ID, KAFKA_GROUP_ID, KAFKA_LISTEN_TO_TOPICS} = require("./settings");
const { Kafka } = require('kafkajs')

const brokers = KAFKA_BROKERS.split(',')

const kafka = new Kafka({
    clientId: KAFKA_CLIENT_ID,
    brokers: brokers
});

const consumer = kafka.consumer({ groupId: KAFKA_GROUP_ID });

const consumerSetup = async () => {
    await Promise.all(
        KAFKA_LISTEN_TO_TOPICS.split(',').map(async topic => {
            const consuming = await consumer.subscribe({ topic })
            console.log(consuming)
        })
    )
}

const consume = async (onEachMessage) => {
    await consumer.run({
        eachMessage: async ({ topic, partition, message, heartbeat }) => {
            onEachMessage({
                topic,
                partition,
                message: JSON.parse(message.value.toString())
            });
        },
    })
}

module.exports = {
    consumerSetup,
    consume
}