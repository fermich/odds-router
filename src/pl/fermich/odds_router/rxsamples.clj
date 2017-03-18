(ns pl.fermich.odds_router.rxsamples)

(require '[rx.lang.clojure.core :as rx])
(import '(rx Observable))
(import '(java.util.concurrent TimeUnit)
        '(rx.functions Func2))

(defn just-obs [v]
  (rx/observable*
    (fn [observer]
      (rx/on-next observer v)
      (prn "Getting new value")
      (rx/on-completed observer)
      )))

(def just-token-obs (rx/cycle (just-obs "1")))

(def const-token-obs
  (rx/cycle (just-obs "1")))


(defn two-arg-fun-call []
  (reify
    Func2
    (call [this token count]
      token)))

(defn login-call [v]
  (prn "Login" v)
  v)
(def login-trigger (Observable/interval 5 TimeUnit/SECONDS))
(def token-obs (rx/map login-call login-trigger))

(def buff-value-obs (rx/cycle (rx/return token-obs)))
(def buff-token-obs (rx/cycle token-obs))

(def operation-trigger (Observable/interval 1 TimeUnit/SECONDS))
(def operation-obs (Observable/zip buff-value-obs operation-trigger (two-arg-fun-call)))

(def subscription (rx/subscribe operation-obs
              (fn [value]
                (prn (str "Got value: " value)))))

(defn -main []
  (Thread/sleep 15000)
  (println "aa"))
