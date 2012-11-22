{:es-url "http://localhost:9200/" ;; es-url must end with a slash
 :es-index "clojuredocs"
 :es-http-opts {:basic-auth "user:Passw0rd"
                :debug false
                :debug-body false
                :save-request? false
                :as :json
                :throw-exceptions false}
 :api-port 5001
 :web-port 5000}
