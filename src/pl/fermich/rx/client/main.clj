(ns pl.fermich.rx.client.main
  (:import (pl.fermich.rx.gui MainApp)))

(require '[pl.fermich.rx.client.calls :as cc])

(defn list-book-api-call [token]
  ;TODO API call implementation goes here
  (println "getting book with token:" token)
  (if (= token "3")
    (throw (Exception. "Getting book list error."))
    (vector 1 2 3 4)))

(defn list-book-value-handler [v]
  (prn "Got value: " v))

(defn list-book-error-handler [e]
  (prn "Got error: " e))

(defn -main []
  (MainApp/main nil)
  )

(defn -main2 []
  (let [heartbeat-interval-sec 2
        relogin-interval-sec 1
        list-book-interval-sec 5
        token-provider (cc/make-connection heartbeat-interval-sec
                                          relogin-interval-sec)
        recurrent-call (cc/make-recurrent-call token-provider
                                               list-book-interval-sec
                                               list-book-api-call
                                               list-book-value-handler
                                               list-book-error-handler)
        ]
    (println "Starting connection...")
    (.onNext token-provider nil)
    (Thread/sleep 20000)
    (println "Sending broken token...")
    (.onNext token-provider "3")
    (Thread/sleep 25000)

    (println "Single call 1...")
    (cc/make-single-call token-provider
                         list-book-api-call
                         list-book-value-handler
                         list-book-error-handler)
    )
  )
