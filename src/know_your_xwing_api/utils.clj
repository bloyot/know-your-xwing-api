(ns know-your-xwing-api.utils
  (:require
   [clojure.string :as str]))

(defn name-to-keyword
  "Turn a card name into a keyword, standardizing casing and whitespace"
  [name]
  (-> name
      (str/replace #" " "-")
      (str/replace #"“" "")
      (str/replace #"”" "")
      (str/replace #"\"" "")
      str/lower-case
      keyword))
