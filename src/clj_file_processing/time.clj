(ns ^{:authors "Sofia Hitlan"}
    clj-file-processing.time
  (:require [clojure.string :as str]
            [clj-time.core :as t]
            [clj-time.format :as tf]))

(defn str-to-seq-of-ints
  "Default separator '-' "
  [string & [seperator]]
  (let [separator-pattern (re-pattern (or seperator "-"))]
    (map #(Integer/parseInt %)
         (str/split string separator-pattern))))

(defn get-date-time
  "Constructs date-time object."
  [[month day year]]
  (let [date-time (t/date-time year month day)]
    date-time))

(defn format-date
  "Parses date-time object in custom format."
  [date-time]
  (let [custom-formatter (tf/formatter "M/d/YYYY")
        parsed-date-time (tf/unparse custom-formatter date-time)]
    parsed-date-time))
