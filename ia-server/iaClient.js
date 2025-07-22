const axios = require('axios');
require('dotenv').config();

async function callMistral(prompt) {
    const res = await axios.post(
        process.env.IA_API_URL,
        {
            model: "mistral-small",
            messages: [
                { role: "user", content: prompt }
            ]
        },
        {
            headers: {
                Authorization: `Bearer ${process.env.IA_API_KEY}`,
                "Content-Type": "application/json"
            }
        }
    );

    return res.data.choices[0].message.content;
}

module.exports = { callMistral };
