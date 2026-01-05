# Implementation of db sharding for applicant/user db

## 1. Shard user db into 7 shards

- The shard are as follows:
    + VN - Vietnam
    + SG - Singapore
    + SEA - Southeast Asia (Thailand, Malaysia,...)
    + EA - East Asian (China, Japan, South Korea)
    + US - US and Canada
    + EU - Europe (Germany, France and Netherlands)
    + Others - any country not lists above
- Both JM and JA use this strategy

## 2. Implementation

- Each shard has it own db - the port and list can be view in docker compose file
- Every request with authenticate can be regconize and redirect to the right shard
- Have implementation of redis for caching look up
- Suggestion: Add in country claims to JWE token to further optimize the sharding lookup

- All features and endpoint is still accessible


## 3. Issues

- Current flyway setup is not properly working with sharding
- Should set countries id with hardcoded UUID or having a different way to seed data, ensure UUIDs is the same on all shards
- Should set the skill ids across shard same with each others

