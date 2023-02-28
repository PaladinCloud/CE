# Paladin - Cloud Discovery
This is a batch job that pull data from the cloud (E.g. AWS). The data is written to ES 

## How to set it up?
This program can be invoked on demand or can be scheduled as a batch job.

##### important env variables to set

aqua_client_domain_url -> domain URL for specific client created by aqua
default_page_size ( recommendation is below 1000)
aqua_image_vul_query_params -> set based on requirements
aqua_api_url = aqua saas URL 
aqua_username 
aqua_password