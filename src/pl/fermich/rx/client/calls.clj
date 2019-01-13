(ns pl.fermich.rx.client.calls
  (:import (rx.subjects ReplaySubject)))

(require '[rx.lang.clojure.core :as rx]
         '[pl.fermich.rx.client.token :as token]
         '[pl.fermich.rx.client.heartbeat :as heart]
         '[pl.fermich.rx.client.login :as login])
(import '(rx Observable))
(import '(java.util.concurrent TimeUnit))


(defn- create-recurrent-call-producer [token-provider interval api-call]
  (let [api-call-trigger (Observable/interval interval TimeUnit/SECONDS)]
    (->> (token/valid-token-provider token-provider api-call-trigger)
         (rx/map api-call)
         (.retry))))

(defn- create-single-call-producer [token-provider api-call]
  (->> (token/valid-token-provider token-provider (Observable/just "call"))
       (rx/map api-call)))

(defn make-connection [heartbeat-interval relogin-interval]
  (let [token-subject (ReplaySubject/create)
        heartbeat-status-producer (heart/create-hearbeat-producer token-subject heartbeat-interval)
        login-token-producer (login/create-login-producer heartbeat-status-producer relogin-interval)]
    (.subscribe login-token-producer token-subject)
    token-subject))

(defn make-recurrent-call [token-subject api-call-interval api-call api-call-value-handler api-call-error-handler]
  (let [api-call-obs (create-recurrent-call-producer token-subject api-call-interval api-call)]
    (rx/subscribe api-call-obs
                  api-call-value-handler
                  api-call-error-handler)))

(defn make-single-call [token-subject api-call api-call-value-handler api-call-error-handler]
  (let [api-call-obs (create-single-call-producer token-subject api-call)]
    (rx/subscribe api-call-obs
                  api-call-value-handler
                  api-call-error-handler)))
