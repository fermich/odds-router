(ns pl.fermich.odds_router.core)

(require '[rx.lang.clojure.core :as rx])
(import '(rx Observable))
(import '(java.util.concurrent TimeUnit)
        '(rx.functions Func2))

(defn just-obs [v]
  (rx/observable*
    (fn [observer]
      (rx/on-next observer v)
      (rx/on-completed observer))))

(defn login-call [v]
  (prn "Login" v)
  12345)
(def login-trigger (Observable/interval 10 TimeUnit/SECONDS))  ;replace with on demand request
(def token-obs (rx/map login-call login-trigger))

(defn heartbeat-call [token]
  (prn "Hartbeat" token)
  token)
(def heartbeat-trigger (Observable/interval 1 TimeUnit/SECONDS))
(def heartbeat-obs (rx/map heartbeat-call heartbeat-trigger))


(defn heartbeat-call-fun [handler]
  (reify
    Func2
    (call [this token count]
      (handler token))))
(defn heartbeat-join [token count] (heartbeat-call token))
(def heartbeat-with-token (Observable/combineLatest token-obs heartbeat-trigger (heartbeat-call-fun heartbeat-call)))

(def subscription (rx/subscribe heartbeat-with-token
              (fn [value]
                (prn (str "Got value: " value)))))

;(rx/unsubscribe subscription)

(defn const-producing-obs [v]
  (rx/cycle
    (rx/return v)))

(defn fast-producing-obs []
  (rx/map inc (Observable/interval 1 TimeUnit/MILLISECONDS)))

(defn -main []
  (Thread/sleep 150000))
