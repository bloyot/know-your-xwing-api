(ns know-your-xwing-api.handler
  (:require [compojure.api.sweet :refer :all]
            [know-your-xwing-api.game :as game]
            [ring.adapter.jetty :refer :all]
            [ring.util.http-response :refer :all]
            [schema.core :as s]))

(s/defschema GameConfig
  {:number-of-cards s/Int
   :card-types [(s/enum :upgrades :pilots)]
   :factions [(s/enum
               :galactic-empire
               :separatist-alliance
               :scum-and-villainy
               :first-order
               :rebel-alliance
               :resistance
               :galactic-republic)]})

(def app
  (api
    {:swagger
     {:ui "/"
      :spec "/swagger.json"
      :data {:info {:title "Know-your-xwing-api"
                    :description "Api to generate card data for xwing trivia game"}
             :tags [{:name "api", :description "the core apis for game cards"}]}}}

    (context "/api" []
      :tags ["api"]

      (POST "/generate-game" []
        :body [{:keys [number-of-cards card-types factions]} GameConfig]
        :summary "generate the cards for a game given a game configuration (the rules)"
        (ok (game/generate-game card-types factions number-of-cards))))))

