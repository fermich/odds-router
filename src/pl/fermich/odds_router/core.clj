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

(defn heartbeat-call [token]
  (prn "Heartbeat: " token)
  (if (= token "1") 200 500))
(def heartbeat-trigger (Observable/interval 8 TimeUnit/SECONDS))
(def heartbeat-obs (->> (latest-token-obs token-subject heartbeat-trigger)
                        (rx/map heartbeat-call)))
;(def catch-heartbeat-obs (->> heartbeat-obs
;                              (rx/catch* Exception
;                                      (fn [e] (rx/return 500)))))

;----------------------------------

(defn login-call [v]
  (prn "Login: " v)
  "1")
(def login-failure-obs (rx/filter (complement (fn [x] (= x 200))) heartbeat-obs))
(def login-result-obs (->> (.debounce login-failure-obs 4 TimeUnit/SECONDS)
                           (rx/map login-call)))

(def login-subscription (.subscribe (.retry login-result-obs)
                                    token-subject))

;----------------------------------

(defn list-book-call [token]
  ;(prn "Book: " token)
  (if (= token "3") (throw (Exception. "Getting book list error.")) (vector 1 2 3 4)))
(def list-book-trigger (Observable/interval 1 TimeUnit/SECONDS))
(def list-book-obs (->> (latest-some-token-obs token-subject list-book-trigger)
                        (rx/map list-book-call)))

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
