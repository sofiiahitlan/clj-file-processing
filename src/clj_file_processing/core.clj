(ns ^{:authors "Sofia Hitlan"}
    clj-file-processing.core
  ;;(:gen-class)
  (:require [clojure.string :as str]
            [clojure.java.io :as io]
            [clj-file-processing.time :as time]))

(comment "Decide on how to process files. Have the atom and swap the value each
new file is processed maybe? If no, figure out how to use transducer.
Assume that all names are identical or not?")

#_(def files-to-process ;; TODO - remove this, now the list is dynamic
    ["mock_data_commas.csv" "mock_data_pipelines.csv" "mock_data_whitespace.csv"])

(defn get-files-dir
  "Builds files directory path based on user directory value.

  Args: none
  Returns: files-dir (str)"
  []
  (let [user-dir (System/getProperty "user.dir")
        files-dir (str user-dir "/src/clj_file_processing/csv")]
    files-dir))

(defn get-tree-seq
  "Returns files sequence using Java File object.

  Args: files-dir (str)
  Returns: tree-seq (vector of string file paths)
  "
  [files-dir]
  (let [tree-seq (file-seq (io/file files-dir))]
    tree-seq))

(defn get-files-to-process ;; rename?
  "Gets a sequence of all file names in a directory for future processing.
  Assumes all files need to be processed.

  Args:    none
  Returns: files-to-parse (vector of strings)

  TODO: add example of usage?"
  []
  (let [files-dir (get-files-dir)
        tree-seq (get-tree-seq files-dir)
        files-to-parse (mapv str (filter #(.isFile %) tree-seq))]
    files-to-parse))

(defn remove-header
  "Removes an optional header from the raw content (if found).

  Args:    raw-content (str), header-pattern (regex pattern)
  Returns: headerless-content (str).
  "
  [raw-content header-pattern]
  (let [headerless-content (str/replace-first raw-content header-pattern "")]))

(defn sort-by-dob
  "Sorts entries by birth date based on datetime objects values, ascending."
  [parsed-entries]
  (sort-by
   (comp time/get-date-time time/str-to-seq-of-ints last)
   parsed-entries))

(defn sort-by-last-name
  "Sorts entries by last name, descending (by default), unless optional order
  arg is provided. In this case, checks order value. Options: 'asc', 'desc'."
  [parsed-entries & [order]]
  (let [order* (or order "desc")
        sorted-asc (sort-by (comp first) parsed-entries)]
    (if (= order* "desc")
      (reverse sorted-asc)
      sorted-asc)))

(defn parse-content
  "Checks for header row. If at least one of the field labels is present (regex
  is case insensitive), the header starting at the match and ending at the
  newline, is removed. Remaining string is split by newline into vector of
  vectors.Processes each line

  Args: raw-content (str)
  Returns: TODO
  "
  [raw-content]
  (let [assoc-formated-date (fn [el]
                              (assoc el 4 (#(-> (nth el 4)
                                                time/str-to-seq-of-ints
                                                time/get-date-time
                                                time/format-date))))
        header-pattern
        (re-pattern ;; case insensitive
         "(?i)(LastName|FirstName|Gender|FavoriteColor|DateOfBirth).*\\n")
        header-match (re-find header-pattern raw-content)
        headerless-content (when (seq header-match) ;; when works too
                             (remove-header raw-content header-pattern))
        delimiter nil ;; TODO
        delimiter-pattern (re-pattern delimiter)
        parsed-entries (as-> (or headerless-content raw-content) arg
                         (str/split arg #"\n")
                         (mapv vector arg)
                         (mapv #(-> % first (str/split delimiter-pattern))
                               arg)
                         (map assoc-formated-date arg))]))

(defn remove-duplicates
  "Removes duplicate entries.

  Args:    all-records (atom storing lazy seq)
  Returns: unique-records (set of vectors)
  "
  [all-records]
  (let [unique-records (set @all-records)]
    unique-records))

(defn process-files
  "Processes files returned by get-files-to-process fn. Assumes all of the files
  need to be processed. Opens each using with-open to ensure the Reader is
  properly closed when processing is complete.
  TODO - use csv fns maybe? or don't assume that all files will be in csv format?"
  []
  (let [all-records (atom nil)
        files-to-parse (get-files-to-process)]
    (try
      (do
        (for [file-path files-to-parse
              :let [raw-content (with-open [reader (io/reader file-path)]
                                  (str/join "\n" (line-seq reader)))
                    content* (parse-content raw-content)]]
          (swap! all-records conj content*))
        (let [_ (swap! all-records flatten)
              unique-records (remove-duplicates all-records)]))
      (catch Exception e
        (println "clj-file-processing.core/process-file error reading file:"
                 [files-to-parse e])))))

#_(defn process-files ;; TODO remove
  [transducer files]
  (into
   []
   (comp (mapcat (fn [file] (process-file transducer file))))
   files))

(defn process-data
  "Removes header (if present). Output is vector of vectors."
  [output]
  (let [output* (into []
                      (for [entry output]
                        (case delimiter
                          ;; TODO process based on the delimiter type.
                          ;; check for type once for efficiency?
                          ;; write to another file or str?
                          )
                        ))]))


(defn -main
  "Processes multiple files with a transducer as if it's one file. Not lazy, but
  processes a line at a time. Checks that there is at least one file to be
  processed. TODO: trim output, split by new line, process each line one by one?
  Store as vector of vectors maybe."
  [& args]
  (let [output (atom nil)]
    #_(when (seq files-to-process)
      (let [output (apply + (process-files (comp (map count))
                                           files-to-process))
            ]))))



