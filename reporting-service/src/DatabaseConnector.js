const { ES_HOSTS} = require("./settings");
const { Client } = require('@elastic/elasticsearch')

const esClient = new Client({ node: ES_HOSTS })

const store = ({topic, message}) => {
    esClient.index({index: topic.toLowerCase(), document: message});
}
module.exports = {
    store
}