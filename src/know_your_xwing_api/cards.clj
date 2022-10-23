(ns know-your-xwing-api.cards
  (:require
   [cheshire.core :as cheshire]
   [clojure.java.io :as io]
   [clojure.string :as str]
   [know-your-xwing-api.utils :as utils]))

;; defs
(def base-data-path "resources/data")
(def upgrade-path (str base-data-path "/upgrades"))
(def pilots-path (str base-data-path "/pilots" ))

;; utility functions
(defn- standard-legal?
  "return true if this card is standard legal, else false"
  [card]
  (true? (:standard card)))

(defn- map-by
  "gives a sequence of items, return a map where the key is the result of calling the
  key-fn on each element, and the val is the result of calling val-fn on each element"
  ([s key-fn]
   (map-by s key-fn identity))
  ([s key-fn val-fn]
   (into {} (map (fn [itm] [(key-fn itm) (val-fn itm)]) s))))

(defn- filter-empty-keys
  "for a map, filter out any keys which have emtpy value"
  [m]
  (let [m-keys (keys m)
        valid-keys (filter #(seq (get m %)) m-keys)]
    ;; check if the val for each key is not empty
    (select-keys m valid-keys)))

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
    (map-by s #(utils/name-to-keyword (:name %)))
    (into {} s)))

(defn- load-upgrades []
  (let [upgrade-files (get-file-names upgrade-path)
        upgrades (map-by upgrade-files
                         #(keyword (get-json-file-name-from-path %))
                         #(load-upgrade-file %))
        standard-legal (filter-empty-keys upgrades)]
    standard-legal))

;; pilots
(defn- load-ship [faction-name ship-name]
  (as-> (slurp (str pilots-path "/" faction-name "/" ship-name ".json")) s
    (cheshire/parse-string s true)
    (:pilots s)
    (filter standard-legal? s)
    (map-by s #(utils/name-to-keyword (:name %)))))

(defn- load-faction [faction-name]
  ;; TODO refactor this to use a thread macro
  (let [ship-names (->> (str pilots-path "/" faction-name)
                        get-file-names
                        (map get-json-file-name-from-path))
        ships (map-by ship-names keyword #(load-ship faction-name %))
        standard-legal (filter-empty-keys ships)]
    standard-legal))

(defn- load-pilots []
  (let [faction-names (->> (get-dir-names pilots-path)
                           (filter #(not= pilots-path %))
                           (map #(str/replace % (re-pattern (str pilots-path "/")) "")))]
    (map-by faction-names #(keyword (last (str/split % #"/"))) #(load-faction %))))

;; load all cards into memory, keyed by type, faction, etc
(def cards
  {:upgrades (load-upgrades)
   :pilots (load-pilots)})

(def factions (keys (:pilots cards)))
(def upgrade-types (keys (:upgrades cards)))

