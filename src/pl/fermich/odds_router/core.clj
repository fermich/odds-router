(ns pl.fermich.odds_router.core
  (:import (rx.subjects ReplaySubject)))

(require '[rx.lang.clojure.core :as rx])
(import '(rx Observable))
(import '(java.util.concurrent TimeUnit)
        '(rx.functions Func2))

(def token-subject (ReplaySubject/create))

(defn latest-token-obs [token-provider trigger]
  (let [token-combiner (reify Func2 (call [this token counter] token))]
    (Observable/combineLatest token-provider trigger token-combiner)))

(defn latest-some-token-obs [token-provider trigger]
  (->> (latest-token-obs token-provider trigger)
       (rx/filter some?)))

;----------------------------------

(def heartbeat-interval 8)

(defn heartbeat-call [token]
  (prn "Heartbeat: " token)
  (if (= token "1") 200 500))

(defn create-hearbeat-obs [token-provider interval]
  (let [heartbeat-trigger (Observable/interval interval TimeUnit/SECONDS)]
    (->> (latest-token-obs token-provider heartbeat-trigger)
         (rx/map heartbeat-call))))

(def heartbeat-obs (create-hearbeat-obs token-subject heartbeat-interval))

;----------------------------------

(def relogin-interval 4)

(defn login-call [v]
  (prn "Login: " v)
  "1")

(defn create-login-obs [heartbeat-obs interval]
  (let [login-failure-obs (rx/filter (complement (fn [x] (= x 200))) heartbeat-obs)]
    (->> (.debounce login-failure-obs interval TimeUnit/SECONDS)
         (rx/map login-call))))

(def login-result-obs (create-login-obs heartbeat-obs relogin-interval))
(def login-subscription (.subscribe (.retry login-result-obs)
                                    token-subject))

;----------------------------------
(def list-book-interval 1)

(defn list-book-call [token]
  ;(prn "Book: " token)
  (if (= token "3")
    (throw (Exception. "Getting book list error."))
    (vector 1 2 3 4)))

(defn create-list-book-obs [token-obs interval]
  (let [list-book-trigger (Observable/interval interval TimeUnit/SECONDS)]
    (->> (latest-some-token-obs token-obs list-book-trigger)
         (rx/map list-book-call))))

(def list-book-obs (create-list-book-obs token-subject list-book-interval))

(def list-book-subscription (rx/subscribe (.retry list-book-obs)
                                          (fn [v] (prn "Got value: " v))
                                          (fn [e] (prn "Got error: " e))))

;----------------------------------

;(rx/unsubscribe subscription)

(defn -main []
  (print "Start!!")
  ;(Thread/sleep 1000)
  (.onNext token-subject nil)
  ;;(.onCompleted token-subject)
  ;(Thread/sleep 5000)
  ;(.onNext token-subject "2")

  (Thread/sleep 15000)
  (.onNext token-subject "3")

  ;(.onCompleted token-subject)

  (Thread/sleep 150000))
