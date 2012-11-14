{:es-url "http://localhost:9200/" ;; es-url must end with a slash
 :es-index "clojuredocs"
 :es-http-opts {:basic-auth "user:Passw0rd"
                :debug false
                :debug-body false
                :save-request? false
                :as :json
                :throw-exceptions false}
 :laeggen {:port 5000
           :append-slash? false
           :websocket false}}
