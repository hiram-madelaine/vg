(ns testvg.utils
  (:import [goog.i18n DateTimeFormat]))


;_________________________________________________
;                                                 |
;          Handle Date Format                     |
;_________________________________________________|

(def fmtr (DateTimeFormat. "kk:mm:ss"))

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