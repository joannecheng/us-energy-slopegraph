(ns format-data
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [clojure.data.json :as json]))

(defn csv-data->maps [csv-data]
  (map zipmap
       (->>
        (first csv-data)
        (map #(clojure.string/replace % " " "-"))
        (map keyword)
        repeat)

       (rest csv-data)))
(defn values-for-year [csv-data-map year]
  (let [year-data (first (filter #(= year (:Year %)) csv-data-map))
        total-for-year (:all-fuels year-data)
        values (select-keys year-data [:natural-gas :coal :nuclear])]

    (->>
     values
     (map (fn [[k v]]
            [k (/ v total-for-year)]))
     (into {})
    )))

(defn parse-numbers [m]
  (into {}
        (map (fn [[k v]] [k (read-string v)])) m))

(defn csv-file->maps []
  (with-open [reader (io/reader "./resources/public/net_generation_for_all_sectors_annual.csv")]
    (doall
     (map parse-numbers (csv-data->maps (csv/read-csv reader))))))

(defn csv-maps->json [csv-data-map]
  [{:year "2005" :values (values-for-year csv-data-map 2005)}
   {:year "2015" :values (values-for-year csv-data-map 2015)}
   ])

(defn write-csv-file [formatted-csv]
  (with-open [writer (io/writer "./resources/public/net_generation.json")]
    (.write writer (json/write-str formatted-csv))))

(defn format-data []
  (->>
   (csv-file->maps)
   (csv-maps->json)
   (write-csv-file)
   ))
