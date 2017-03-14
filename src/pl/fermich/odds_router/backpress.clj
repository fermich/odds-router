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
