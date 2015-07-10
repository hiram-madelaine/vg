(ns ^:figwheel-always testvg.vigiglobe-api
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require
    [reagent.core :as reagent :refer [atom]]
    [goog.events :as events]
    [goog.net.XhrIo :as xhr]
    [cljs.core.async :refer [chan put! <! timeout]]
    [testvg.utils :as u]
    [cognitect.transit :as t]
    [cljs.pprint :refer [pprint]]
    [testvg.test3 :refer [start-stop  startable-component]])
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


(defn <-transit
  "Convert payload from transit format."
  [t]
  (t/read rdr t))


(defn parse-date
  "Parse the date in the tuple
   Return a javascript array."
  [[t v]]
  (clj->js [(js/Date.parse t) v]))


(def xtransit-data
  (comp (map <-transit)
        (map :data)))

(def xfirst-parse-date
  (comp (map first)
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
                           (put! out :ko))}))


;___________________________________________________________
;                                                           |
;        Highcharts.js                                      |
;___________________________________________________________|

(def chart-config
  {:chart   {:renderTo "chartdiv"}
   :type    "StockChart"
   :title   {:text  ""
             :style {:fontWeight "bold"
                     :fontSize   "20px"}}
   :credits {:enabled false}
   :xAxis   {:type                 "datetime"
             :dateTimeLabelFormats {:day "%H:%M"}}
   :series  [{:title        ""
              :id           0
              :showInLegend true
              :name         "Viggiglobe Volume"
              :data         []
              :lineWidth    1}]}
  )


(defn build-chart
  [config]
  (js/Highcharts.Chart. (clj->js config))
  #_(js/Highcharts.StockChart. (clj->js config)))
;___________________________________________________________
;                                                           |
;        Reagent components                                 |
;___________________________________________________________|


(defn graph-component
  [state]
  [:div {:class "container"}
   [:div {:class "graph-container"}
    [:div {:id (get-in @state [:chart-config :chart :renderTo])}]
    [start-stop state {:start "btn-default"
                       :stop  "btn-info"}]]])


(defn build-graph-state
  [{:keys [id url title config] :or {config chart-config}}]
  (atom {:comm         (chan)
         :in           (chan 1 xtransit-data)
         :url          url
         :state        :stop
         :chart-config (-> config
                           (assoc-in [:chart :renderTo] id)
                           (assoc-in [:title :text] title))}))

(defn data-volume!
  "The function that crunches data returned by the raw query "
  [chart v]
  (.addPoint (.get chart 0) (parse-date (first v)) true false true))


(defn data-volume-by-tag!
  "The function that crunches the data returned when the option group by tag is on"
  [chart raw-data]
  (doseq [[k v] raw-data]
    (let [serie (.get chart k)
          data (parse-date (first v))]
      (if serie
        (.addPoint serie data)
        (.addSeries chart (clj->js {:id   k
                                    :name k
                                    :data [data]}))))))


(defn data-volume-by-tag-tree!
  [chart raw-data]
  (let [serie (.get chart 0)
        data (map-indexed (fn [idx [k [[_ v]]]] {:name       k
                                                 :value      v
                                                 :colorValue (inc idx)}) raw-data)]
    (.setData serie (clj->js data))))

(defn startable-graph-component
  [{:keys [title data-fn] :as spec}]
  (let [state (build-graph-state spec)]
    (reagent/create-class
      {:component-did-mount
       #(let [{:keys [in chart-config]} @state
              chart (build-chart chart-config)]
         (go-loop []
                  (let [vs (<! in)]
                    (data-fn chart vs)
                    (recur))))
       :display-name
       (str "Graph View Component " title)
       :reagent-render
       (fn []
         [(startable-component graph-component
                               (fn [s]
                                 (let [{:keys [url in]} @s]
                                   (async-action url in)))
                               5000) state])})))


(defn chart []
  [:div {:class "container"}
   [startable-graph-component {:id "chart-raw"
                               :title "statistics v1 volume vgteam-TV_Shows"
                               :url "http://api.vigiglobe.com/api/statistics/v1/volume?project_id=vgteam-TV_Shows"
                               :data-fn data-volume!}]
   [startable-graph-component {:id "chart-tag"
                               :title "statistics v1 volume vgteam-TV_Shows grouped by tag"
                               :url "http://api.vigiglobe.com/api/statistics/v1/volume?grouped=true&project_id=vgteam-TV_Shows"
                               :data-fn data-volume-by-tag!}]
   [startable-graph-component {:id "chart-tree"
                               :title "statistics v1 volume vgteam-TV_Shows grouped by tag - Tree map"
                               :url "http://api.vigiglobe.com/api/statistics/v1/volume?grouped=true&project_id=vgteam-TV_Shows"
                               :data-fn data-volume-by-tag-tree!
                               :config {
                                        :series [{:animation false
                                                  :id 0
                                                  :colorByPoint true
                                                  :type "treemap"
                                                  :layoutAlgorithm "squarified"}]}}]])
