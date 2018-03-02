(ns us-energy-slopegraph.core
  (:require [clojure.string :as str]
            [cljsjs.d3]
            [goog.string.format]
            [goog.string :as gstr]))

(enable-console-print!)

;; D3 Configuration variables 
(def w 310)
(def h 400)
(def column-space 200)

;; Helper functions
(def height-scale
  (.. js/d3
      (scaleLinear)
      (domain #js [0 1])
      (range #js [(- h 15) 0])))

(defn format-fuel-name [fuel-name]
  (-> (name fuel-name)
      (str/capitalize)
      (str/replace #"-" " ")))

(defn format-percent [percent]
  (gstr/format "%.2f%%" (* 100 percent)))

;; Functions that draw our SVG elements

(defn draw-header [svg years]
  (.. svg
      (selectAll "text.slopegraph-header")
      (data (into-array years))
      (enter)
      (append "text")
      (classed "slopegraph-header" true)
      (attr "x" (fn [d i] (+ 10 (* i column-space)) ))
      (attr "y" 15)
      (text #(str %))))

(defn column1 [svg data]
  (.. svg
      (selectAll "text.slopegraph-column-1")
      (data (into-array data))
      (enter)
      (append "text")
      (classed "slopegraph-column" true)
      (classed "slopegraph-column-1" true)
      (attr "x" 10)
      (attr "y" #(height-scale (val %)))
      (text #(format-percent (val %)))))

(defn column2 [svg data]
  (.. svg
      (selectAll "text.slopegraph-column-2")
      (data (into-array data))
      (enter)
      (append "text")
      (classed "slopegraph-column" true)
      (classed "slopegraph-column-2" true)
      (attr "x" column-space)
      (attr "y" #(height-scale (val %)))
      (text #(str
              (format-percent (val %))
              " "
              (format-fuel-name (key %))))))

(defn draw-line [svg data-col-1 data-col-2]
  (.. svg
      (selectAll "line.slopegraph-line")
      (data (into-array data-col-1))
      (enter)
      (append "line")
      (classed "slopegraph-line" true)
      (attr "x1" 55)
      (attr "x2" (- column-space 5))
      (attr "y1" #(height-scale (val %)))
      (attr "y2" #(height-scale ((key %) data-col-2)))))

(defn draw-slopegraph [svg data]
  (let [data-2005 (:values (first (filter #(= 2005 (:year %)) data)))
        data-2015 (:values (first (filter #(= 2015 (:year %)) data)))]
    (draw-header svg [2005 2015])
    (column1 svg data-2005)
    (column2 svg data-2015)
    (draw-line svg data-2005 data-2015)))

;; Drawing our slopegraph

;; Creating our svg container
(def svg (.. (.select js/d3 "#slopegraph")
             (append "svg")
             (attr "height" h)
             (attr "width" w)))

(def data
  [{:year 2005
    :values {:natural-gas 0.2008611514256557
             :coal 0.48970650816857986
             :nuclear 0.19367190804075465}}
   {:year 2015
    :values {:natural-gas 0.33808321253456974
             :coal 0.3039492492908485
             :nuclear 0.1976276775179704}}])

(defn ^:export main []
  (draw-slopegraph svg data))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
  (.remove (.select js/d3 "#slopegraph svg"))
  (main))
