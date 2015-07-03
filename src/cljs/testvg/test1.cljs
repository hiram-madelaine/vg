(ns ^:figwheel-always testvg.test1
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require
    [reagent.core :as reagent :refer [atom]]
    [cljs.core.async :refer [chan put! <! timeout]]
    [testvg.utils :as u]))



(defn build-clock-state
  ([]
   (build-clock-state "#ff3344"))
  ([color]
   {:date  (js/Date.)
    :color color}))

(def clock-state (atom (build-clock-state)))


;_________________________________________________
;                                                 |
;          View Components                        |
;_________________________________________________|

(defn color-input [clock-state]
  [:input {:type      "text"
           :class     "form-control input-lg"
           :value     (:color @clock-state)
           :on-change #(swap! clock-state assoc :color (u/e-value %))}])


(defn clock-view [timer]
  [:div {:style {:color (:color @timer)}
         :class "clock"}
   (u/fmt (:date @timer))])



(defn clock-component [timer]
  [:div {:class "well"}
   [:h1 "Hello world, it is now : "]
   [clock-view timer]
   [color-input timer]])


;_________________________________________________
;                                                 |
;          State handling                         |
;_________________________________________________|

(defn clock-component-test1 [timer]
  (with-meta clock-component
             {:component-did-mount
              (go-loop []
                       (<! (timeout 1000))
                       (swap! timer assoc :date (js/Date.))
                       (recur))}))



(defn test1-page []
  [:div
   [:h2 "Test page !"]
   [clock-component-test1 clock-state]
   [:div [:a {:href "#/"} "go to the home page"]]])
