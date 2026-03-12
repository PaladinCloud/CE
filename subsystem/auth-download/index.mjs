import jwt from 'jsonwebtoken'
import jwkToPem from 'jwk-to-pem'
import { DynamoDBClient, GetItemCommand } from "@aws-sdk/client-dynamodb"

let cachedKeys = {}

// CONFIG 
const CONFIG  = {
    region: "",
    jwksURL: "",
    tableName: "",
    accessClaim: "custom:accessId"
}

// Fetch Cognito JWKS
async function getPublicKeys(jwksURL) {

    if (cachedKeys[jwksURL]) {
        return cachedKeys[jwksURL]
    }

    const res = await fetch(jwksURL)
    const { keys } = await res.json()

    const pems = {}

    keys.forEach(k => {
        pems[k.kid] = jwkToPem(k)
    })

    cachedKeys[jwksURL] = pems

    return pems
}

// Validate JWT Token
const validateToken = async (token, jwksURL) => {
    try {
        const sections = token.split('.')
        if (sections.length !== 3) return null

        const header = JSON.parse(
            Buffer.from(sections[0], 'base64').toString('utf8')
        )

        const pems = await getPublicKeys(jwksURL)
        const pem = pems[header.kid]
        if (!pem) return null

        const decoded = jwt.verify(token, pem, { algorithms: ['RS256'] })

        if (decoded.token_use !== 'id') return null

        return decoded
    } catch (err) {
        console.error("Token validation error:", err)
        return null
    }
}

// Extract folderName + domainName
const extractDetails = (request) => {

    const uri = request.uri

    const pathParts = uri.replace(/^\/+/, '').split('/')
    if (!pathParts[0]) {
        throw new Error("invalid URI format")
    }

    const folderName = pathParts[0]

    const domainName = request.headers.host?.[0]?.value || "unknown"

    const authHeader = request.headers.authorization?.[0]?.value

    if (!authHeader || !authHeader.startsWith("Bearer ")) {
        throw new Error("invalid authorization header")
    }

    const token = authHeader.replace("Bearer ", "")

    return { domainName, folderName, token }
}

// Validate access from DynamoDB
const validateAccess = async (tenantId, region, domainName, folderName) => {

    const client = new DynamoDBClient({ region })

    const command = new GetItemCommand({
        TableName: CONFIG.tableName,
        Key: {
            tenant_id: { S: tenantId }
        }
    })

    const result = await client.send(command)

    if (!result.Item) {
        throw new Error("No tenant found")
    }

    const s3FolderName = result.Item.s3_folder_name?.S
    const s3DistributionDomainName = result.Item.distribution_domain_name?.S

    return (s3FolderName === folderName && s3DistributionDomainName === domainName)
}

// Lambda@Edge handler
export const handler = async (event) => {

    try {
        
        const request = event.Records[0].cf.request

        if (!CONFIG.region || !CONFIG.jwksURL) {
            return unauthorized()
        }

        const { domainName, folderName, token } = extractDetails(request)
        const claims = await validateToken(token, CONFIG.jwksURL)

        if (!claims) {
            console.log("Token validation failed.")
            return unauthorized()
        }

        const tenantId = claims[CONFIG.accessClaim]

        if (!tenantId) {
            console.log("Tenant Id not found.")
            return unauthorized()
        }

        const accessAllowed = await validateAccess(tenantId, CONFIG.region, domainName, folderName)

        if (!accessAllowed) {
            console.log("Validation failed")
            return unauthorized()
        }

        return request

    } catch (err) {
        console.error("Authorization error:", err)
        return unauthorized()
    }
}

// Unauthorized response
const unauthorized = () => ({
    status: '401',
    statusDescription: 'Unauthorized',
    body: 'Unauthorized',
    headers: {
        'www-authenticate': [
            { key: 'WWW-Authenticate', value: 'Bearer' }
        ],
        'content-type': [
            { key: 'Content-Type', value: 'text/plain' }
        ]
    }
})