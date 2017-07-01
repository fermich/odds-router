(ns pl.fermich.rx.client.main
  (:import (rx.subjects ReplaySubject)))

(require '[rx.lang.clojure.core :as rx]
         '[pl.fermich.rx.client.core :as cc])
(import '(rx Observable))
(import '(java.util.concurrent TimeUnit)
        '(pl.fermich.rx.client.core))


(defn list-book-api-call [token]
  ;TODO API call implementation goes here
  (if (= token "3")
    (throw (Exception. "Getting book list error."))
    (vector 1 2 3 4)))

(defn list-book-value-handler [v]
  (prn "Got value: " v))

(defn list-book-error-handler [e]
  (prn "Got error: " e))

(defn -main []
  (let [heartbeat-interval-sec 8
        relogin-interval-sec 4
        list-book-interval-sec 1
        token-subject (cc/make-connection heartbeat-interval-sec
                                          relogin-interval-sec
                                          list-book-interval-sec
                                          list-book-api-call
                                          list-book-value-handler
                                          list-book-error-handler)]
    (println "Starting connection...")
    (.onNext token-subject nil)
    (Thread/sleep 20000)
    (println "Sending broken token...")
    (.onNext token-subject "!1")
    (Thread/sleep 35000)))
