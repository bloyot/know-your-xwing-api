(ns know-your-xwing-api.cards
  (:require
   [cheshire.core :as cheshire]
   [clojure.java.io :as io]
   [clojure.string :as str]))

;; defs
(def base-data-path "resources/data")
(def upgrade-path (str base-data-path "/upgrades"))
(def pilots-path (str base-data-path "/pilots" ))

;; utility functions
(defn- standard-legal?
  "return true if this card is standard legal, else false"
  [card]
  (true? (:standard card)))

(defn- name-to-keyword
  "Turn a card name into a keyword, standardizing casing and whitespace"
  [name]
  (keyword (str/lower-case (str/replace name #" " "-"))))

(defn- get-json-file-name-from-path
  "Given a full path like 'foo/bar/something.json' return 'something'. Assumes file ends
  with .json and strips the path"
  [path]
  (str/replace (last (str/split path #"/")) ".json" ""))

(defn- get-file-names [dir]
  (mapv str (filter #(and (.isFile %) (str/ends-with? % ".json")) (file-seq (clojure.java.io/file dir)))))

(defn- get-dir-names [dir]
  (mapv str (filter #(not (.isFile %)) (file-seq (clojure.java.io/file dir)))))

;; upgrades
(defn- load-upgrade-file [file-name]
  (as-> (slurp file-name) s
    (cheshire/parse-string s true)
    (filter standard-legal? s)
    (map #(conj [] (name-to-keyword (:name %)) %) s)
    (into {} s)))

(defn- load-upgrades []
  (let [upgrade-files (get-file-names upgrade-path)]
    (into {} (map #(conj [] (keyword (get-json-file-name-from-path %)) (load-upgrade-file %)) upgrade-files))))

;; pilots
(defn- load-ship [faction-name ship-name]
  (as-> (slurp (str faction-name "/" ship-name ".json")) s
    (cheshire/parse-string s true)
    (:pilots s)
    (filter standard-legal? s)
    (into {} (map #(conj [] (name-to-keyword (:name %)) %) s))))

(defn- load-faction [faction-name]
  ;; TODO refactor this to use a thread macro
  (let [ship-names (->> faction-name
                        get-file-names
                        (map get-json-file-name-from-path))
        ships (into {} (map #(conj [] (keyword %) (load-ship faction-name %)) ship-names))
        standard-legal (filter (fn [[k v]] (seq v)) ships)]
    standard-legal))

(defn- load-pilots []
  (let [faction-names (filter #(not= pilots-path %) (get-dir-names pilots-path))]
    (into {} (map #(conj [] (keyword (last (str/split % #"/"))) (load-faction %)) faction-names))))

;; load all cards into memory, keyed by type, faction, etc
(def cards
  {:upgrades (load-upgrades)
   :pilots (load-pilots)})

(def factions (keys (:pilots cards)))
(def upgrade-types (keys (:upgrades cards)))

