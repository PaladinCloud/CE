// Run "npm install" to generate node_modules folder.
// Add Bearer token as value for Authorization key.
// run "node test.mjs"

import { handler } from "./index.mjs"

const event = {
    Records: [
        {
            cf: {
                request: {
                    uri: "/download-report/report.csv",
                    method: "GET",
                    headers: {
                        host: [
                            {
                                key: "Host",
                                value: ""
                            }
                        ],
                        authorization: [
                            {
                                key: "Authorization",
                                value: ""
                            }
                        ]
                    }
                }
            }
        }
    ]
}

async function runTest() {
    const result = await handler(event)
    console.log("Lambda Result:")
    console.log(JSON.stringify(result, null, 2))
}

runTest()
