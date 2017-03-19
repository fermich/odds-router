(ns pl.fermich.odds-router.core
  (:import (rx.subjects ReplaySubject)))

(require '[rx.lang.clojure.core :as rx])
(import '(rx Observable))
(import '(java.util.concurrent TimeUnit)
        '(rx.functions Func2))

;----------------------------------

(defn latest-token-obs [token-provider trigger]
  (let [token-combiner (reify Func2 (call [this token counter] token))]
    (Observable/combineLatest token-provider trigger token-combiner)))

(defn latest-some-token-obs [token-provider trigger]
  (->> (latest-token-obs token-provider trigger)
       (rx/filter some?)))

;----------------------------------

(defn heartbeat-call [token]
  (prn "Heartbeat: " token)
  (if (= token "1") 200 500))

(defn create-hearbeat-obs [token-provider interval]
  (let [heartbeat-trigger (Observable/interval interval TimeUnit/SECONDS)]
    (->> (latest-token-obs token-provider heartbeat-trigger)
         (rx/map heartbeat-call))))

;----------------------------------

(defn login-call [v]
  (prn "Login: " v)
  "1")

(defn create-login-obs [heartbeat-obs interval]
  (let [heartbeat-failure-obs (rx/filter (complement (fn [x] (= x 200))) heartbeat-obs)]
    (->> (.debounce heartbeat-failure-obs interval TimeUnit/SECONDS)
         (rx/map login-call)
         (.retry))))

;----------------------------------

(defn list-book-call [token]
  ;(prn "Book: " token)
  (if (= token "3")
    (throw (Exception. "Getting book list error."))
    (vector 1 2 3 4)))

(defn create-list-book-obs [token-obs interval]
  (let [list-book-trigger (Observable/interval interval TimeUnit/SECONDS)]
    (->> (latest-some-token-obs token-obs list-book-trigger)
         (rx/map list-book-call)
         (.retry))))

;----------------------------------

(defn make-connection [heartbeat-interval relogin-interval list-book-interval]
  (let [token-subject (ReplaySubject/create)
        heartbeat-obs (create-hearbeat-obs token-subject heartbeat-interval)
        login-token-obs (create-login-obs heartbeat-obs relogin-interval)
        list-book-obs (create-list-book-obs token-subject list-book-interval)]
    (.subscribe login-token-obs token-subject)
    (rx/subscribe list-book-obs
                  (fn [v] (prn "Got value: " v))
                  (fn [e] (prn "Got error: " e)))
    token-subject))


;(rx/unsubscribe subscription)

(defn -main []
  (print "Start!!")
  (def token-subject (make-connection 8 4 1))
  ;(Thread/sleep 1000)
  (.onNext token-subject nil)
  ;;(.onCompleted token-subject)
  ;(Thread/sleep 5000)
  ;(.onNext token-subject "2")

  (Thread/sleep 15000)
  (.onNext token-subject "3")

  ;(.onCompleted token-subject)

  (Thread/sleep 150000))
