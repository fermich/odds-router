(defproject odds-router "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [io.reactivex/rxclojure "1.0.0"]]
  :profiles {:prod {:main pl.fermich.odds_router.core}
             :samples {:main pl.fermich.odds_router.rxsamples}})

