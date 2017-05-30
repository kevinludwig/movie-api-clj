### Overview
Clojure + Datomic Rest API

### Known issues

* you can't get history on rating in the way you'd expect, i.e if you pass the movie id and `rating_value` as the attribute, it 
won't return results (it wants you to pass the rating id not the movie id 
* If you call the update API to change the genres, you end up with a superset of the prior and current array values.
* generally validation is not very good, i.e. if you forget a request body parameter (say, the audit fields) it will just blow 
up deep in datomic somewhere, vs rejecting with 400 in the controller
* it needs to have separate APIs to create actors (it should not be isComponent)
* it needs to have separate APIs to create genres (genres shoud have ids, not just array of strings)

### Setup Clojure

Install datomic: www.datomic.com

Install leiningen: `brew install leiningen`

Fetch Dependencies: `lein deps`

Fire it up: `ring server-headless 3000`

### Plugin usage

* Run tests: `lein midje`
* View coverage `lein cloverage`
* Lint files: `lein eastwood`
* Beautify: `lein cljfmt check` (or `lein cljfmt fix`)
* Check for outdated dependencies: `lein ancient`
* Check for code simplifications: `lein kibit`

### Docker

`docker-compose build && docker-compose up`

### Endpoints

#### `POST /schema`

Create the movie schema

#### `POST /movie`

Create a new movie.

Request body

```
{
    "movie": {
        "title": "Gravity",
        "synopsis": "",
        "release_year": 2013,
        "genres": ["Thriller"],
        "runtime": 90,
        "cast": [
            {"person_name": "Sandra Bullock", "person_role": "Actor"}
        ],
        "rating": {
            "rating_value": "PG-13",
            "rating_source": "MPAA"
        }
    }, 
    "audit": {
        "user": "some user",
        "message": "some commit message"
    }
}
```

Returns

```
{
    "id": 17592186045418
}
```

#### `GET /movie/:id`

Find an existing movie by id

Returns the movie record

#### `GET /movie/:id/asof/:time`

Get the state of a movie at a point in time

#### `PUT /movie/:id`

Update an existing id. Request body contains attributes to update.

Request body

```
{
    "movie": {
        "genres": ["Drama"],
        "synopsis": "updated synopsis"
    },
    "audit": {
        "user": "some user",
        "message": "some commit message"
    }
}
```

Returns the updated movie record

#### `DELETE /movie/:id`

Removes a movie by id

Request body

```
{
    "audit": {
        "user": "some user",
        "message": "some commit message"
    }
}
```

#### `GET /movie/:id/history`

Get transaction history for a movie

Returns

```
{
    "history": [
        {
            "id": 17592186045418,
            "user": "some user",
            "message": "some commit message",
            "tid": 13194139534315,
            "ts": "2017-05-27T21:29:51Z"
        }, ...
    ]
}
```

#### `GET /movie/:id/history/:attribute`

Get transaction history for an attribute of a movie

Returns 

```
{
    "history": [
        {
            "id": 17592186045418,
            "user": "some user",
            "message": "some commit message",
            "added": true,
            "attr": "movie/genre",
            "value": "Drama",
            "tid": 13194139534315,
            "ts": "2017-05-27T21:29:51Z"
        }, ...
    ]
}
```
