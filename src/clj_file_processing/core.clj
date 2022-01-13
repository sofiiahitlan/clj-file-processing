(ns ^{:authors "Sofia Hitlan"}
    clj-file-processing.core
  ;;(:gen-class)
  (:require [clojure.string :as str]
            [clojure.java.io :as io])
  )

(comment "Decide on how to process files. Have the atom and swap the value each
new file is processed maybe? If no, figure out how to use transducer.
Assume that all names are identical or not?")

(def files-to-process ;; hard-code all file names vs access all files in folder?
  ["mock_data_commas.csv" "mock_data_pipelines.csv" "mock_data_whitespace.csv"])

(defn process-file
  "Opens file using with-open to ensure the Reader is properly closed when
  processing is completed."
  [transducer file]
  (let [user-dir (System/getProperty "user.dir")
        currernt-dir (str user-dir "/src/clj_file_processing/csv")
        file-path (str user-dir "/" file)]
    (try
      (let [content (with-open [reader (io/reader file-path)]
                      (str/join "\n" (line-seq reader))
                      #_(into []
                            transducer
                            (line-seq reader)))])
      (catch Exception e
        (println "clj-file-processing.core/process-file error reading file:"
                 [file-path e])))))

(defn process-files
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
  (let [output (atom nil)])
  (when (seq files-top-process)
    (let [output (apply + (process-files (comp (map count))
                                         files-to-process))
          ])))