;; (mapv str (filter #(.isFile %) (file-seq (clojure.java.io/file "/home/sofiia/clj-file-processing/src/clj_file_processing/csv/"))))
;; ["/home/sofiia/clj-file-processing/src/clj_file_processing/csv/mock_data_whitespace.csv"
;;  "/home/sofiia/clj-file-processing/src/clj_file_processing/csv/mock_data_pipelines.csv"
;;  "/home/sofiia/clj-file-processing/src/clj_file_processing/csv/mock_data_commas.csv"]


(def test-data
  "LastName, FirstName, Gender, FavoriteColor, DateOfBirth\nMort, Aeriela, Female, Orange, 5/14/2010\nCroll, Lenee, Male, Red, 8/18/1948\nBrosetti, Netta, Female, Crimson, 9/8/1931\nBickmore, Lindy, Male, Pink, 1/22/2021\nGenthner, Hetti, Male, Orange, 10/17/1999\nStelli, Hatti, Male, Mauv, 10/17/1969\nRichards, Mitch, Female, Red, 11/24/1941\nHalpeine, Greta, Agender, Fuscia, 6/26/1939\nOrrell, Renae, Female, Violet, 12/3/2018\nBtham, Zsazsa, Male, Crimson, 7/18/1987\nSeelbach, Liza, Male, Red, 9/27/1928\nUmpleby, Geordie, Female, Khaki, 10/16/1988\nBensley, Tate, Female, Turquoise, 6/27/1950\nTretwell, Lexie, Polygender, Orange, 5/13/1961\nMcArthur, Rafaellle, Female, Puce, 3/3/2016\nJobey, Doralynn, Male, Crimson, 1/19/1977\nCastellaccio, Teddie, Male, Teal, 11/7/2002\nLightbowne, Aubrie, Male, Teal, 1/31/1930\nBiaggetti, Thea, Female, Violet, 7/29/1976")



#_(mapv vector ["Mort, Aeriela, Female, Orange, 5/14/2010"
                "Croll, Lenee, Male, Red, 8/18/1948"
                "Brosetti, Netta, Female, Crimson, 9/8/1931"
                "Biaggetti, Thea, Female, Violet, 7/29/1976"])
#_[["Mort, Aeriela, Female, Orange, 5/14/2010"]
   ["Croll, Lenee, Male, Red, 8/18/1948"]
   ["Brosetti, Netta, Female, Crimson, 9/8/1931"]
   ["Biaggetti, Thea, Female, Violet, 7/29/1976"]]


#_(sort-by
   identity  clj-time.core/before?
   (map #(-> % (time/str-to-seq-of-ints "/") time/get-date-time)
        ["2/10/1948" "3/18/1748"]))


;; (sort (map first [["Droll" "Lenee" "Male" "Red" "8-18-1948"] ["Croll" "Lenee" "Male" "Red" "8-18-1900"]]))
;; ("Croll" "Droll")


#_(sort-by ;; TODO -remove (outdated )
 identity  clj-time.core/before?
 (map #(-> % (time/str-to-seq-of-ints "/") time/get-date-time)
      ["2/10/1948" "3/18/1748" "11/22/1700"]))


;; (sort-by (comp time/get-date-time time/str-to-seq-of-ints  last) [["Droll" "ALenee" "FeMale" "Red" "8-18-1944"] ["Croll" "BLenee" "Male" "Red" "8-18-1900"] ["Wroll" "Lenee" "Male" "Red" "8-18-1940"] ["Aroll" "Lenee" "FeMale" "Red" "8-18-1948"]])
;; (["Croll" "BLenee" "Male" "Red" "8-18-1900"]
;;  ["Wroll" "Lenee" "Male" "Red" "8-18-1940"]
;;  ["Droll" "ALenee" "FeMale" "Red" "8-18-1944"]
;;  ["Aroll" "Lenee" "FeMale" "Red" "8-18-1948"])
