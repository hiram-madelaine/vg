(ns ^:figwheel-always testvg.vigiglobe-api
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require
    [reagent.core :as reagent :refer [atom]]
    [goog.events :as events]
    [goog.net.XhrIo :as xhr]
    [cljs.core.async :refer [chan put! <! timeout]]
    [testvg.utils :as u]
    [cognitect.transit :as t]
    [cljs.pprint :refer [pprint]])
  (:import [goog.net XhrIo]
           goog.net.EventType
           [goog.events EventType]
           [goog.ui IdGenerator]))


; ___________________________________________________________________________
;|                                                                           |
;|                           Transit                                         |
;|___________________________________________________________________________|

(def wtr (t/writer :json))

(def rdr (t/reader :json))

(defn ->transit
  "Convert payload to transit format."
  [m]
  (t/write wtr m))


(defn <-transit
  "Convert payload from transit format."
  [t]
  (t/read rdr t))


(defn parse-date
  [[t v]]
  (clj->js [(js/Date.parse t) v]))


(def <-transit-data
  (comp (map <-transit)
        (map :data)))

(def <-first-parse-date
  (comp (map first)
        (map parse-date)))



(def <api
  "Transducer to convert data from api to Highcharts data"
  (comp (map <-transit)
        (map :data)
        (map first)
        (map parse-date)))


;___________________________________________________________
;                                                           |
;        XhrIo call                                         |
;___________________________________________________________|

(defn edn-xhr [{:keys [method url data on-complete on-error]}]
  (let [xhr (XhrIo.)]
    (events/listen xhr goog.net.EventType.SUCCESS
                   (fn [e]
                     (on-complete (.getResponseText xhr))))
    (events/listen xhr goog.net.EventType.ERROR
                   (fn [e]
                     (on-error (.getResponseText xhr))))
    (.send xhr url method nil #js {"Accept" "application/transit+json"})))

(defn async-action
  [url out]
  (edn-xhr {:method      "GET"
            :url         url
            :on-complete (fn [e]
                           (put! out e))
            :on-error    (fn [e]
                           (put! out [:ko "Fuck an error"]))}))




;___________________________________________________________
;                                                           |
;        Highcharts.js                                      |
;___________________________________________________________|

(def chart-config
  {:chart   {:renderTo "chartdiv"}
   :type    "StockChart"
   :title   {:text  "HighCharts Demo"
             :style {:fontWeight "bold"
                     :fontSize   "25px"}}
   :credits {:enabled false}
   :xAxis   {:type                 "datetime"
             :dateTimeLabelFormats {:day "%H:%M"}}
   :series  [{:title        "Serie title"
              :id           0
              :showInLegend true
              :name         "Viggiglobe Volume"
              :data         []
              :lineWidth    1}]}
  )


(defn build-chart
  [config]
  (js/Highcharts.StockChart. (clj->js config)))
;___________________________________________________________
;                                                           |
;        Reagent components                                 |
;___________________________________________________________|


(defn graph-view []
  (let [comm (chan)]
    (reagent/create-class
      {:component-did-mount
       #(let [chart (build-chart chart-config)
              in (chan 1 <api)
              url "http://api.vigiglobe.com/api/statistics/v1/volume?project_id=vgteam-TV_Shows"]
         (go-loop []
                  (let [v (<! in)]
                    (.addPoint (.get chart 0) v true false true)
                    (recur)))
         (go-loop []
                  (<! comm)
                  (loop []
                    (<! (timeout 1000))
                    (async-action url in) #_(>! in #js [(.getTime (js/Date.))  (rand-int 20)])
                    (recur))))
       :display-name
       "Graph View Component"
       :reagent-render
       (fn []
         [:div {:class "container"}
          [:button {:class    "btn btn-primary"
                    :on-click #(put! comm :go)} "Start"]
          [:div {:id "chartdiv"}]])})))


(defn graph-view-by-tag []
  (let [comm (chan)]
    (reagent/create-class
      {:component-did-mount
       #(let [chart (build-chart chart-config)
              in (chan 1 (comp (map <-transit)
                               (map :data)))
              url "http://api.vigiglobe.com/api/statistics/v1/volume?grouped=true&project_id=vgteam-TV_Shows"]
         (go-loop []
                  (let [vs (<! in)]
                    (doseq [[k v] vs]
                      (if-let [serie (.get chart k)]
                        (.addPoint serie (parse-date (first v)))
                        (.addSeries chart (clj->js {:id   k
                                                    :name k}))))
                    (recur))
                  )
         (go-loop []
                  (<! comm)
                  (loop []
                    (<! (timeout 5000))
                    (async-action url in) #_(>! in #js [(.getTime (js/Date.))  (rand-int 20)])
                    (recur))))
       :display-name
       "Graph View Component"
       :reagent-render
       (fn []
         [:div {:class "container"}
          [:button {:class    "btn btn-primary"
                    :on-click #(put! comm :go)} "Start"]
          [:div {:id "chartdiv"}]])})))

(defn chart []
  (let [out (chan 1 <-transit-data)]
    (go-loop []
             (pprint (<! out))
             (recur))
    (fn []
      [:div "HEllo Chart"
      [:button {:class    "btn"
                :on-click #(async-action "http://api.vigiglobe.com/api/statistics/v1/volume?grouped=true&project_id=vgteam-TV_Shows" out)}]
       ;[graph-view]
       [graph-view-by-tag]])))