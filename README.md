# Eisago

Next-gen clojuredocs importer and API

## Getting started

Eisago uses [ElasticSearch](http://elasticsearch.org) as the database.
First download the latest ElasticSearch release from the
[downloads](http://www.elasticsearch.org/download/) page.

```shell
% tar zkxf elasticsearch-0.19.11.tar.gz
% cd elasticsearch-0.19.11
% bin/elasticsearch -f
```

Then make sure you have a "clojure-1.4.0.json.gz" file (or a file for
whatever project you'd like to import), if you don't have one, use
[lein-clojuredocs](https://github.com/dakrone/lein-clojuredocs) or
[cadastre](https://github.com/dakrone/cadastre) to generate one.

To import a file using leiningen:

```shell
% lein eisago /path/to/clj-http-0.5.7.json.gz
Importing /path/to/clj-http-0.5.7.json.gz...
Done importing /path/to/clj-http-0.5.7.json.gz
```

Or, from inside `eisago.import`:

```clojure
(test-it "/path/to/clojure-1.4.0.json.gz")

(clojure.pprint/pprint (es/meta-for "clojure" "clojure.core" "reduce"))
{:arglists [["f" "coll"] ["f" "val" "coll"]],
 :children
 ({:type "example",
   :id "b6e5a6c931caef73dc9d69065df1f02b",
   :body "This is an example for reduce.",
   :parent-id "e194bda67affc5915db2460bd4bc29a6",
   :index-date "2012-11-12T15:01:19Z"}
  {:type "example",
   :id "141ae10ac4bdf67662dcbd8e30936516",
   :body "This is a different example for reduce.",
   :parent-id "e194bda67affc5915db2460bd4bc29a6",
   :index-date "2012-11-12T15:01:19Z"}
  {:type "comment",
   :id "6cb10162cba16e4de89178d01d13e387",
   :body "This is a comment for reduce.",
   :parent-id "e194bda67affc5915db2460bd4bc29a6",
   :index-date "2012-11-12T15:01:19Z"}),
 :lib-version "1.4.0",
 :ns "clojure.core",
 :name "reduce",
 :library "clojure",
 :doc
 "f should be a function of 2 arguments. ...etc...",
 :index-date #inst "2012-11-12T15:01:17.000-00:00",
 :line 6016,
 :source
 "(defn reduce\n ...etc...",
 :id "e194bda67affc5915db2460bd4bc29a6",
 :file "clojure/core.clj",
 :project "org.clojure/clojure"}
;; :doc and :source shortened to fit here
```

Or, run the web server and try some REST calls (these examples assume
you imported clojure-1.4.0.json.gz):

```
% lein run
```

```
% curl -s "localhost:5000/_search?name=reduce" | python -mjson.tool
{
    "hits": [
        {
            "arglists": "([\"f\" \"coll\"] [\"f\" \"val\" \"coll\"])",
            "id": "e194bda67affc5915db2460bd4bc29a6",
            "library": "clojure",
            "name": "reduce",
            "ns": "clojure.core",
            "project": "org.clojure/clojure",
            "score": 5.248495
        }
    ],
    "time": 2,
    "total": 1
}
```

```
% curl -s "localhost:5000/doc/clojure/clojure.core/reduce" | python -mjson.tool
{
    "arglists": "([\"f\" \"coll\"] [\"f\" \"val\" \"coll\"])",
    "children": [
        {
            "body": "This is an example for reduce.",
            "id": "aa21fbb2034a0492312b0aa316e70b76",
            "index-date": "2012-11-14T22:32:24Z",
            "parent-id": "e194bda67affc5915db2460bd4bc29a6",
            "type": "example"
        },
        {
            "body": "This is a different example for reduce.",
            "id": "64f279cb443cb1488fb4b57990ae0be8",
            "index-date": "2012-11-14T22:32:24Z",
            "parent-id": "e194bda67affc5915db2460bd4bc29a6",
            "type": "example"
        },
        {
            "body": "This is a comment for reduce.",
            "id": "bf00599f524b1a8949b9625f245b1eea",
            "index-date": "2012-11-14T22:32:24Z",
            "parent-id": "e194bda67affc5915db2460bd4bc29a6",
            "type": "comment"
        }
    ],
    "doc": "... documentation here ...",
    "file": "clojure/core.clj",
    "id": "e194bda67affc5915db2460bd4bc29a6",
    "index-date": "2012-11-14T22:32:21Z",
    "lib-version": "1.4.0",
    "library": "clojure",
    "line": 6016,
    "name": "reduce",
    "ns": "clojure.core",
    "project": "org.clojure/clojure",
    "source": "... source code here ..."
}
```

```
% curl -s "localhost:5000/meta/clojure/clojure.core/reduce" | python -mjson.tool
[
    {
        "body": "This is an example for reduce.",
        "id": "aa21fbb2034a0492312b0aa316e70b76",
        "index-date": "2012-11-14T22:32:24Z",
        "parent-id": "e194bda67affc5915db2460bd4bc29a6",
        "type": "example"
    },
    {
        "body": "This is a different example for reduce.",
        "id": "64f279cb443cb1488fb4b57990ae0be8",
        "index-date": "2012-11-14T22:32:24Z",
        "parent-id": "e194bda67affc5915db2460bd4bc29a6",
        "type": "example"
    },
    {
        "body": "This is a comment for reduce.",
        "id": "bf00599f524b1a8949b9625f245b1eea",
        "index-date": "2012-11-14T22:32:24Z",
        "parent-id": "e194bda67affc5915db2460bd4bc29a6",
        "type": "comment"
    }
]
```

Expect a fully documented API as soon as one is entirely decided upon.

## Progress

- import [should be relatively stable]
- api [not entirely settled upon]
- migration tool [not started]
- website [not started]

## Known issues

- <del>still have some issues with `arglist` in the ES mapping</del>

## License

Copyright © 2012 Matthew Lee Hinman
