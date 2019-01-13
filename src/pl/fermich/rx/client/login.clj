(ns pl.fermich.rx.client.login)

(require '[rx.lang.clojure.core :as rx])
(import '(java.util.concurrent TimeUnit))

(defn- login-call [v]
  ;TODO login implementation goes here
  (println "Received login request with broken token: " v)
  (let [new-token "1"]
    (print "Logged in. Sending token: " new-token)
    new-token))

(defn create-login-producer [heartbeat-producer relogin-interval]
  (let [heartbeat-failure-producer (rx/filter (complement (fn [x] (= x 200))) heartbeat-producer)]
    (->> (.debounce heartbeat-failure-producer relogin-interval TimeUnit/SECONDS)
         (rx/map login-call)
         (.retry))))
