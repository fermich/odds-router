(ns pl.fermich.odds-router.core-test
  (:require [clojure.test :refer :all]
            [pl.fermich.odds-router.core :refer :all]))

(deftest test-login-call
  (let [token "1"]
    (is (= (login-call token) "1"))))

;(run-tests)
