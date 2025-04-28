GET _cat/indices?v

GET /_search
{
    "query": {
        "multi_match": {
            "query": "admin",
            "fields": [
                "*"
            ]
        }
    }
}

GET /posts/_mapping

GET /posts/_search
{
    "query": {
        "bool": {
            "should": [
                {
                    "match": {
                        "title": "鬼灭"
                    }
                },
                {
                    "match": {
                        "content": "鬼灭"
                    }
                }
            ]
        }
    }
}

GET /users/_mapping

GET /users/_search
{
    "query": {
        "bool": {
            "should": [
                {
                    "wildcard": {
                        "title": {
                