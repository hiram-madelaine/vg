(ns testvg.utils
  #_(:require [goog.string :as gstring]
            [goog.string.format])
  (:import [goog.i18n DateTimeFormat]
           ))


;_________________________________________________
;                                                 |
;          Handle Date Format                     |
;_________________________________________________|

(def fmtr (DateTimeFormat. "mm:ss"))

(defn fmt
  "Format a date with default format"
  [d]
  (.format fmtr d))


;_________________________________________________
;                                                 |
;          Handle Event                           |
;_________________________________________________|


(defn e-value
  "get the value from an input"
  [ev]
  (-> ev .-target .-value))


;_________________________________________________
;                                                 |
;          Color Generator                        |
;_________________________________________________|

(def rand-255 (partial rand-int 255))

(defn to-hex
  [s]
  (.toString s 16))

(defn pad-0
  ([s]
    (pad-0 s 2))
  ([s n]
   (if (= n (count s))
     s
     (pad-0 (str "0" s)))))

(defn color-gen
  []
  (->> (repeatedly rand-255)
       (take 3)
       (map to-hex)
       (map pad-0)
       (apply str)
       (str "#")))