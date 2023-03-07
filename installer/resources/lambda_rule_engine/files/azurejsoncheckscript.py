
import json

# Opening JSON file
f = open('rule_engine_cloudwatch_azure_rules.json')

# returns JSON object as
# a dictionary
data = json.load(f)

for i in data:
    if len(i) >= 64:
        raise Exception(i, "cannot be longer than 64 characters")


print("json file valid")
