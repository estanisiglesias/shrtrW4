const {consume, consumerSetup} =  require("./QueueConsumer");
const {store} = require("./DatabaseConnector");


/*
kafka ---- event ---> [service] ---- data ---> service_db
 */


const main = async () => {
    await consumerSetup();
    await consume(event =>{
        store(event)
    });
}

main();