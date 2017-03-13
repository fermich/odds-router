(ns pl.fermich.odds_router)

(require '[rx.lang.clojure.core :as rx])
(import '(rx Observable))
(import '(java.util.concurrent TimeUnit))

;https://github.com/ReactiveX/RxJava/wiki/Observable-Utility-Operators

;(def obs (Observable/interval 100 TimeUnit/MILLISECONDS))
;
;(def subscription (rx/subscribe obs
;              (fn [value]
;                (prn (str "Got value: " value)))))
;
;(Thread/sleep 1000)
;(rx/unsubscribe subscription)
;(prn "Unsubscribed")

(defn just-obs [v]
  (rx/observable*
    (fn [observer]
      (rx/on-next observer v)
      (rx/on-completed observer))))


(defn heartbeat-call [v]
  12345)
(def heartbeat-trigger (Observable/interval 1 TimeUnit/SECONDS))
(def heartbeat-obs (rx/map heartbeat-call heartbeat-trigger))




(def subscription (rx/subscribe heartbeat-obs
              (fn [value]
                (prn (str "Got value: " value)))))


;(rx/unsubscribe subscription)

(defn const-producing-obs [v]
  (rx/cycle
    (rx/return v)))

(defn fast-producing-obs []
  (rx/map inc (Observable/interval 1 TimeUnit/MILLISECONDS)))

;(rx/subscribe (->> (rx/map vector
;                           (.onBackpressureBuffer (fast-producing-obs))
;                           (slow-producing-obs))
;                   (rx/map (fn [[x y]] (+ x y)))
;                   (rx/take 10)) prn (fn [e] (prn "error is " e)))

;(rx/subscribe (->> (rx/map vector
;                           (fast-producing-obs)
;                           (const-producing-obs 666))
;                   (rx/map (fn [[x y]] (+ x y)))
;                   (rx/take 10)) prn (fn [e] (prn "error is " e)))

(Thread/sleep 15000)
