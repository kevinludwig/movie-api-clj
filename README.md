### Overview
Clojure + Datomic Rest API

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
        "release_year": 2013,
        "genre": "Thriller"
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
        "genre": "Drama"
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
