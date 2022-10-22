(ns know-your-xwing-api.game
  (:require
   [know-your-xwing-api.cards :as cards]))

(defn choose-random-value
  "given a sequence return a random value"
  [s]
  (nth (seq s) (rand-int (count s))))

(defn choose-upgrade
  "Choose an upgrade card at random"
  []
  (let [upgrade-type (choose-random-value (keys (:upgrades cards/cards)))]
    (choose-random-value (get-in cards/cards [:upgrades upgrade-type]))))

(defn choose-pilot
  "Choose a pilot within the given faction at random"
  [faction]
  (let [ship (choose-random-value (keys (get-in cards/cards [:pilots faction])))]
    (choose-random-value (get-in cards/cards [:pilots faction ship]))))

(defn choose-card
  "Choose a single card at random, within the given card type and faction"
  [card-types factions]
  (let [card-type (choose-random-value card-types)]
    (case card-type
      :upgrades (assoc (val (choose-upgrade)) :type card-type)
      :pilots (assoc (val (choose-pilot (choose-random-value factions))) :type card-type)
      nil)))

(defmulti card->question
  "Given a card, transform it to a question by removing uneeded fields, and adding
  related wrong answers."
  :type)

(defmethod card->question :upgrades
  [card]
  {:name (:name card)
   :card-type :upgrade
   :ability (:ability (first (:sides card)))
   :image-url :todo
   :wrong-abilities ["ability 1" "ability 2"]})

(defmethod card->question :pilots
  [card]
  {:name (:name card)
   :card-type :upgrade
   :ability :todo
   :image-url :todo
   :wrong-abilities ["ability 1" "ability 2"]})

(defmethod card->question :default
  [card]
  (println "Invalid card type when converting to question")
  nil)


(defn generate-game
  "Given a set of card types, factions, and the number of cards to generate, return
  an array of randomly selected cards that fit the criteria."
  [card-types factions num-cards]
  (for [x (range num-cards)]
    (choose-card card-types factions)))
