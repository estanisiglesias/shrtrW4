const process = require('process');

const KAFKA_BROKERS = process.env.KAFKA_BROKERS || '127.0.0.1:9092';
const KAFKA_LISTEN_TO_TOPICS = process.env.KAFKA_LISTEN_TO_TOPICS || 'User-created,User-updated,User-deleted';
const KAFKA_GROUP_ID = process.env.KAFKA_GROUP_ID || 'reporting-service';
const KAFKA_CLIENT_ID = process.env.KAFKA_CLIENT_ID || 'reporting-service';

const ES_HOSTS = process.env.ES_HOSTS || 'http://127.0.0.1:9200';

module.exports = {
    KAFKA_BROKERS,
    KAFKA_LISTEN_TO_TOPICS,
    KAFKA_GROUP_ID,
    KAFKA_CLIENT_ID,
    ES_HOSTS,
}