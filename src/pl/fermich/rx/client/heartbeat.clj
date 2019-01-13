(ns pl.fermich.rx.client.heartbeat)

(require '[rx.lang.clojure.core :as rx]
         '[pl.fermich.rx.client.token :as token])
(import '(rx Observable))
(import '(java.util.concurrent TimeUnit))

(defn- heartbeat-call [token]
  ;TODO heartbeat implmentation goes here
  (println "Heartbeat call with token: " token)
  (let [heartbeat-status (= token "1")]
    (if heartbeat-status 200 500)))

(defn create-hearbeat-producer [token-provider heartbeat-interval]
  (let [heartbeat-trigger (Observable/interval heartbeat-interval TimeUnit/SECONDS)]
    (->> (token/latest-token-provider token-provider heartbeat-trigger)
         (rx/map heartbeat-call))))
