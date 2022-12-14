(ns know-your-xwing-api.game
  (:require
   [know-your-xwing-api.cards :as cards]
   [know-your-xwing-api.utils :as utils]))

(defn- is-valid-for-faction?
  "return true if the upgrade is valid for the faction (including if it's neutral)"
  [upgrade faction]
  (let [faction-restriction (:factions (first (:restrictions upgrade)))
        faction-restriction-set (set (map utils/name-to-keyword faction-restriction))]
    (or (nil? faction-restriction) (contains? faction-restriction-set faction))))

(defn- filter-faction-upgrades
  "given a map of upgrades, remove all upgrades that don't match the given faction,
  or are neutral"
  [upgrades faction]
  (as-> upgrades u
    (keys u)
    (filter #(is-valid-for-faction? (get upgrades %) faction) u)
    (select-keys upgrades u)))

(defn choose-random-value
  "given a sequence return a random value"
  [s]
  (nth (seq s) (rand-int (count s))))

(defn choose-upgrade
  "Choose an upgrade card at random for the given faction (including neutral)"
  [faction]
  (let [upgrade-type (choose-random-value (keys (:upgrades cards/cards)))
        upgrades (get-in cards/cards [:upgrades upgrade-type])
        faction-upgrades (filter-faction-upgrades upgrades faction)]
    ;; there's an unlikely edge case for a handful of upgrade type/faction combinations where
    ;; there may not be a valid upgrade of that type. In that case, just pick again
    (if (seq faction-upgrades)
      (assoc (val (choose-random-value faction-upgrades))
             :card-type :upgrades
             :faction faction)
      (choose-upgrade faction))))

(defn choose-pilot
  "Choose a pilot within the given faction at random"
  [faction]
  (let [ship (choose-random-value (keys (get-in cards/cards [:pilots faction])))
        pilot (val (choose-random-value (get-in cards/cards [:pilots faction ship])))]
    ;; some pilots don't have an ability, in which case simply choose again
    (if (nil? (:ability pilot))
      (choose-pilot faction)
      (assoc pilot
             :faction faction
             :card-type :pilots))))

(defn choose-card
  "Choose a single card at random, within the given card type and faction"
  [card-types factions]
  (let [card-type (choose-random-value card-types)
        faction (choose-random-value factions)]
    (case card-type
      :upgrades (choose-upgrade faction)
      :pilots (choose-pilot faction)
      nil)))

(defmulti card->question
  "Given a card, transform it to a question by removing uneeded fields, and adding
  related wrong answers."
  :card-type)

(defmethod card->question :upgrades
  [{:keys [name faction sides]}]
  {:name name
   :card-type :upgrades
   :faction faction
   :ability (:ability (first sides))
   :image-url (:image (first sides))
   :wrong-abilities [(:ability (first (:sides (choose-card #{:upgrades} #{faction}))))
                     (:ability (first (:sides (choose-card #{:upgrades} #{faction}))))]})

(defmethod card->question :pilots
  [{:keys [name faction image ability]}]
  {:name name
   :card-type :pilots
   :faction faction
   :ability ability
   :image-url image
   :wrong-abilities [(:ability (choose-card #{:pilots} #{faction}))
                     (:ability (choose-card #{:pilots} #{faction}))]})

(defmethod card->question :default
  [card]
  (println "Invalid card type when converting to question")
  nil)


(defn generate-game
  "Given a set of card types, factions, and the number of cards to generate, return
  an array of randomly selected cards that fit the criteria."
  [card-types factions num-cards]
  ;; DEBUG for testing purposes
  (Thread/sleep 500)
  (for [x (range num-cards)]
    (card->question (choose-card card-types factions))))
