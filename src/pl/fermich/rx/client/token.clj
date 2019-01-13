(ns pl.fermich.rx.client.token)

(require '[rx.lang.clojure.core :as rx])
(import '(rx Observable))
(import '(rx.functions Func2))

(defn latest-token-provider [token-provider trigger]
  (let [token-combiner (reify Func2 (call [this token counter] token))]
    (Observable/combineLatest token-provider trigger token-combiner)))

(defn valid-token-provider [token-provider trigger]
  (->> (latest-token-provider token-provider trigger)
       (rx/filter some?)))
