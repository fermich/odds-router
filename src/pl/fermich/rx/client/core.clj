(ns pl.fermich.rx.client.core
  (:import (rx.subjects ReplaySubject)))

(require '[rx.lang.clojure.core :as rx])
(import '(rx Observable))
(import '(java.util.concurrent TimeUnit)
        '(rx.functions Func2))


(defn- latest-token-obs [token-provider trigger]
  (let [token-combiner (reify Func2 (call [this token counter] token))]
    (Observable/combineLatest token-provider trigger token-combiner)))

(defn- latest-some-token-obs [token-provider trigger]
  (->> (latest-token-obs token-provider trigger)
       (rx/filter some?)))


(defn- heartbeat-call [token]
  ;TODO heartbeat implmentation goes here
  (println "Heartbeat call with token: " token)
  (let [heartbeat-status (= token "1")]
    (if heartbeat-status 200 500)))

(defn- create-hearbeat-obs [token-provider interval]
  (let [heartbeat-trigger (Observable/interval interval TimeUnit/SECONDS)]
    (->> (latest-token-obs token-provider heartbeat-trigger)
         (rx/map heartbeat-call))))


(defn- login-call [v]
  ;TODO login implementation goes here
  (println "Received login request with broken token: " v)
  (let [new-token "1"]
    (print "Logged in. Sending token: " new-token)
    new-token))

(defn- create-login-obs [heartbeat-obs interval]
  (let [heartbeat-failure-obs (rx/filter (complement (fn [x] (= x 200))) heartbeat-obs)]
    (->> (.debounce heartbeat-failure-obs interval TimeUnit/SECONDS)
         (rx/map login-call)
         (.retry))))


(defn- create-api-call-obs [token-obs interval api-call]
  (let [list-book-trigger (Observable/interval interval TimeUnit/SECONDS)]
    (->> (latest-some-token-obs token-obs list-book-trigger)
         (rx/map api-call)
         (.retry))))


(defn make-connection [heartbeat-interval relogin-interval api-call-interval api-call api-call-value-handler api-call-error-handler]
  (let [token-subject (ReplaySubject/create)
        heartbeat-obs (create-hearbeat-obs token-subject heartbeat-interval)
        login-token-obs (create-login-obs heartbeat-obs relogin-interval)
        list-book-obs (create-api-call-obs token-subject api-call-interval api-call)]
    (.subscribe login-token-obs token-subject)
    (rx/subscribe list-book-obs
                  api-call-value-handler
                  api-call-error-handler)
    token-subject))
