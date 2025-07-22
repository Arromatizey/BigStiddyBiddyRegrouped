const { Kafka } = require('kafkajs');
const { callMistral } = require('./iaClient');
require('dotenv').config();

const kafka = new Kafka({
    clientId: 'ia-server',
    brokers: [process.env.KAFKA_BROKER],
});

const consumer = kafka.consumer({ groupId: process.env.KAFKA_GROUP_ID });
const producer = kafka.producer();

const run = async () => {
    await producer.connect();
    await consumer.connect();

    await consumer.subscribe({ topic: 'ai-room-events' });
    await consumer.subscribe({ topic: 'ai-message-events' });

    console.log("🤖 IA Server is listening for room and message events...");

    await consumer.run({
        eachMessage: async ({ topic, partition, message }) => {
            const payload = JSON.parse(message.value.toString());
            console.log(`📥 Received on ${topic}:`, payload);

            if (topic === 'ai-room-events') {
                const systemPrompt = `You just joined a study room. The topic is "${payload.roomTopic}". Introduce yourself as StudyBuddy AI and offer to help.`;
                const aiResponse = await callMistral(systemPrompt);

                await producer.send({
                    topic: 'ia-response',
                    messages: [{
                        key: payload.roomId,
                        value: JSON.stringify({
                            roomId: payload.roomId,
                            userId: payload.userId,
                            response: aiResponse
                        }),
                    }],
                });
                console.log(`🧠 Replied to AI room join with: "${aiResponse}"`);
            }

            if (topic === 'ai-message-events') {
                const context = payload.context.join('\n');
                const userInput = payload.content;
                const prompt = `${context}\nAI:`;

                const aiResponse = await callMistral(prompt);

                await producer.send({
                    topic: 'ai-response-events',
                    messages: [{
                        key: payload.roomId,
                        value: JSON.stringify({
                            roomId: payload.roomId,
                            response: aiResponse
                        }),
                    }],
                });
                console.log(`💬 Replied to message in room ${payload.roomId}`);
                console.log(`🧠 AI Response: "${aiResponse}"`);
            }
        },
    });
};

run().catch(console.error);
